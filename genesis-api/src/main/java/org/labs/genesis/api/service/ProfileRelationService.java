
package org.labs.genesis.api.service;

import org.labs.genesis.api.context.GenerationContextStore;
import org.labs.genesis.api.dto.MetadataDtos.GenerationParameterRequest;
import org.labs.genesis.api.dto.RelationDtos.*;
import org.labs.genesis.api.exception.RelationException;
import org.labs.genesis.api.mapper.RelationMapper;
import org.labs.genesis.config.ProjectGenerationContext;
import org.labs.genesis.connexion.model.RelationParameter;
import org.labs.genesis.connexion.model.TableMetadata;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.sql.Connection;

@Service
public class ProfileRelationService {

    private final GenerationContextStore contextStore;
    private final GenerationProfilesStore profilesStore;
    private final ForeignKeyRelationValidator foreignKeyValidator;

    private final Map<String, List<RelationParameter>> manualRelations = new HashMap<>();

    public ProfileRelationService(GenerationContextStore contextStore, GenerationProfilesStore profilesStore, ForeignKeyRelationValidator foreignKeyValidator) {
        this.contextStore = contextStore;
        this.profilesStore = profilesStore;
        this.foreignKeyValidator = foreignKeyValidator;
    }

    public synchronized List<RelationResponse> getRelations(String profileName) {
        ProjectGenerationContext context = buildContext(profileName);
        List<RelationParameter> relations = context.getRelationParameters();
        return relations.stream()
                .map(RelationMapper::toResponse)
                .toList();
    }

    public synchronized RelationParametersResponse saveRelations(String profileName, RelationParametersRequest request) {
        if (request == null || request.relations() == null) {
            throw new RelationException("La liste des relations est obligatoire");
        }

        ProjectGenerationContext context = buildContext(profileName);

        Set<String> selectedTables = context.getEntityTables()
                .stream()
                .map(TableMetadata::getTableName)
                .collect(Collectors.toSet());

        List<RelationParameter> validated = new ArrayList<>();
        Set<String> unique = new HashSet<>();

        for (RelationUpdateRequest relation : request.relations()) {
            if (relation == null) {
                throw new RelationException("Une relation de la liste est null");
            }

            String parent = normalize(relation.parentTable());
            String child = normalize(relation.childTable());

            if (!selectedTables.contains(parent) || !selectedTables.contains(child)) {
                throw new RelationException("Les deux tables doivent appartenir " + "au profil " + profileName);
            }

            foreignKeyValidator.validate(context.getConnection(), parent, child);

            String key = parent + "->" + child;

            if (!unique.add(key)) {
                throw new RelationException("Relation dupliquée : " + key);
            }

            validated.add(new RelationParameter(parent, child, relation.mandatory(), relation.hasForm()));
        }

        // Une sauvegarde vide signifie une suppression volontaire.
        manualRelations.put(profileName, List.copyOf(validated));

        return new RelationParametersResponse(true, validated.size() + " relation(s) enregistrée(s) " + "pour le profil " + profileName);
    }

    public synchronized ProjectGenerationContext buildContext(String profileName) {
        ProjectGenerationContext source = contextStore.getContext();
        return buildContext(profileName, source.getConnection());
    }

    public synchronized ProjectGenerationContext buildContext(String profileName, Connection connection) {
        if (connection == null) {
            throw new RelationException("Aucune connexion JDBC disponible pour le profil " + profileName);
        }

        ProjectGenerationContext source = contextStore.getContext();
        GenerationParameterRequest profile = findProfile(profileName, source);
        // Copier les paramètres sans modifier le contexte global.
        ProjectGenerationContext copy = source.duplicateWithNoTables();
        // La connexion doit être affectée avant setTables().
        copy.setConnection(connection);
        copy.setEntityNames(new ArrayList<>(profile.selectedTables()));
        copy.setViewNames(new ArrayList<>(profile.selectedViews()));
        copy.setGenerationOptions(mapComponents(profile.selectedComponents()));
        // Ne pas importer les relations du contexte global.
        copy.setRelationParameters(new ArrayList<>());
        try {
            copy.setTables();
        } catch (RuntimeException exception) {
            throw new IllegalStateException("Impossible de charger les métadonnées " + "du profil : " + profileName, exception);
        }

        // containsKey est volontaire :
        // une liste vide représente une suppression explicite.
        if (manualRelations.containsKey(profileName)) {
            copy.setRelationParameters(new ArrayList<>(manualRelations.get(profileName)));
        } else {
            copy.autoDetectRelationParameters();
        }

        return copy;
    }

    public ProjectGenerationContext buildContext(GenerationParameterRequest profile, GenerationConfigurationSnapshot.RelationSelection selection, Connection connection) {
        Objects.requireNonNull(profile, "Profil obligatoire");
        Objects.requireNonNull(selection, "Relations obligatoires");
        Objects.requireNonNull(connection, "Connexion obligatoire");

        ProjectGenerationContext source = contextStore.getContext();

        ProjectGenerationContext copy = source.duplicateWithNoTables();

        copy.setConnection(connection);
        copy.setEntityNames(new ArrayList<>(profile.selectedTables()));
        copy.setViewNames(new ArrayList<>(profile.selectedViews()));
        copy.setGenerationOptions(mapComponents(profile.selectedComponents()));
        copy.setRelationParameters(new ArrayList<>());
        copy.setTables();

        if (selection.manuallyConfigured()) {
            List<RelationParameter> relations = new ArrayList<>();
            for (var value : selection.relations()) {
                relations.add(new RelationParameter(value.parentTable(), value.childTable(), value.mandatory(), value.hasForm()));
            }
            copy.setRelationParameters(relations);
        } else {
            copy.autoDetectRelationParameters();
        }
        return copy;
    }

    public synchronized void clear() {
        manualRelations.clear();
    }

    private GenerationParameterRequest findProfile(String profileName, ProjectGenerationContext context) {
        if (profileName == null || profileName.isBlank()) {
            throw new RelationException("Le nom du profil est obligatoire");
        }

        return profilesStore.getProfiles(context)
                .stream()
                .filter(profile -> profile.profileName()
                        .equals(profileName))
                .findFirst()
                .orElseThrow(() -> new RelationException("Profil inconnu ou expiré : " + profileName));
    }

    private List<String> mapComponents(List<String> components) {
        List<String> result = new ArrayList<>();
        for (String component : components) {
            switch (component.toLowerCase(Locale.ROOT)) {
                case "model" -> result.add(ProjectGenerationContext.COMPONENT_MODEL);
                case "dao" -> result.add(ProjectGenerationContext.COMPONENT_DAO);
                case "service" -> result.add(ProjectGenerationContext.COMPONENT_SERVICE);
                case "controller" -> result.add(ProjectGenerationContext.COMPONENT_CONTROLLER);
                default -> throw new RelationException("Composant inconnu : " + component);
            }
        }
        return result;
    }

    private String normalize(String name) {
        return name == null ? null : name.trim();
    }

    public synchronized void reconcileProfiles(List<GenerationParameterRequest> previous, List<GenerationParameterRequest> next) {
        Map<String, GenerationParameterRequest> previousByName = new HashMap<>();
        for (GenerationParameterRequest profile : previous) {
            previousByName.put(profile.profileName(), profile);
        }
        Set<String> profilesToKeep = new HashSet<>();
        for (GenerationParameterRequest profile : next) {
            GenerationParameterRequest old = previousByName.get(profile.profileName());
            if (old != null && old.selectedTables().equals(profile.selectedTables()) && old.selectedViews().equals(profile.selectedViews())) {
                profilesToKeep.add(profile.profileName());
            }
        }
        manualRelations.keySet().retainAll(profilesToKeep);
    }

    public synchronized Map<String, GenerationConfigurationSnapshot.RelationSelection> snapshotRelations(List<GenerationParameterRequest> profiles) {
        Map<String, GenerationConfigurationSnapshot.RelationSelection> result = new LinkedHashMap<>();

        for (GenerationParameterRequest profile : profiles) {
            String name = profile.profileName();
            // containsKey distingue une absence de configuration
            // d'une configuration manuelle volontairement vide.
            boolean manual = manualRelations.containsKey(name);
            List<GenerationConfigurationSnapshot.RelationValue> values = new ArrayList<>();

            if (manual) {
                for (RelationParameter relation : manualRelations.get(name)) {
                    values.add(new GenerationConfigurationSnapshot.RelationValue(
                                    relation.getParentTable(),
                                    relation.getChildTable(),
                                    relation.getMandatory(),
                                    relation.getHasForm()
                            )
                    );
                }
            }

            result.put(name, new GenerationConfigurationSnapshot.RelationSelection(manual, values));
        }
        return Map.copyOf(result);
    }

}

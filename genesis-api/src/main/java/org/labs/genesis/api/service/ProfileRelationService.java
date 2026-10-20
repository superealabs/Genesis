
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
        GenerationParameterRequest profile = findProfile(profileName, source);
        ProjectGenerationContext copy = source.duplicateWithNoTables();
        copy.setEntityNames(new ArrayList<>(profile.selectedTables()));
        copy.setViewNames(new ArrayList<>(profile.selectedViews()));
        copy.setGenerationOptions(mapComponents(profile.selectedComponents()));

        // Ne pas conserver les relations du contexte global.
        copy.setRelationParameters(new ArrayList<>());

        try {
            copy.setTables();
        } catch (RuntimeException exception) {
            throw new RelationException("Impossible de charger les tables du profil : " + profileName);
        }

        if (manualRelations.containsKey(profileName)) {
            copy.setRelationParameters(new ArrayList<>(manualRelations.get(profileName)));
        } else {
            // Défauts détectés uniquement dans le contexte dérivé.
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
}

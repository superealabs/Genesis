package org.labs.genesis.api.service;

import org.labs.genesis.api.context.GenerationContextStore;
import org.labs.genesis.api.dto.MetadataDtos.GenerationParameterRequest;
import org.labs.genesis.api.exception.RelationException;
import org.labs.genesis.config.ProjectGenerationContext;
import org.labs.genesis.connexion.model.RelationParameter;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.util.*;

@Service
public class ConsolidatedGenerationContextService {

    private final GenerationContextStore contextStore;
    private final GenerationProfilesStore profilesStore;
    private final ProfileRelationService profileRelationService;
    private final GenerationRelationConflictValidator conflictValidator;

    public ConsolidatedGenerationContextService(GenerationContextStore contextStore, GenerationProfilesStore profilesStore, ProfileRelationService profileRelationService, GenerationRelationConflictValidator conflictValidator) {
        this.contextStore = contextStore;
        this.profilesStore = profilesStore;
        this.profileRelationService = profileRelationService;
        this.conflictValidator = conflictValidator;
    }


    public synchronized ProjectGenerationContext build(Connection executionConnection) {
        Objects.requireNonNull(executionConnection, "La connexion JDBC est obligatoire");
        ProjectGenerationContext source = contextStore.getContext();
        List<GenerationParameterRequest> profiles = profilesStore.getProfiles(source);
        if (profiles == null || profiles.isEmpty()) {
            throw new RelationException("Aucun profil de génération disponible");
        }

        // 1. Charger chaque profil une seule fois,
        // avec la connexion JDBC fournie.
        Map<String, List<RelationParameter>> relationsByProfile = new LinkedHashMap<>();

        Set<String> tableNames = new LinkedHashSet<>();
        Set<String> viewNames = new LinkedHashSet<>();

        for (GenerationParameterRequest profile : profiles) {
            if (profile == null || profile.selectedTables() == null || profile.selectedViews() == null) {
                throw new RelationException("Profil de génération invalide");
            }

            tableNames.addAll(profile.selectedTables());
            viewNames.addAll(profile.selectedViews());

            ProjectGenerationContext profileContext = profileRelationService.buildContext(profile.profileName(), executionConnection);

            List<RelationParameter> relations = profileContext.getRelationParameters();

            relationsByProfile.put(profile.profileName(), relations == null
                            ? List.of()
                            : List.copyOf(relations)
            );
        }

        // 2. Rejeter toute contradiction avant la fusion.
        conflictValidator.validateRelations(relationsByProfile);

        // 3. Construire un contexte distinct du contexte global.
        ProjectGenerationContext consolidated = source.duplicateWithNoTables();

        consolidated.setConnection(executionConnection);
        consolidated.setEntityNames(new ArrayList<>(tableNames));
        consolidated.setViewNames(new ArrayList<>(viewNames));
        // Les options des composants seront définies
        // par l'orchestrateur, lot par lot.
        consolidated.setGenerationOptions(new ArrayList<>());
        consolidated.setRelationParameters(new ArrayList<>());

        // 4. Charger toutes les entités sélectionnées.
        try {
            consolidated.setTables();
        } catch (RuntimeException exception) {
            throw new IllegalStateException("Impossible de charger les métadonnées consolidées", exception);
        }

        // 5. Fusionner les relations validées.
        Map<RelationKey, RelationParameter> merged = new LinkedHashMap<>();
        for (List<RelationParameter> relations : relationsByProfile.values()) {
            for (RelationParameter relation : relations) {
                RelationKey key = new RelationKey(normalize(relation.getParentTable()), normalize(relation.getChildTable()));
                merged.putIfAbsent(key, new RelationParameter(relation.getParentTable(), relation.getChildTable(), relation.getMandatory(), relation.getHasForm()));
            }
        }
        consolidated.setRelationParameters(new ArrayList<>(merged.values())
        );
        return consolidated;
    }

    private String normalize(String name) {
        if (name == null || name.isBlank()) {
            throw new RelationException("Nom de table invalide dans une relation");
        }
        return name.trim().toLowerCase(Locale.ROOT);
    }

    public ProjectGenerationContext build(Connection connection, GenerationConfigurationSnapshot snapshot) {
        Objects.requireNonNull(connection, "Connexion obligatoire");
        Objects.requireNonNull(snapshot, "Instantané obligatoire");

        if (snapshot.profiles().isEmpty()) {
            throw new RelationException("Aucun profil de génération disponible");
        }

        ProjectGenerationContext source = contextStore.getContext();

        Set<String> tableNames = new LinkedHashSet<>();
        Set<String> viewNames = new LinkedHashSet<>();

        Map<String, List<RelationParameter>> relationsByProfile = new LinkedHashMap<>();

        for (GenerationParameterRequest profile : snapshot.profiles()) {
            tableNames.addAll(profile.selectedTables());
            viewNames.addAll(profile.selectedViews());
            ProjectGenerationContext profileContext = profileRelationService.buildContext(profile, snapshot.relationsFor(profile.profileName()), connection);

            List<RelationParameter> relations = profileContext.getRelationParameters();

            relationsByProfile.put(profile.profileName(), relations == null
                            ? List.of()
                            : List.copyOf(relations)
            );
        }

        // Même configuration relationnelle dans tous
        // les profils qui définissent une même relation.
        conflictValidator.validateRelations(relationsByProfile);

        ProjectGenerationContext consolidated = source.duplicateWithNoTables();

        consolidated.setConnection(connection);
        consolidated.setEntityNames(new ArrayList<>(tableNames));
        consolidated.setViewNames(new ArrayList<>(viewNames));
        consolidated.setGenerationOptions(new ArrayList<>());
        consolidated.setRelationParameters(new ArrayList<>());
        consolidated.setTables();

        Map<RelationKey, RelationParameter> merged = new LinkedHashMap<>();

        for (List<RelationParameter> relations : relationsByProfile.values()) {
            for (RelationParameter relation : relations) {
                RelationKey key = new RelationKey(normalize(relation.getParentTable()), normalize(relation.getChildTable()));
                merged.putIfAbsent(key, new RelationParameter(
                                relation.getParentTable(),
                                relation.getChildTable(),
                                relation.getMandatory(),
                                relation.getHasForm()
                        )
                );
            }
        }
        consolidated.setRelationParameters(new ArrayList<>(merged.values()));
        return consolidated;
    }

    public List<String> findUnconfiguredForeignKeys(ProjectGenerationContext consolidated) {
        Set<String> configured = new HashSet<>();
        for (RelationParameter relation : consolidated.getRelationParameters()) {
            configured.add(normalize(relation.getParentTable())
                            + "->"
                            + normalize(relation.getChildTable())
            );
        }

        Set<String> missing = new LinkedHashSet<>();
        for (var child : consolidated.getEntityTables()) {
            if (child.getColumns() == null) {
                continue;
            }
            for (var column : child.getColumns()) {
                if (!column.isForeign() || column.getReferencedTable() == null) {
                    continue;
                }
                var parent = consolidated.findTableByName(column.getReferencedTable(), consolidated.getEntityTables());
                if (parent == null) {
                    continue;
                }
                String key = normalize(parent.getTableName())
                                + "->"
                                + normalize(child.getTableName());
                if (!configured.contains(key)) {
                    missing.add(parent.getTableName()
                                    + " -> "
                                    + child.getTableName()
                    );
                }
            }
        }
        return List.copyOf(missing);
    }


    private record RelationKey(
            String parent,
            String child
    ) {
    }
}

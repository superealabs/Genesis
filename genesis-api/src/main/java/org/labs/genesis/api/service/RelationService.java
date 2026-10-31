package org.labs.genesis.api.service;

import org.labs.genesis.api.context.GenerationContextStore;
import org.labs.genesis.api.dto.MetadataDtos.TableMetadataResponse;
import org.labs.genesis.api.dto.RelationDtos.RelationResponse;
import org.labs.genesis.api.dto.RelationDtos.RelationUpdateRequest;
import org.labs.genesis.api.dto.RelationDtos.RelationUpdateResponse;
import org.labs.genesis.api.dto.RelationDtos.RelationParametersRequest;
import org.labs.genesis.api.dto.RelationDtos.RelationParametersResponse;
import org.labs.genesis.api.exception.RelationException;
import org.labs.genesis.api.mapper.RelationMapper;
import org.labs.genesis.api.mapper.TableMetadataMapper;
import org.labs.genesis.config.ProjectGenerationContext;
import org.labs.genesis.connexion.model.RelationParameter;
import org.labs.genesis.connexion.model.TableMetadata;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RelationService {
    private final ForeignKeyRelationValidator foreignKeyValidator;
    private static final String PARENTS = "PARENTS";
    private static final String CHILDS = "CHILDS";
    private final GenerationProfilesStore generationProfilesStore;
    private final ConsolidatedGenerationContextService consolidatedService;
    private final MetadataService metadataService;

    private final GenerationContextStore generationContextStore;

    public RelationService(GenerationContextStore generationContextStore, ForeignKeyRelationValidator foreignKeyValidator, GenerationProfilesStore generationProfilesStore, ConsolidatedGenerationContextService consolidatedService, MetadataService metadataService) {
        this.generationContextStore = generationContextStore;
        this.foreignKeyValidator = foreignKeyValidator;
        this.generationProfilesStore = generationProfilesStore;
        this.consolidatedService = consolidatedService;
        this.metadataService = metadataService;
    }

    public List<TableMetadataResponse> getParents() {
        return getRelatedTables(null, true);
    }

    public List<TableMetadataResponse> getParents(String childTableName) {
        return getRelatedTables(childTableName, true);
    }

    public List<TableMetadataResponse> getChildren() {
        return getRelatedTables(null, false);
    }

    public List<TableMetadataResponse> getChildren(String parentTableName) {
        return getRelatedTables(parentTableName, false);
    }

    private List<TableMetadataResponse> getRelatedTables(String tableName, boolean parents) {
        String filter = tableName == null ? "" : tableName.trim();

        // Source unique : relations consolidées des profils
        Set<String> relatedNames = new LinkedHashSet<>();

        for (RelationResponse relation : getRelations()) {
            String sourceName = parents
                    ? relation.childTable()
                    : relation.parentTable();

            String relatedName = parents
                    ? relation.parentTable()
                    : relation.childTable();

            if (!filter.isEmpty() && !sourceName.equalsIgnoreCase(filter)) {
                continue;
            }

            relatedNames.add(relatedName.toLowerCase(Locale.ROOT));
        }

        if (relatedNames.isEmpty()) {
            return List.of();
        }

        return metadataService.loadAll().tables()
                .stream()
                .filter(table -> relatedNames.contains(table.tableName().toLowerCase(Locale.ROOT)))
                .toList();
    }

    public List<RelationResponse> getRelations() {
        ProjectGenerationContext context = generationContextStore.getContext();
        var profiles = generationProfilesStore.getProfiles(context);
        // Aucun profil : conserver le comportement global existant.
        if (profiles.isEmpty()) {
            List<RelationParameter> relations = context.getRelationParameters();
            if (relations == null || relations.isEmpty()) {
                return List.of();
            }
            return relations.stream()
                    .map(RelationMapper::toResponse)
                    .toList();
        }
        // Des profils existent : charger leurs relations consolidées.
        if (context.getDatabase() == null || context.getCredentials() == null) {
            throw new RelationException(
                    "Configuration de la base de données manquante"
            );
        }
        try (Connection connection = context.getDatabase().getConnection(context.getCredentials())) {
            ProjectGenerationContext consolidated = consolidatedService.build(connection);
            List<RelationParameter> relations = consolidated.getRelationParameters();
            if (relations == null || relations.isEmpty()) {
                return List.of();
            }
            return relations.stream()
                    .map(RelationMapper::toResponse)
                    .toList();
        } catch (SQLException exception) {
            throw new RelationException("Impossible de charger les relations consolidées : " + exception.getMessage());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public List<RelationResponse> detectRelations() {
        ProjectGenerationContext context = requireSelectedTables();
        return context.autoDetectRelationParameters()
                .stream()
                .map(RelationMapper::toResponse)
                .toList();
    }

    private ProjectGenerationContext requireSelectedTables() {
        ProjectGenerationContext context = generationContextStore.getContext();
        if (context.getEntityTables() == null || context.getEntityTables().isEmpty()) {
            throw new RelationException("Sélectionnez d'abord les tables avant de charger les relations");
        }
        return context;
    }


    public RelationParametersResponse saveRelationParameters(RelationParametersRequest request) {
        if (request == null || request.relations() == null) {
            throw new RelationException("La liste des relations est obligatoire");
        }
        RelationUpdateResponse result = updateRelations(request.relations());
        if (!result.success()) {
            throw new RelationException("Impossible d'enregistrer les relations");
        }
        return new RelationParametersResponse(true, result.relations().size() + " relation(s) enregistrée(s) avec succès");
    }


    public RelationUpdateResponse updateRelations(List<RelationUpdateRequest> requests) {
        ProjectGenerationContext context = requireSelectedTables();

        if (requests == null) {
            throw new RelationException("La liste des relations est obligatoire");
        }

        Set<String> selectedTables = context.getEntityTables()
                .stream()
                .map(TableMetadata::getTableName)
                .collect(Collectors.toSet());

        List<RelationParameter> relationParameters = new ArrayList<>();

        Set<String> uniqueRelations = new HashSet<>();

        for (RelationUpdateRequest request : requests) {
            if (request == null) {
                throw new RelationException("Une relation de la liste est null");
            }
            String parentTable = normalizeTableName(request.parentTable());
            String childTable = normalizeTableName(request.childTable());
            validateRelation(parentTable, childTable, selectedTables);
            foreignKeyValidator.validate(context.getConnection(), parentTable, childTable);
            String relationKey = parentTable + "->" + childTable;
            if (!uniqueRelations.add(relationKey)) {
                throw new RelationException("Relation dupliquée : " + parentTable + " -> " + childTable);
            }
            relationParameters.add(new RelationParameter(parentTable, childTable, request.mandatory(), request.hasForm()));
        }

        context.setRelationParameters(relationParameters);

        List<RelationResponse> responses = relationParameters.stream()
                        .map(RelationMapper::toResponse)
                        .toList();

        return new RelationUpdateResponse(true, responses);
    }

    private void validateRelation(String parentTable, String childTable, Set<String> selectedTables) {
        if (parentTable == null || parentTable.isBlank()) {
            throw new RelationException("La table parente est obligatoire");
        }

        if (childTable == null || childTable.isBlank()) {
            throw new RelationException("La table enfant est obligatoire");
        }

        if (parentTable.equals(childTable)) {
            throw new RelationException("Une relation ne peut pas relier une table à elle-même");
        }

        if (!selectedTables.contains(parentTable)) {
            throw new RelationException("Table parente inconnue : " + parentTable);
        }

        if (!selectedTables.contains(childTable)) {
            throw new RelationException("Table enfant inconnue : " + childTable);
        }
    }

    private String normalizeTableName(String tableName) {
        if (tableName == null) {
            return null;
        }
        return tableName.trim();
    }

    private ProjectGenerationContext getProfilesContext() {
        ProjectGenerationContext source = generationContextStore.getContext();
        var profiles = generationProfilesStore.getProfiles(source);
        if (profiles.isEmpty()) {
            throw new RelationException("Enregistrez d'abord les profils de génération");
        }
        Set<String> tables = new LinkedHashSet<>();
        for (var profile : profiles) {
            tables.addAll(profile.selectedTables());
        }
        ProjectGenerationContext copy = source.duplicateWithNoTables();
        copy.setEntityNames(new ArrayList<>(tables));
        copy.setViewNames(new ArrayList<>());
        copy.setTables();
        return copy;
    }
}

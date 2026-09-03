package org.labs.genesis.api.service;

import org.labs.genesis.api.context.GenerationContextStore;
import org.labs.genesis.api.dto.MetadataDtos.TableMetadataResponse;
import org.labs.genesis.api.dto.RelationDtos.RelationResponse;
import org.labs.genesis.api.dto.RelationDtos.RelationUpdateRequest;
import org.labs.genesis.api.dto.RelationDtos.RelationUpdateResponse;
import org.labs.genesis.api.exception.RelationException;
import org.labs.genesis.api.mapper.RelationMapper;
import org.labs.genesis.api.mapper.TableMetadataMapper;
import org.labs.genesis.config.ProjectGenerationContext;
import org.labs.genesis.connexion.model.RelationParameter;
import org.labs.genesis.connexion.model.TableMetadata;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RelationService {

    private static final String PARENTS = "PARENTS";
    private static final String CHILDS = "CHILDS";

    private final GenerationContextStore generationContextStore;

    public RelationService(GenerationContextStore generationContextStore) {
        this.generationContextStore = generationContextStore;
    }

    private List<TableMetadataResponse> getTablesByRelationType(String relationType) {
        ProjectGenerationContext context = requireSelectedTables();
        Dictionary<String, List<TableMetadata>> splitTables = context.splitTableByRelations();
        List<TableMetadata> tables = splitTables.get(relationType);
        if (tables == null) {
            return List.of();
        }
        return tables.stream()
                .map(TableMetadataMapper::toResponse)
                .toList();
    }

    public List<TableMetadataResponse> getParents() {
        return getTablesByRelationType(PARENTS);
    }

    public List<TableMetadataResponse> getChildren() {
        return getTablesByRelationType(CHILDS);
    }

    public List<RelationResponse> getRelations() {
        ProjectGenerationContext context = requireSelectedTables();
        if (context.getRelationParameters() == null) {
            return List.of();
        }
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
                continue;
            }
            String parentTable = normalizeTableName(request.parentTable());
            String childTable = normalizeTableName(request.childTable());
            validateRelation(parentTable, childTable, selectedTables);
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
}

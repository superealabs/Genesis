package org.labs.genesis.api.service;

import java.sql.Connection;
import java.util.*;
import org.labs.genesis.api.context.GenerationContextStore;
import org.labs.genesis.api.dto.MetadataDtos.TableMetadataResponse;
import org.labs.genesis.api.dto.MetadataDtos.TableSelectionRequest;
import org.labs.genesis.api.dto.MetadataDtos.TableSelectionResponse;
import org.labs.genesis.api.dto.MetadataDtos.TablesMetadataResponse;
import org.labs.genesis.api.exception.MetadataException;
import org.labs.genesis.api.exception.TableSelectionException;
import org.labs.genesis.api.mapper.TableMetadataMapper;
import org.labs.genesis.config.ProjectGenerationContext;
import org.labs.genesis.connexion.model.TableMetadata;
import org.springframework.stereotype.Service;

@Service
public class MetadataService {

    public MetadataService(GenerationContextStore generationContextStore) {
        this.generationContextStore = generationContextStore;
    }

    private final GenerationContextStore generationContextStore;

    public TablesMetadataResponse loadAll() {
        ProjectGenerationContext context = generationContextStore.getContext();
        validateMetadataContext(context);
        try {
            List<TableMetadata> tables = context.getDatabase().getEntities(context.getConnection(), context.getCredentials(), context.getLanguage(), context.getFramework());
            List<TableMetadata> views = context.getDatabase().getViews(context.getConnection(), context.getCredentials(), context.getLanguage(), context.getFramework());
            List<TableMetadataResponse> tableResponses = tables.stream().map(TableMetadataMapper::toResponse).toList();
            List<TableMetadataResponse> viewResponses = views.stream().map(TableMetadataMapper::toResponse).toList();
            return new TablesMetadataResponse(tableResponses, viewResponses);
        } catch (Exception exception) {
            throw new MetadataException("Impossible de charger les métadonnées : " + exception.getMessage());
        }
    }

    private void validateMetadataContext(ProjectGenerationContext context) {
        if (context.getFramework() == null) {
            throw new MetadataException("Le framework doit être sélectionné avant de charger les métadonnées");
        }
        if (context.getLanguage() == null) {
            throw new MetadataException("Aucun langage configuré");
        }
        if (context.getDatabase() == null) {
            throw new MetadataException("Aucune base de données configurée");
        }
        if (context.getCredentials() == null) {
            throw new MetadataException("Aucun identifiant de connexion configuré");
        }
        Connection connection = context.getConnection();
        if (connection == null) {
            throw new MetadataException("Aucune connexion à la base de données disponible");
        }
        try {
            if (connection.isClosed()) {
                throw new MetadataException("La connexion à la base de données est fermée");
            }
        } catch (MetadataException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new MetadataException("Impossible de vérifier l'état de la connexion JDBC");
        }
    }

    public TableSelectionResponse select(TableSelectionRequest request) {
        validateRequest(request);
        ProjectGenerationContext context = generationContextStore.getContext();
        validateSelectionContext(context);
        List<String> selectedTables = normalizeNames(request.selectedTables());
        List<String> selectedViews = normalizeNames(request.selectedViews());
        if (selectedTables.isEmpty() && selectedViews.isEmpty()) {
            throw new TableSelectionException("Sélectionnez au moins une table ou une vue");
        }
        List<String> generationOptions = mapComponents(request.selectedComponents());
        context.setEntityNames(selectedTables);
        context.setViewNames(selectedViews);
        context.setGenerationOptions(generationOptions);
        try {
            context.setTables();
            context.autoDetectRelationParameters();
        } catch (RuntimeException exception) {
            throw new TableSelectionException("Impossible de charger les tables et vues sélectionnées : " + rootMessage(exception));
        }
        return new TableSelectionResponse(true, List.copyOf(context.getEntityNames()), List.copyOf(context.getViewNames()), List.copyOf(context.getGenerationOptions()));
    }

    private void validateRequest(TableSelectionRequest request) {
        if (request == null) {
            throw new TableSelectionException("La sélection est obligatoire");
        }
        if (request.selectedComponents() == null || request.selectedComponents().isEmpty()) {
            throw new TableSelectionException("Sélectionnez au moins un composant");
        }
    }

    void validateSelectionContext(ProjectGenerationContext context) {
        if (context.getDatabase() == null || context.getCredentials() == null || context.getConnection() == null) {
            throw new TableSelectionException("La base de données doit être configurée avant la sélection");
        }
        if (context.getFramework() == null || context.getLanguage() == null) {
            throw new TableSelectionException("Le framework doit être sélectionné avant la sélection des tables");
        }
    }

    List<String> normalizeNames(List<String> names) {
        if (names == null) {
            return List.of();
        }
        Set<String> normalized = new LinkedHashSet<>();
        for (String name : names) {
            if (name != null && !name.isBlank()) {
                normalized.add(name.trim());
            }
        }
        return new ArrayList<>(normalized);
    }

    List<String> mapComponents(List<String> components) {
        LinkedHashSet<String> result = new LinkedHashSet<>();
        for (String component : components) {
            if (component == null || component.isBlank()) {
                continue;
            }
            switch(component.trim().toLowerCase(Locale.ROOT)) {
                case "model" ->
                    result.add(ProjectGenerationContext.COMPONENT_MODEL);
                case "dao" ->
                    result.add(ProjectGenerationContext.COMPONENT_DAO);
                case "service" ->
                    result.add(ProjectGenerationContext.COMPONENT_SERVICE);
                case "controller" ->
                    result.add(ProjectGenerationContext.COMPONENT_CONTROLLER);
                default ->
                    throw new TableSelectionException("Composant inconnu : " + component);
            }
        }
        if (result.isEmpty()) {
            throw new TableSelectionException("Sélectionnez au moins un composant valide");
        }
        return new ArrayList<>(result);
    }

    private String rootMessage(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null) {
            current = current.getCause();
        }
        return current.getMessage() == null ? current.getClass().getSimpleName() : current.getMessage();
    }
}

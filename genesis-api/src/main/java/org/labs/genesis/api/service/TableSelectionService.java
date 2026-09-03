package org.labs.genesis.api.service;

import org.labs.genesis.api.context.GenerationContextStore;
import org.labs.genesis.api.dto.metadata.TableSelectionRequest;
import org.labs.genesis.api.dto.metadata.TableSelectionResponse;
import org.labs.genesis.api.exception.TableSelectionException;
import org.labs.genesis.config.ProjectGenerationContext;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class TableSelectionService {

    private final GenerationContextStore generationContextStore;

    public TableSelectionService(GenerationContextStore generationContextStore) {
        this.generationContextStore = generationContextStore;
    }

    public TableSelectionResponse select(TableSelectionRequest request) {
        validateRequest(request);
        ProjectGenerationContext context = generationContextStore.getContext();
        validateContext(context);
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
        } catch (RuntimeException exception) {
            throw new TableSelectionException("Impossible de charger les tables et vues sélectionnées : " + rootMessage(exception));
        }
        return new TableSelectionResponse(
                true,
                List.copyOf(context.getEntityNames()),
                List.copyOf(context.getViewNames()),
                List.copyOf(context.getGenerationOptions())
        );
    }

    private void validateRequest(TableSelectionRequest request) {
        if (request == null) {
            throw new TableSelectionException("La sélection est obligatoire");
        }
        if (request.selectedComponents() == null || request.selectedComponents().isEmpty()) {
            throw new TableSelectionException("Sélectionnez au moins un composant");
        }
    }

    private void validateContext(ProjectGenerationContext context) {
        if (context.getDatabase() == null || context.getCredentials() == null || context.getConnection() == null) {
            throw new TableSelectionException("La base de données doit être configurée avant la sélection");
        }
        if (context.getFramework() == null || context.getLanguage() == null) {
            throw new TableSelectionException("Le framework doit être sélectionné avant la sélection des tables");
        }
    }

    private List<String> normalizeNames(List<String> names) {
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

    private List<String> mapComponents(List<String> components) {
        LinkedHashSet<String> result = new LinkedHashSet<>();
        for (String component : components) {
            if (component == null || component.isBlank()) {
                continue;
            }
            switch (component.trim().toLowerCase(Locale.ROOT)) {
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

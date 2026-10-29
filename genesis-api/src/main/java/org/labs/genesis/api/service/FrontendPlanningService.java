package org.labs.genesis.api.service;

import java.util.*;
import org.labs.genesis.api.dto.GenerationDtos.ExecutionBatch;
import org.labs.genesis.api.dto.MetadataDtos.GenerationParameterRequest;
import org.labs.genesis.api.exception.TableSelectionException;
import org.labs.genesis.config.ProjectGenerationContext;
import org.labs.genesis.config.langage.FrameworkMVC;
import org.springframework.stereotype.Service;

@Service
public class FrontendPlanningService {

    public List<FrontendTask> resolve(List<ExecutionBatch> batches) {
        if (batches == null) {
            throw new IllegalArgumentException("Le plan d'exécution est obligatoire");
        }
        Map<EntityKey, Set<String>> byEntity = new LinkedHashMap<>();
        for (ExecutionBatch batch : batches) {
            if (batch == null || batch.entityName() == null || batch.entityName().isBlank() || batch.components() == null) {
                throw new IllegalArgumentException("Lot de génération invalide");
            }
            EntityKey key = new EntityKey(batch.entityName(), batch.view());
            Set<String> components = byEntity.computeIfAbsent(key, ignored -> new LinkedHashSet<>());
            for (String component : batch.components()) {
                components.add(normalize(component));
            }
        }
        List<FrontendTask> result = new ArrayList<>();
        for (var entry : byEntity.entrySet()) {
            Set<String> components = entry.getValue();
            boolean model = components.contains("model");
            boolean dao = components.contains("dao");
            boolean service = components.contains("service");
            boolean controller = components.contains("controller");
            boolean complete = model && dao && service && controller;
            List<String> options = new ArrayList<>();
            if (model) {
                options.add(ProjectGenerationContext.FRONTEND_COMPONENT_MODEL);
            }
            if (service) {
                options.add(ProjectGenerationContext.FRONTEND_COMPONENT_SERVICE);
            }
            if (complete) {
                options.add(ProjectGenerationContext.FRONTEND_COMPONENT);
            }
            result.add(new FrontendTask(entry.getKey().entityName(), entry.getKey().view(), complete, List.copyOf(options)));
        }
        return List.copyOf(result);
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Composant de génération invalide");
        }
        return switch(value.trim().toLowerCase(Locale.ROOT)) {
            case "model" ->
                "model";
            case "dao", "repository" ->
                "dao";
            case "service" ->
                "service";
            case "controller" ->
                "controller";
            default ->
                throw new IllegalArgumentException("Composant inconnu : " + value);
        };
    }

    private record EntityKey(String entityName, boolean view) {
    }

    public record FrontendTask(String entityName, boolean view, boolean complete, List<String> frontendOptions) {
    }

    public List<FrontendExecutionTask> prepare(List<ExecutionBatch> batches, ProjectGenerationContext context) {
        if (context == null || context.getFramework() == null) {
            throw new IllegalArgumentException("Framework non configuré");
        }
        boolean mvc = context.getFramework() instanceof FrameworkMVC;
        boolean frontendEnabled = context.isGenerateFrontendApp();
        List<String> configuredOptions = context.getFrontendGenerationOptions() == null ? List.of() : context.getFrontendGenerationOptions();
        List<FrontendExecutionTask> result = new ArrayList<>();
        for (FrontendPlanningService.FrontendTask task : resolve(batches)) {
            if (mvc) {
                if (task.complete()) {
                    result.add(new FrontendExecutionTask(task.entityName(), task.view(), true, List.of(), true));
                }
                continue;
            }
            if (!frontendEnabled || context.getFrontendFramework() == null || context.getFrontendLanguage() == null) {
                continue;
            }
            List<String> allowed = task.frontendOptions().stream().filter(configuredOptions::contains).toList();
            if (!allowed.isEmpty()) {
                result.add(new FrontendExecutionTask(task.entityName(), task.view(), false, allowed, task.complete() && allowed.contains(ProjectGenerationContext.FRONTEND_COMPONENT)));
            }
        }
        return List.copyOf(result);
    }

    public record FrontendExecutionTask(String entityName, boolean view, boolean mvc, List<String> options, boolean completeDisplay) {
    }

    public String resolve(List<GenerationParameterRequest> profiles, String entityName, boolean view) {
        if (profiles == null || profiles.isEmpty()) {
            throw new TableSelectionException("Aucun profil disponible");
        }
        for (GenerationParameterRequest profile : profiles) {
            if (profile == null) {
                continue;
            }
            List<String> names = view ? profile.selectedViews() : profile.selectedTables();
            if (names != null && names.contains(entityName)) {
                return profile.profileName();
            }
        }
        throw new TableSelectionException("Aucun profil ne contient l'entité : " + entityName);
    }
}

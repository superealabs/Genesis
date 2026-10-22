package org.labs.genesis.api.service;

import org.labs.genesis.api.dto.GenerationExecutionPlanDtos.ExecutionBatch;
import org.labs.genesis.config.ProjectGenerationContext;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class FrontendGenerationPolicy {
    public List<FrontendTask> resolve(List<ExecutionBatch> batches) {
        if (batches == null) {
            throw new IllegalArgumentException("Le plan d'exécution est obligatoire");
        }

        Map<EntityKey, Set<String>> byEntity = new LinkedHashMap<>();

        for (ExecutionBatch batch : batches) {
            if (batch == null
                    || batch.entityName() == null
                    || batch.entityName().isBlank()
                    || batch.components() == null) {
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

        return switch (value.trim().toLowerCase(Locale.ROOT)) {
            case "model" -> "model";
            case "dao", "repository" -> "dao";
            case "service" -> "service";
            case "controller" -> "controller";
            default -> throw new IllegalArgumentException("Composant inconnu : " + value);
        };
    }

    private record EntityKey(
            String entityName,
            boolean view
    ) {
    }

    public record FrontendTask(
            String entityName,
            boolean view,
            boolean complete,
            List<String> frontendOptions
    ) {
    }
}

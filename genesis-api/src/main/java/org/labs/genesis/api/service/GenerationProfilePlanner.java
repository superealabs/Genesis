package org.labs.genesis.api.service;

import org.labs.genesis.api.dto.GenerationPlanDtos.*;
import org.labs.genesis.api.dto.MetadataDtos.GenerationParameterRequest;
import org.labs.genesis.api.exception.TableSelectionException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class GenerationProfilePlanner {

    public GenerationPlanResponse plan(List<GenerationParameterRequest> profiles) {
        if (profiles == null || profiles.isEmpty()) {
            throw new TableSelectionException("Aucun profil de génération enregistré");
        }

        List<GenerationTask> tasks = new ArrayList<>();
        List<SkippedTask> skipped = new ArrayList<>();

        Map<TaskKey, String> assigned = new LinkedHashMap<>();

        for (GenerationParameterRequest profile : profiles) {
            if (profile == null || profile.profileName() == null || profile.profileName().isBlank()) {
                throw new TableSelectionException("Profil de génération invalide");
            }

            if (profile.selectedComponents() == null || profile.selectedComponents().isEmpty()) {
                throw new TableSelectionException("Aucun composant pour le profil " + profile.profileName());
            }

            appendTasks(profile, profile.selectedTables(), false, assigned, tasks, skipped);
            appendTasks(profile, profile.selectedViews(), true, assigned, tasks, skipped);
        }

        return new GenerationPlanResponse(profiles.size(), tasks.size(), skipped.size(), List.copyOf(tasks), List.copyOf(skipped));
    }

    private void appendTasks(GenerationParameterRequest profile, List<String> entities, boolean view, Map<TaskKey, String> assigned, List<GenerationTask> tasks, List<SkippedTask> skipped) {
        if (entities == null) {
            return;
        }

        for (String entity : entities) {
            if (entity == null || entity.isBlank()) {
                throw new TableSelectionException("Nom d'entité invalide");
            }

            String entityName = entity.trim();

            for (String component : profile.selectedComponents()) {
                String normalizedComponent = normalizeComponent(component);
                TaskKey key = new TaskKey(view, entityName, normalizedComponent);
                String owner = assigned.putIfAbsent(key, profile.profileName());

                if (owner == null) {
                    tasks.add(new GenerationTask(profile.profileName(), entityName, view, normalizedComponent));
                } else {
                    skipped.add(new SkippedTask(profile.profileName(), entityName, view, normalizedComponent, owner
                    ));
                }
            }
        }
    }

    private String normalizeComponent(String component) {
        if (component == null) {
            throw new TableSelectionException("Composant null");
        }

        String normalized = component.trim().toLowerCase(Locale.ROOT);

        return switch (normalized) {
            case "model" -> "model";
            case "dao", "repository" -> "dao";
            case "service" -> "service";
            case "controller" -> "controller";
            default -> throw new TableSelectionException("Composant inconnu : " + component);
        };
    }

    private record TaskKey(boolean view, String entityName, String component) {
    }
}

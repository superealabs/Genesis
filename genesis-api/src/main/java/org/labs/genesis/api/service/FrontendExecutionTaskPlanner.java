package org.labs.genesis.api.service;

import org.labs.genesis.api.dto.GenerationExecutionPlanDtos.ExecutionBatch;
import org.labs.genesis.config.ProjectGenerationContext;
import org.labs.genesis.config.langage.FrameworkMVC;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class FrontendExecutionTaskPlanner {

    private final FrontendGenerationPolicy frontendPolicy;

    public FrontendExecutionTaskPlanner(FrontendGenerationPolicy frontendPolicy) {
        this.frontendPolicy = frontendPolicy;
    }

    public List<FrontendExecutionTask> prepare(List<ExecutionBatch> batches, ProjectGenerationContext context) {
        if (context == null || context.getFramework() == null) {
            throw new IllegalArgumentException("Framework non configuré");
        }

        boolean mvc = context.getFramework() instanceof FrameworkMVC;
        boolean frontendEnabled = context.isGenerateFrontendApp();

        List<String> configuredOptions = context.getFrontendGenerationOptions() == null
                        ? List.of()
                        : context.getFrontendGenerationOptions();

        List<FrontendExecutionTask> result = new ArrayList<>();

        for (FrontendGenerationPolicy.FrontendTask task : frontendPolicy.resolve(batches)) {
            if (mvc) {
                if (task.complete()) {
                    result.add(new FrontendExecutionTask(task.entityName(), task.view(), true, List.of(), true));
                }
                continue;
            }

            if (!frontendEnabled || context.getFrontendFramework() == null || context.getFrontendLanguage() == null) {
                continue;
            }

            List<String> allowed = task.frontendOptions()
                    .stream()
                    .filter(configuredOptions::contains)
                    .toList();

            if (!allowed.isEmpty()) {
                result.add(new FrontendExecutionTask(
                        task.entityName(),
                        task.view(),
                        false,
                        allowed,
                        task.complete() && allowed.contains(ProjectGenerationContext.FRONTEND_COMPONENT)
                ));
            }
        }
        return List.copyOf(result);
    }

    public record FrontendExecutionTask(
            String entityName,
            boolean view,
            boolean mvc,
            List<String> options,
            boolean completeDisplay
    ) {
    }
}

package org.labs.genesis.api.dto;

import java.util.List;

public final class GenerationPlanDtos {

    private GenerationPlanDtos() {
    }

    public record GenerationTask(
            String profileName,
            String entityName,
            boolean view,
            String component
    ) {
    }

    public record SkippedTask(
            String profileName,
            String entityName,
            boolean view,
            String component,
            String alreadyAssignedTo
    ) {
    }

    public record GenerationPlanResponse(
            int profileCount,
            int taskCount,
            int skippedCount,
            List<GenerationTask> tasks,
            List<SkippedTask> skipped
    ) {
    }
}

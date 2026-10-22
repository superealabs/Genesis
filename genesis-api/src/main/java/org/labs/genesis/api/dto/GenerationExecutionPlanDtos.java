package org.labs.genesis.api.dto;

import java.util.List;

public final class GenerationExecutionPlanDtos {

    private GenerationExecutionPlanDtos() {
    }

    public record ExecutionBatch(
            String profileName,
            String entityName,
            boolean view,
            List<String> components
    ) {
    }

    public record ExecutionPlanResponse(
            int profileCount,
            int taskCount,
            int batchCount,
            int skippedCount,
            List<ExecutionBatch> batches
    ) {
    }
}

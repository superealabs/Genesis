package org.labs.genesis.api.dto;

import java.util.List;

public final class GenerationDtos {
    private GenerationDtos() {
    }

    public record GenerationModeResponse(
            String id,
            String label
    ) {
    }

    public record GenerationModeSelectionRequest(
            String mode
    ) {
    }

    public record GenerationModeSelectionResponse(
            boolean success,
            String mode
    ) {
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

    public record GenerationLaunchResponse(
            boolean success,
            String message,
            int profileCount,
            int generatedTaskCount,
            int skippedDuplicateTaskCount,
            int preparedFileCount,
            List<String> createdFiles,
            List<String> skippedExistingFiles
    ) {
        public GenerationLaunchResponse {
            createdFiles = List.copyOf(createdFiles);
            skippedExistingFiles = List.copyOf(skippedExistingFiles);
        }
    }

    public record ValidationResponse(boolean valid, String message) {
    }

    public record LaunchResult(
            boolean success,
            int backendBatchCount,
            int frontendTaskCount,
            GenerationDtos.PublicationReport publication
    ) {
    }

    public record PublicationReport(
            int preparedFileCount,
            List<String> createdFiles,
            List<String> skippedExistingFiles
    ) {
        public PublicationReport {
            createdFiles = List.copyOf(createdFiles);
            skippedExistingFiles = List.copyOf(skippedExistingFiles);
        }
    }
}

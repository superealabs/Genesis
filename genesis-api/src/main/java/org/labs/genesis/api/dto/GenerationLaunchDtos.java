package org.labs.genesis.api.dto;

import java.util.List;

public final class GenerationLaunchDtos {

    private GenerationLaunchDtos() {
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
}

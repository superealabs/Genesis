package org.labs.genesis.api.dto;

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
}

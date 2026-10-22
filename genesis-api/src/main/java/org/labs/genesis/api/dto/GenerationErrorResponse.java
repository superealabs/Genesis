package org.labs.genesis.api.dto;

import java.time.Instant;

public record GenerationErrorResponse(
        Instant timestamp,
        int status,
        String code,
        String message
) {
}
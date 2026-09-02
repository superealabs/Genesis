package org.labs.genesis.api.dto.database;

public record DatabaseConfigResponse(
        boolean success,
        boolean connected,
        String message
) {
}
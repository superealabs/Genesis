package org.labs.genesis.api.dto.database;

public record DatabaseResponse(
        int id,
        String name,
        String defaultPort
) {
}
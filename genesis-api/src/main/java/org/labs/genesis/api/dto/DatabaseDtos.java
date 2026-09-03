package org.labs.genesis.api.dto;

public final class DatabaseDtos {
    private DatabaseDtos() {
    }

    public record DatabaseConfigRequest(
            Integer databaseId,
            String host,
            String port,
            String databaseName,
            String schema,
            String username,
            String password,
            String sid,
            Boolean trustCertificate,
            Boolean allowPublicKeyRetrieval
    ) {
    }

    public record DatabaseConfigResponse(
            boolean success,
            boolean connected,
            String message
    ) {
    }

    public record DatabaseResponse(
            int id,
            String name,
            String defaultPort
    ) {
    }
}

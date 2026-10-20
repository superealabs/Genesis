package org.labs.genesis.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

public final class DatabaseDtos {

    private DatabaseDtos() {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record DatabaseConfigRequest(
            Integer databaseId,
            String engine,
            String host,
            String port,
            String databaseName,
            String schema,
            String username,
            String password,
            String driverType,
            String driverName,
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

    public record DatabaseConnectionTestResponse(
            boolean success,
            String message
    ) {
    }

    public record DatabaseResponse(
            int id,
            String name,
            String defaultPort
    ) {
    }

    public record DatabaseEngineResponse(
            int id,
            String name,
            String driver,
            String driverName,
            String port,
            String driverType,
            String sid,
            String driverVersion,
            int databaseMajorVersion
    ) {
    }
}

package org.labs.genesis.api.dto.database;

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
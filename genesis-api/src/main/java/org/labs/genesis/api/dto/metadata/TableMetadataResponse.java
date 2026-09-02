package org.labs.genesis.api.dto.metadata;

public record TableMetadataResponse(
        String tableName,
        String className,
        boolean isView
) {
}

package org.labs.genesis.api.mapper;

import org.labs.genesis.api.dto.MetadataDtos.TableMetadataResponse;
import org.labs.genesis.connexion.model.TableMetadata;

public final class TableMetadataMapper {

    private TableMetadataMapper() {
    }

    public static TableMetadataResponse toResponse(TableMetadata tableMetadata) {
        return new TableMetadataResponse(
                tableMetadata.getTableName(),
                tableMetadata.getClassName(),
                Boolean.TRUE.equals(tableMetadata.getIsView())
        );
    }
}

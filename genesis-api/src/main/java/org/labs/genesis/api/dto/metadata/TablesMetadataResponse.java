package org.labs.genesis.api.dto.metadata;

import java.util.List;

public record TablesMetadataResponse(
        List<TableMetadataResponse> tables,
        List<TableMetadataResponse> views
) {
}

package org.labs.genesis.api.dto;

import java.util.List;

public final class MetadataDtos {
    private MetadataDtos() {
    }

    public record TableMetadataResponse(
            String tableName,
            String className,
            boolean isView
    ) {
    }

    public record TableSelectionRequest(
            List<String> selectedTables,
            List<String> selectedViews,
            List<String> selectedComponents
    ) {
    }

    public record TableSelectionResponse(
            boolean success,
            List<String> selectedTables,
            List<String> selectedViews,
            List<String> generationOptions
    ) {
    }

    public record TablesMetadataResponse(
            List<TableMetadataResponse> tables,
            List<TableMetadataResponse> views
    ) {
    }
}

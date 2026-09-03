package org.labs.genesis.api.dto.metadata;

import java.util.List;

public record TableSelectionResponse(
        boolean success,
        List<String> selectedTables,
        List<String> selectedViews,
        List<String> generationOptions
) {
}

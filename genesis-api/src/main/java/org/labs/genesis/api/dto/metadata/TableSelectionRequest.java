package org.labs.genesis.api.dto.metadata;

import java.util.List;

public record TableSelectionRequest(
        List<String> selectedTables,
        List<String> selectedViews,
        List<String> selectedComponents
) {
}

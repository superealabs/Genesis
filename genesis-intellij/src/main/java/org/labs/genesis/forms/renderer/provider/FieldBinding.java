package org.labs.genesis.forms.renderer.provider;

import org.labs.genesis.forms.ui.visualization.model.QueryRole;

public record FieldBinding(
        FieldReference field,
        QueryRole role,
        boolean measure,
        String aggregation,
        String alias
) {
    public FieldBinding {
        if (field == null || role == null) throw new IllegalArgumentException("Field binding is incomplete");
        aggregation = aggregation == null ? null : aggregation.trim().toUpperCase().replace(' ', '_');
        alias = alias == null || alias.isBlank() ? field.column() : alias;
    }

    public boolean aggregated() {
        return measure && aggregation != null && !aggregation.isBlank() && !"NONE".equals(aggregation);
    }
}

package org.labs.genesis.forms.renderer.provider;

import org.jooq.Field;

public record SelectExpression(Field<?> expression, String alias, boolean aggregated) {
    public SelectExpression {
        if (expression == null) throw new IllegalArgumentException("Select expression cannot be null");
    }
}

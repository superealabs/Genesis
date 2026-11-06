package org.labs.genesis.forms.ui.visualization.model;

public enum FilterLogicalOperator {
    AND("AND"),
    OR("OR");

    private final String label;

    FilterLogicalOperator(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }

    public static FilterLogicalOperator fromValue(Object value) {
        if (value == null) {
            return AND;
        }

        String normalized = value.toString().trim();
        if (normalized.equalsIgnoreCase("OR")) {
            return OR;
        }
        return AND;
    }
}

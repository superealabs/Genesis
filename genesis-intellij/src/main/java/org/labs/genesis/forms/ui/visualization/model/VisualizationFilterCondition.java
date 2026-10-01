package org.labs.genesis.forms.ui.visualization.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VisualizationFilterCondition implements VisualizationFilterNode {

    private String column;
    private String operator = "is";
    private String value;
    private FilterLogicalOperator relationToPrevious = FilterLogicalOperator.AND;

    public VisualizationFilterCondition() {
    }

    public VisualizationFilterCondition(String column, String operator, String value) {
        this.column = column;
        this.operator = operator;
        this.value = value;
    }

    public boolean requiresValue() {
        if (operator == null || operator.isBlank()) {
            return false;
        }

        String normalized = operator.toLowerCase();
        return !normalized.contains("empty")
                && !normalized.contains("not empty");
    }
}

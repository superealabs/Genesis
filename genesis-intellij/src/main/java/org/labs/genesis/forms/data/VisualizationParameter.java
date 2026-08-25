package org.labs.genesis.forms.data;

import lombok.Getter;

@Getter
public class VisualizationParameter {

    private final String key;
    private final String label;
    private final VisualizationParameterType type;

    public VisualizationParameter(
            String key,
            String label,
            VisualizationParameterType type
    ) {
        this.key = key;
        this.label = label;
        this.type = type;
    }

    public static VisualizationParameter text(
            String key,
            String label
    ) {
        return new VisualizationParameter(
                key,
                label,
                VisualizationParameterType.TEXT
        );
    }

    public static VisualizationParameter number(
            String key,
            String label
    ) {
        return new VisualizationParameter(
                key,
                label,
                VisualizationParameterType.NUMBER
        );
    }

    public static VisualizationParameter dbColumn(
            String key,
            String label
    ) {
        return new VisualizationParameter(
                key,
                label,
                VisualizationParameterType.DB_COLUMN
        );
    }

    public static VisualizationParameter formula(
            String key,
            String label
    ) {
        return new VisualizationParameter(
                key,
                label,
                VisualizationParameterType.FORMULA
        );
    }

    public static VisualizationParameter columnOrFormula(
            String key,
            String label
    ) {
        return new VisualizationParameter(
                key,
                label,
                VisualizationParameterType.DB_COLUMN_OR_FORMULA
        );
    }

    public static VisualizationParameter sort(
            String key,
            String label
    ) {
        return new VisualizationParameter(
                key,
                label,
                VisualizationParameterType.SORT
        );
    }

    public static VisualizationParameter columns(
            String key,
            String label
    ) {
        return new VisualizationParameter(
                key,
                label,
                VisualizationParameterType.COLUMNS
        );
    }
}
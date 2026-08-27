package org.labs.genesis.forms.ui.visualization.model;

import lombok.Getter;
import lombok.Setter;

@Getter
public class VisualizationParameter {

    private final String key;
    private final String label;
    private final VisualizationParameterType type;

    private String mode;
    @Setter
    private String value;
    private boolean required = false;

    public VisualizationParameter(
            String key,
            String label,
            VisualizationParameterType type
    ) {
        this.key = key;
        this.label = label;
        this.type = type;
    }

    public VisualizationParameter(
            String key,
            String label,
            VisualizationParameterType type,
            String mode
    ) {
        this.key = key;
        this.label = label;
        this.type = type;
        this.mode = mode;
    }

    // -------------------------------------------------------------------------
    // Factory methods
    // -------------------------------------------------------------------------

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
                VisualizationParameterType.DB_COLUMN_OR_FORMULA,
                "COLUMN"
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

    public static VisualizationParameter condition(
            String key,
            String label
    ) {
        return new VisualizationParameter(
                key,
                label,
                VisualizationParameterType.CONDITION
        );
    }

    // -------------------------------------------------------------------------
    // Value / mode
    // -------------------------------------------------------------------------

    public void setMode(String mode) {
        this.mode = mode;
    }

    public void setMode(
            String mode,
            boolean clearValue
    ) {
        this.mode = mode;

        if (clearValue) {
            this.value = null;
        }
    }

    // -------------------------------------------------------------------------
    // Mode utility methods
    // -------------------------------------------------------------------------

    /**
     * Vérifie si le mode est "COLUMN".
     * @return true si le mode est "COLUMN", false sinon
     */
    public boolean isColumnMode() {
        return "COLUMN".equals(mode);
    }

    /**
     * Vérifie si le mode est "FORMULA".
     * @return true si le mode est "FORMULA", false sinon
     */
    public boolean isFormulaMode() {
        return "FORMULA".equals(mode);
    }

    /**
     * Vérifie si le mode est "ASCENDING".
     * @return true si le mode est "ASCENDING", false sinon
     */
    public boolean isAscendingMode() {
        return "ASCENDING".equals(mode);
    }

    /**
     * Vérifie si le mode est "DESCENDING".
     * @return true si le mode est "DESCENDING", false sinon
     */
    public boolean isDescendingMode() {
        return "DESCENDING".equals(mode);
    }

    /**
     * Définit le mode sur "COLUMN" et efface la valeur.
     */
    public void setColumnMode() {
        setMode("COLUMN", true);
    }

    /**
     * Définit le mode sur "FORMULA" et efface la valeur.
     */
    public void setFormulaMode() {
        setMode("FORMULA", true);
    }

    /**
     * Définit le mode sur "ASCENDING" et efface la valeur.
     */
    public void setAscendingMode() {
        setMode("ASCENDING", true);
    }

    /**
     * Définit le mode sur "DESCENDING" et efface la valeur.
     */
    public void setDescendingMode() {
        setMode("DESCENDING", true);
    }
}
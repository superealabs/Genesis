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

    private final boolean required;

    // -------------------------------------------------------------------------
    // Query definition
    // -------------------------------------------------------------------------

    /**
     * Rôle du paramètre dans la construction des données.
     *
     * Exemples :
     * - X Axis    -> DIMENSION
     * - Y Axis    -> MEASURE
     * - Scatter X -> VALUE
     * - Limit     -> LIMIT
     * - Sort      -> SORT
     * - Filter    -> FILTER
     */
    private final QueryRole role;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    public VisualizationParameter(
            String key,
            String label,
            VisualizationParameterType type,
            boolean required
    ) {
        this(
                key,
                label,
                type,
                null,
                required,
                null
        );
    }

    public VisualizationParameter(
            String key,
            String label,
            VisualizationParameterType type,
            String mode,
            boolean required
    ) {
        this(
                key,
                label,
                type,
                mode,
                required,
                null
        );
    }

    /**
     * Constructeur principal.
     */
    public VisualizationParameter(
            String key,
            String label,
            VisualizationParameterType type,
            String mode,
            boolean required,
            QueryRole role
    ) {
        this.key = key;
        this.label = label;
        this.type = type;
        this.mode = mode;
        this.required = required;
        this.role = role;
    }

    // -------------------------------------------------------------------------
    // Factory methods
    // -------------------------------------------------------------------------

    public static VisualizationParameter text(
            String key,
            String label,
            boolean required
    ) {
        return new VisualizationParameter(
                key,
                label,
                VisualizationParameterType.TEXT,
                required
        );
    }

    public static VisualizationParameter number(
            String key,
            String label,
            boolean required
    ) {
        return new VisualizationParameter(
                key,
                label,
                VisualizationParameterType.NUMBER,
                required
        );
    }

    public static VisualizationParameter dbColumn(
            String key,
            String label,
            boolean required
    ) {
        return new VisualizationParameter(
                key,
                label,
                VisualizationParameterType.DB_COLUMN,
                required
        );
    }

    public static VisualizationParameter formula(
            String key,
            String label,
            boolean required
    ) {
        return new VisualizationParameter(
                key,
                label,
                VisualizationParameterType.FORMULA,
                required
        );
    }

    /**
     * Paramètre colonne ou formule sans rôle spécifique.
     */
    public static VisualizationParameter columnOrFormula(
            String key,
            String label,
            boolean required
    ) {
        return new VisualizationParameter(
                key,
                label,
                VisualizationParameterType.DB_COLUMN_OR_FORMULA,
                "COLUMN",
                required
        );
    }

    public static VisualizationParameter sort(
            String key,
            String label,
            boolean required
    ) {
        return ordering(key, label, required);
    }

    public static VisualizationParameter columns(
            String key,
            String label,
            boolean required
    ) {
        return new VisualizationParameter(
                key,
                label,
                VisualizationParameterType.COLUMNS,
                null,
                required,
                QueryRole.COLUMNS
        );
    }

    public boolean isColumns() {
        return role == QueryRole.COLUMNS;
    }

    public static VisualizationParameter condition(
            String key,
            String label,
            boolean required
    ) {
        return filter(key, label, required);
    }

    // -------------------------------------------------------------------------
    // Query-aware factory methods
    // -------------------------------------------------------------------------

    /**
     * Paramètre utilisé comme dimension.
     *
     * Exemple :
     *
     *     country
     *
     * Le DataProvider pourra produire :
     *
     *     GROUP BY country
     */
    public static VisualizationParameter dimension(
            String key,
            String label,
            boolean required
    ) {
        return new VisualizationParameter(
                key,
                label,
                VisualizationParameterType.DB_COLUMN,
                "COLUMN",
                required,
                QueryRole.DIMENSION
        );
    }

    /**
     * Paramètre colonne ou formule utilisé comme dimension.
     *
     * Exemple :
     *
     *     YEAR(date)
     *
     * Le DataProvider pourra l'utiliser comme dimension.
     */
    public static VisualizationParameter dimensionOrFormula(
            String key,
            String label,
            boolean required
    ) {
        return new VisualizationParameter(
                key,
                label,
                VisualizationParameterType.DB_COLUMN_OR_FORMULA,
                "COLUMN",
                required,
                QueryRole.DIMENSION
        );
    }

    /**
     * Paramètre utilisé comme mesure.
     *
     * Une MEASURE est toujours agrégée avec SUM.
     *
     * Exemple :
     *
     *     amount
     *
     * devient conceptuellement :
     *
     *     SUM(amount)
     */
    public static VisualizationParameter measure(
            String key,
            String label,
            boolean required
    ) {
        return new VisualizationParameter(
                key,
                label,
                VisualizationParameterType.DB_COLUMN,
                "COLUMN",
                required,
                QueryRole.MEASURE
        );
    }

    /**
     * Paramètre colonne ou formule utilisé comme mesure.
     *
     * Une MEASURE est toujours agrégée avec SUM.
     */
    public static VisualizationParameter measureOrFormula(
            String key,
            String label,
            boolean required
    ) {
        return new VisualizationParameter(
                key,
                label,
                VisualizationParameterType.DB_COLUMN_OR_FORMULA,
                "COLUMN",
                required,
                QueryRole.MEASURE
        );
    }

    /**
     * Paramètre utilisé comme valeur brute.
     *
     * Contrairement à MEASURE, VALUE n'est pas agrégé.
     *
     * Utile notamment pour :
     *
     * - Scatter Plot
     * - coordonnées
     * - valeurs individuelles
     */
    public static VisualizationParameter value(
            String key,
            String label,
            boolean required
    ) {
        return new VisualizationParameter(
                key,
                label,
                VisualizationParameterType.DB_COLUMN,
                "COLUMN",
                required,
                QueryRole.VALUE
        );
    }

    /**
     * Paramètre colonne ou formule utilisé comme valeur brute.
     */
    public static VisualizationParameter valueOrFormula(
            String key,
            String label,
            boolean required
    ) {
        return new VisualizationParameter(
                key,
                label,
                VisualizationParameterType.DB_COLUMN_OR_FORMULA,
                "COLUMN",
                required,
                QueryRole.VALUE
        );
    }

    /**
     * Paramètre utilisé comme limite.
     *
     * Exemple :
     *
     *     LIMIT 10
     */
    public static VisualizationParameter limit(
            String key,
            String label,
            boolean required
    ) {
        return new VisualizationParameter(
                key,
                label,
                VisualizationParameterType.NUMBER,
                required,
                QueryRole.LIMIT
        );
    }

    /**
     * Paramètre utilisé pour le tri.
     *
     * Le sens du tri est déterminé par le mode :
     *
     *     ASCENDING
     *     DESCENDING
     */
    public static VisualizationParameter ordering(
            String key,
            String label,
            boolean required
    ) {
        return new VisualizationParameter(
                key,
                label,
                VisualizationParameterType.SORT,
                required,
                QueryRole.SORT
        );
    }

    /**
     * Paramètre utilisé comme filtre.
     *
     * Exemple :
     *
     *     status = 'ACTIVE'
     */
    public static VisualizationParameter filter(
            String key,
            String label,
            boolean required
    ) {
        return new VisualizationParameter(
                key,
                label,
                VisualizationParameterType.CONDITION,
                required,
                QueryRole.FILTER
        );
    }

    /**
     * Paramètre latitude.
     *
     * La latitude est toujours une valeur brute.
     */
    public static VisualizationParameter latitude(
            String key,
            String label,
            boolean required
    ) {
        return new VisualizationParameter(
                key,
                label,
                VisualizationParameterType.DB_COLUMN,
                "COLUMN",
                required,
                QueryRole.LATITUDE
        );
    }

    /**
     * Paramètre longitude.
     *
     * La longitude est toujours une valeur brute.
     */
    public static VisualizationParameter longitude(
            String key,
            String label,
            boolean required
    ) {
        return new VisualizationParameter(
                key,
                label,
                VisualizationParameterType.DB_COLUMN,
                "COLUMN",
                required,
                QueryRole.LONGITUDE
        );
    }

    // -------------------------------------------------------------------------
    // Internal query constructors
    // -------------------------------------------------------------------------

    private VisualizationParameter(
            String key,
            String label,
            VisualizationParameterType type,
            boolean required,
            QueryRole role
    ) {
        this(
                key,
                label,
                type,
                null,
                required,
                role
        );
    }

    // -------------------------------------------------------------------------
    // Query utility methods
    // -------------------------------------------------------------------------

    /**
     * Vérifie si le paramètre possède un rôle de requête.
     */
    public boolean hasQueryRole() {
        return role != null;
    }

    public boolean isDimension() {
        return role == QueryRole.DIMENSION;
    }

    public boolean isMeasure() {
        return role == QueryRole.MEASURE;
    }

    public boolean isValue() {
        return role == QueryRole.VALUE;
    }

    public boolean isLimit() {
        return role == QueryRole.LIMIT;
    }

    public boolean isSort() {
        return role == QueryRole.SORT;
    }

    public boolean isFilter() {
        return role == QueryRole.FILTER;
    }

    public boolean isLatitude() {
        return role == QueryRole.LATITUDE;
    }

    public boolean isLongitude() {
        return role == QueryRole.LONGITUDE;
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

    public boolean isColumnMode() {
        return "COLUMN".equals(mode);
    }

    public boolean isFormulaMode() {
        return "FORMULA".equals(mode);
    }

    public boolean isAscendingMode() {
        return "ASCENDING".equals(mode);
    }

    public boolean isDescendingMode() {
        return "DESCENDING".equals(mode);
    }

    public void setColumnMode() {
        setMode("COLUMN", true);
    }

    public void setFormulaMode() {
        setMode("FORMULA", true);
    }

    public void setAscendingMode() {
        setMode("ASCENDING", true);
    }

    public void setDescendingMode() {
        setMode("DESCENDING", true);
    }
}
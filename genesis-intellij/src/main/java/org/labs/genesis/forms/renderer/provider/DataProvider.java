package org.labs.genesis.forms.renderer.provider;

import org.jooq.*;
import org.jooq.Record;
import org.jooq.impl.DSL;
import org.labs.genesis.forms.ui.visualization.model.VisualizationConfig;
import org.labs.genesis.forms.ui.visualization.model.VisualizationItem;
import org.labs.genesis.forms.ui.visualization.model.VisualizationParameter;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

public class DataProvider {

    // =========================================================================
    // CHART
    // =========================================================================

    public ChartData loadChart(
            Connection connection,
            String tableName,
            VisualizationConfig config,
            VisualizationItem item
    ) throws Exception {

        validateInputs(
                connection,
                tableName,
                config,
                item
        );

        DSLContext dsl =
                DSL.using(
                        connection,
                        dialect(connection)
                );

        List<VisualizationParameter> parameters =
                item.parameters
                        .stream()
                        .filter(
                                VisualizationParameter::hasQueryRole
                        )
                        .toList();

        List<VisualizationParameter> dimensions =
                parameters
                        .stream()
                        .filter(
                                VisualizationParameter::isDimension
                        )
                        .toList();

        List<VisualizationParameter> measures =
                parameters
                        .stream()
                        .filter(
                                VisualizationParameter::isMeasure
                        )
                        .toList();

        List<VisualizationParameter> values =
                parameters
                        .stream()
                        .filter(
                                VisualizationParameter::isValue
                        )
                        .toList();

        return executeChart(
                dsl,
                tableName,
                config,
                dimensions,
                measures,
                values
        );
    }

    // =========================================================================
    // TABLE
    // =========================================================================

    public TableData loadTable(
            Connection connection,
            String tableName,
            VisualizationConfig config
    ) throws Exception {

        validateTableInputs(
                connection,
                tableName,
                config
        );

        DSLContext dsl =
                DSL.using(
                        connection,
                        dialect(connection)
                );

        return executeTable(
                dsl,
                tableName,
                config
        );
    }

    // =========================================================================
    // VALIDATION
    // =========================================================================

    private void validateInputs(
            Connection connection,
            String tableName,
            VisualizationConfig config,
            VisualizationItem item
    ) {

        if (connection == null) {

            throw new IllegalArgumentException(
                    "Database connection cannot be null"
            );
        }

        if (tableName == null
                || tableName.isBlank()) {

            throw new IllegalArgumentException(
                    "Table name cannot be empty"
            );
        }

        if (config == null) {

            throw new IllegalArgumentException(
                    "Visualization config cannot be null"
            );
        }

        if (item == null) {

            throw new IllegalArgumentException(
                    "Visualization item cannot be null"
            );
        }
    }

    private void validateTableInputs(
            Connection connection,
            String tableName,
            VisualizationConfig config
    ) {

        if (connection == null) {

            throw new IllegalArgumentException(
                    "Database connection cannot be null"
            );
        }

        if (tableName == null
                || tableName.isBlank()) {

            throw new IllegalArgumentException(
                    "Table name cannot be empty"
            );
        }

        if (config == null) {

            throw new IllegalArgumentException(
                    "Visualization config cannot be null"
            );
        }
    }

    // =========================================================================
    // CHART EXECUTION
    // =========================================================================

    private ChartData executeChart(
            DSLContext dsl,
            String tableName,
            VisualizationConfig config,
            List<VisualizationParameter> dimensions,
            List<VisualizationParameter> measures,
            List<VisualizationParameter> values
    ) {

        Table<?> table =
                DSL.table(
                        DSL.name(tableName)
                );

        List<SelectField<?>> selectFields =
                new ArrayList<>();

        List<Field<?>> dimensionFields =
                new ArrayList<>();

        List<Field<?>> measureFields =
                new ArrayList<>();

        dimensionFields =
                buildDimensionFields(
                        config,
                        dimensions,
                        selectFields
                );

        measureFields =
                buildMeasureFields(
                        config,
                        measures,
                        selectFields
                );

        buildValueFields(
                config,
                values,
                selectFields
        );

        if (selectFields.isEmpty()) {

            throw new IllegalArgumentException(
                    "No visualization value has been configured"
            );
        }

        Select<?> query =
                buildChartQuery(
                        dsl,
                        table,
                        selectFields,
                        dimensionFields,
                        measureFields
                );

        System.out.println(
                "[DataProvider] Chart SQL: " + query
        );

        Result<?> result =
                query.fetch();

        return convertChartResult(
                result,
                dimensions,
                measures,
                values
        );
    }

    // =========================================================================
    // TABLE EXECUTION
    // =========================================================================

    private TableData executeTable(
            DSLContext dsl,
            String tableName,
            VisualizationConfig config
    ) {

        Table<?> table =
                DSL.table(
                        DSL.name(tableName)
                );

        List<SelectField<?>> selectFields =
                buildTableColumns(
                        config
                );

        if (selectFields.isEmpty()) {

            throw new IllegalArgumentException(
                    "No table column has been configured"
            );
        }

        Select<?> query =
                buildTableQuery(
                        dsl,
                        table,
                        selectFields,
                        config
                );

        System.out.println(
                "[DataProvider] Table SQL: " + query
        );

        Result<?> result =
                query.fetch();

        return convertTableResult(
                result
        );
    }

    // =========================================================================
    // TABLE COLUMNS
    // =========================================================================

    private List<SelectField<?>> buildTableColumns(
            VisualizationConfig config
    ) {

        List<SelectField<?>> selectFields =
                new ArrayList<>();

        Object columns =
                config.getValue("columns");

        if (!(columns instanceof List<?> list)) {
            return selectFields;
        }

        for (Object item : list) {

            if (item == null) {
                continue;
            }

            String rawValue =
                    item.toString().trim();

            if (rawValue.isEmpty()) {
                continue;
            }

            String column =
                    extractColumnName(
                            rawValue
                    );

            if (column == null
                    || column.isBlank()) {

                continue;
            }

            Field<?> field =
                    DSL.field(
                            DSL.name(column)
                    );

            selectFields.add(field);
        }

        return selectFields;
    }

    // =========================================================================
    // TABLE QUERY
    // =========================================================================

    private Select<?> buildTableQuery(
            DSLContext dsl,
            Table<?> table,
            List<SelectField<?>> selectFields,
            VisualizationConfig config
    ) {

        SelectJoinStep<Record> from =
                dsl.select(selectFields)
                        .from(table);

        Integer limit =
                getLimit(config);

        if (limit != null
                && limit > 0) {

            return from.limit(limit);
        }

        return from;
    }

    // =========================================================================
    // TABLE RESULT
    // =========================================================================

    /**
     * Transforme directement le résultat SQL en TableData.
     *
     * Exemple :
     *
     * columns:
     *
     * [
     *     "id",
     *     "nom",
     *     "salaire"
     * ]
     *
     * rows:
     *
     * [
     *     [1, "Jean", 2500],
     *     [2, "Paul", 3000]
     * ]
     */
    private TableData convertTableResult(
            Result<?> result
    ) {

        List<String> columns =
                new ArrayList<>();

        List<List<Object>> rows =
                new ArrayList<>();

        // =====================================================================
        // COLUMNS
        // =====================================================================

        for (Field<?> field :
                result.fields()) {

            columns.add(
                    field.getName()
            );
        }

        // =====================================================================
        // ROWS
        // =====================================================================

        for (Record record : result) {

            List<Object> row =
                    new ArrayList<>();

            for (Field<?> field :
                    result.fields()) {

                Object value =
                        record.get(field);

                row.add(value);
            }

            rows.add(row);
        }

        return new TableData(
                columns,
                rows
        );
    }

    // =========================================================================
    // DIMENSIONS
    // =========================================================================

    private List<Field<?>> buildDimensionFields(
            VisualizationConfig config,
            List<VisualizationParameter> dimensions,
            List<SelectField<?>> selectFields
    ) {

        List<Field<?>> fields =
                new ArrayList<>();

        for (VisualizationParameter param :
                dimensions) {

            String column =
                    getColumnValue(
                            config,
                            param
                    );

            if (column == null) {
                continue;
            }

            Field<?> field =
                    DSL.field(
                            DSL.name(column)
                    );

            fields.add(field);

            selectFields.add(field);
        }

        return fields;
    }

    // =========================================================================
    // MEASURES
    // =========================================================================

    private List<Field<?>> buildMeasureFields(
            VisualizationConfig config,
            List<VisualizationParameter> measures,
            List<SelectField<?>> selectFields
    ) {

        List<Field<?>> fields =
                new ArrayList<>();

        for (VisualizationParameter param :
                measures) {

            String column =
                    getColumnValue(
                            config,
                            param
                    );

            if (column == null) {
                continue;
            }

            Field<BigDecimal> numericField =
                    DSL.field(
                            DSL.name(
                                    column
                            ),
                            BigDecimal.class
                    );

            Field<BigDecimal> aggregated =
                    DSL.sum(
                            numericField
                    ).as(
                            param.getKey()
                    );

            fields.add(
                    aggregated
            );

            selectFields.add(
                    aggregated
            );
        }

        return fields;
    }

    // =========================================================================
    // VALUES
    // =========================================================================

    private void buildValueFields(
            VisualizationConfig config,
            List<VisualizationParameter> values,
            List<SelectField<?>> selectFields
    ) {

        for (VisualizationParameter param :
                values) {

            String column =
                    getColumnValue(
                            config,
                            param
                    );

            if (column == null) {
                continue;
            }

            selectFields.add(
                    DSL.field(
                            DSL.name(column)
                    )
            );
        }
    }

    // =========================================================================
    // CHART QUERY
    // =========================================================================

    private Select<?> buildChartQuery(
            DSLContext dsl,
            Table<?> table,
            List<SelectField<?>> selectFields,
            List<Field<?>> dimensionFields,
            List<Field<?>> measureFields
    ) {

        SelectJoinStep<Record> from =
                dsl.select(selectFields)
                        .from(table);

        if (!dimensionFields.isEmpty()
                && !measureFields.isEmpty()) {

            return from.groupBy(
                    dimensionFields
            );
        }

        return from;
    }

    // =========================================================================
    // CHART RESULT
    // =========================================================================

    private ChartData convertChartResult(
            Result<?> result,
            List<VisualizationParameter> dimensions,
            List<VisualizationParameter> measures,
            List<VisualizationParameter> values
    ) {

        List<String> labels =
                new ArrayList<>();

        List<Double> chartValues =
                new ArrayList<>();

        List<double[]> points =
                new ArrayList<>();

        // =====================================================================
        // DIMENSION + MEASURE
        // =====================================================================

        if (!dimensions.isEmpty()
                && !measures.isEmpty()) {

            Field<?> dimensionField =
                    result.field(0);

            Field<?> measureField =
                    result.field(
                            dimensions.size()
                    );

            if (dimensionField == null
                    || measureField == null) {

                throw new IllegalStateException(
                        "Required fields missing from query result"
                );
            }

            for (Record record : result) {

                Object label =
                        record.get(
                                dimensionField
                        );

                labels.add(
                        label == null
                                ? ""
                                : String.valueOf(label)
                );

                chartValues.add(
                        toDouble(
                                record.get(
                                        measureField
                                )
                        )
                );
            }
        }

        // =====================================================================
        // SCATTER
        // =====================================================================

        else if (values.size() >= 2) {

            Field<?> xField =
                    result.field(0);

            Field<?> yField =
                    result.field(1);

            if (xField == null
                    || yField == null) {

                throw new IllegalStateException(
                        "Scatter plot requires at least two fields"
                );
            }

            for (Record record : result) {

                points.add(
                        new double[]{
                                toDouble(
                                        record.get(
                                                xField
                                        )
                                ),

                                toDouble(
                                        record.get(
                                                yField
                                        )
                                )
                        }
                );
            }
        }

        // =====================================================================
        // MEASURE ONLY
        // =====================================================================

        else if (!measures.isEmpty()) {

            Field<?> measureField =
                    result.field(0);

            if (measureField == null) {

                throw new IllegalStateException(
                        "Measure field is missing from query result"
                );
            }

            for (Record record : result) {

                chartValues.add(
                        toDouble(
                                record.get(
                                        measureField
                                )
                        )
                );
            }
        }

        return new ChartData(
                labels,
                chartValues
                        .stream()
                        .mapToDouble(
                                Double::doubleValue
                        )
                        .toArray(),
                points.toArray(
                        new double[0][]
                )
        );
    }

    // =========================================================================
    // CONFIG HELPERS
    // =========================================================================

    private String getColumnValue(
            VisualizationConfig config,
            VisualizationParameter parameter
    ) {

        Object value =
                config.getValue(
                        parameter.getKey()
                );

        if (value == null) {
            return null;
        }

        String stringValue =
                value.toString().trim();

        if (stringValue.isEmpty()) {
            return null;
        }

        return extractColumnName(
                stringValue
        );
    }

    /**
     * Exemples :
     *
     * COLUMN:poste.libelle -> libelle
     * poste.libelle        -> libelle
     * libelle              -> libelle
     */
    private String extractColumnName(
            String value
    ) {

        if (value == null) {
            return null;
        }

        value =
                value.trim();

        if (value.isEmpty()) {
            return null;
        }

        if (value
                .toUpperCase()
                .startsWith("COLUMN:")) {

            value =
                    value.substring(7).trim();
        }

        int lastDotIndex =
                value.lastIndexOf('.');

        if (lastDotIndex >= 0
                && lastDotIndex < value.length() - 1) {

            return value.substring(
                    lastDotIndex + 1
            );
        }

        return value;
    }

    // =========================================================================
    // LIMIT
    // =========================================================================

    private Integer getLimit(
            VisualizationConfig config
    ) {

        Object value =
                config.getValue("limit");

        if (value == null) {
            return null;
        }

        if (value instanceof Number number) {

            return number.intValue();
        }

        try {

            String stringValue =
                    value.toString().trim();

            if (stringValue.isEmpty()) {
                return null;
            }

            return Integer.parseInt(
                    stringValue
            );

        } catch (NumberFormatException e) {

            return null;
        }
    }

    // =========================================================================
    // NUMERIC CONVERSION
    // =========================================================================

    private double toDouble(
            Object value
    ) {

        if (value == null) {
            return 0.0;
        }

        if (value instanceof Number number) {

            return number.doubleValue();
        }

        try {

            return Double.parseDouble(
                    value.toString()
            );

        } catch (NumberFormatException e) {

            throw new IllegalArgumentException(
                    "Value is not numeric: " + value,
                    e
            );
        }
    }

    // =========================================================================
    // SQL DIALECT
    // =========================================================================

    private SQLDialect dialect(
            Connection connection
    ) {

        try {

            String product =
                    connection
                            .getMetaData()
                            .getDatabaseProductName()
                            .toLowerCase();

            if (product.contains("postgres")) {
                return SQLDialect.POSTGRES;
            }

            if (product.contains("mariadb")) {
                return SQLDialect.MARIADB;
            }

            if (product.contains("mysql")) {
                return SQLDialect.MYSQL;
            }

            if (product.contains("sqlite")) {
                return SQLDialect.SQLITE;
            }

            if (product.contains("oracle")
                    || product.contains("sql server")
                    || product.contains("microsoft")) {

                return SQLDialect.DEFAULT;
            }

            return SQLDialect.DEFAULT;

        } catch (Exception e) {

            return SQLDialect.DEFAULT;
        }
    }

    // =========================================================================
    // TABLE NAME
    // =========================================================================

    /**
     * Exemples :
     *
     * COLUMN:poste.libelle -> poste
     * poste.libelle        -> poste
     * libelle              -> null
     */
    public static String extractTableNameStatic(
            String value
    ) {

        if (value == null) {
            return null;
        }

        value =
                value.trim();

        if (value.isEmpty()) {
            return null;
        }

        if (value
                .toUpperCase()
                .startsWith("COLUMN:")) {

            value =
                    value.substring(7).trim();
        }

        int lastDotIndex =
                value.lastIndexOf('.');

        if (lastDotIndex > 0
                && lastDotIndex < value.length() - 1) {

            return value.substring(
                    0,
                    lastDotIndex
            );
        }

        return null;
    }
}
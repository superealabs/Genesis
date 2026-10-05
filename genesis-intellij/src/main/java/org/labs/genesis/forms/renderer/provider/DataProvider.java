package org.labs.genesis.forms.renderer.provider;

import org.jooq.*;
import org.jooq.Record;
import org.jooq.impl.DSL;
import org.labs.genesis.connexion.model.TableMetadata;
import org.labs.genesis.forms.ui.visualization.model.VisualizationConfig;
import org.labs.genesis.forms.ui.visualization.model.VisualizationItem;
import org.labs.genesis.forms.ui.visualization.model.VisualizationParameter;
import org.labs.genesis.dashboard.model.DashboardVisualization;
import org.labs.genesis.dashboard.query.DashboardQueryBuilder;
import org.labs.genesis.dashboard.query.DashboardQueryPlan;
import org.labs.genesis.forms.ui.visualization.DashboardVisualizationMapper;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataProvider {

    public static final String[] AGGREGATIONS = new String[]{
            "NONE", "SUM", "COUNT", "AVG", "MIN", "MAX", "COUNT DISTINCT"
    };
    public static final String DEFAULT_AGGREGATION = AGGREGATIONS[0];

    // =========================
    // MAP
    // =========================

    public MapData loadMap(
            Connection connection,
            String tableName,
            VisualizationConfig config,
            VisualizationItem item,
            List<TableMetadata> availableTables
    ) throws Exception {

        validateTableInputs(connection, tableName, config);

        DSLContext dsl = DSL.using(
                connection,
                dialect(connection)
        );

        DashboardQueryPlan commonPlan = buildCommonPlan(tableName, config, item, availableTables);

        Select<?> query =
                new JooqDashboardQueryRenderer(
                        connection,
                        dsl
                ).render(
                        commonPlan
                );

        System.out.println("[DataProvider] Map SQL: " + query);

        Result<?> result = query.fetch();

        return convertMapResult(
                result,
                config
        );
    }

    private MapData convertMapResult(
            Result<?> result,
            VisualizationConfig config
    ) {

        // ---------------------------------
        // Configuration des colonnes
        // ---------------------------------

        String latitudeColumn = getOptionalColumnValue(
                config,
                "latitude"
        );

        String longitudeColumn = getOptionalColumnValue(
                config,
                "longitude"
        );

        String valueColumn = getOptionalColumnValue(
                config,
                "valueColumn"
        );

        String labelColumn = getOptionalColumnValue(
                config,
                "labelColumn"
        );

        // ---------------------------------
        // Validation
        // ---------------------------------

        if (latitudeColumn == null || latitudeColumn.isBlank()) {
            throw new IllegalStateException(
                    "Map requires a latitude column"
            );
        }

        if (longitudeColumn == null || longitudeColumn.isBlank()) {
            throw new IllegalStateException(
                    "Map requires a longitude column"
            );
        }

        // ---------------------------------
        // Marker type
        //
        // PIN    -> Leaflet marker
        // BUBBLE -> Leaflet circleMarker
        // ---------------------------------

        String markerType = getString(
                config,
                "markerType",
                "PIN"
        );

        markerType = normalizeMarkerType(
                markerType
        );

        // ---------------------------------
        // Map mode
        //
        // On le conserve car MapData le demande,
        // même si le renderer utilise surtout
        // MapPoint.markerType.
        // ---------------------------------

        MapData.MapMode mode =
                "PIN".equals(markerType)
                        ? MapData.MapMode.PIN
                        : MapData.MapMode.BUBBLE;

        // ---------------------------------
        // Recherche des champs dans le Result
        // ---------------------------------

        Field<?> latitudeField = findResultField(
                result,
                latitudeColumn
        );

        Field<?> longitudeField = findResultField(
                result,
                longitudeColumn
        );

        Field<?> valueField = valueColumn == null
                ? null
                : findResultField(
                result,
                valueColumn
        );

        Field<?> labelField = labelColumn == null
                ? null
                : findResultField(
                result,
                labelColumn
        );

        if (latitudeField == null) {
            throw new IllegalStateException(
                    "Latitude column '" +
                            latitudeColumn +
                            "' was not found in the query result"
            );
        }

        if (longitudeField == null) {
            throw new IllegalStateException(
                    "Longitude column '" +
                            longitudeColumn +
                            "' was not found in the query result"
            );
        }

        // ---------------------------------
        // Construction des points
        // ---------------------------------

        List<MapData.MapPoint> points =
                new ArrayList<>();

        for (Record record : result) {

            Object latitudeObject =
                    record.get(latitudeField);

            Object longitudeObject =
                    record.get(longitudeField);

            // Coordonnées obligatoires
            if (latitudeObject == null ||
                    longitudeObject == null) {
                continue;
            }

            Double latitude =
                    toNullableDouble(latitudeObject);

            Double longitude =
                    toNullableDouble(longitudeObject);

            // Valeurs invalides
            if (latitude == null ||
                    longitude == null) {
                continue;
            }

            // Latitude : -90 -> 90
            if (latitude < -90.0 ||
                    latitude > 90.0) {
                continue;
            }

            // Longitude : -180 -> 180
            if (longitude < -180.0 ||
                    longitude > 180.0) {
                continue;
            }

            // ---------------------------------
            // Value
            //
            // Peut être null.
            // Le renderer pourra utiliser 1
            // par défaut pour les bubbles.
            // ---------------------------------

            Double value = null;

            if (valueField != null) {
                value = toNullableDouble(
                        record.get(valueField)
                );
            }

            // ---------------------------------
            // Label
            // ---------------------------------

            String label = null;

            if (labelField != null) {

                Object labelObject =
                        record.get(labelField);

                if (labelObject != null) {
                    label = labelObject.toString();
                }
            }

            // ---------------------------------
            // Création du point
            // ---------------------------------

            points.add(
                    new MapData.MapPoint(
                            label,
                            markerType,
                            latitude,
                            longitude,
                            value
                    )
            );
        }

        // ---------------------------------
        // MapData final
        // ---------------------------------

        return new MapData(
                points,
                mode
        );
    }

    private String getOptionalColumnValue(
            VisualizationConfig config,
            String key
    ) {

        if (config == null) {
            return null;
        }

        Object value = config.getValue(key);

        if (value == null) {
            return null;
        }

        String stringValue = value.toString().trim();

        if (stringValue.isBlank()) {
            return null;
        }

        return extractColumnName(stringValue);
    }

    private String normalizeMarkerType(
            String markerType
    ) {

        if (markerType == null ||
                markerType.isBlank()) {
            return "PIN";
        }

        String normalized =
                markerType.trim().toUpperCase();

        if ("PIN".equals(normalized)) {
            return "PIN";
        }

        return "BUBBLE";
    }

    private Field<?> findResultField(
            Result<?> result,
            String columnName
    ) {

        if (result == null ||
                columnName == null ||
                columnName.isBlank()) {
            return null;
        }

        String target =
                extractColumnName(columnName);

        if (target == null ||
                target.isBlank()) {
            return null;
        }

        // ---------------------------------
        // 1. Recherche exacte
        // ---------------------------------

        Field<?> field =
                result.field(target);

        if (field != null) {
            return field;
        }

        // ---------------------------------
        // 2. Recherche insensible à la casse
        // ---------------------------------

        for (Field<?> current : result.fields()) {

            if (current.getName()
                    .equalsIgnoreCase(target)) {

                return current;
            }
        }

        // ---------------------------------
        // 3. Recherche sur la dernière partie
        //
        // Exemple :
        // "cities.latitude"
        // devient "latitude"
        // ---------------------------------

        for (Field<?> current : result.fields()) {

            String fieldName =
                    current.getName();

            if (fieldName == null) {
                continue;
            }

            int dotIndex =
                    fieldName.lastIndexOf('.');

            String simpleName =
                    dotIndex >= 0
                            ? fieldName.substring(
                            dotIndex + 1
                    )
                            : fieldName;

            if (simpleName.equalsIgnoreCase(target)) {
                return current;
            }
        }

        return null;
    }

    private Double toNullableDouble(
            Object value
    ) {

        if (value == null) {
            return null;
        }

        if (value instanceof Number number) {
            double result = number.doubleValue();

            if (Double.isNaN(result) ||
                    Double.isInfinite(result)) {
                return null;
            }

            return result;
        }

        try {

            String text =
                    value.toString().trim();

            if (text.isBlank()) {
                return null;
            }

            double result =
                    Double.parseDouble(text);

            if (Double.isNaN(result) ||
                    Double.isInfinite(result)) {
                return null;
            }

            return result;

        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String getString(VisualizationConfig config, String key, String fallback) {
        Object v = config.getValue(key);
        return v == null || v.toString().isBlank() ? fallback : v.toString(); }

    // =========================================================================
    // CHART
    // =========================================================================

    public ChartData loadChart(
            Connection connection,
            String tableName,
            VisualizationConfig config,
            VisualizationItem item,
            List<TableMetadata> availableTables
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
                connection,
                dsl,
                tableName,
                config,
                item,
                dimensions,
                measures,
                values,
                availableTables
        );
    }

    // =========================================================================
    // TABLE
    // =========================================================================

    public TableData loadTable(
            Connection connection,
            String tableName,
            VisualizationConfig config,
            VisualizationItem item,
            List<TableMetadata> availableTables
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
                connection,
                dsl,
                tableName,
                config,
                item,
                availableTables
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
            Connection connection,
            DSLContext dsl,
            String tableName,
            VisualizationConfig config,
            VisualizationItem item,
            List<VisualizationParameter> dimensions,
            List<VisualizationParameter> measures,
            List<VisualizationParameter> values,
            List<TableMetadata> availableTables

    ) throws Exception {
        DashboardQueryPlan commonPlan = buildCommonPlan(tableName, config, item, availableTables);

        Select<?> query =
                new JooqDashboardQueryRenderer(
                        connection,
                        dsl
                ).render(
                        commonPlan
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
            Connection connection,
            DSLContext dsl,
            String tableName,
            VisualizationConfig config,
            VisualizationItem item,
            List<TableMetadata> availableTables

    ) throws Exception {
        DashboardQueryPlan commonPlan = buildCommonPlan(tableName, config, item, availableTables);

        Select<?> query =
                new JooqDashboardQueryRenderer(
                        connection,
                        dsl
                ).render(
                        commonPlan
                );

        System.out.println(
                "[DataProvider] Table SQL: " + query
        );

        Result<?> result =
                query.fetch();

        return convertTableResult(
                result,
                config
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

    private TableData convertTableResult(
            Result<?> result,
            VisualizationConfig config
    ) {
        List<String> columns = new ArrayList<>();
        List<List<Object>> rows = new ArrayList<>();

        Map<String, String> configuredHeaders = null;
        Object headersObj = config.getValue("columnsHeaders");

        if (headersObj instanceof Map<?, ?> map) {

            configuredHeaders = new HashMap<>();

            for (Map.Entry<?, ?> entry : map.entrySet()) {

                if (entry.getKey() instanceof String key
                        && entry.getValue() instanceof String value) {

                    if (key.contains(":")) {
                        key = key.substring(
                                key.lastIndexOf(":") + 1
                        );
                    }

                    if (key.contains(".")) {
                        key = key.substring(
                                key.lastIndexOf(".") + 1
                        );
                    }

                    configuredHeaders.put(
                            key,
                            value
                    );
                }
            }
        }

        for (Field<?> field : result.fields()) {

            String fieldName =
                    field.getName();

            if (configuredHeaders != null
                    && configuredHeaders.containsKey(fieldName)) {

                columns.add(
                        configuredHeaders.get(fieldName)
                );

            } else {

                columns.add(fieldName);
            }
        }

        for (Record record : result) {

            List<Object> row =
                    new ArrayList<>();

            for (Field<?> field : result.fields()) {

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

            Field<?> aggregated =
                    aggregateField(config, numericField)
                            .as(param.getKey());

            fields.add(
                    aggregated
            );

            selectFields.add(
                    aggregated
            );
        }

        return fields;
    }

    private Field<?> aggregateField(
            VisualizationConfig config,
            Field<BigDecimal> numericField
    ) {
        Object configuredAggregation = config.getValue("aggregation");
        String aggregation = configuredAggregation == null
                ? DEFAULT_AGGREGATION
                : configuredAggregation.toString().trim().toUpperCase();

        return switch (aggregation.replace('_', ' ')) {
                        case "NONE" -> numericField;
            case "COUNT" -> DSL.count(numericField);
            case "COUNT DISTINCT" -> DSL.countDistinct(numericField);
            case "AVG" -> DSL.avg(numericField);
            case "MIN" -> DSL.min(numericField);
            case "MAX" -> DSL.max(numericField);
            default -> DSL.sum(numericField);
        };
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
            List<Field<?>> measureFields,
            VisualizationConfig config,
            List<VisualizationParameter> dimensions,
            List<VisualizationParameter> measures,
            List<VisualizationParameter> values
    ) {

        SelectQuery<Record> query =
                dsl.selectQuery();

        query.addSelect(
                selectFields
        );

        query.addFrom(
                table
        );

        // =====================================================================
        // GROUP BY
        // =====================================================================

        if (!dimensionFields.isEmpty()
                && !measureFields.isEmpty()) {

            query.addGroupBy(
                    dimensionFields
            );
        }

        // =====================================================================
        // SORT
        // =====================================================================

        List<SortField<?>> sortFields =
                buildSortFields(
                        config,
                        dimensions,
                        measures,
                        values
                );

        if (!sortFields.isEmpty()) {

            query.addOrderBy(
                    sortFields
            );
        }

        // =====================================================================
        // LIMIT
        // =====================================================================

        Integer limit =
                getChartLimit(
                        config,
                        dimensions,
                        measures,
                        values
                );

        if (limit != null
                && limit > 0) {

            query.addLimit(limit);
        }

        return query;
    }

    // =========================================================================
    // CHART SORT
    // =========================================================================

    private List<SortField<?>> buildSortFields(
            VisualizationConfig config,
            List<VisualizationParameter> dimensions,
            List<VisualizationParameter> measures,
            List<VisualizationParameter> values
    ) {

        List<SortField<?>> sortFields =
                new ArrayList<>();

        for (VisualizationParameter parameter :
                dimensions) {

            SortField<?> sortField =
                    buildSortField(
                            config,
                            parameter
                    );

            if (sortField != null) {

                sortFields.add(sortField);
            }
        }

        for (VisualizationParameter parameter :
                measures) {

            SortField<?> sortField =
                    buildSortField(
                            config,
                            parameter
                    );

            if (sortField != null) {

                sortFields.add(sortField);
            }
        }

        for (VisualizationParameter parameter :
                values) {

            SortField<?> sortField =
                    buildSortField(
                            config,
                            parameter
                    );

            if (sortField != null) {

                sortFields.add(sortField);
            }
        }

        return sortFields;
    }

    private SortField<?> buildSortField(
            VisualizationConfig config,
            VisualizationParameter parameter
    ) {

        Object sortValue =
                config.getValue(
                        parameter.getKey() + ".sort"
                );

        if (sortValue == null) {
            return null;
        }

        String direction =
                sortValue
                        .toString()
                        .trim()
                        .toUpperCase();

        if (direction.isEmpty()
                || "NONE".equals(direction)) {

            return null;
        }

        String column =
                getColumnValue(
                        config,
                        parameter
                );

        if (column == null) {
            return null;
        }

        // =====================================================================
        // MEASURE
        // =====================================================================

        if (parameter.isMeasure()) {

            Field<BigDecimal> numericField =
                    DSL.field(
                            DSL.name(column),
                            BigDecimal.class
                    );

            Field<?> aggregated = aggregateField(config, numericField);

            if ("ASCENDING".equals(direction)
                    || "ASC".equals(direction)) {

                return aggregated.asc();
            }

            if ("DESCENDING".equals(direction)
                    || "DESC".equals(direction)) {

                return aggregated.desc();
            }

            return null;
        }

        // =====================================================================
        // DIMENSION / VALUE
        // =====================================================================

        Field<?> field =
                DSL.field(
                        DSL.name(column)
                );

        if ("ASCENDING".equals(direction)
                || "ASC".equals(direction)) {

            return field.asc();
        }

        if ("DESCENDING".equals(direction)
                || "DESC".equals(direction)) {

            return field.desc();
        }

        return null;
    }

    // =========================================================================
    // CHART LIMIT
    // =========================================================================

    private Integer getChartLimit(
            VisualizationConfig config,
            List<VisualizationParameter> dimensions,
            List<VisualizationParameter> measures,
            List<VisualizationParameter> values
    ) {

        Integer limit =
                getParameterLimit(
                        config,
                        dimensions
                );

        if (limit != null) {
            return limit;
        }

        limit =
                getParameterLimit(
                        config,
                        measures
                );

        if (limit != null) {
            return limit;
        }

        return getParameterLimit(
                config,
                values
        );
    }

    private Integer getParameterLimit(
            VisualizationConfig config,
            List<VisualizationParameter> parameters
    ) {

        for (VisualizationParameter parameter :
                parameters) {

            Object value =
                    config.getValue(
                            parameter.getKey() + ".limit"
                    );

            if (value == null) {
                continue;
            }

            Integer limit =
                    parseInteger(value);

            if (limit != null
                    && limit > 0) {

                return limit;
            }
        }

        return null;
    }

    private Integer parseInteger(
            Object value
    ) {

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

    private double toDouble(Object value) {

        if (value == null) {
            return 0.0;
        }

        if (value instanceof Number number) {
            return number.doubleValue();
        }

        if (value instanceof java.sql.Timestamp timestamp) {
            return timestamp.getTime();
        }

        if (value instanceof java.sql.Date date) {
            return date.getTime();
        }

        if (value instanceof java.util.Date date) {
            return date.getTime();
        }

        if (value instanceof java.time.LocalDateTime dateTime) {
            return dateTime
                    .atZone(java.time.ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli();
        }

        if (value instanceof java.time.LocalDate date) {
            return date
                    .atStartOfDay(java.time.ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli();
        }

        if (value instanceof java.time.Instant instant) {
            return instant.toEpochMilli();
        }

        try {
            return Double.parseDouble(
                    value.toString()
            );

        } catch (NumberFormatException e) {

            throw new IllegalArgumentException(
                    "Value is not supported: " + value,
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

    private DashboardQueryPlan buildCommonPlan(String tableName, VisualizationConfig config, VisualizationItem item, List<TableMetadata> availableTables) {
        DashboardVisualization visualization = DashboardVisualizationMapper.map(tableName, config, item);
        TableMetadata sourceTable = findTable(tableName, availableTables);

        if (sourceTable == null) {
            throw new IllegalArgumentException("Dashboard source table not found: " + tableName);
        }
        return DashboardQueryBuilder.build(visualization, sourceTable, availableTables);
    }

    private TableMetadata findTable(String tableName, List<TableMetadata> availableTables) {
        if (tableName == null || availableTables == null) {
            return null;
        }

        return availableTables.stream()
                .filter(table -> table != null
                                && table.getTableName() != null
                                && tableName.equalsIgnoreCase(
                                table.getTableName()
                        )
                )
                .findFirst()
                .orElse(null);
    }
}
package org.labs.genesis.forms.renderer.chart.provider;

import org.jooq.*;
import org.jooq.Record;
import org.jooq.impl.DSL;
import org.labs.genesis.forms.renderer.chart.ChartData;
import org.labs.genesis.forms.ui.visualization.model.VisualizationConfig;
import org.labs.genesis.forms.ui.visualization.model.VisualizationItem;
import org.labs.genesis.forms.ui.visualization.model.VisualizationParameter;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

public class DataProvider {

    public ChartData load(Connection connection, String tableName,
                          VisualizationConfig config, VisualizationItem item) throws Exception {
        validateInputs(connection, tableName, config, item);

        DSLContext dsl = DSL.using(connection, dialect(connection));

        List<VisualizationParameter> parameters = item.parameters.stream()
                .filter(VisualizationParameter::hasQueryRole)
                .toList();

        return execute(dsl, tableName, config,
                parameters.stream().filter(VisualizationParameter::isDimension).toList(),
                parameters.stream().filter(VisualizationParameter::isMeasure).toList(),
                parameters.stream().filter(VisualizationParameter::isValue).toList());
    }

    private void validateInputs(Connection connection, String tableName,
                                VisualizationConfig config, VisualizationItem item) {
        if (connection == null) throw new IllegalArgumentException("Database connection cannot be null");
        if (tableName == null || tableName.isBlank()) throw new IllegalArgumentException("Table name cannot be empty");
        if (config == null) throw new IllegalArgumentException("Visualization config cannot be null");
        if (item == null) throw new IllegalArgumentException("Visualization item cannot be null");
    }

    private ChartData execute(DSLContext dsl, String tableName, VisualizationConfig config,
                              List<VisualizationParameter> dimensions,
                              List<VisualizationParameter> measures,
                              List<VisualizationParameter> values) {
        Table<?> table = DSL.table(DSL.name(tableName));
        List<SelectField<?>> selectFields = new ArrayList<>();

        List<Field<?>> dimensionFields = buildDimensionFields(config, dimensions, selectFields);
        List<Field<?>> measureFields = buildMeasureFields(config, measures, selectFields);
        buildValueFields(config, values, selectFields);

        if (selectFields.isEmpty()) {
            throw new IllegalArgumentException("No visualization value has been configured");
        }

        Select<?> query = buildQuery(dsl, table, selectFields, dimensionFields, measureFields);
        Result<?> result = query.fetch();

        return convertResult(result, dimensions, measures, values);
    }

    private List<Field<?>> buildDimensionFields(VisualizationConfig config,
                                                List<VisualizationParameter> dimensions,
                                                List<SelectField<?>> selectFields) {
        List<Field<?>> fields = new ArrayList<>();
        for (VisualizationParameter param : dimensions) {
            String column = getColumnValue(config, param);
            if (column != null) {
                Field<?> field = DSL.field(DSL.name(column));
                fields.add(field);
                selectFields.add(field);
            }
        }
        return fields;
    }

    private List<Field<?>> buildMeasureFields(VisualizationConfig config,
                                              List<VisualizationParameter> measures,
                                              List<SelectField<?>> selectFields) {
        List<Field<?>> fields = new ArrayList<>();
        for (VisualizationParameter param : measures) {
            String column = getColumnValue(config, param);
            if (column != null) {
                Field<? extends BigDecimal> numericField = DSL.field(DSL.name(column), BigDecimal.class);
                Field<?> aggregated = DSL.sum(numericField).as(param.getKey());
                fields.add(aggregated);
                selectFields.add(aggregated);
            }
        }
        return fields;
    }

    private void buildValueFields(VisualizationConfig config,
                                  List<VisualizationParameter> values,
                                  List<SelectField<?>> selectFields) {
        for (VisualizationParameter param : values) {
            String column = getColumnValue(config, param);
            if (column != null) {
                selectFields.add(DSL.field(DSL.name(column)));
            }
        }
    }

    private Select<?> buildQuery(DSLContext dsl, Table<?> table, List<SelectField<?>> selectFields,
                                 List<Field<?>> dimensionFields, List<Field<?>> measureFields) {
        SelectJoinStep<Record> from = dsl.select(selectFields).from(table);

        if (!dimensionFields.isEmpty() && !measureFields.isEmpty()) {
            return from.groupBy(dimensionFields);
        }
        return from;
    }

    private ChartData convertResult(Result<?> result,
                                    List<VisualizationParameter> dimensions,
                                    List<VisualizationParameter> measures,
                                    List<VisualizationParameter> values) {
        List<String> labels = new ArrayList<>();
        List<Double> chartValues = new ArrayList<>();
        List<double[]> points = new ArrayList<>();

        if (!dimensions.isEmpty() && !measures.isEmpty()) {
            Field<?> dimensionField = result.field(0);
            Field<?> measureField = result.field(dimensions.size());

            if (dimensionField == null || measureField == null) {
                throw new IllegalStateException("Required fields missing from query result");
            }

            for (Record record : result) {
                labels.add(String.valueOf(record.get(dimensionField)));
                chartValues.add(toDouble(record.get(measureField)));
            }
        } else if (values.size() >= 2) {
            Field<?> xField = result.field(0);
            Field<?> yField = result.field(1);

            if (xField == null || yField == null) {
                throw new IllegalStateException("Scatter plot requires at least two fields");
            }

            for (Record record : result) {
                points.add(new double[]{toDouble(record.get(xField)), toDouble(record.get(yField))});
            }
        } else if (!measures.isEmpty()) {
            Field<?> measureField = result.field(0);
            if (measureField == null) {
                throw new IllegalStateException("Measure field is missing from query result");
            }

            for (Record record : result) {
                chartValues.add(toDouble(record.get(measureField)));
            }
        }

        return new ChartData(labels, chartValues.stream().mapToDouble(Double::doubleValue).toArray(),
                points.toArray(new double[0][]));
    }

    private String getColumnValue(VisualizationConfig config, VisualizationParameter parameter) {
        Object value = config.getValue(parameter.getKey());
        if (value == null) return null;

        String stringValue = value.toString().trim();
        return stringValue.isEmpty() ? null : extractColumnName(stringValue);
    }

    private String extractColumnName(String value) {
        if (value.toUpperCase().startsWith("COLUMN:")) {
            value = value.substring(7).trim();
        }

        int lastDotIndex = value.lastIndexOf('.');
        if (lastDotIndex >= 0 && lastDotIndex < value.length() - 1) {
            return value.substring(lastDotIndex + 1);
        }
        return value;
    }

    private double toDouble(Object value) {
        if (value == null) return 0.0;
        if (value instanceof Number) return ((Number) value).doubleValue();

        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Value is not numeric: " + value, e);
        }
    }

    private SQLDialect dialect(Connection connection) {
        try {
            String product = connection.getMetaData().getDatabaseProductName().toLowerCase();

            if (product.contains("postgres")) return SQLDialect.POSTGRES;
            if (product.contains("mysql")) return SQLDialect.MYSQL;
            if (product.contains("mariadb")) return SQLDialect.MARIADB;
            if (product.contains("sqlite")) return SQLDialect.SQLITE;
            if (product.contains("oracle") || product.contains("sql server") || product.contains("microsoft")) {
                return SQLDialect.DEFAULT;
            }
            return SQLDialect.DEFAULT;
        } catch (Exception e) {
            return SQLDialect.DEFAULT;
        }
    }

    public static String extractTableNameStatic(String value) {
        if (value == null) return null;

        value = value.trim();
        if (value.toUpperCase().startsWith("COLUMN:")) {
            value = value.substring(7).trim();
        }

        int lastDotIndex = value.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < value.length() - 1) {
            return value.substring(0, lastDotIndex);
        }
        return null;
    }
}
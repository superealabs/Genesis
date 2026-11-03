package org.labs.genesis.dashboard.query;

import org.labs.genesis.connexion.model.ColumnMetadata;
import org.labs.genesis.dashboard.model.*;
import org.labs.genesis.dashboard.rules.DashboardVisualizationRules;
import org.labs.genesis.dashboard.rules.DashboardMetadataValidator;
import org.labs.genesis.connexion.model.TableMetadata;
import org.labs.genesis.dashboard.rules.DashboardStatisticResolver;

import java.util.List;

public final class DashboardQueryBuilder {
    private DashboardQueryBuilder() {
    }

    public static DashboardQueryPlan build(DashboardVisualization visualization) {
        validate(visualization);
        return createPlan(visualization);
    }

    public static DashboardQueryPlan build(DashboardVisualization visualization, TableMetadata sourceTable, List<TableMetadata> availableTables) {
        validate(visualization);
        if (!DashboardMetadataValidator.isValid(visualization, sourceTable, availableTables)) {
            throw new IllegalArgumentException("Invalid dashboard metadata");
        }
        DashboardQueryPlan plan = createPlan(visualization);
        normalizeFilterValues(plan, sourceTable, availableTables);
        plan.getJoins().addAll(DashboardJoinResolver.resolve(visualization, sourceTable, availableTables));
        return plan;
    }

    private static DashboardQueryPlan createPlan(DashboardVisualization visualization) {
        DashboardQueryPlan plan = new DashboardQueryPlan();
        plan.setSource(visualization.getDataSource());
        plan.setOptions(visualization.getQueryOptions());
        for (DashboardField field : visualization.getFields()) {
            plan.getSelections().add(createSelection(field));
        }
        buildGroupBy(plan);

        return plan;
    }

    private static DashboardQuerySelection createSelection(DashboardField field) {
        return new DashboardQuerySelection(
                field.getKey(),
                field.getColumnName(),
                field.getKey(),
                field.getRole(),
                DashboardStatisticResolver.resolve(field)
        );
    }

    private static void buildGroupBy(DashboardQueryPlan plan) {
        boolean hasAggregation =
                plan.getSelections()
                        .stream()
                        .anyMatch(DashboardQuerySelection::isAggregated);

        if (!hasAggregation) {
            return;
        }

        List<String> groupBy = plan.getSelections()
                        .stream()
                        .filter(selection -> !selection.isAggregated())
                        .map(DashboardQuerySelection::getColumnName)
                        .distinct()
                        .toList();

        plan.getGroupBy().addAll(groupBy);
    }

    private static void normalizeFilterValues(DashboardQueryPlan plan, TableMetadata sourceTable, List<TableMetadata> availableTables) {
        if (plan == null || plan.getOptions() == null || plan.getOptions().getFilters() == null) {
            return;
        }
        for (DashboardFilterNode node : plan.getOptions().getFilters()) {
            normalizeFilterNode(node, sourceTable, availableTables);
        }
    }

    private static void normalizeFilterNode(DashboardFilterNode node, TableMetadata sourceTable, List<TableMetadata> availableTables) {
        if (node instanceof DashboardFilter filter) {
            normalizeFilterValue(filter, sourceTable, availableTables);
            return;
        }

        if (node instanceof DashboardFilterGroup group && group.getChildren() != null) {
            for (DashboardFilterNode child : group.getChildren()) {
                normalizeFilterNode(child, sourceTable, availableTables);
            }
        }
    }

    private static void normalizeFilterValue(DashboardFilter filter, TableMetadata sourceTable, List<TableMetadata> availableTables) {
        if (filter == null || filter.getColumnName() == null || filter.getValue() == null) {
            return;
        }
        ColumnMetadata column = findFilterColumn(filter.getColumnName(), sourceTable, availableTables);
        if (column == null) {
            return;
        }
        if (isBooleanColumn(column)) {
            Boolean booleanValue = parseBooleanValue(filter.getValue());
            if (booleanValue != null) {
                filter.setValue(booleanValue);
            }
        }
    }

    private static ColumnMetadata findFilterColumn(String reference, TableMetadata sourceTable, List<TableMetadata> availableTables) {
        String tableName = null;
        String columnName = reference;

        int dot = reference.indexOf('.');

        if (dot >= 0) {
            tableName = reference.substring(0, dot);
            columnName = reference.substring(dot + 1);
        }

        if (tableName == null || tableName.equalsIgnoreCase(sourceTable.getTableName())) {
            ColumnMetadata column = findColumn(sourceTable, columnName);

            if (column != null) {
                return column;
            }
        }

        if (availableTables == null) {
            return null;
        }

        for (TableMetadata table : availableTables) {

            if (table == null) {
                continue;
            }

            if (tableName != null && !tableName.equalsIgnoreCase(table.getTableName())) {
                continue;
            }

            ColumnMetadata column = findColumn(table, columnName);

            if (column != null) {
                return column;
            }
        }

        return null;
    }

    private static ColumnMetadata findColumn(TableMetadata table, String columnName) {
        if (table == null || table.getColumns() == null) {
            return null;
        }

        for (ColumnMetadata column : table.getColumns()) {
            if (column != null && column.getName() != null && column.getName().equalsIgnoreCase(columnName)) {
                return column;
            }
        }

        return null;
    }

    private static boolean isBooleanColumn(ColumnMetadata column) {
        if (column == null) {
            return false;
        }
        if (column.getType() != null && column.getType().equalsIgnoreCase("Boolean")) {
            return true;
        }
        String columnType = column.getColumnType();
        if (columnType == null) {
            return false;
        }
        String type = columnType.trim().toLowerCase();
        return type.equals("boolean")
                || type.equals("bool")
                || type.equals("bit");
    }

    private static Boolean parseBooleanValue(Object value) {
        if (value instanceof Boolean booleanValue) {
            return booleanValue;
        }
        String text = value.toString().trim().toLowerCase();
        return switch (text) {
            case "true", "1", "yes", "y", "on" -> Boolean.TRUE;
            case "false", "0", "no", "n", "off" -> Boolean.FALSE;
            default -> null;
        };
    }

    private static void validate(DashboardVisualization visualization) {
        if (!DashboardVisualizationRules.isValid(visualization)) {
            throw new IllegalArgumentException("Invalid dashboard visualization");
        }
    }

    public static DashboardQueryPlan build(DashboardVisualization visualization, TableMetadata table) {
        validate(visualization);

        if (!DashboardMetadataValidator.isValid(visualization, table)) {
            throw new IllegalArgumentException("Invalid dashboard metadata");
        }

        return createPlan(visualization);
    }

    public static DashboardQueryPlan buildRowCount(DashboardDataSource source, String alias) {
        if (source == null) {
            throw new IllegalArgumentException("Dashboard data source is required");
        }

        DashboardQueryPlan plan = new DashboardQueryPlan();
        plan.setSource(source);
        plan.setRowStatistic(DashboardStatisticResolver.countRows(alias));

        return plan;
    }
}
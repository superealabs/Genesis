package org.labs.genesis.dashboard.query;

import org.labs.genesis.dashboard.model.DashboardField;
import org.labs.genesis.dashboard.model.DashboardVisualization;
import org.labs.genesis.dashboard.model.DashboardEnums.DashboardCoordinateMode;
import org.labs.genesis.dashboard.model.DashboardEnums.DashboardFieldRole;
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

    public static DashboardQueryPlan build(DashboardVisualization visualization, TableMetadata sourceTable, List<TableMetadata> availableTables) {
        validate(visualization);
        if (!DashboardMetadataValidator.isValid(visualization, sourceTable, availableTables)) {
            throw new IllegalArgumentException("Invalid dashboard metadata");
        }
        DashboardQueryPlan plan = createPlan(visualization, sourceTable, availableTables);
        if (sourceTable.getDatabase() != null) {
            plan.setDatabaseName(sourceTable.getDatabase().getName());
        }
        normalizeFilterValues(plan, sourceTable, availableTables);
        plan.getJoins().addAll(DashboardJoinResolver.resolve(visualization, sourceTable, availableTables));
        return plan;
    }

    private static DashboardQueryPlan createPlan(
            DashboardVisualization visualization,
            TableMetadata sourceTable,
            List<TableMetadata> availableTables
    ) {
        DashboardQueryPlan plan = new DashboardQueryPlan();
        plan.setSource(visualization.getDataSource());
        plan.setOptions(copyQueryOptions(visualization.getQueryOptions()));
        for (DashboardField field : visualization.getFields()) {
            if (visualization.getType() == org.labs.genesis.dashboard.model.DashboardEnums.DashboardVisualizationType.MAP
                    && visualization.getCoordinateMode() == DashboardCoordinateMode.GEOGRAPHY
                    && field.getRole() == DashboardFieldRole.GEOGRAPHY) {
                plan.getSelections().add(createCoordinateSelection(
                        field,
                        DashboardQuerySelection.CoordinateAxis.LATITUDE,
                        sourceTable,
                        availableTables
                ));
                plan.getSelections().add(createCoordinateSelection(
                        field,
                        DashboardQuerySelection.CoordinateAxis.LONGITUDE,
                        sourceTable,
                        availableTables
                ));
            } else {
                plan.getSelections().add(createSelection(field, sourceTable, availableTables));
            }
        }
        buildGroupBy(plan);

        return plan;
    }

    private static DashboardQuerySelection createCoordinateSelection(
            DashboardField field,
            DashboardQuerySelection.CoordinateAxis axis,
            TableMetadata sourceTable,
            List<TableMetadata> availableTables
    ) {
        DashboardQuerySelection selection = createSelection(field, sourceTable, availableTables);
        selection.setAlias(axis == DashboardQuerySelection.CoordinateAxis.LATITUDE ? "latitude" : "longitude");
        selection.setCoordinateAxis(axis);
        return selection;
    }

    private static DashboardQuerySelection createSelection(
            DashboardField field,
            TableMetadata sourceTable,
            List<TableMetadata> availableTables
    ) {
        DashboardQuerySelection selection = new DashboardQuerySelection(
                field.getKey(),
                field.getColumnName(),
                field.getKey(),
                field.getRole(),
                DashboardStatisticResolver.resolve(field)
        );
        selection.setNativeColumnType(nativeColumnType(sourceTable, availableTables, field.getColumnName()));
        return selection;
    }

    private static String nativeColumnType(
            TableMetadata sourceTable,
            List<TableMetadata> availableTables,
            String columnName
    ) {
        if (sourceTable == null || columnName == null) {
            return null;
        }
        String reference = columnName.trim();
        if (reference.regionMatches(true, 0, "COLUMN:", 0, "COLUMN:".length())) {
            reference = reference.substring("COLUMN:".length()).trim();
        }

        TableMetadata table = sourceTable;
        int dot = reference.lastIndexOf('.');
        if (dot >= 0) {
            String tableReference = normalizeIdentifier(reference.substring(0, dot));
            reference = reference.substring(dot + 1);
            table = availableTables == null ? null : availableTables.stream()
                    .filter(candidate -> candidate != null && candidate.getTableName() != null)
                    .filter(candidate -> matchesTableReference(tableReference, candidate.getTableName()))
                    .findFirst()
                    .orElse(null);
        }
        if (table == null || table.getColumns() == null) {
            return null;
        }

        String simpleName = normalizeIdentifier(reference);
        return java.util.Arrays.stream(table.getColumns())
                .filter(column -> column != null)
                .filter(column -> simpleName.equalsIgnoreCase(column.getReferencedColumn())
                        || simpleName.equalsIgnoreCase(column.getName()))
                .map(org.labs.genesis.connexion.model.ColumnMetadata::getColumnType)
                .filter(type -> type != null)
                .findFirst()
                .orElse(null);
    }

    private static boolean matchesTableReference(String reference, String tableName) {
        String normalizedReference = normalizeIdentifier(reference);
        String normalizedTable = normalizeIdentifier(tableName);
        return normalizedReference.equalsIgnoreCase(normalizedTable)
                || normalizedReference.toLowerCase(java.util.Locale.ROOT)
                .endsWith("." + normalizedTable.toLowerCase(java.util.Locale.ROOT))
                || normalizedTable.toLowerCase(java.util.Locale.ROOT)
                .endsWith("." + normalizedReference.toLowerCase(java.util.Locale.ROOT))
                || lastIdentifier(normalizedReference).equalsIgnoreCase(lastIdentifier(normalizedTable));
    }

    private static String normalizeIdentifier(String value) {
        return value.replace("`", "").replace("\"", "").replace("[", "").replace("]", "").trim();
    }

    private static String lastIdentifier(String value) {
        int dot = value.lastIndexOf('.');
        return dot < 0 ? value : value.substring(dot + 1);
    }

    private static DashboardQueryOptions copyQueryOptions(DashboardQueryOptions options) {
        if (options == null) {
            return null;
        }

        DashboardQueryOptions copy = new DashboardQueryOptions();
        copy.setLimit(options.getLimit());
        if (options.getSorts() != null) {
            List<DashboardSort> sorts = new java.util.ArrayList<>();
            for (DashboardSort sort : options.getSorts()) {
                sorts.add(sort == null ? null : new DashboardSort(sort.getColumnName(), sort.getDirection()));
            }
            copy.setSorts(sorts);
        }
        if (options.getFilters() != null) {
            List<DashboardFilterNode> filters = new java.util.ArrayList<>();
            for (DashboardFilterNode filter : options.getFilters()) {
                filters.add(copyFilterNode(filter));
            }
            copy.setFilters(filters);
        }
        return copy;
    }

    private static DashboardFilterNode copyFilterNode(DashboardFilterNode node) {
        if (node instanceof DashboardFilter filter) {
            return new DashboardFilter(
                    filter.getColumnName(),
                    filter.getOperator(),
                    filter.getValue(),
                    filter.getRelationToPrevious()
            );
        }
        if (node instanceof DashboardFilterGroup group) {
            DashboardFilterGroup copy = new DashboardFilterGroup();
            copy.setOperator(group.getOperator());
            copy.setRelationToPrevious(group.getRelationToPrevious());
            if (group.getChildren() != null) {
                List<DashboardFilterNode> children = new java.util.ArrayList<>();
                for (DashboardFilterNode child : group.getChildren()) {
                    children.add(copyFilterNode(child));
                }
                copy.setChildren(children);
            }
            return copy;
        }
        return node;
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

        return createPlan(visualization, table, List.of(table));
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
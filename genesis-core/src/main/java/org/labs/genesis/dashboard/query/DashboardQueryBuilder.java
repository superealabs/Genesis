package org.labs.genesis.dashboard.query;

import org.labs.genesis.dashboard.model.DashboardField;
import org.labs.genesis.dashboard.model.DashboardVisualization;
import org.labs.genesis.dashboard.model.DashboardEnums.DashboardCoordinateMode;
import org.labs.genesis.dashboard.model.DashboardEnums.DashboardFieldRole;
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
        DashboardQueryPlan plan = createPlan(visualization, sourceTable);
        if (sourceTable.getDatabase() != null) {
            plan.setDatabaseName(sourceTable.getDatabase().getName());
        }
        plan.getJoins().addAll(DashboardJoinResolver.resolve(visualization, sourceTable, availableTables));
        return plan;
    }

    private static DashboardQueryPlan createPlan(DashboardVisualization visualization, TableMetadata sourceTable) {
        DashboardQueryPlan plan = new DashboardQueryPlan();
        plan.setSource(visualization.getDataSource());
        plan.setOptions(visualization.getQueryOptions());
        for (DashboardField field : visualization.getFields()) {
            if (visualization.getType() == org.labs.genesis.dashboard.model.DashboardEnums.DashboardVisualizationType.MAP
                    && visualization.getCoordinateMode() == DashboardCoordinateMode.GEOGRAPHY
                    && field.getRole() == DashboardFieldRole.GEOGRAPHY) {
                plan.getSelections().add(createCoordinateSelection(field, DashboardQuerySelection.CoordinateAxis.LATITUDE, sourceTable));
                plan.getSelections().add(createCoordinateSelection(field, DashboardQuerySelection.CoordinateAxis.LONGITUDE, sourceTable));
            } else {
                plan.getSelections().add(createSelection(field, sourceTable));
            }
        }
        buildGroupBy(plan);

        return plan;
    }

    private static DashboardQuerySelection createCoordinateSelection(
            DashboardField field, DashboardQuerySelection.CoordinateAxis axis, TableMetadata sourceTable) {
        DashboardQuerySelection selection = createSelection(field, sourceTable);
        selection.setAlias(axis == DashboardQuerySelection.CoordinateAxis.LATITUDE ? "latitude" : "longitude");
        selection.setCoordinateAxis(axis);
        return selection;
    }

    private static DashboardQuerySelection createSelection(DashboardField field, TableMetadata sourceTable) {
        DashboardQuerySelection selection = new DashboardQuerySelection(
                field.getKey(),
                field.getColumnName(),
                field.getKey(),
                field.getRole(),
                DashboardStatisticResolver.resolve(field)
        );
        selection.setNativeColumnType(nativeColumnType(sourceTable, field.getColumnName()));
        return selection;
    }

    private static String nativeColumnType(TableMetadata table, String columnName) {
        if (table == null || table.getColumns() == null || columnName == null) {
            return null;
        }
        String simpleName = columnName.substring(Math.max(columnName.lastIndexOf('.') + 1, 0));
        return java.util.Arrays.stream(table.getColumns())
                .filter(column -> column != null)
                .filter(column -> simpleName.equalsIgnoreCase(column.getReferencedColumn())
                        || simpleName.equalsIgnoreCase(column.getName()))
                .map(org.labs.genesis.connexion.model.ColumnMetadata::getColumnType)
                .findFirst()
                .orElse(null);
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

    private static void validate(DashboardVisualization visualization) {
        if (!DashboardVisualizationRules.isValid(visualization)) {
            throw new IllegalArgumentException("Invalid dashboard visualization");
        }
    }

}

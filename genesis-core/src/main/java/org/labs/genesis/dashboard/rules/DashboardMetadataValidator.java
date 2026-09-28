package org.labs.genesis.dashboard.rules;

import org.labs.genesis.connexion.model.ColumnMetadata;
import org.labs.genesis.connexion.model.TableMetadata;
import org.labs.genesis.dashboard.model.DashboardField;
import org.labs.genesis.dashboard.model.DashboardVisualization;

import java.util.Arrays;

public final class DashboardMetadataValidator {

    private DashboardMetadataValidator() {
    }

    public static boolean isValid(DashboardVisualization visualization, TableMetadata table) {
        if (visualization == null || table == null || table.getColumns() == null) {
            return false;
        }

        for (DashboardField field : visualization.getFields()) {
            ColumnMetadata column = findColumn(table, field.getColumnName());

            if (column == null) {
                return false;
            }

            if (field.getStatistic() != null && !DashboardStatisticRules.supports(column, field.getStatistic())) {
                return false;
            }
        }

        return true;
    }

    private static ColumnMetadata findColumn(TableMetadata table, String columnName) {
        if (columnName == null) {
            return null;
        }

        return Arrays.stream(table.getColumns())
                .filter(column ->
                        columnName.equalsIgnoreCase(column.getReferencedColumn()) || columnName.equalsIgnoreCase(column.getName()))
                .findFirst()
                .orElse(null);
    }
}
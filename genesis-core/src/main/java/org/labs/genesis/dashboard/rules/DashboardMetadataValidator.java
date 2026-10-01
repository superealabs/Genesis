package org.labs.genesis.dashboard.rules;

import org.labs.genesis.connexion.model.ColumnMetadata;
import org.labs.genesis.connexion.model.TableMetadata;
import org.labs.genesis.dashboard.model.*;

import java.util.Arrays;
import java.util.List;

public final class DashboardMetadataValidator {

    private DashboardMetadataValidator() {
    }

    public static boolean isValid(DashboardVisualization visualization, TableMetadata table) {
        return isValid(visualization, table, table == null ? List.of() : List.of(table));
    }

    public static boolean isValid(DashboardVisualization visualization, TableMetadata sourceTable,
                                  List<TableMetadata> availableTables) {
        if (visualization == null || sourceTable == null || sourceTable.getColumns() == null
                || visualization.getFields() == null) {
            return false;
        }

        for (DashboardField field : visualization.getFields()) {
            if (field == null) return false;
            ColumnMetadata column = findVisualizationColumn(sourceTable, visualization, field.getColumnName(), availableTables);

            if (column == null) {
                return false;
            }

            if (field.getStatistic() != null && !DashboardStatisticRules.supports(column, field.getStatistic())) {
                return false;
            }
        }

        if (visualization.getQueryOptions() != null) {
            List<DashboardSort> sorts = visualization.getQueryOptions().getSorts();
            if (sorts != null) for (DashboardSort sort : sorts)
                if (sort != null && findVisualizationColumn(sourceTable, visualization, sort.getColumnName(), availableTables) == null) return false;
            List<DashboardFilterNode> filters = visualization.getQueryOptions().getFilters();
            if (filters != null) {
                for (DashboardFilterNode filter : filters) {
                    if (isInvalidFilterNode(filter, sourceTable, visualization, availableTables)) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    private static boolean isInvalidFilterNode(DashboardFilterNode node, TableMetadata sourceTable, DashboardVisualization visualization, List<TableMetadata> availableTables) {
        if (node == null) {
            return true;
        }
        if (node instanceof DashboardFilter filter) {
            return findVisualizationColumn(sourceTable, visualization, filter.getColumnName(), availableTables) == null;
        }

        if (node instanceof DashboardFilterGroup group) {
            if (group.getChildren() == null) {
                return true;
            }
            for (DashboardFilterNode child : group.getChildren()) {
                if (isInvalidFilterNode(child, sourceTable, visualization, availableTables)) {
                    return true;
                }
            }
            return false;
        }
        return true;
    }

    private static ColumnMetadata findVisualizationColumn(TableMetadata sourceTable, DashboardVisualization visualization,
                                                            String columnName, List<TableMetadata> availableTables) {
        if (columnName == null) return null;
        DashboardField selected = visualization.getFields().stream()
                .filter(field -> field != null && columnName.equalsIgnoreCase(field.getKey())).findFirst().orElse(null);
        String name = selected == null ? columnName : selected.getColumnName();
        if (name == null || name.isBlank()) return null;
        String trimmed = name.trim();
        int open = trimmed.indexOf('(');
        if (open >= 0 && trimmed.endsWith(")")) trimmed = trimmed.substring(open + 1, trimmed.length() - 1).trim();
        trimmed = trimmed.replace("COLUMN:", "").replace("`", "").replace("\"", "").replace("[", "").replace("]", "");
        int dot = trimmed.lastIndexOf('.');
        TableMetadata table = sourceTable;
        if (dot >= 0) {
            String tableName = trimmed.substring(0, dot).trim();
            trimmed = trimmed.substring(dot + 1).trim();
            List<TableMetadata> tables = availableTables == null ? List.of() : availableTables;
            table = tables.stream().filter(candidate -> candidate != null && candidate.getTableName() != null)
                    .filter(candidate -> tableName.equalsIgnoreCase(candidate.getTableName())
                            || tableName.toLowerCase(java.util.Locale.ROOT).endsWith("." + candidate.getTableName().toLowerCase(java.util.Locale.ROOT)))
                    .findFirst().orElse(null);
        }
        return table == null ? null : findColumn(table, trimmed);
    }

    private static ColumnMetadata findColumn(TableMetadata table, String columnName) {
        if (columnName == null) {
            return null;
        }

        return Arrays.stream(table.getColumns())
                .filter(column -> column != null)
                .filter(column ->
                        columnName.equalsIgnoreCase(column.getReferencedColumn()) || columnName.equalsIgnoreCase(column.getName()))
                .findFirst()
                .orElse(null);
    }
}

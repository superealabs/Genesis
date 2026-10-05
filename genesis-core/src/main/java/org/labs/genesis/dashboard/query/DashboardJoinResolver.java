package org.labs.genesis.dashboard.query;

import org.labs.genesis.connexion.model.ColumnMetadata;
import org.labs.genesis.connexion.model.TableMetadata;
import org.labs.genesis.dashboard.model.DashboardFilter;
import org.labs.genesis.dashboard.model.DashboardFilterGroup;
import org.labs.genesis.dashboard.model.DashboardFilterNode;
import org.labs.genesis.dashboard.model.DashboardSort;
import org.labs.genesis.dashboard.model.DashboardVisualization;
import org.labs.genesis.dashboard.model.DashboardEnums.DashboardJoinType;

import java.util.*;

public final class DashboardJoinResolver {

    private DashboardJoinResolver() {
    }

    public static List<DashboardJoin> resolve(DashboardVisualization visualization, TableMetadata sourceTable, List<TableMetadata> availableTables) {
        if (visualization == null || sourceTable == null || availableTables == null) {
            return List.of();
        }
        Set<String> requiredTables = collectRequiredTables(visualization, sourceTable.getTableName());
        requiredTables.removeIf(
                table -> table.equalsIgnoreCase(sourceTable.getTableName())
        );
        List<DashboardJoin> joins = new ArrayList<>();
        Set<String> alreadyJoined = new HashSet<>();
        alreadyJoined.add(sourceTable.getTableName().toLowerCase(Locale.ROOT));
        for (String target : requiredTables) {
            List<RelationEdge> path = findPath(sourceTable.getTableName(), target, availableTables);
            for (RelationEdge edge : path) {
                String leftKey = edge.leftTable().toLowerCase(Locale.ROOT);
                String rightKey = edge.rightTable().toLowerCase(Locale.ROOT);
                if (alreadyJoined.contains(leftKey) && alreadyJoined.contains(rightKey)) {
                    continue;
                }
                String tableToJoin = alreadyJoined.contains(leftKey) ? edge.rightTable() : edge.leftTable();
                DashboardJoin join = new DashboardJoin();
                join.setTable(tableToJoin);
                join.setType(DashboardJoinType.LEFT);
                join.getConditions().add(new DashboardJoinCondition(edge.leftTable(), edge.leftColumn(), edge.rightTable(), edge.rightColumn()));
                joins.add(join);
                alreadyJoined.add(tableToJoin.toLowerCase(Locale.ROOT));
            }
        }

        return joins;
    }

    private static Set<String> collectRequiredTables(DashboardVisualization visualization, String defaultTable) {
        Set<String> tables = new LinkedHashSet<>();
        visualization.getFields()
                .forEach(field ->
                        addTableFromReference(field.getColumnName(), defaultTable, tables)
                );
        if (visualization.getQueryOptions() != null) {
            for (DashboardSort sort : visualization.getQueryOptions().getSorts()) {
                addTableFromReference(sort.getColumnName(), defaultTable, tables);
            }
            for (DashboardFilterNode filter : visualization.getQueryOptions().getFilters()) {
                collectFilterTables(filter, defaultTable, tables);
            }
        }

        return tables;
    }

    private static void collectFilterTables(DashboardFilterNode node, String defaultTable, Set<String> tables
    ) {
        if (node instanceof DashboardFilter filter) {
            addTableFromReference(filter.getColumnName(), defaultTable, tables);
            return;
        }

        if (node instanceof DashboardFilterGroup group) {
            for (DashboardFilterNode child : group.getChildren()) {
                collectFilterTables(child, defaultTable, tables);
            }
        }
    }

    private static void addTableFromReference(String value, String defaultTable, Set<String> tables) {
        if (value == null || value.isBlank()) {
            return;
        }
        String normalized = stripAggregation(value);
        int dot = normalized.indexOf('.');
        if (dot > 0) {
            tables.add(normalized.substring(0, dot));
        } else {
            tables.add(defaultTable);
        }
    }

    private static String stripAggregation(String value) {
        String result = value.trim();
        if (result.matches("(?i)^(SUM|AVG|COUNT|COUNT_DISTINCT|MIN|MAX)\\s*\\(.*\\)$")) {
            int open = result.indexOf('(');
            int close = result.lastIndexOf(')');
            if (open >= 0 && close > open) {
                return result.substring(open + 1, close).trim();
            }
        }

        return result;
    }

    private static List<RelationEdge> buildGraphEdges(List<TableMetadata> tables) {
        List<RelationEdge> edges = new ArrayList<>();
        for (TableMetadata table : tables) {
            if (table == null || table.getColumns() == null) {
                continue;
            }
            for (ColumnMetadata column : table.getColumns()) {
                if (column == null
                        || !column.isForeign()
                        || column.getReferencedTable() == null
                        || column.getReferencedPrimaryKeyColumn() == null
                        || column.getReferencedColumn() == null) {

                    continue;
                }
                edges.add(
                        new RelationEdge(
                                table.getTableName(),
                                column.getReferencedColumn(),
                                column.getReferencedTable(),
                                column.getReferencedPrimaryKeyColumn()
                        )
                );
            }
        }
        return edges;
    }

    private static List<RelationEdge> findPath(String source, String target, List<TableMetadata> tables) {
        String sourceKey = source.toLowerCase(Locale.ROOT);
        String targetKey = target.toLowerCase(Locale.ROOT);
        List<RelationEdge> edges = buildGraphEdges(tables);
        Map<String, List<RelationEdge>> graph = new HashMap<>();
        for (RelationEdge edge : edges) {
            graph.computeIfAbsent(edge.leftTable().toLowerCase(Locale.ROOT), key -> new ArrayList<>()).add(edge);
            graph.computeIfAbsent(edge.rightTable().toLowerCase(Locale.ROOT), key -> new ArrayList<>()).add(edge);
        }
        Deque<String> queue = new ArrayDeque<>();
        Set<String> visited = new HashSet<>();
        Map<String, String> previousTable = new HashMap<>();
        Map<String, RelationEdge> previousEdge = new HashMap<>();
        queue.add(sourceKey);
        visited.add(sourceKey);
        while (!queue.isEmpty()) {
            String current = queue.removeFirst();
            if (current.equals(targetKey)) {
                break;
            }
            for (RelationEdge edge : graph.getOrDefault(current, List.of())) {
                String left = edge.leftTable().toLowerCase(Locale.ROOT);
                String right = edge.rightTable().toLowerCase(Locale.ROOT);
                String next = left.equals(current) ? right : left;
                if (visited.add(next)) {
                    previousTable.put(next, current);
                    previousEdge.put(next, edge);
                    queue.addLast(next);
                }
            }
        }

        if (!visited.contains(targetKey)) {
            throw new IllegalArgumentException("No relationship path found between " + source + " and " + target);
        }
        List<RelationEdge> path = new ArrayList<>();
        String current = targetKey;
        while (!current.equals(sourceKey)) {
            RelationEdge edge = previousEdge.get(current);
            path.add(0, edge);
            current = previousTable.get(current);
        }

        return path;
    }

    private record RelationEdge(
            String leftTable,
            String leftColumn,
            String rightTable,
            String rightColumn
    ) {
    }
}
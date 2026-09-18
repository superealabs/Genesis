package org.labs.genesis.forms.renderer.provider;

import org.jooq.*;
import org.jooq.impl.DSL;
import org.labs.genesis.forms.ui.visualization.model.*;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.*;

/** Converts visualization intent into a database-independent jOOQ query plan. */
public final class QueryPlanner {
    private final Connection connection;
    private final FilterConditionBuilder filterBuilder;

    public QueryPlanner(Connection connection, DSLContext dsl) {
        this.connection    = Objects.requireNonNull(connection);
        this.filterBuilder = new FilterConditionBuilder(connection);
    }

    public QueryPlan planMap(String tableName, VisualizationConfig config) throws Exception {

        QueryPlan plan = new QueryPlan(DSL.table(DSL.name(tableName)));
        Set<String> requiredTables = new LinkedHashSet<>();
        requiredTables.add(tableName);

        List<FieldReference> references = new ArrayList<>();

        Object lng = config.getValue("longitude");
        Object lat = config.getValue("latitude");
        Object val = config.getValue("valueColumn");
        Object lab = config.getValue("labelColumn");

        // Colonnes attendues par la carte
        String[] keys = {
                lat != null ? lat.toString() : null,
                lng != null ? lng.toString() : null,
                val != null ? val.toString() : null,
                lab != null ? lab.toString() : null
        };

        for (String raw : keys) {
            if (raw == null || raw.isBlank()) continue;

            FieldReference reference = FieldReference.parse(raw, tableName);
            if (reference == null) continue;

            requiredTables.add(reference.table());
            references.add(reference);

            plan.select().add(new SelectExpression(
                    qualified(reference),
                    reference.column(),      // alias = nom simple
                    false));
        }

        if (plan.select().isEmpty()) {
            throw new IllegalArgumentException(
                    "Map requires at least latitude and longitude columns");
        }

        // Filtres + tris → collecte des tables (comme pour chart/table)
        collectTablesFromFilters(config.getValue("filters"), tableName, requiredTables);
        collectTablesFromSorts(config, tableName, requiredTables);

        // Jointures
        addJoins(plan, tableName, requiredTables);

        // Bindings (pour filtres/tris)
        List<FieldBinding> bindings = new ArrayList<>();
        for (FieldReference reference : references) {
            bindings.add(new FieldBinding(
                    reference,
                    QueryRole.COLUMNS,
                    false,
                    null,
                    reference.column()));
        }

        addFilters(plan, config, tableName, bindings);
        addSort(plan, config, bindings);
        plan.limit(parseInteger(config.getValue("limit")));

        return plan;
    }

    public QueryPlan planChart(String tableName,
                               VisualizationConfig config,
                               List<VisualizationParameter> parameters) throws Exception {

        QueryPlan plan = new QueryPlan(DSL.table(DSL.name(tableName)));
        List<FieldBinding> bindings = new ArrayList<>();
        Set<String> requiredTables = new LinkedHashSet<>();
        requiredTables.add(tableName);

        String aggregation = config.getString("aggregation", "SUM");

        // -------- 1a. Colonnes du SELECT --------
        for (VisualizationParameter parameter : parameters) {
            if (!parameter.hasQueryRole()
                    || parameter.isLimit()
                    || parameter.isSort()
                    || parameter.isFilter()) continue;

            FieldReference reference = FieldReference.parse(value(config, parameter), tableName);
            if (reference == null) continue;
            requiredTables.add(reference.table());

            boolean measure = parameter.isMeasure();
            String currentAggregation = measure ? aggregation : null;
            Field<?> field = qualified(reference);
            Field<?> expression = measure ? aggregate(currentAggregation, field) : field;
            boolean aggregated = measure
                    && currentAggregation != null
                    && !"NONE".equalsIgnoreCase(currentAggregation);

            plan.select().add(new SelectExpression(expression, parameter.getKey(), aggregated));
            bindings.add(new FieldBinding(reference, parameter.getRole(), measure,
                    currentAggregation, parameter.getKey()));

            if (!aggregated) plan.groupBy().add(field);
        }

        if (plan.select().isEmpty()) {
            throw new IllegalArgumentException("No visualization value has been configured");
        }

        // -------- 1b. NOUVEAU : colonnes des filtres et tris --------
        collectTablesFromFilters(config.getValue("filters"), tableName, requiredTables);
        collectTablesFromSorts(config, tableName, requiredTables);

        // -------- 2. Jointures (maintenant exhaustives) --------
        addJoins(plan, tableName, requiredTables);

        // -------- 3. GROUP BY conditionnel --------
        boolean hasAggregation = plan.select().stream().anyMatch(SelectExpression::aggregated);
        if (!hasAggregation) plan.groupBy().clear();

        // -------- 4. Filtres et tris (les jointures sont déjà là) --------
        addFilters(plan, config, tableName, bindings);
        addSort(plan, config, bindings);

        plan.limit(parseInteger(config.getValue("limit")));
        return plan;
    }

    public QueryPlan planTable(String tableName, VisualizationConfig config) throws Exception {

        QueryPlan plan = new QueryPlan(DSL.table(DSL.name(tableName)));
        Set<String> requiredTables = new LinkedHashSet<>();
        requiredTables.add(tableName);

        List<FieldReference> references = new ArrayList<>();

        // -------- 1a. Colonnes du SELECT --------
        Object columns = config.getValue("columns");
        if (columns instanceof List<?> list) {
            for (Object item : list) {
                if (item == null) continue;
                FieldReference reference = FieldReference.parse(item.toString(), tableName);
                if (reference == null) continue;

                requiredTables.add(reference.table());
                references.add(reference);

                plan.select().add(new SelectExpression(
                        qualified(reference),
                        reference.column(),
                        false));
            }
        }

        if (plan.select().isEmpty()) {
            throw new IllegalArgumentException("No table column has been configured");
        }

        // -------- 1b. NOUVEAU : filtres + tris --------
        collectTablesFromFilters(config.getValue("filters"), tableName, requiredTables);
        collectTablesFromSorts(config, tableName, requiredTables);

        // -------- 2. Jointures --------
        addJoins(plan, tableName, requiredTables);

        // -------- 3. Bindings (basés sur les vraies références) --------
        List<FieldBinding> bindings = new ArrayList<>();
        for (FieldReference reference : references) {
            bindings.add(new FieldBinding(
                    reference,
                    QueryRole.COLUMNS,
                    false,
                    null,
                    reference.column()));
        }

        // -------- 4. Filtres + tris --------
        addFilters(plan, config, tableName, bindings);
        addSort(plan, config, bindings);

        plan.limit(parseInteger(config.getValue("limit")));
        return plan;
    }

    private Field<?> qualified(FieldReference reference) {
        return DSL.field(DSL.name(reference.table(), reference.column()));
    }

    private Field<?> aggregate(String name, Field<?> field) {
        String normalized = name == null ? "SUM" : name.trim().toUpperCase().replace(' ', '_');
        @SuppressWarnings("unchecked")
        Field<? extends Number> numericField = (Field<? extends Number>) field;
        return switch (normalized) {
            case "COUNT" -> DSL.count(field);
            case "COUNT_DISTINCT" -> DSL.countDistinct(field);
            case "AVG" -> DSL.avg(numericField);
            case "MIN" -> DSL.min(numericField);
            case "MAX" -> DSL.max(numericField);
            case "NONE" -> field;
            default -> DSL.sum(numericField);
        };
    }

    private void addFilters(QueryPlan plan, VisualizationConfig config, String defaultTable, List<FieldBinding> bindings) {
        Object filters = config.getValue("filters");
        if (!(filters instanceof List<?> list)) return;
        Condition where = null;
        Condition having = null;
        for (Object item : list) {
            FilterResult result = filterResult(item, defaultTable, bindings);
            if (result.condition() == null) continue;
            if (result.aggregate()) having = combine(having, result.condition(), result.operator());
            else where = combine(where, result.condition(), result.operator());
        }
        plan.where(where);
        plan.having(having);
    }

    private FilterResult filterResult(Object item, String defaultTable, List<FieldBinding> bindings) {
        if (item instanceof VisualizationFilterCondition filter) {
            String rawColumn = filter.getColumn();
            boolean aggregate = rawColumn != null && rawColumn.trim().matches("(?i)(SUM|AVG|COUNT|COUNT_DISTINCT|MIN|MAX)\\(.*\\)");
            String fieldValue = rawColumn;
            String aggregation = null;
            if (aggregate) {
                int open = rawColumn.indexOf('(');
                aggregation = rawColumn.substring(0, open);
                fieldValue = rawColumn.substring(open + 1, rawColumn.lastIndexOf(')'));
            } else {
                FieldBinding binding = bindings.stream().filter(value -> value.alias().equals(rawColumn)).findFirst().orElse(null);
                aggregate = binding != null && binding.aggregated();
                if (aggregate) aggregation = binding.aggregation();
            }
            FieldReference reference = FieldReference.parse(fieldValue, defaultTable);
            if (reference == null)
                return new FilterResult(null, filter.getRelationToPrevious(), false);

            Field<?> field = aggregate
                    ? aggregate(aggregation, qualified(reference))
                    : qualified(reference);

            Condition cond = filterBuilder.build(
                    field,
                    reference.table(),
                    reference.column(),
                    filter.getOperator(),
                    filter.getValue()
            );

            return new FilterResult(cond, filter.getRelationToPrevious(), aggregate);
        }
        if (item instanceof VisualizationFilterGroup group) {
            Condition combined = null;
            for (VisualizationFilterNode child : group.getChildren()) {
                FilterResult childResult = filterResult(child, defaultTable, bindings);
                if (childResult.condition() != null) combined = combine(combined, childResult.condition(), group.getRelation());
            }
            return new FilterResult(combined, FilterLogicalOperator.AND, false);
        }
        return new FilterResult(null, FilterLogicalOperator.AND, false);
    }

    private record FilterResult(Condition condition, FilterLogicalOperator operator, boolean aggregate) {}

    private Condition combine(Condition current, Condition next, FilterLogicalOperator operator) {
        if (current == null) return next;
        return operator == FilterLogicalOperator.OR ? current.or(next) : current.and(next);
    }

    private void addSort(QueryPlan plan, VisualizationConfig config, List<FieldBinding> bindings) {
        String sortColumn = config.getString("sortColumn");
        String sortDirection = config.getString("sortDirection", "ASC");
        if (sortColumn != null && !sortColumn.isBlank()) {
            FieldBinding binding = bindings.stream().filter(item -> item.alias().equals(sortColumn) || item.field().qualifiedName().equals(sortColumn)).findFirst().orElse(null);
            Field<?> field = binding == null ? qualified(FieldReference.parse(sortColumn, plan.from().getName())) : binding.measure() ? aggregate(binding.aggregation(), qualified(binding.field())) : qualified(binding.field());
            plan.orderBy().add("DESC".equalsIgnoreCase(sortDirection) ? field.desc() : field.asc());
        }
        Object sorts = config.getValue("sorts");
        if (sorts instanceof List<?> list) {
            for (Object sort : list) if (sort instanceof Map<?, ?> map) {
                Object column = map.get("column");
                FieldReference reference = FieldReference.parse(column == null ? null : column.toString(), plan.from().getName());
                if (reference == null) continue;
                Field<?> field = qualified(reference);
                plan.orderBy().add("DESC".equalsIgnoreCase(String.valueOf(map.get("direction"))) ? field.desc() : field.asc());
            }
        }
    }

    private void addJoins(QueryPlan plan, String root, Set<String> requiredTables) throws Exception {

        // On stocke les tables déjà présentes dans le FROM/JOIN
        // sous forme clé = nom en minuscule -> nom réel

        Map<String, String> joined = new LinkedHashMap<>();
        joined.put(root.toLowerCase(Locale.ROOT), root);

        for (String target : requiredTables) {
            if (target.equalsIgnoreCase(root)) continue;

            List<Relationship> path = findPath(root, target);
            for (Relationship relation : path) {
                String left  = relation.leftTable();
                String right = relation.rightTable();
                String leftKey  = left.toLowerCase(Locale.ROOT);
                String rightKey = right.toLowerCase(Locale.ROOT);

                boolean leftJoined  = joined.containsKey(leftKey);
                boolean rightJoined = joined.containsKey(rightKey);
                // Les deux côtés déjà joints → relation déjà couverte
                if (leftJoined && rightJoined) continue;

                String newTable;
                if (leftJoined) {
                    newTable = right;          // on ajoute le côté encore absent
                } else if (rightJoined) {
                    newTable = left;
                } else {
                    // Cas anormal (chemin non connecté) : on joint le côté "enfant"
                    newTable = left;
                }

                Table<?> table = DSL.table(DSL.name(newTable));
                Condition on = DSL.field(DSL.name(left,  relation.leftColumn()))
                        .eq(DSL.field(DSL.name(right, relation.rightColumn())));

                plan.joins().add(new QueryPlan.Join(table, on, relation.joinType()));
                joined.put(newTable.toLowerCase(Locale.ROOT), newTable);
            }
        }
    }

    private List<Relationship> findPath(String source, String target) throws Exception {
        String src = source.toLowerCase(Locale.ROOT);
        String dst = target.toLowerCase(Locale.ROOT);

        Map<String, List<Relationship>> graph = new HashMap<>();
        DatabaseMetaData metadata = connection.getMetaData();

        try (ResultSet tables = metadata.getTables(null, null, "%", new String[]{"TABLE"})) {
            while (tables.next()) {
                String table = tables.getString("TABLE_NAME");
                try (ResultSet keys = metadata.getImportedKeys(null, null, table)) {
                    while (keys.next()) {
                        Relationship relation = new Relationship(
                                table,
                                keys.getString("FKCOLUMN_NAME"),
                                keys.getString("PKTABLE_NAME"),
                                keys.getString("PKCOLUMN_NAME"),
                                Relationship.JoinType.LEFT,
                                "MANY_TO_ONE");
                        graph.computeIfAbsent(relation.leftTable().toLowerCase(Locale.ROOT),  k -> new ArrayList<>()).add(relation);
                        graph.computeIfAbsent(relation.rightTable().toLowerCase(Locale.ROOT), k -> new ArrayList<>()).add(relation);
                    }
                }
            }
        }

        Map<String, Relationship> previousRelation = new HashMap<>();
        Map<String, String>       previousTable    = new HashMap<>();
        Deque<String> queue = new ArrayDeque<>();
        queue.add(src);
        Set<String> visited = new HashSet<>();
        visited.add(src);

        while (!queue.isEmpty()) {
            String current = queue.removeFirst();
            if (current.equals(dst)) break;

            for (Relationship relation : graph.getOrDefault(current, List.of())) {
                String leftKey  = relation.leftTable().toLowerCase(Locale.ROOT);
                String rightKey = relation.rightTable().toLowerCase(Locale.ROOT);
                String next = leftKey.equals(current) ? rightKey : leftKey;

                if (visited.add(next)) {
                    previousTable.put(next, current);
                    previousRelation.put(next, relation);
                    queue.addLast(next);
                }
            }
        }

        if (!visited.contains(dst)) {
            throw new IllegalArgumentException(
                    "No relationship path found between " + source + " and " + target);
        }

        List<Relationship> path = new ArrayList<>();
        for (String current = dst; !current.equals(src); current = previousTable.get(current)) {
            path.add(0, previousRelation.get(current));
        }
        return path;
    }

    private Relationship findRelationship(String left, String right) throws Exception {
        DatabaseMetaData metadata = connection.getMetaData();
        try (ResultSet keys = metadata.getImportedKeys(null, null, left)) {
            while (keys.next()) if (right.equalsIgnoreCase(keys.getString("PKTABLE_NAME"))) return new Relationship(left, keys.getString("FKCOLUMN_NAME"), right, keys.getString("PKCOLUMN_NAME"), Relationship.JoinType.LEFT, "MANY_TO_ONE");
        }
        try (ResultSet keys = metadata.getImportedKeys(null, null, right)) {
            while (keys.next()) if (left.equalsIgnoreCase(keys.getString("PKTABLE_NAME"))) return new Relationship(right, keys.getString("FKCOLUMN_NAME"), left, keys.getString("PKCOLUMN_NAME"), Relationship.JoinType.LEFT, "MANY_TO_ONE");
        }
        return null;
    }

    /** Ajoute à {@code out} toutes les tables référencées par les filtres. */
    private void collectTablesFromFilters(Object filters,
                                          String defaultTable,
                                          Set<String> out) {
        if (!(filters instanceof List<?> list)) return;
        for (Object item : list) {
            collectTablesFromFilterNode(item, defaultTable, out);
        }
    }

    private void collectTablesFromFilterNode(Object node,
                                             String defaultTable,
                                             Set<String> out) {
        if (node instanceof VisualizationFilterCondition c) {
            String col = c.getColumn();
            if (col == null || col.isBlank()) return;
            FieldReference ref = FieldReference.parse(stripAggregation(col.trim()), defaultTable);
            if (ref != null) out.add(ref.table());

        } else if (node instanceof VisualizationFilterGroup g) {
            for (VisualizationFilterNode child : g.getChildren()) {
                collectTablesFromFilterNode(child, defaultTable, out);
            }
        }
    }

    /** Ajoute à {@code out} toutes les tables référencées par les tris. */
    private void collectTablesFromSorts(VisualizationConfig config,
                                        String defaultTable,
                                        Set<String> out) {

        // Tri principal
        String sortColumn = config.getString("sortColumn");
        if (sortColumn != null && !sortColumn.isBlank()) {
            FieldReference ref = FieldReference.parse(stripAggregation(sortColumn.trim()), defaultTable);
            if (ref != null) out.add(ref.table());
        }

        // Liste de tris secondaires
        Object sorts = config.getValue("sorts");
        if (sorts instanceof List<?> list) {
            for (Object item : list) {
                if (!(item instanceof Map<?, ?> map)) continue;
                Object column = map.get("column");
                if (column == null) continue;
                FieldReference ref = FieldReference.parse(
                        stripAggregation(column.toString().trim()), defaultTable);
                if (ref != null) out.add(ref.table());
            }
        }
    }

    /**
     * Retire un éventuel habillage d'agrégat.
     *   "SUM(poste.montant)"   → "poste.montant"
     *   "COUNT_DISTINCT(id)"   → "id"
     *   "libelle"              → "libelle"
     */
    private String stripAggregation(String value) {
        if (value == null) return null;
        String v = value.trim();
        if (v.matches("(?i)^(SUM|AVG|COUNT|COUNT_DISTINCT|MIN|MAX)\\s*\\(.*\\)$")) {
            int open  = v.indexOf('(');
            int close = v.lastIndexOf(')');
            if (open >= 0 && close > open) return v.substring(open + 1, close).trim();
        }
        return v;
    }

    private String value(VisualizationConfig config, VisualizationParameter parameter) {
        Object value = config.getValue(parameter.getKey());
        return value == null ? parameter.getValue() : value.toString();
    }

    private Integer parseInteger(Object value) {
        if (value instanceof Number number) return number.intValue();
        try { return value == null || value.toString().isBlank() ? null : Integer.parseInt(value.toString().trim()); }
        catch (NumberFormatException ignored) { return null; }
    }
}

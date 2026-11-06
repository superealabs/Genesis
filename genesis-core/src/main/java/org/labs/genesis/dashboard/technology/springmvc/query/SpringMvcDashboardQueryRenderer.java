package org.labs.genesis.dashboard.technology.springmvc.query;

import org.labs.genesis.dashboard.model.DashboardFilter;
import org.labs.genesis.dashboard.model.DashboardFilterGroup;
import org.labs.genesis.dashboard.model.DashboardFilterNode;
import org.labs.genesis.dashboard.model.DashboardSort;
import org.labs.genesis.dashboard.model.DashboardEnums.DashboardJoinType;
import org.labs.genesis.dashboard.model.DashboardEnums.DashboardLogicalOperator;
import org.labs.genesis.dashboard.model.DashboardEnums.DashboardSortDirection;
import org.labs.genesis.dashboard.model.DashboardEnums.StatisticType;
import org.labs.genesis.dashboard.query.DashboardJoin;
import org.labs.genesis.dashboard.query.DashboardJoinCondition;
import org.labs.genesis.dashboard.query.DashboardQueryPlan;
import org.labs.genesis.dashboard.query.DashboardQuerySelection;
import org.labs.genesis.dashboard.query.DashboardStatisticExpression;

import java.util.*;

public final class SpringMvcDashboardQueryRenderer {

    private SpringMvcDashboardQueryRenderer() {
    }

    public static SpringMvcRenderedQuery render(DashboardQueryPlan plan
    ) {
        validatePlan(plan);
        String defaultTable = plan.getSource().getName();
        List<Object> parameters = new ArrayList<>();
        List<String> aliases = new ArrayList<>();
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ");
        renderSelections(sql, plan, defaultTable, aliases);
        sql.append(" FROM ").append(defaultTable);
        renderJoins(sql, plan);
        FilterSql filterSql = renderFilters(plan, defaultTable, parameters);
        if (!filterSql.where().isBlank()) {
            sql.append(" WHERE ").append(filterSql.where());
        }
        renderGroupBy(sql, plan, defaultTable);
        if (!filterSql.having().isBlank()) {
            sql.append(" HAVING ").append(filterSql.having());
        }
        renderSorts(sql, plan, defaultTable);
        Integer limit = plan.getOptions() == null
                ? null
                : plan.getOptions().getLimit();

        return new SpringMvcRenderedQuery(sql.toString(), parameters, limit, aliases);
    }

    private static void validatePlan(DashboardQueryPlan plan) {
        if (plan == null
                || plan.getSource() == null
                || plan.getSource().getName() == null
                || plan.getSource().getName().isBlank()) {
            throw new IllegalArgumentException("Dashboard query source is required");
        }
    }

    private static void renderSelections(StringBuilder sql, DashboardQueryPlan plan, String defaultTable, List<String> aliases) {
        List<String> selections = new ArrayList<>();
        if (plan.getRowStatistic() != null) {
            DashboardStatisticExpression statistic = plan.getRowStatistic();
            String alias = statistic.getAlias() == null || statistic.getAlias().isBlank()
                    ? "value"
                    : statistic.getAlias();
            selections.add(renderStatistic(statistic, defaultTable) + " AS " + alias);
            aliases.add(alias);
        } else {
            for (DashboardQuerySelection selection : plan.getSelections()) {
                if (selection == null) {
                    continue;
                }
                String expression = renderSelection(selection, defaultTable);
                if (expression == null) {
                    continue;
                }
                selections.add(expression);
                String alias = selection.getAlias();
                if (alias == null || alias.isBlank()) {
                    alias = simpleColumnName(selection.getColumnName());
                }
                aliases.add(alias);
            }
        }

        if (selections.isEmpty()) {
            throw new IllegalArgumentException("Dashboard query requires at least one selection");
        }
        sql.append(String.join(", ", selections));
    }

    private static String renderSelection(DashboardQuerySelection selection, String defaultTable) {
        String expression;
        if (selection.getStatisticExpression() != null) {
            expression = renderStatistic(selection.getStatisticExpression(), defaultTable);
        } else {
            expression = qualify(selection.getColumnName(), defaultTable);
        }
        if (expression == null) {
            return null;
        }
        if (selection.getAlias() != null && !selection.getAlias().isBlank()) {
            expression += " AS " + selection.getAlias();
        }
        return expression;
    }

    private static String renderStatistic(DashboardStatisticExpression expression, String defaultTable
    ) {
        if (expression == null) {
            return null;
        }
        if (expression.targetsRows()) {
            return expression.getStatistic() == StatisticType.COUNT
                    ? "COUNT(*)"
                    : "*";
        }
        String field = qualify(expression.getColumnName(), defaultTable);
        StatisticType statistic = expression.getStatistic();
        if (statistic == null || statistic == StatisticType.NONE) {
            return field;
        }
        return switch (statistic) {
            case COUNT -> "COUNT(" + field + ")";
            case COUNT_DISTINCT -> "COUNT(DISTINCT " + field + ")";
            case SUM -> "SUM(" + field + ")";
            case AVG -> "AVG(" + field + ")";
            case MIN -> "MIN(" + field + ")";
            case MAX -> "MAX(" + field + ")";
            case NONE -> field;
        };
    }

    private static void renderJoins(StringBuilder sql, DashboardQueryPlan plan) {
        if (plan.getJoins() == null) {
            return;
        }
        for (DashboardJoin join : plan.getJoins()) {
            if (join == null
                    || join.getTable() == null
                    || join.getConditions() == null
                    || join.getConditions().isEmpty()) {
                continue;
            }
            sql.append(" ").append(joinKeyword(join.getType()))
                    .append(" ")
                    .append(join.getTable())
                    .append(" ON ");
            List<String> conditions = new ArrayList<>();
            for (DashboardJoinCondition condition : join.getConditions()) {
                if (condition == null) {
                    continue;
                }
                conditions.add(condition.getLeftTable() + "." + condition.getLeftColumn() + " = " + condition.getRightTable() + "." + condition.getRightColumn()
                );
            }
            sql.append(String.join(" AND ", conditions));
        }
    }

    private static String joinKeyword(DashboardJoinType type) {
        if (type == null) {
            return "LEFT JOIN";
        }
        return switch (type) {
            case INNER -> "INNER JOIN";
            case LEFT -> "LEFT JOIN";
            case RIGHT -> "RIGHT JOIN";
            case FULL -> "FULL JOIN";
        };
    }

    private static void renderGroupBy(StringBuilder sql, DashboardQueryPlan plan, String defaultTable) {
        if (plan.getGroupBy() == null || plan.getGroupBy().isEmpty()) {
            return;
        }
        List<String> fields = new ArrayList<>();
        for (String column : plan.getGroupBy()) {
            String field = qualify(column, defaultTable);
            if (field != null) {
                fields.add(field);
            }
        }
        if (!fields.isEmpty()) {
            sql.append(" GROUP BY ").append(String.join(", ", fields));
        }
    }

    private static void renderSorts(StringBuilder sql, DashboardQueryPlan plan, String defaultTable) {
        if (plan.getOptions() == null
                || plan.getOptions().getSorts() == null
                || plan.getOptions().getSorts().isEmpty()) {
            return;
        }
        List<String> sorts = new ArrayList<>();
        for (DashboardSort sort : plan.getOptions().getSorts()) {
            if (sort == null
                    || sort.getColumnName() == null
                    || sort.getColumnName().isBlank()) {
                continue;
            }
            String expression = resolveSortExpression(sort.getColumnName(), plan, defaultTable
            );
            if (expression == null) {
                continue;
            }
            DashboardSortDirection direction = sort.getDirection();
            sorts.add(expression + " " + (direction == DashboardSortDirection.DESC ? "DESC" : "ASC")
            );
        }

        if (!sorts.isEmpty()) {
            sql.append(" ORDER BY ").append(String.join(", ", sorts));
        }
    }

    private static String resolveSortExpression(String target, DashboardQueryPlan plan, String defaultTable) {
        for (DashboardQuerySelection selection : plan.getSelections()) {
            if (selection == null) {
                continue;
            }
            boolean matches = target.equalsIgnoreCase(selection.getAlias()) || target.equalsIgnoreCase(selection.getColumnName());
            if (!matches) {
                continue;
            }
            if (selection.getStatisticExpression() != null) {
                return renderStatistic(selection.getStatisticExpression(), defaultTable);
            }
            return qualify(selection.getColumnName(), defaultTable);
        }
        return qualify(target, defaultTable);
    }

    private record FilterSql(String where, String having) {
        private static FilterSql empty() {
            return new FilterSql("", "");
        }
    }

    private static final Map<String, String> COMPARATORS = Map.ofEntries(
            Map.entry(">", ">"), Map.entry("greater than", ">"), Map.entry("gt", ">"), Map.entry("after", ">"),
            Map.entry(">=", ">="), Map.entry("greater than or equal", ">="), Map.entry("gte", ">="), Map.entry("at least", ">="),
            Map.entry("<", "<"), Map.entry("less than", "<"), Map.entry("lt", "<"), Map.entry("before", "<"),
            Map.entry("<=", "<="), Map.entry("less than or equal", "<="), Map.entry("lte", "<="), Map.entry("at most", "<="));

    private static FilterSql renderFilters(DashboardQueryPlan plan, String table, List<Object> params) {
        var options = plan.getOptions();
        if (options == null || options.getFilters() == null || options.getFilters().isEmpty()) return FilterSql.empty();
        return renderNodes(options.getFilters(), null, plan, table, params);
    }

    /** forced == null : on utilise relationToPrevious de chaque noeud (niveau racine) ; sinon l'operateur du groupe. */
    private static FilterSql renderNodes(List<DashboardFilterNode> nodes, DashboardLogicalOperator forced, DashboardQueryPlan plan, String table, List<Object> params) {
        String where = "", having = "";
        for (DashboardFilterNode node : nodes) {
            FilterSql r = renderNode(node, plan, table, params);
            boolean w = !r.where().isBlank(), h = !r.having().isBlank();
            DashboardLogicalOperator op = forced != null ? forced : relationToPrevious(node);

            if (op == DashboardLogicalOperator.OR && ((w && !having.isBlank()) || (h && !where.isBlank()) || (w && h && forced != null))) {
                throw new IllegalArgumentException("OR groups cannot mix aggregated and non-aggregated filters");
            }
            where = combine(where, r.where(), op);
            having = combine(having, r.having(), op);
        }
        return new FilterSql(where, having);
    }

    private static FilterSql renderNode(DashboardFilterNode node, DashboardQueryPlan plan, String table, List<Object> params) {
        if (node instanceof DashboardFilter f) return renderFilter(f, plan, table, params);
        if (node instanceof DashboardFilterGroup g && g.getChildren() != null && !g.getChildren().isEmpty()) {
            var op = g.getOperator() == DashboardLogicalOperator.OR ? DashboardLogicalOperator.OR : DashboardLogicalOperator.AND;
            FilterSql r = renderNodes(g.getChildren(), op, plan, table, params);
            return new FilterSql(parenthesize(r.where()), parenthesize(r.having()));
        }
        return FilterSql.empty();
    }

    private static FilterSql renderFilter(DashboardFilter f, DashboardQueryPlan plan, String table, List<Object> params) {
        if (f.getColumnName() == null || f.getColumnName().isBlank()) return FilterSql.empty();

        DashboardQuerySelection sel = findSelection(plan, f.getColumnName());
        String field = sel != null && sel.getStatisticExpression() != null
                ? renderStatistic(sel.getStatisticExpression(), table)
                : qualify(sel != null ? sel.getColumnName() : f.getColumnName(), table);

        String condition = renderCondition(field, f.getOperator(), f.getValue(), params);
        if (condition == null || condition.isBlank()) return FilterSql.empty();

        return sel != null && sel.isAggregated() ? new FilterSql("", condition) : new FilterSql(condition, "");
    }

    private static DashboardQuerySelection findSelection(DashboardQueryPlan plan, String target) {
        return plan.getSelections().stream()
                .filter(s -> s != null && (target.equalsIgnoreCase(s.getAlias()) || target.equalsIgnoreCase(s.getColumnName())))
                .findFirst().orElse(null);
    }

    private static String renderCondition(String field, String operatorRaw, Object value, List<Object> params) {
        String op = operatorRaw == null ? "is" : operatorRaw.trim().toLowerCase(Locale.ROOT).replace('_', ' ').replaceAll("\\s+", " ");
        String v = value == null ? null : value.toString();

        String comparator = COMPARATORS.get(op);
        if (comparator != null) return renderComparison(field, comparator, v, params);

        return switch (op) {
            case "is not", "!=", "<>", "not equals", "ne" -> renderEquals(field, v, true, params);
            case "contains", "like" -> renderLike(field, v, "%", "%", false, params);
            case "doesn't contain", "does not contain", "not contains", "not like" -> renderLike(field, v, "%", "%", true, params);
            case "starts with", "startswith" -> renderLike(field, v, "", "%", false, params);
            case "ends with", "endswith" -> renderLike(field, v, "%", "", false, params);
            case "is empty" -> "(" + field + " IS NULL OR " + field + " = '')";
            case "is not empty" -> "(" + field + " IS NOT NULL AND " + field + " <> '')";
            case "between" -> renderBetween(field, v, params);
            case "in", "is one of", "one of" -> renderIn(field, v, false, params);
            case "not in", "is not one of" -> renderIn(field, v, true, params);
            default -> renderEquals(field, v, false, params); // "is", "=", "equals", "eq" et inconnus
        };
    }

    private static String addParameter(Object value, List<Object> params) {
        params.add(value);
        return "?" + params.size();
    }

    private static String renderEquals(String field, String value, boolean negate, List<Object> params) {
        if (value == null || value.isBlank()) return field + (negate ? " IS NOT NULL" : " IS NULL");
        return field + (negate ? " <> " : " = ") + addParameter(value, params);
    }

    private static String renderLike(String field, String value, String prefix, String suffix, boolean negate, List<Object> params) {
        return value == null ? null : field + (negate ? " NOT LIKE " : " LIKE ") + addParameter(prefix + value + suffix, params);
    }

    private static String renderComparison(String field, String operator, String value, List<Object> params) {
        return value == null || value.isBlank() ? null : field + " " + operator + " " + addParameter(value, params);
    }

    private static String renderBetween(String field, String value, List<Object> params) {
        if (value == null || value.isBlank()) return null;
        String[] p = value.contains("..") ? value.split("\\.\\.", 2) : value.split(",", 2);
        if (p.length != 2) return null;
        return field + " BETWEEN " + addParameter(p[0].trim(), params) + " AND " + addParameter(p[1].trim(), params);
    }

    private static String renderIn(String field, String value, boolean negate, List<Object> params) {
        if (value == null || value.isBlank()) return null;
        List<String> placeholders = Arrays.stream(value.split(","))
                .map(String::trim).filter(s -> !s.isEmpty())
                .map(s -> addParameter(s, params)).toList();
        return placeholders.isEmpty() ? null : field + (negate ? " NOT IN (" : " IN (") + String.join(", ", placeholders) + ")";
    }

    private static String combine(String current, String next, DashboardLogicalOperator op) {
        if (next == null || next.isBlank()) return current;
        if (current == null || current.isBlank()) return next;
        return "(" + current + ") " + (op == DashboardLogicalOperator.OR ? "OR" : "AND") + " " + next;
    }

    private static String parenthesize(String sql) {
        return sql == null || sql.isBlank() ? "" : "(" + sql + ")";
    }

    private static DashboardLogicalOperator relationToPrevious(DashboardFilterNode node) {
        DashboardLogicalOperator op = node instanceof DashboardFilter f ? f.getRelationToPrevious()
                : node instanceof DashboardFilterGroup g ? g.getRelationToPrevious() : null;
        return op == null ? DashboardLogicalOperator.AND : op;
    }


    private static String qualify(String column, String defaultTable) {
        if (column == null || column.isBlank()) {
            return null;
        }
        String value = column.trim();
        if (value.toUpperCase(Locale.ROOT).startsWith("COLUMN:")) {
            value = value.substring(7).trim();
        }
        if (value.contains(".")) {
            return value;
        }
        return defaultTable + "." + value;
    }

    private static String simpleColumnName(String value) {
        if (value == null || value.isBlank()) {
            return "value";
        }
        String result = value;
        int dot = result.lastIndexOf('.');
        if (dot >= 0 && dot < result.length() - 1) {
            result = result.substring(dot + 1);
        }
        return result;
    }
}
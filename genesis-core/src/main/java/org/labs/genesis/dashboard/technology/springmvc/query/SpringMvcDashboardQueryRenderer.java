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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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

    private static FilterSql renderFilters(DashboardQueryPlan plan, String defaultTable, List<Object> parameters) {
        return FilterSql.empty();
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
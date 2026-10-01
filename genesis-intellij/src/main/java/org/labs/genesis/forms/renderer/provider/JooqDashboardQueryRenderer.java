package org.labs.genesis.forms.renderer.provider;

import org.jooq.*;
import org.jooq.Record;
import org.jooq.impl.DSL;
import org.labs.genesis.dashboard.model.*;
import org.labs.genesis.dashboard.model.DashboardEnums.*;
import org.labs.genesis.dashboard.query.*;

import java.sql.Connection;
import java.util.List;

public final class JooqDashboardQueryRenderer implements DashboardQueryRenderer<Select<?>> {
    private final DSLContext dsl;
    private final FilterConditionBuilder filterBuilder;

    public JooqDashboardQueryRenderer(Connection connection, DSLContext dsl) {
        this.dsl = dsl;
        this.filterBuilder = new FilterConditionBuilder(connection);
    }

    @Override
    public Select<?> render(DashboardQueryPlan plan) {
        if (plan == null || plan.getSource() == null || plan.getSource().getName() == null) {
            throw new IllegalArgumentException("Dashboard query source is required");
        }
        SelectQuery<Record> query = dsl.selectQuery();
        String sourceName = plan.getSource().getName();
        query.addFrom(DSL.table(DSL.name(sourceName)));
        addJoins(query, plan);
        addSelections(query, plan, sourceName);
        addFilters(query, plan, sourceName);
        addGroupBy(query, plan, sourceName);
        addSorts(query, plan, sourceName);
        addLimit(query, plan);
        return query;
    }

    private void addSelections(SelectQuery<Record> query, DashboardQueryPlan plan, String defaultTable
    ) {
        if (plan.getRowStatistic() != null) {
            Field<?> field = renderStatistic(plan.getRowStatistic(), defaultTable);
            query.addSelect(field);
            return;
        }
        for (DashboardQuerySelection selection : plan.getSelections()) {
            Field<?> field = renderSelection(selection, defaultTable);
            if (field != null) {
                query.addSelect(field);
            }
        }
    }

    private Field<?> renderSelection(DashboardQuerySelection selection, String defaultTable) {
        if (selection.getStatisticExpression() != null) {
            return renderStatistic(selection.getStatisticExpression(), defaultTable);
        }
        FieldReference reference = FieldReference.parse(selection.getColumnName(), defaultTable);
        if (reference == null) {
            return null;
        }
        Field<?> field = qualified(reference);
        if (selection.getAlias() != null && !selection.getAlias().isBlank()) {
            return field.as(selection.getAlias());
        }
        return field;
    }

    private Field<?> renderStatistic(DashboardStatisticExpression expression, String defaultTable) {
        if (expression == null) {
            return null;
        }
        Field<?> result;
        if (expression.targetsRows()) {
            result = DSL.count();
        } else {
            FieldReference reference = FieldReference.parse(expression.getColumnName(), defaultTable);
            if (reference == null) {
                throw new IllegalArgumentException("Invalid statistic column");
            }
            Field<?> field = qualified(reference);
            result = aggregate(expression.getStatistic(), field);
        }
        if (expression.getAlias() != null && !expression.getAlias().isBlank()) {
            result = result.as(expression.getAlias());
        }
        return result;
    }

    private Field<?> aggregate(StatisticType statistic, Field<?> field) {
        if (statistic == null || statistic == StatisticType.NONE) {
            return field;
        }
        return switch (statistic) {
            case COUNT -> DSL.count(field);
            case COUNT_DISTINCT -> DSL.countDistinct(field);
            case AVG -> DSL.avg(numericField(field));
            case MIN -> DSL.min(field);
            case MAX -> DSL.max(field);
            case SUM -> DSL.sum(numericField(field));
            case NONE -> field;
        };
    }

    @SuppressWarnings("unchecked")
    private Field<? extends Number> numericField(Field<?> field
    ) {
        return (Field<? extends Number>) field;
    }

    private Field<?> qualified(FieldReference reference
    ) {
        return DSL.field(DSL.name(reference.table(), reference.column()));
    }

    private void addGroupBy(SelectQuery<Record> query, DashboardQueryPlan plan, String defaultTable) {
        for (String column : plan.getGroupBy()) {
            FieldReference reference = FieldReference.parse(column, defaultTable);
            if (reference != null) {
                query.addGroupBy(qualified(reference));
            }
        }
    }

    private void addLimit(SelectQuery<Record> query, DashboardQueryPlan plan) {
        if (plan.getOptions() == null || plan.getOptions().getLimit() == null || plan.getOptions().getLimit() <= 0) {
            return;
        }
        query.addLimit(plan.getOptions().getLimit());
    }

    private void addSorts(SelectQuery<Record> query, DashboardQueryPlan plan, String defaultTable
    ) {
        if (plan.getOptions() == null || plan.getOptions().getSorts() == null) {
            return;
        }
        for (DashboardSort sort : plan.getOptions().getSorts()) {
            if (sort == null || sort.getColumnName() == null || sort.getColumnName().isBlank()) {
                continue;
            }
            Field<?> field = findSortField(plan, sort.getColumnName(), defaultTable);
            if (field == null) {
                continue;
            }
            SortField<?> sortField = sort.getDirection() == DashboardSortDirection.DESC ? field.desc() : field.asc();
            query.addOrderBy(sortField);
        }
    }

    private Field<?> findSortField(DashboardQueryPlan plan, String column, String defaultTable) {
        for (DashboardQuerySelection selection : plan.getSelections()) {
            if (column.equalsIgnoreCase(selection.getAlias()) || column.equalsIgnoreCase(selection.getColumnName())) {
                if (selection.getStatisticExpression() != null) {
                    return renderStatistic(selection.getStatisticExpression(), defaultTable);
                }
                FieldReference reference = FieldReference.parse(selection.getColumnName(), defaultTable);
                return reference == null ? null : qualified(reference);
            }
        }

        FieldReference reference = FieldReference.parse(column, defaultTable);
        return reference == null ? null : qualified(reference);
    }

    private void addFilters(SelectQuery<Record> query, DashboardQueryPlan plan, String defaultTable) {
        if (plan.getOptions() == null || plan.getOptions().getFilters() == null || plan.getOptions().getFilters().isEmpty()) {
            return;
        }
        FilterSplit result = splitFilterList(plan.getOptions().getFilters(), plan, defaultTable);
        if (result.where() != null) {
            query.addConditions(result.where());
        }
        if (result.having() != null) {
            query.addHaving(result.having());
        }
    }

    private FilterSplit splitFilterNode(DashboardFilterNode node, DashboardQueryPlan plan, String defaultTable) {
        if (node instanceof DashboardFilter filter) {
            return splitCondition(filter, plan, defaultTable);
        }
        if (node instanceof DashboardFilterGroup group) {
            return splitGroup(group, plan, defaultTable);
        }
        return FilterSplit.empty();
    }

    private Condition renderFilter(DashboardFilter filter, DashboardQueryPlan plan, String defaultTable) {
        if (filter == null || filter.getColumnName() == null || filter.getColumnName().isBlank()) {
            return null;
        }
        Field<?> field = resolveFilterField(filter, plan, defaultTable);
        if (field == null) {
            return null;
        }
        return filterBuilder.build(field,
                null, filter.getColumnName(), filter.getOperator(), filter.getValue() == null ? null : filter.getValue().toString());
    }

    private Field<?> resolveFilterField(DashboardFilter filter, DashboardQueryPlan plan, String defaultTable) {
        String target = filter.getColumnName();
        for (DashboardQuerySelection selection : plan.getSelections()) {
            boolean matches = target.equalsIgnoreCase(selection.getAlias()) || target.equalsIgnoreCase(selection.getColumnName());
            if (!matches) {
                continue;
            }
            if (selection.getStatisticExpression() != null) {
                return renderStatistic(selection.getStatisticExpression(), defaultTable);
            }
            FieldReference reference = FieldReference.parse(selection.getColumnName(), defaultTable);
            return reference == null ? null : qualified(reference);
        }
        FieldReference reference = FieldReference.parse(target, defaultTable);
        return reference == null ? null : qualified(reference);
    }

    private FilterSplit splitGroup(DashboardFilterGroup group, DashboardQueryPlan plan, String defaultTable) {
        if (group == null || group.getChildren() == null || group.getChildren().isEmpty()) {
            return FilterSplit.empty();
        }
        return group.getOperator()
                == DashboardLogicalOperator.OR
                ? splitOrGroup(
                group,
                plan,
                defaultTable
        )
                : splitAndGroup(
                group,
                plan,
                defaultTable
        );
    }

    private FilterSplit splitAndGroup(DashboardFilterGroup group, DashboardQueryPlan plan, String defaultTable) {
        Condition where = null;
        Condition having = null;
        for (DashboardFilterNode child : group.getChildren()) {
            FilterSplit split = splitFilterNode(child, plan, defaultTable);
            where = andNullable(where, split.where());
            having = andNullable(having, split.having());
        }
        return new FilterSplit(where, having);
    }

    private FilterSplit splitOrGroup(DashboardFilterGroup group, DashboardQueryPlan plan, String defaultTable) {
        Condition where = null;
        Condition having = null;
        boolean containsWhere = false;
        boolean containsHaving = false;
        for (DashboardFilterNode child : group.getChildren()) {
            FilterSplit split = splitFilterNode(child, plan, defaultTable);
            if (split.isMixed()) {
                throw unsupportedMixedOr();
            }
            if (split.hasWhere()) {
                containsWhere = true;
                where = orNullable(where, split.where());
            }
            if (split.hasHaving()) {
                containsHaving = true;
                having = orNullable(having, split.having());
            }
            if (containsWhere && containsHaving) {
                throw unsupportedMixedOr();
            }
        }
        return new FilterSplit(where, having);
    }

    private IllegalArgumentException unsupportedMixedOr() {
        return new IllegalArgumentException(
                "OR groups cannot mix aggregated and non-aggregated filters"
        );
    }

    private Condition andNullable(Condition current, Condition next) {
        if (next == null) {
            return current;
        }
        if (current == null) {
            return next;
        }
        return current.and(next);
    }

    private Condition orNullable(Condition current, Condition next) {
        if (next == null) {
            return current;
        }
        if (current == null) {
            return next;
        }
        return current.or(next);
    }

    private Condition combineNullable(Condition current, Condition next, DashboardLogicalOperator operator) {
        if (next == null) {
            return current;
        }
        if (current == null) {
            return next;
        }
        return operator == DashboardLogicalOperator.OR ? current.or(next) : current.and(next);
    }

    private boolean isAggregateFilter(DashboardFilter filter, DashboardQueryPlan plan) {
        if (filter == null || filter.getColumnName() == null) {
            return false;
        }
        String target = filter.getColumnName();
        return plan.getSelections()
                .stream()
                .anyMatch(selection -> selection.isAggregated() && (target.equalsIgnoreCase(selection.getAlias()) || target.equalsIgnoreCase(selection.getColumnName()))
                );
    }

    private record FilterSplit(Condition where, Condition having) {
        private static FilterSplit empty() {
            return new FilterSplit(null, null);
        }
        private boolean hasWhere() {
            return where != null;
        }
        private boolean hasHaving() {
            return having != null;
        }
        private boolean isMixed() {
            return hasWhere() && hasHaving();
        }
    }

    private FilterSplit splitFilterList(List<DashboardFilterNode> nodes, DashboardQueryPlan plan, String defaultTable) {
        Condition where = null;
        Condition having = null;
        boolean hasPreviousWhere = false;
        boolean hasPreviousHaving = false;
        for (DashboardFilterNode node : nodes) {
            FilterSplit split = splitFilterNode(node, plan, defaultTable);
            DashboardLogicalOperator relation = relationToPrevious(node);
            if (relation == DashboardLogicalOperator.OR) {
                boolean crossesBoundary = (split.hasWhere() && hasPreviousHaving) || (split.hasHaving() && hasPreviousWhere);
                if (crossesBoundary) {
                    throw unsupportedMixedOr();
                }
            }
            where = combineNullable(where, split.where(), relation);
            having = combineNullable(having, split.having(), relation);
            hasPreviousWhere |= split.hasWhere();
            hasPreviousHaving |= split.hasHaving();
        }
        return new FilterSplit(where, having);
    }

    private DashboardLogicalOperator relationToPrevious(DashboardFilterNode node) {
        if (node instanceof DashboardFilter filter) {
            return filter.getRelationToPrevious() == null
                    ? DashboardLogicalOperator.AND
                    : filter.getRelationToPrevious();
        }
        if (node instanceof DashboardFilterGroup group) {
            return group.getRelationToPrevious() == null
                    ? DashboardLogicalOperator.AND
                    : group.getRelationToPrevious();
        }
        return DashboardLogicalOperator.AND;
    }

    private FilterSplit splitCondition(DashboardFilter filter, DashboardQueryPlan plan, String defaultTable) {
        Condition condition = renderFilter(filter, plan, defaultTable);
        if (condition == null) {
            return FilterSplit.empty();
        }
        if (isAggregateFilter(filter, plan)) {
            return new FilterSplit(null, condition);
        }
        return new FilterSplit(condition, null);
    }

    private void addJoins(SelectQuery<Record> query, DashboardQueryPlan plan) {
        if (plan.getJoins() == null || plan.getJoins().isEmpty()) {
            return;
        }
        for (DashboardJoin join : plan.getJoins()) {
            if (join == null
                    || join.getTable() == null
                    || join.getConditions() == null
                    || join.getConditions().isEmpty()) {
                continue;
            }
            Table<?> table = DSL.table(DSL.name(join.getTable()));
            Condition condition = buildJoinCondition(join);
            if (condition == null) {
                continue;
            }
            query.addJoin(table, toJooqJoinType(join.getType()), condition);
        }
    }

    private Condition buildJoinCondition(DashboardJoin join) {
        Condition result = null;
        for (DashboardJoinCondition condition : join.getConditions()) {
            if (condition == null) {
                continue;
            }
            Condition current = DSL.field(DSL.name(condition.getLeftTable(), condition.getLeftColumn())).eq(DSL.field(DSL.name(condition.getRightTable(), condition.getRightColumn())));
            result = result == null
                            ? current
                            : result.and(current);
        }
        return result;
    }

    private JoinType toJooqJoinType(DashboardJoinType type) {
        if (type == null) {
            return JoinType.LEFT_OUTER_JOIN;
        }
        return switch (type) {
            case INNER ->
                    JoinType.JOIN;

            case LEFT ->
                    JoinType.LEFT_OUTER_JOIN;

            case RIGHT ->
                    JoinType.RIGHT_OUTER_JOIN;

            case FULL ->
                    JoinType.FULL_OUTER_JOIN;
        };
    }
}
package org.labs.genesis.forms.renderer.provider;

import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.SortField;
import org.jooq.Table;

import java.util.ArrayList;
import java.util.List;

public final class QueryPlan {
    private final Table<?> from;
    private final List<SelectExpression> select = new ArrayList<>();
    private final List<Join> joins = new ArrayList<>();
    private final List<Field<?>> groupBy = new ArrayList<>();
    private final List<SortField<?>> orderBy = new ArrayList<>();
    private Condition where;
    private Condition having;
    private Integer limit;

    public QueryPlan(Table<?> from) { this.from = from; }
    public Table<?> from() { return from; }
    public List<SelectExpression> select() { return select; }
    public List<Join> joins() { return joins; }
    public List<Field<?>> groupBy() { return groupBy; }
    public List<SortField<?>> orderBy() { return orderBy; }
    public Condition where() { return where; }
    public Condition having() { return having; }
    public Integer limit() { return limit; }
    public void where(Condition value) { where = value; }
    public void having(Condition value) { having = value; }
    public void limit(Integer value) { limit = value; }

    public record Join(Table<?> table, Condition condition, Relationship.JoinType type) {}
}

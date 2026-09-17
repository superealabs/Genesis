package org.labs.genesis.forms.renderer.provider;

import org.jooq.DSLContext;
import org.jooq.JoinType;
import org.jooq.Select;
import org.jooq.SelectQuery;
import org.jooq.Record;

import java.util.List;

public final class SQLGenerator {
    public Select<?> generate(DSLContext dsl, QueryPlan plan) {
        SelectQuery<Record> query = dsl.selectQuery();
        for (SelectExpression expression : plan.select()) {
            query.addSelect(expression.alias() == null
                    ? expression.expression()
                    : expression.expression().as(expression.alias()));
        }
        query.addFrom(plan.from());
        for (QueryPlan.Join join : plan.joins()) {
            if (join.type() == Relationship.JoinType.INNER) query.addJoin(join.table(), JoinType.JOIN, join.condition());
            else query.addJoin(join.table(), JoinType.LEFT_OUTER_JOIN, join.condition());
        }
        if (plan.where() != null) query.addConditions(plan.where());
        if (!plan.groupBy().isEmpty()) query.addGroupBy(plan.groupBy());
        if (plan.having() != null) query.addHaving(plan.having());
        if (!plan.orderBy().isEmpty()) query.addOrderBy(plan.orderBy());
        if (plan.limit() != null && plan.limit() > 0) query.addLimit(plan.limit());
        return query;
    }
}

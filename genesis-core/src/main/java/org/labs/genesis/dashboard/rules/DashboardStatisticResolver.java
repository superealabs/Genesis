package org.labs.genesis.dashboard.rules;

import org.labs.genesis.dashboard.model.DashboardField;
import org.labs.genesis.dashboard.model.DashboardEnums.StatisticType;
import org.labs.genesis.dashboard.query.DashboardStatisticExpression;

public final class DashboardStatisticResolver {
    private DashboardStatisticResolver() {
    }

    public static DashboardStatisticExpression resolve(DashboardField field) {
        if (field == null || field.getStatistic() == null) {
            return null;
        }
        StatisticType statistic = field.getStatistic();
        String columnName = field.getColumnName();
        validate(statistic, columnName);
        return new DashboardStatisticExpression(statistic, columnName, field.getKey(), statistic == StatisticType.COUNT_DISTINCT);
    }

    public static DashboardStatisticExpression countRows(String alias) {
        return new DashboardStatisticExpression(StatisticType.COUNT, null, alias, false);
    }

    private static void validate(StatisticType statistic, String columnName) {
        if (columnName == null || columnName.isBlank()) {

            if (statistic != StatisticType.COUNT) {
                throw new IllegalArgumentException("Only COUNT can target rows");
            }
        }
    }
}
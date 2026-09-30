package org.labs.genesis.dashboard.rules;

import org.labs.genesis.connexion.model.ColumnMetadata;
import org.labs.genesis.dashboard.model.DashboardEnums.DashboardDataType;
import org.labs.genesis.dashboard.model.DashboardEnums.StatisticType;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public final class DashboardStatisticRules {

    private static final Set<StatisticType> TABLE_STATISTICS = Collections.unmodifiableSet(
            EnumSet.of(StatisticType.NONE, StatisticType.COUNT));

    private static final Map<DashboardDataType, Set<StatisticType>> COLUMN_STATISTICS = createRules();

    private DashboardStatisticRules() {
    }

    public static Set<StatisticType> getTableStatistics() {
        return TABLE_STATISTICS;
    }

    public static Set<StatisticType> getColumnStatistics(ColumnMetadata column) {
        DashboardDataType dataType = DashboardDataTypeResolver.resolve(column);
        return getColumnStatistics(dataType);
    }

    public static Set<StatisticType> getColumnStatistics(DashboardDataType dataType) {
        if (dataType == null) {
            return Collections.emptySet();
        }

        Set<StatisticType> supported = COLUMN_STATISTICS.getOrDefault(dataType, Collections.emptySet());
        if (supported.isEmpty()) return supported;
        EnumSet<StatisticType> withNone = EnumSet.copyOf(supported);
        withNone.add(StatisticType.NONE);
        return Collections.unmodifiableSet(withNone);
    }

    public static boolean supports(ColumnMetadata column, StatisticType statisticType) {
        if (statisticType == null) {
            return false;
        }
        if (statisticType == StatisticType.NONE) {
            return column != null;
        }
        return getColumnStatistics(column).contains(statisticType);
    }

    private static Map<DashboardDataType, Set<StatisticType>> createRules() {
        EnumMap<DashboardDataType, Set<StatisticType>> rules = new EnumMap<>(DashboardDataType.class);
        rules.put(DashboardDataType.NUMERIC, immutableSet(
                        StatisticType.COUNT,
                        StatisticType.COUNT_DISTINCT,
                        StatisticType.SUM,
                        StatisticType.AVG,
                        StatisticType.MIN,
                        StatisticType.MAX
                )
        );

        rules.put(DashboardDataType.TEXT, immutableSet(
                        StatisticType.COUNT,
                        StatisticType.COUNT_DISTINCT
                )
        );

        rules.put(DashboardDataType.BOOLEAN, immutableSet(
                        StatisticType.COUNT,
                        StatisticType.COUNT_DISTINCT
                )
        );

        rules.put(DashboardDataType.TEMPORAL, immutableSet(
                        StatisticType.COUNT,
                        StatisticType.COUNT_DISTINCT,
                        StatisticType.MIN,
                        StatisticType.MAX
                )
        );

        rules.put(DashboardDataType.OTHER, immutableSet(
                        StatisticType.COUNT
                )
        );
        return Collections.unmodifiableMap(rules);
    }

    private static Set<StatisticType> immutableSet(StatisticType... values) {
        EnumSet<StatisticType> set = EnumSet.noneOf(StatisticType.class);
        Collections.addAll(set, values);
        return Collections.unmodifiableSet(set);
    }
}

package org.labs.genesis.dashboard.model;

/**
 * Common dashboard enum types.
 */
public final class DashboardEnums {

    private DashboardEnums() {
    }

    public enum DashboardDataType {
        NUMERIC,
        TEXT,
        BOOLEAN,
        TEMPORAL,
        OTHER
    }

    public enum DashboardFieldRole {
        DIMENSION,
        MEASURE,
        VALUE,
        COLUMN,
        LATITUDE,
        LONGITUDE
    }

    public enum DashboardSortDirection {
        ASC,
        DESC
    }

    public enum DashboardSourceType {
        TABLE,
        VIEW
    }

    public enum DashboardVisualizationType {
        BAR_VERTICAL,
        BAR_HORIZONTAL,
        PIE,
        DONUT,
        LINE,
        GAUGE,
        KPI,
        TABLE,
        MAP,
        SCATTER
    }

    public enum StatisticType {
        NONE,
        COUNT,
        COUNT_DISTINCT,
        SUM,
        AVG,
        MIN,
        MAX
    }

    public enum DashboardLogicalOperator {
        AND,
        OR
    }
}

package org.labs.genesis.dashboard.query;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.labs.genesis.dashboard.model.DashboardEnums.DashboardFieldRole;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DashboardQuerySelection {
    private String key;
    private String columnName;
    private String alias;
    private DashboardFieldRole role;
    private DashboardStatisticExpression statisticExpression;
    private CoordinateAxis coordinateAxis;
    private String nativeColumnType;

    public DashboardQuerySelection(String key, String columnName, String alias,
                                   DashboardFieldRole role,
                                   DashboardStatisticExpression statisticExpression) {
        this(key, columnName, alias, role, statisticExpression, null, null);
    }

    public enum CoordinateAxis {
        LATITUDE,
        LONGITUDE
    }
    public boolean isAggregated() {
        return statisticExpression != null;
    }
}

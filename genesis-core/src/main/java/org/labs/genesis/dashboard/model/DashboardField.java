package org.labs.genesis.dashboard.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.labs.genesis.dashboard.model.DashboardEnums.DashboardFieldRole;
import org.labs.genesis.dashboard.model.DashboardEnums.StatisticType;

@Getter
@Setter
@NoArgsConstructor
public class DashboardField {
    private String key;
    private String columnName;
    private DashboardFieldRole role;
    private StatisticType statistic;

    public DashboardField(String key, String columnName, DashboardFieldRole role) {
        this.key = key;
        this.columnName = columnName;
        this.role = role;
    }
}

package org.labs.genesis.dashboard.model;

import org.labs.genesis.dashboard.model.DashboardEnums.DashboardFieldRole;
import org.labs.genesis.dashboard.model.DashboardEnums.DashboardSortDirection;
import org.labs.genesis.dashboard.model.DashboardEnums.StatisticType;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DashboardField {
    private String key;
    private String columnName;
    private DashboardFieldRole role;
    private StatisticType statistic;
    private Integer limit;
    private DashboardSortDirection sortDirection;
    private String filter;

    public DashboardField(String key, String columnName, DashboardFieldRole role) {
        this.key = key;
        this.columnName = columnName;
        this.role = role;
    }
}

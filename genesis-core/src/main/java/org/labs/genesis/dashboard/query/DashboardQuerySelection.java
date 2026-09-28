package org.labs.genesis.dashboard.query;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.labs.genesis.dashboard.model.DashboardEnums.DashboardFieldRole;
import org.labs.genesis.dashboard.model.DashboardEnums.StatisticType;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DashboardQuerySelection {
    private String key;
    private String columnName;
    private String alias;
    private DashboardFieldRole role;
    private StatisticType statistic;
    public boolean isAggregated() {
        return statistic != null;
    }
}
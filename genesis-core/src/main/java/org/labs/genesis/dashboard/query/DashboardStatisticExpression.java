package org.labs.genesis.dashboard.query;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.labs.genesis.dashboard.model.DashboardEnums.StatisticType;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatisticExpression {
    private StatisticType statistic;
    private String columnName;
    private String alias;
    private boolean distinct;
    public boolean targetsRows() {
        return columnName == null || columnName.isBlank();
    }
}
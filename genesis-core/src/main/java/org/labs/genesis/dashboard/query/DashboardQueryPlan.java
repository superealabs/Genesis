package org.labs.genesis.dashboard.query;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.labs.genesis.dashboard.model.DashboardDataSource;
import org.labs.genesis.dashboard.model.DashboardQueryOptions;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class DashboardQueryPlan {
    private DashboardDataSource source;
    private List<DashboardQuerySelection> selections = new ArrayList<>();
    private List<String> groupBy = new ArrayList<>();
    private DashboardQueryOptions options = new DashboardQueryOptions();
    private DashboardStatisticExpression rowStatistic;
    private List<DashboardJoin> joins = new ArrayList<>();
}
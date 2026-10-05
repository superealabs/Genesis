package org.labs.genesis.dashboard.query;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.labs.genesis.dashboard.model.DashboardEnums.DashboardJoinType;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class DashboardJoin {
    private String table;
    private DashboardJoinType type = DashboardJoinType.INNER;
    private List<DashboardJoinCondition> conditions = new ArrayList<>();
}
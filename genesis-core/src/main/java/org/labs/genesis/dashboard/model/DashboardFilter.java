package org.labs.genesis.dashboard.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.labs.genesis.dashboard.model.DashboardEnums.DashboardLogicalOperator;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DashboardFilter {
    private String columnName;
    private String operator;
    private Object value;
    private DashboardLogicalOperator relationToPrevious = DashboardLogicalOperator.AND;
}

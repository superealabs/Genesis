package org.labs.genesis.dashboard.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.labs.genesis.dashboard.model.DashboardEnums.DashboardLogicalOperator;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class DashboardFilterGroup implements DashboardFilterNode {
    private DashboardLogicalOperator operator = DashboardLogicalOperator.AND;
    private DashboardLogicalOperator relationToPrevious = DashboardLogicalOperator.AND;
    private List<DashboardFilterNode> children = new ArrayList<>();

    public void addChild(DashboardFilterNode child) {
        if (child != null) {
            children.add(child);
        }
    }
}
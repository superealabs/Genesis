package org.labs.genesis.dashboard.query;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DashboardJoinCondition {
    private String leftTable;
    private String leftColumn;
    private String rightTable;
    private String rightColumn;
}
package org.labs.genesis.dashboard.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.labs.genesis.dashboard.model.DashboardEnums.DashboardSortDirection;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSort {
    private String columnName;
    private DashboardSortDirection direction;
}
package org.labs.genesis.dashboard.model;

import org.labs.genesis.dashboard.model.DashboardEnums.DashboardSourceType;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DashboardDataSource {
    private String name;
    private DashboardSourceType type;
}

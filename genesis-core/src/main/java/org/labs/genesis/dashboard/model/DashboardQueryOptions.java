package org.labs.genesis.dashboard.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class DashboardQueryOptions {
    private Integer limit;
    private List<DashboardSort> sorts = new ArrayList<>();
    private List<DashboardFilterNode> filters = new ArrayList<>();
}
package org.labs.genesis.dashboard.model;

import org.labs.genesis.dashboard.model.DashboardEnums.DashboardVisualizationType;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public class DashboardVisualization {
    private String id;
    private String title;
    private DashboardVisualizationType type;
    private DashboardDataSource dataSource;
    private DashboardLayout layout;
    private List<DashboardField> fields = new ArrayList<>();
    private DashboardQueryOptions queryOptions = new DashboardQueryOptions();
    private Map<String, Object> options = new HashMap<>();
}

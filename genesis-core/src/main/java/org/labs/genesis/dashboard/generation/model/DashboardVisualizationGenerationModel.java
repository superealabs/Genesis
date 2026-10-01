package org.labs.genesis.dashboard.generation.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.labs.genesis.dashboard.model.DashboardEnums.DashboardVisualizationType;
import org.labs.genesis.dashboard.model.DashboardField;
import org.labs.genesis.dashboard.model.DashboardLayout;
import org.labs.genesis.dashboard.query.DashboardQueryPlan;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public class DashboardVisualizationGenerationModel {
    private String id;
    private String title;
    private DashboardVisualizationType type;
    private DashboardLayout layout;
    private List<DashboardField> fields = new ArrayList<>();
    private DashboardQueryPlan queryPlan;
    private Map<String, Object> options = new HashMap<>();
}
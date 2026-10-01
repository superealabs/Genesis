package org.labs.genesis.dashboard.generation.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class DashboardPageGenerationModel {
    private String id;
    private String title;
    private List<DashboardVisualizationGenerationModel> visualizations = new ArrayList<>();
    public boolean isEmpty() {
        return visualizations == null || visualizations.isEmpty();
    }
}
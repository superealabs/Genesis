package org.labs.genesis.dashboard.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class DashboardPage {
    private String id;
    private String title;
    private List<DashboardVisualization> visualizations = new ArrayList<>();
}
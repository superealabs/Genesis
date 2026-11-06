package org.labs.genesis.dashboard.generation.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class DashboardGenerationModel {
    private boolean enabled = true;
    private List<DashboardPageGenerationModel> pages = new ArrayList<>();

    public boolean isEmpty() {
        return pages == null || pages.isEmpty();
    }
}
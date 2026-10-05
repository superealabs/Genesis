package org.labs.genesis.dashboard.generation;

import org.labs.genesis.config.ProjectGenerationContext;
import org.labs.genesis.dashboard.generation.model.DashboardGenerationModel;

public interface DashboardGenerator {
    void generate(DashboardGenerationModel dashboard, ProjectGenerationContext context);
}
package org.labs.genesis.dashboard.rules;

import org.labs.genesis.dashboard.model.DashboardConfiguration;

public final class DashboardConfigurationValidator {
    private DashboardConfigurationValidator() {
    }

    public static boolean isValid(DashboardConfiguration configuration) {
        if (configuration == null) {
            return false;
        }

        if (!configuration.isEnabled()) {
            return true;
        }

        if (configuration.getPages() == null) {
            return false;
        }

        return configuration.getPages()
                .stream()
                .filter(page -> page != null)
                .allMatch(page -> page.getVisualizations() != null && page.getVisualizations()
                                .stream()
                                .allMatch(DashboardVisualizationRules::isValid)
                );
    }
}
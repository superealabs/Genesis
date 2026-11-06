package org.labs.genesis.dashboard.rules;

import org.labs.genesis.dashboard.model.DashboardEnums.DashboardFieldRole;
import org.labs.genesis.dashboard.model.DashboardEnums.DashboardCoordinateMode;
import org.labs.genesis.dashboard.model.DashboardVisualization;

public final class DashboardVisualizationRules {
    private DashboardVisualizationRules() {
    }

    public static boolean isValid(
            DashboardVisualization visualization
    ) {
        if (visualization == null
                || visualization.getType() == null
                || visualization.getDataSource() == null) {

            return false;
        }

        boolean structureValid =
                switch (visualization.getType()) {
                    case BAR_VERTICAL,
                         BAR_HORIZONTAL,
                         PIE,
                         DONUT,
                         LINE ->
                            hasRole(visualization, DashboardFieldRole.DIMENSION, 1) && hasRole(visualization, DashboardFieldRole.MEASURE, 1);

                    case KPI ->
                            hasRole(visualization, DashboardFieldRole.MEASURE, 1);

                    case TABLE ->
                            hasRole(visualization, DashboardFieldRole.COLUMN, 1);

                    case MAP -> mapCoordinatesValid(visualization);

                    case SCATTER ->
                            hasRole(visualization, DashboardFieldRole.VALUE, 2);
                };

        return structureValid && hasValidMeasures(
                visualization
        );
    }

    private static boolean mapCoordinatesValid(DashboardVisualization visualization) {
        if (visualization.getCoordinateMode() == DashboardCoordinateMode.GEOGRAPHY) {
            return hasRole(visualization, DashboardFieldRole.GEOGRAPHY, 1);
        }
        return hasRole(visualization, DashboardFieldRole.LATITUDE, 1)
                && hasRole(visualization, DashboardFieldRole.LONGITUDE, 1);
    }

    public static boolean hasRole(DashboardVisualization visualization, DashboardFieldRole role, int minimum) {
        if (visualization.getFields() == null) {
            return false;
        }

        long count = visualization.getFields()
                        .stream()
                        .filter(field -> field != null && field.getRole() == role)
                        .count();
        return count >= minimum;
    }

    public static boolean hasValidMeasures(DashboardVisualization visualization) {
        if (visualization == null || visualization.getFields() == null) {
            return false;
        }

        return visualization.getFields()
                .stream()
                .filter(field -> field != null && field.getRole() == DashboardFieldRole.MEASURE)
                .allMatch(field ->
                        field.getStatistic() != null
                );
    }
}

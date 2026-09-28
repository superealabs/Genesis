package org.labs.genesis.dashboard.query;

import org.labs.genesis.dashboard.model.DashboardField;
import org.labs.genesis.dashboard.model.DashboardVisualization;
import org.labs.genesis.dashboard.rules.DashboardVisualizationRules;
import org.labs.genesis.dashboard.rules.DashboardMetadataValidator;
import org.labs.genesis.connexion.model.TableMetadata;

import java.util.List;

public final class DashboardQueryBuilder {
    private DashboardQueryBuilder() {
    }

    public static DashboardQueryPlan build(DashboardVisualization visualization) {
        validate(visualization);
        return createPlan(visualization);
    }

    private static DashboardQueryPlan createPlan(DashboardVisualization visualization) {
        DashboardQueryPlan plan = new DashboardQueryPlan();
        plan.setSource(visualization.getDataSource());
        plan.setOptions(visualization.getQueryOptions());
        for (DashboardField field : visualization.getFields()) {
            plan.getSelections().add(createSelection(field));
        }
        buildGroupBy(plan);

        return plan;
    }

    private static DashboardQuerySelection createSelection(DashboardField field) {
        return new DashboardQuerySelection(
                field.getKey(),
                field.getColumnName(),
                field.getKey(),
                field.getRole(),
                field.getStatistic()
        );
    }

    private static void buildGroupBy(DashboardQueryPlan plan) {
        boolean hasAggregation =
                plan.getSelections()
                        .stream()
                        .anyMatch(DashboardQuerySelection::isAggregated);

        if (!hasAggregation) {
            return;
        }

        List<String> groupBy = plan.getSelections()
                        .stream()
                        .filter(selection -> !selection.isAggregated())
                        .map(DashboardQuerySelection::getColumnName)
                        .distinct()
                        .toList();

        plan.getGroupBy().addAll(groupBy);
    }

    private static void validate(DashboardVisualization visualization) {
        if (!DashboardVisualizationRules.isValid(visualization)) {
            throw new IllegalArgumentException("Invalid dashboard visualization");
        }
    }

    public static DashboardQueryPlan build(DashboardVisualization visualization, TableMetadata table) {
        validate(visualization);

        if (!DashboardMetadataValidator.isValid(visualization, table)) {
            throw new IllegalArgumentException("Invalid dashboard metadata");
        }

        return createPlan(visualization);
    }
}
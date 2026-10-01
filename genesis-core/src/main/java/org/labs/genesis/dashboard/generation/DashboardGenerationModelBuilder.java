package org.labs.genesis.dashboard.generation;

import org.labs.genesis.connexion.model.TableMetadata;
import org.labs.genesis.dashboard.generation.model.DashboardGenerationModel;
import org.labs.genesis.dashboard.generation.model.DashboardPageGenerationModel;
import org.labs.genesis.dashboard.generation.model.DashboardVisualizationGenerationModel;
import org.labs.genesis.dashboard.model.DashboardConfiguration;
import org.labs.genesis.dashboard.model.DashboardPage;
import org.labs.genesis.dashboard.model.DashboardVisualization;
import org.labs.genesis.dashboard.query.DashboardQueryBuilder;
import org.labs.genesis.dashboard.query.DashboardQueryPlan;
import org.labs.genesis.dashboard.rules.DashboardConfigurationValidator;

import java.util.List;

public final class DashboardGenerationModelBuilder {

    private DashboardGenerationModelBuilder() {
    }

    public static DashboardGenerationModel build(DashboardConfiguration configuration, List<TableMetadata> availableTables) {
        validateInputs(configuration, availableTables);
        DashboardGenerationModel result = new DashboardGenerationModel();
        result.setEnabled(configuration.isEnabled());
        if (!configuration.isEnabled()) {
            return result;
        }
        for (DashboardPage page : configuration.getPages()) {
            result.getPages()
                    .add(buildPage(page, availableTables));
        }
        return result;
    }

    private static DashboardPageGenerationModel buildPage(DashboardPage page, List<TableMetadata> availableTables) {
        DashboardPageGenerationModel result = new DashboardPageGenerationModel();
        result.setId(page.getId());
        result.setTitle(page.getTitle());
        for (DashboardVisualization visualization : page.getVisualizations()) {
            result.getVisualizations()
                    .add(buildVisualization(visualization, availableTables));
        }

        return result;
    }

    private static DashboardVisualizationGenerationModel buildVisualization(DashboardVisualization visualization, List<TableMetadata> availableTables) {
        TableMetadata sourceTable = findSourceTable(visualization, availableTables);
        DashboardQueryPlan queryPlan = DashboardQueryBuilder.build(visualization, sourceTable, availableTables);
        DashboardVisualizationGenerationModel result = new DashboardVisualizationGenerationModel();
        result.setId(visualization.getId());
        result.setTitle(visualization.getTitle());
        result.setType(visualization.getType());
        result.setLayout(visualization.getLayout());
        if (visualization.getFields() != null) {
            result.getFields().addAll(visualization.getFields());
        }
        if (visualization.getOptions() != null) {
            result.getOptions().putAll(visualization.getOptions());
        }
        result.setQueryPlan(queryPlan);
        return result;
    }

    private static TableMetadata findSourceTable(DashboardVisualization visualization, List<TableMetadata> availableTables) {
        if (visualization.getDataSource() == null
                || visualization.getDataSource().getName() == null
                || visualization.getDataSource().getName().isBlank()) {
            throw new IllegalArgumentException("Dashboard visualization has no data source: " + visualization.getId());
        }
        String sourceName = visualization.getDataSource().getName();
        return availableTables.stream()
                .filter(table ->
                        table != null
                                && table.getTableName() != null
                                && sourceName.equalsIgnoreCase(table.getTableName())
                )
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Dashboard source not found: " + sourceName));
    }

    private static void validateInputs(DashboardConfiguration configuration, List<TableMetadata> availableTables) {
        if (configuration == null) {
            throw new IllegalArgumentException("Dashboard configuration cannot be null");
        }

        if (availableTables == null) {
            throw new IllegalArgumentException("Dashboard metadata cannot be null");
        }

        if (!DashboardConfigurationValidator.isValid(configuration)) {
            throw new IllegalArgumentException("Invalid dashboard configuration");
        }
    }
}
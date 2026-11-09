package dashboard;

import org.junit.jupiter.api.Test;
import org.labs.genesis.dashboard.generation.model.DashboardGenerationModel;
import org.labs.genesis.dashboard.generation.model.DashboardPageGenerationModel;
import org.labs.genesis.dashboard.generation.model.DashboardVisualizationGenerationModel;
import org.labs.genesis.dashboard.model.DashboardDataSource;
import org.labs.genesis.dashboard.model.DashboardEnums.DashboardSourceType;
import org.labs.genesis.dashboard.query.DashboardQueryPlan;
import org.labs.genesis.dashboard.query.DashboardQuerySelection;
import org.labs.genesis.dashboard.technology.springapi.metadata.SpringApiDashboardMetadataProvider;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class SpringApiDashboardMetadataProviderTest {

    @Test
    void keepsLimitOutOfSqlForJpaDialectHandling() {
        DashboardQueryPlan queryPlan = new DashboardQueryPlan();
        queryPlan.setSource(new DashboardDataSource("orders", DashboardSourceType.TABLE));
        queryPlan.getSelections().add(selection("orders.id", "id"));
        queryPlan.getOptions().setLimit(25);

        DashboardVisualizationGenerationModel visualization = new DashboardVisualizationGenerationModel();
        visualization.setTitle("Orders");
        visualization.setQueryPlan(queryPlan);

        DashboardPageGenerationModel page = new DashboardPageGenerationModel();
        page.setTitle("Main");
        page.getVisualizations().add(visualization);

        DashboardGenerationModel dashboard = new DashboardGenerationModel();
        dashboard.getPages().add(page);

        HashMap<String, Object> metadata =
                SpringApiDashboardMetadataProvider.getDashboardHashMap(dashboard);
        List<?> pages = (List<?>) metadata.get("dashboardPages");
        Map<?, ?> pageMetadata = (Map<?, ?>) pages.get(0);
        List<?> visualizations = (List<?>) pageMetadata.get("visualizations");
        Map<?, ?> visualizationMetadata = (Map<?, ?>) visualizations.get(0);

        assertEquals("\"SELECT orders.id AS id FROM orders\"", visualizationMetadata.get("sqlJava"));
        assertEquals("25", visualizationMetadata.get("limitJava"));
        assertFalse(((String) visualizationMetadata.get("sqlJava")).contains("LIMIT"));
    }

    private static DashboardQuerySelection selection(String columnName, String alias) {
        DashboardQuerySelection selection = new DashboardQuerySelection();
        selection.setColumnName(columnName);
        selection.setAlias(alias);
        return selection;
    }
}

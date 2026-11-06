package dashboard;

import org.junit.jupiter.api.Test;
import org.labs.genesis.dashboard.generation.model.DashboardGenerationModel;
import org.labs.genesis.dashboard.generation.model.DashboardPageGenerationModel;
import org.labs.genesis.dashboard.generation.model.DashboardVisualizationGenerationModel;
import org.labs.genesis.dashboard.model.DashboardDataSource;
import org.labs.genesis.dashboard.model.DashboardEnums.DashboardSourceType;
import org.labs.genesis.dashboard.model.DashboardFilter;
import org.labs.genesis.dashboard.query.DashboardQueryPlan;
import org.labs.genesis.dashboard.query.DashboardQuerySelection;
import org.labs.genesis.dashboard.technology.dotnetapi.metadata.DotNetApiDashboardMetadataProvider;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DotNetApiDashboardMetadataProviderTest {

    @Test
    void buildsParameterizedDashboardPageMetadata() {
        DashboardQueryPlan queryPlan = new DashboardQueryPlan();
        queryPlan.setSource(new DashboardDataSource("orders", DashboardSourceType.TABLE));
        queryPlan.getSelections().add(selection("orders.status", "status"));
        queryPlan.getOptions().getFilters().add(new DashboardFilter("orders.status", "is", "open", null));
        queryPlan.getOptions().setLimit(20);

        DashboardVisualizationGenerationModel visualization = new DashboardVisualizationGenerationModel();
        visualization.setTitle("Orders by status");
        visualization.setQueryPlan(queryPlan);

        DashboardPageGenerationModel page = new DashboardPageGenerationModel();
        page.setTitle("Sales Overview");
        page.getVisualizations().add(visualization);

        DashboardGenerationModel dashboard = new DashboardGenerationModel();
        dashboard.getPages().add(page);

        HashMap<String, Object> metadata =
                DotNetApiDashboardMetadataProvider.getDashboardHashMap(dashboard);
        List<?> pages = (List<?>) metadata.get("dashboardPages");
        Map<?, ?> pageMetadata = (Map<?, ?>) pages.get(0);
        List<?> visualizations = (List<?>) pageMetadata.get("visualizations");
        Map<?, ?> visualizationMetadata = (Map<?, ?>) visualizations.get(0);

        assertEquals("sales-overview", pageMetadata.get("slug"));
        assertEquals("LoadPageSalesOverview", pageMetadata.get("methodName"));
        assertEquals("orders-by-status", visualizationMetadata.get("graphSlug"));
        assertEquals(
                "\"SELECT orders.status AS status FROM orders WHERE orders.status = @p0\"",
                visualizationMetadata.get("sqlCs")
        );
        assertEquals("new object?[] { \"open\" }", visualizationMetadata.get("parametersCs"));
        assertEquals("new string[] { \"status\" }", visualizationMetadata.get("aliasesCs"));
        assertEquals("20", visualizationMetadata.get("limitCs"));
        assertEquals("@", visualizationMetadata.get("parameterPrefix"));
    }

    @Test
    void usesOracleParameterSyntax() {
        DashboardQueryPlan queryPlan = new DashboardQueryPlan();
        queryPlan.setSource(new DashboardDataSource("orders", DashboardSourceType.TABLE));
        queryPlan.setDatabaseName("Oracle");
        queryPlan.getSelections().add(selection("orders.id", "id"));
        queryPlan.getOptions().getFilters().add(new DashboardFilter("orders.id", "is", 4, null));

        DashboardVisualizationGenerationModel visualization = new DashboardVisualizationGenerationModel();
        visualization.setTitle("Oracle Orders");
        visualization.setQueryPlan(queryPlan);

        DashboardPageGenerationModel page = new DashboardPageGenerationModel();
        page.setTitle("Oracle");
        page.getVisualizations().add(visualization);

        DashboardGenerationModel dashboard = new DashboardGenerationModel();
        dashboard.getPages().add(page);

        List<?> pages = (List<?>) DotNetApiDashboardMetadataProvider
                .getDashboardHashMap(dashboard)
                .get("dashboardPages");
        Map<?, ?> pageMetadata = (Map<?, ?>) pages.get(0);
        List<?> visualizations = (List<?>) pageMetadata.get("visualizations");
        Map<?, ?> visualizationMetadata = (Map<?, ?>) visualizations.get(0);

        assertEquals(
                "\"SELECT orders.id AS id FROM orders WHERE orders.id = :p0\"",
                visualizationMetadata.get("sqlCs")
        );
        assertEquals(":", visualizationMetadata.get("parameterPrefix"));
    }

    private static DashboardQuerySelection selection(String columnName, String alias) {
        DashboardQuerySelection selection = new DashboardQuerySelection();
        selection.setColumnName(columnName);
        selection.setAlias(alias);
        return selection;
    }
}

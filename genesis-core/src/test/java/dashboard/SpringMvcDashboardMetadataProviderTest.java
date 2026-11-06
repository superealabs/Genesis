package dashboard;

import org.junit.jupiter.api.Test;
import org.labs.genesis.dashboard.generation.model.DashboardGenerationModel;
import org.labs.genesis.dashboard.generation.model.DashboardPageGenerationModel;
import org.labs.genesis.dashboard.generation.model.DashboardVisualizationGenerationModel;
import org.labs.genesis.dashboard.model.DashboardDataSource;
import org.labs.genesis.dashboard.model.DashboardField;
import org.labs.genesis.dashboard.model.DashboardLayout;
import org.labs.genesis.dashboard.model.DashboardEnums.DashboardFieldRole;
import org.labs.genesis.dashboard.model.DashboardEnums.DashboardSourceType;
import org.labs.genesis.dashboard.model.DashboardEnums.DashboardVisualizationType;
import org.labs.genesis.dashboard.model.DashboardEnums.StatisticType;
import org.labs.genesis.dashboard.query.DashboardQueryPlan;
import org.labs.genesis.dashboard.query.DashboardQuerySelection;
import org.labs.genesis.dashboard.query.DashboardStatisticExpression;
import org.labs.genesis.dashboard.technology.springmvc.metadata.SpringMvcDashboardMetadataProvider;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SpringMvcDashboardMetadataProviderTest {

    @Test
    void disabledDashboardProducesDisabledMetadata() {

        DashboardGenerationModel dashboard =
                new DashboardGenerationModel();

        dashboard.setEnabled(false);

        HashMap<String, Object> metadata =
                SpringMvcDashboardMetadataProvider
                        .getDashboardHashMap(
                                dashboard
                        );

        assertFalse(
                (Boolean) metadata.get(
                        "dashboardEnabled"
                )
        );

        assertFalse(
                (Boolean) metadata.get(
                        "hasDashboardPages"
                )
        );

        assertFalse(
                (Boolean) metadata.get(
                        "hasDashboardVisualizations"
                )
        );

        assertTrue(
                ((List<?>) metadata.get(
                        "dashboardPages"
                )).isEmpty()
        );

        assertTrue(
                ((List<?>) metadata.get(
                        "dashboardQueries"
                )).isEmpty()
        );
    }

    @Test
    void buildsDashboardMetadata() {

        DashboardGenerationModel dashboard =
                createDashboard();

        HashMap<String, Object> metadata =
                SpringMvcDashboardMetadataProvider
                        .getDashboardHashMap(
                                dashboard
                        );

        assertTrue(
                (Boolean) metadata.get(
                        "dashboardEnabled"
                )
        );

        assertTrue(
                (Boolean) metadata.get(
                        "hasDashboardPages"
                )
        );

        assertTrue(
                (Boolean) metadata.get(
                        "hasDashboardVisualizations"
                )
        );

        List<?> pages =
                (List<?>) metadata.get(
                        "dashboardPages"
                );

        assertEquals(
                1,
                pages.size()
        );

        Map<?, ?> page =
                (Map<?, ?>) pages.get(0);

        assertEquals(
                "main",
                page.get("id")
        );

        assertEquals(
                "Dashboard principal",
                page.get("title")
        );

        List<?> visualizations =
                (List<?>) page.get(
                        "visualizations"
                );

        assertEquals(
                1,
                visualizations.size()
        );

        Map<?, ?> visualization =
                (Map<?, ?>) visualizations.get(0);

        assertEquals(
                "sales-by-status",
                visualization.get("id")
        );

        assertEquals(
                "Montant par statut",
                visualization.get("title")
        );

        assertEquals(
                "BAR_VERTICAL",
                visualization.get("type")
        );

        assertEquals(
                6,
                visualization.get("layoutWidth")
        );

        assertEquals(
                4,
                visualization.get("layoutHeight")
        );

        List<?> queries =
                (List<?>) metadata.get(
                        "dashboardQueries"
                );

        assertEquals(
                1,
                queries.size()
        );

        Map<?, ?> query =
                (Map<?, ?>) queries.get(0);

        assertEquals(
                "sales-by-status",
                query.get("id")
        );

        assertNotNull(
                query.get("sql")
        );

        assertNotNull(
                query.get("sqlJava")
        );

        assertEquals(
                "java.util.Arrays.asList(\"xAxis\", \"yAxis\")",
                query.get("aliasesJava")
        );

        assertEquals(
                "java.util.Arrays.asList()",
                query.get("parametersJava")
        );

        assertEquals(
                "null",
                query.get("limitJava")
        );
    }

    private DashboardGenerationModel createDashboard() {

        DashboardGenerationModel dashboard =
                new DashboardGenerationModel();

        dashboard.setEnabled(true);

        DashboardPageGenerationModel page =
                new DashboardPageGenerationModel();

        page.setId(
                "main"
        );

        page.setTitle(
                "Dashboard principal"
        );

        DashboardVisualizationGenerationModel visualization =
                new DashboardVisualizationGenerationModel();

        visualization.setId(
                "sales-by-status"
        );

        visualization.setTitle(
                "Montant par statut"
        );

        visualization.setType(
                DashboardVisualizationType.BAR_VERTICAL
        );

        visualization.setLayout(
                new DashboardLayout(
                        0,
                        0,
                        6,
                        4
                )
        );

        DashboardField dimension =
                new DashboardField(
                        "xAxis",
                        "commande.statut",
                        DashboardFieldRole.DIMENSION
                );

        DashboardField measure =
                new DashboardField(
                        "yAxis",
                        "commande.montant",
                        DashboardFieldRole.MEASURE
                );

        measure.setStatistic(
                StatisticType.SUM
        );

        visualization.getFields()
                .add(dimension);

        visualization.getFields()
                .add(measure);

        visualization.setQueryPlan(
                createQueryPlan()
        );

        page.getVisualizations()
                .add(
                        visualization
                );

        dashboard.getPages()
                .add(
                        page
                );

        return dashboard;
    }

    private DashboardQueryPlan createQueryPlan() {

        DashboardQueryPlan plan =
                new DashboardQueryPlan();

        plan.setSource(
                new DashboardDataSource(
                        "commande",
                        DashboardSourceType.TABLE
                )
        );

        DashboardQuerySelection dimension =
                new DashboardQuerySelection();

        dimension.setKey(
                "xAxis"
        );

        dimension.setColumnName(
                "commande.statut"
        );

        dimension.setAlias(
                "xAxis"
        );

        dimension.setRole(
                DashboardFieldRole.DIMENSION
        );

        DashboardStatisticExpression statistic =
                new DashboardStatisticExpression();

        statistic.setStatistic(
                StatisticType.SUM
        );

        statistic.setColumnName(
                "commande.montant"
        );

        statistic.setAlias(
                "yAxis"
        );

        DashboardQuerySelection measure =
                new DashboardQuerySelection();

        measure.setKey(
                "yAxis"
        );

        measure.setColumnName(
                "commande.montant"
        );

        measure.setAlias(
                "yAxis"
        );

        measure.setRole(
                DashboardFieldRole.MEASURE
        );

        measure.setStatisticExpression(
                statistic
        );

        plan.getSelections()
                .add(dimension);

        plan.getSelections()
                .add(measure);

        plan.getGroupBy()
                .add(
                        "commande.statut"
                );

        return plan;
    }

    @Test
    void buildsDashboardPagesJavaMetadata() {

        DashboardGenerationModel dashboard =
                createDashboard();

        HashMap<String, Object> metadata =
                SpringMvcDashboardMetadataProvider
                        .getDashboardHashMap(
                                dashboard
                        );

        String pagesJava =
                (String) metadata.get(
                        "dashboardPagesJava"
                );

        assertNotNull(
                pagesJava
        );

        assertTrue(
                pagesJava.contains(
                        "\"main\""
                )
        );

        assertTrue(
                pagesJava.contains(
                        "\"Dashboard principal\""
                )
        );

        assertTrue(
                pagesJava.startsWith(
                        "java.util.List.of("
                )
        );
    }
}
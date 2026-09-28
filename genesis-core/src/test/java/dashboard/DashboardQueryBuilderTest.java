package dashboard;

import org.junit.jupiter.api.Test;
import org.labs.genesis.dashboard.model.*;
import org.labs.genesis.dashboard.query.DashboardQueryPlan;
import org.labs.genesis.dashboard.query.DashboardQueryBuilder;

import static org.labs.genesis.dashboard.model.DashboardEnums.*;
import static org.junit.jupiter.api.Assertions.*;

class DashboardQueryBuilderTest {

    @Test
    void shouldCreateGroupedQueryPlan() {
        DashboardVisualization chart = new DashboardVisualization();
        chart.setType(DashboardVisualizationType.BAR_VERTICAL);
        chart.setDataSource(new DashboardDataSource("vente", DashboardSourceType.TABLE));
        DashboardField produit = new DashboardField("xAxis", "produit", DashboardFieldRole.DIMENSION);
        DashboardField montant = new DashboardField("yAxis", "montant",DashboardFieldRole.MEASURE);
        montant.setStatistic(StatisticType.SUM);
        chart.getFields().add(produit);
        chart.getFields().add(montant);

        DashboardQueryPlan plan = DashboardQueryBuilder.build(chart);
        assertEquals(2, plan.getSelections().size());
        assertEquals(1, plan.getGroupBy().size());
        assertEquals("produit", plan.getGroupBy().get(0));
        assertEquals(StatisticType.SUM, plan.getSelections().get(1).getStatisticExpression().getStatistic());
    }

    @Test
    void shouldCreateRowCountPlan() {
        DashboardDataSource source = new DashboardDataSource("utilisateur", DashboardSourceType.TABLE);
        DashboardQueryPlan plan = DashboardQueryBuilder.buildRowCount(source, "totalUtilisateurs");
        assertNotNull(plan.getRowStatistic());
        assertEquals(StatisticType.COUNT, plan.getRowStatistic().getStatistic());
        assertTrue(plan.getRowStatistic().targetsRows());
    }
}
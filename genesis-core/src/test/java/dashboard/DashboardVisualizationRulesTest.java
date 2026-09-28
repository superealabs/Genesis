package dashboard;

import org.junit.jupiter.api.Test;
import org.labs.genesis.dashboard.model.*;
import org.labs.genesis.dashboard.rules.DashboardVisualizationRules;
import org.labs.genesis.dashboard.model.DashboardEnums.*;
import static org.junit.jupiter.api.Assertions.*;

class DashboardVisualizationRulesTest {

    @Test
    void barChartRequiresDimensionAndMeasure() {
        DashboardVisualization chart = new DashboardVisualization();

        chart.setType(DashboardVisualizationType.BAR_VERTICAL);
        chart.setDataSource(new DashboardDataSource("vente", DashboardSourceType.TABLE));
        DashboardField dimension = new DashboardField("xAxis", "produit", DashboardFieldRole.DIMENSION);
        DashboardField measure = new DashboardField("yAxis", "montant", DashboardFieldRole.MEASURE);
        measure.setStatistic(StatisticType.SUM);
        chart.getFields().add(dimension);
        chart.getFields().add(measure);
        assertTrue(DashboardVisualizationRules.isValid(chart));
    }

    @Test
    void measureRequiresStatistic() {
        DashboardVisualization chart = new DashboardVisualization();
        chart.setType(DashboardVisualizationType.KPI);
        chart.setDataSource(new DashboardDataSource("vente", DashboardSourceType.TABLE));
        DashboardField measure = new DashboardField("value", "montant", DashboardFieldRole.MEASURE);
        chart.getFields().add(measure);
        assertFalse(DashboardVisualizationRules.isValid(chart));
    }

    @Test
    void scatterRequiresTwoValues() {
        DashboardVisualization chart = new DashboardVisualization();
        chart.setType(DashboardVisualizationType.SCATTER);
        chart.setDataSource(new DashboardDataSource("produit",DashboardSourceType.TABLE));
        chart.getFields().add(new DashboardField("xAxis", "prix", DashboardFieldRole.VALUE));
        assertFalse(DashboardVisualizationRules.isValid(chart));
        chart.getFields().add(new DashboardField("yAxis", "quantite", DashboardFieldRole.VALUE));
        assertTrue(DashboardVisualizationRules.isValid(chart));
    }
}
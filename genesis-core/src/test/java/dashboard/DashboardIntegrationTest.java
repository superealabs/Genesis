package dashboard;

import org.junit.jupiter.api.Test;
import org.labs.genesis.connexion.model.ColumnMetadata;
import org.labs.genesis.connexion.model.TableMetadata;
import org.labs.genesis.dashboard.model.*;
import org.labs.genesis.dashboard.query.DashboardQueryBuilder;
import org.labs.genesis.dashboard.query.DashboardQueryPlan;

import static org.junit.jupiter.api.Assertions.*;
import static org.labs.genesis.dashboard.model.DashboardEnums.*;

class DashboardIntegrationTest {

    @Test
    void shouldBuildDashboardQueryFromGenesisMetadata() {
        ColumnMetadata produit = new ColumnMetadata();
        produit.setName("produit");
        produit.setReferencedColumn("produit");
        produit.setText(true);
        ColumnMetadata montant = new ColumnMetadata();
        montant.setName("montant");
        montant.setReferencedColumn("montant");
        montant.setNumeric(true);
        TableMetadata vente = new TableMetadata();
        vente.setTableName("vente");
        vente.setColumns(new ColumnMetadata[]{produit, montant});
        DashboardVisualization chart = new DashboardVisualization();

        chart.setId("chart_1");
        chart.setTitle("Ventes par produit");
        chart.setType(DashboardVisualizationType.BAR_VERTICAL);

        chart.setDataSource(new DashboardDataSource("vente", DashboardSourceType.TABLE));

        DashboardField dimension = new DashboardField("xAxis", "produit", DashboardFieldRole.DIMENSION);
        DashboardField measure = new DashboardField("yAxis", "montant", DashboardFieldRole.MEASURE);

        measure.setStatistic(StatisticType.SUM);

        chart.getFields().add(dimension);
        chart.getFields().add(measure);

        DashboardQueryPlan plan = DashboardQueryBuilder.build(chart, vente);
        assertEquals("vente", plan.getSource().getName());
        assertEquals(2, plan.getSelections().size());
        assertEquals("produit", plan.getGroupBy().get(0));
        assertEquals(StatisticType.SUM, plan.getSelections()
                        .get(1)
                        .getStatisticExpression()
                        .getStatistic()
        );
        assertEquals("montant", plan.getSelections()
                        .get(1)
                        .getStatisticExpression()
                        .getColumnName()
        );
    }
}
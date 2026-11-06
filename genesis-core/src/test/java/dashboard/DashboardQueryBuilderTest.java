package dashboard;

import org.junit.jupiter.api.Test;
import org.labs.genesis.dashboard.model.*;
import org.labs.genesis.dashboard.query.DashboardQueryPlan;
import org.labs.genesis.dashboard.query.DashboardQueryBuilder;
import org.labs.genesis.dashboard.query.DashboardQuerySelection;

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

        DashboardQueryPlan plan = DashboardQueryBuilder.build(chart, table("vente"), java.util.List.of(table("vente")));
        assertEquals(2, plan.getSelections().size());
        assertEquals(1, plan.getGroupBy().size());
        assertEquals("produit", plan.getGroupBy().get(0));
        assertEquals(StatisticType.SUM, plan.getSelections().get(1).getStatisticExpression().getStatistic());
    }

    @Test
    void shouldExpandGeographicMapColumnIntoNormalizedCoordinates() {
        DashboardVisualization map = new DashboardVisualization();
        map.setType(DashboardVisualizationType.MAP);
        map.setCoordinateMode(DashboardCoordinateMode.GEOGRAPHY);
        map.setDataSource(new DashboardDataSource("places", DashboardSourceType.TABLE));
        map.getFields().add(new DashboardField("geography", "location", DashboardFieldRole.GEOGRAPHY));

        DashboardQueryPlan plan = DashboardQueryBuilder.build(map, geographicTable("places"), java.util.List.of(geographicTable("places")));

        assertEquals(2, plan.getSelections().size());
        assertEquals("latitude", plan.getSelections().get(0).getAlias());
        assertEquals("geography", plan.getSelections().get(0).getNativeColumnType());
        assertEquals(DashboardQuerySelection.CoordinateAxis.LATITUDE, plan.getSelections().get(0).getCoordinateAxis());
        assertEquals("longitude", plan.getSelections().get(1).getAlias());
        assertEquals(DashboardQuerySelection.CoordinateAxis.LONGITUDE, plan.getSelections().get(1).getCoordinateAxis());
    }

    private static org.labs.genesis.connexion.model.TableMetadata table(String name) {
        org.labs.genesis.connexion.model.TableMetadata table = new org.labs.genesis.connexion.model.TableMetadata();
        table.setTableName(name);
        org.labs.genesis.connexion.model.ColumnMetadata produit = new org.labs.genesis.connexion.model.ColumnMetadata();
        produit.setName("produit");
        produit.setReferencedColumn("produit");
        produit.setText(true);
        org.labs.genesis.connexion.model.ColumnMetadata montant = new org.labs.genesis.connexion.model.ColumnMetadata();
        montant.setName("montant");
        montant.setReferencedColumn("montant");
        montant.setNumeric(true);
        table.setColumns(new org.labs.genesis.connexion.model.ColumnMetadata[]{produit, montant});
        return table;
    }

    private static org.labs.genesis.connexion.model.TableMetadata geographicTable(String name) {
        org.labs.genesis.connexion.model.TableMetadata table = new org.labs.genesis.connexion.model.TableMetadata();
        table.setTableName(name);
        org.labs.genesis.connexion.model.ColumnMetadata location = new org.labs.genesis.connexion.model.ColumnMetadata();
        location.setName("location");
        location.setReferencedColumn("location");
        location.setGeographic(true);
        location.setColumnType("geography");
        table.setColumns(new org.labs.genesis.connexion.model.ColumnMetadata[]{location});
        return table;
    }
}

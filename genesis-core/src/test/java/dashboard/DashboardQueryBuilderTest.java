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

    @Test
    void shouldResolveJoinPathsAcrossThreeRelationshipsAndQualifiedTableNames() {
        org.labs.genesis.connexion.model.TableMetadata orders =
                joinTable("orders", foreignKey("profile_id", "profiles"));
        org.labs.genesis.connexion.model.TableMetadata profiles =
                joinTable("profiles", foreignKey("department_id", "departments"));
        org.labs.genesis.connexion.model.TableMetadata departments =
                joinTable("departments", foreignKey("country_id", "countries"));
        org.labs.genesis.connexion.model.TableMetadata countries =
                joinTable("countries", column("name"));

        DashboardVisualization table = new DashboardVisualization();
        table.setType(DashboardVisualizationType.TABLE);
        table.setDataSource(new DashboardDataSource("orders", DashboardSourceType.TABLE));
        table.getFields().add(new DashboardField(
                "countryName",
                "public.countries.name",
                DashboardFieldRole.COLUMN
        ));

        DashboardQueryPlan plan = DashboardQueryBuilder.build(
                table,
                orders,
                java.util.List.of(orders, profiles, departments, countries)
        );

        assertEquals(3, plan.getJoins().size());
        assertEquals(
                java.util.List.of("profiles", "departments", "countries"),
                plan.getJoins().stream()
                        .map(org.labs.genesis.dashboard.query.DashboardJoin::getTable)
                        .toList()
        );
    }

    private static org.labs.genesis.connexion.model.TableMetadata joinTable(
            String tableName,
            org.labs.genesis.connexion.model.ColumnMetadata... columns
    ) {
        org.labs.genesis.connexion.model.TableMetadata table =
                new org.labs.genesis.connexion.model.TableMetadata();
        table.setTableName(tableName);
        table.setColumns(columns);
        return table;
    }

    private static org.labs.genesis.connexion.model.ColumnMetadata foreignKey(
            String columnName,
            String referencedTable
    ) {
        org.labs.genesis.connexion.model.ColumnMetadata column = column(columnName);
        column.setForeign(true);
        column.setReferencedTable(referencedTable);
        column.setReferencedPrimaryKeyColumn("id");
        return column;
    }

    private static org.labs.genesis.connexion.model.ColumnMetadata column(String name) {
        org.labs.genesis.connexion.model.ColumnMetadata column =
                new org.labs.genesis.connexion.model.ColumnMetadata();
        column.setName(name);
        column.setReferencedColumn(name);
        return column;
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

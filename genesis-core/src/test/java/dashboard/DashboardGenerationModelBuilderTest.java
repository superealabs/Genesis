package dashboard;

import org.junit.jupiter.api.Test;
import org.labs.genesis.connexion.model.ColumnMetadata;
import org.labs.genesis.connexion.model.TableMetadata;
import org.labs.genesis.dashboard.generation.DashboardGenerationModelBuilder;
import org.labs.genesis.dashboard.generation.model.DashboardGenerationModel;
import org.labs.genesis.dashboard.generation.model.DashboardPageGenerationModel;
import org.labs.genesis.dashboard.generation.model.DashboardVisualizationGenerationModel;
import org.labs.genesis.dashboard.model.DashboardConfiguration;
import org.labs.genesis.dashboard.model.DashboardDataSource;
import org.labs.genesis.dashboard.model.DashboardField;
import org.labs.genesis.dashboard.model.DashboardLayout;
import org.labs.genesis.dashboard.model.DashboardPage;
import org.labs.genesis.dashboard.model.DashboardVisualization;
import org.labs.genesis.dashboard.model.DashboardEnums.DashboardFieldRole;
import org.labs.genesis.dashboard.model.DashboardEnums.DashboardSourceType;
import org.labs.genesis.dashboard.model.DashboardEnums.DashboardVisualizationType;
import org.labs.genesis.dashboard.model.DashboardEnums.StatisticType;
import org.labs.genesis.dashboard.query.DashboardJoin;
import org.labs.genesis.dashboard.query.DashboardJoinCondition;
import org.labs.genesis.dashboard.query.DashboardQueryPlan;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DashboardGenerationModelBuilderTest {

    @Test
    void disabledDashboardProducesEmptyGenerationModel() {

        DashboardConfiguration configuration =
                new DashboardConfiguration();

        configuration.setEnabled(false);

        DashboardGenerationModel result =
                DashboardGenerationModelBuilder.build(
                        configuration,
                        List.of()
                );

        assertNotNull(result);
        assertFalse(result.isEnabled());
        assertTrue(result.isEmpty());
        assertNotNull(result.getPages());
        assertTrue(result.getPages().isEmpty());
    }

    @Test
    void buildsPageAndVisualizationGenerationModel() {

        TableMetadata commande =
                createCommandeTable();

        DashboardVisualization visualization =
                createCommandeByStatusVisualization();

        DashboardPage page =
                new DashboardPage();

        page.setId("main");
        page.setTitle("Dashboard principal");
        page.getVisualizations()
                .add(visualization);

        DashboardConfiguration configuration =
                new DashboardConfiguration();

        configuration.setEnabled(true);
        configuration.getPages()
                .add(page);

        DashboardGenerationModel result =
                DashboardGenerationModelBuilder.build(
                        configuration,
                        List.of(commande)
                );

        assertNotNull(result);
        assertTrue(result.isEnabled());
        assertFalse(result.isEmpty());

        assertEquals(
                1,
                result.getPages().size()
        );

        DashboardPageGenerationModel generatedPage =
                result.getPages().get(0);

        assertEquals(
                "main",
                generatedPage.getId()
        );

        assertEquals(
                "Dashboard principal",
                generatedPage.getTitle()
        );

        assertEquals(
                1,
                generatedPage.getVisualizations().size()
        );

        DashboardVisualizationGenerationModel generatedVisualization =
                generatedPage.getVisualizations().get(0);

        assertEquals(
                "sales-by-status",
                generatedVisualization.getId()
        );

        assertEquals(
                "Montant par statut",
                generatedVisualization.getTitle()
        );

        assertEquals(
                DashboardVisualizationType.BAR_VERTICAL,
                generatedVisualization.getType()
        );

        assertNotNull(
                generatedVisualization.getLayout()
        );

        assertEquals(
                2,
                generatedVisualization.getFields().size()
        );

        assertNotNull(
                generatedVisualization.getQueryPlan()
        );

        assertTrue(
                generatedVisualization.getQueryPlan()
                        .getJoins()
                        .isEmpty()
        );
    }

    @Test
    void buildsQueryPlanWithTwoJoins() {

        TableMetadata commande =
                createCommandeTable();

        TableMetadata client =
                createClientTable();

        TableMetadata ville =
                createVilleTable();

        DashboardVisualization visualization =
                createAmountByCityVisualization();

        DashboardPage page =
                new DashboardPage();

        page.setId("main");
        page.setTitle("Dashboard principal");
        page.getVisualizations()
                .add(visualization);

        DashboardConfiguration configuration =
                new DashboardConfiguration();

        configuration.getPages()
                .add(page);

        DashboardGenerationModel result =
                DashboardGenerationModelBuilder.build(
                        configuration,
                        List.of(
                                commande,
                                client,
                                ville
                        )
                );

        DashboardVisualizationGenerationModel generatedVisualization =
                result.getPages()
                        .get(0)
                        .getVisualizations()
                        .get(0);

        DashboardQueryPlan plan =
                generatedVisualization.getQueryPlan();

        assertNotNull(plan);

        assertNotNull(
                plan.getSource()
        );

        assertEquals(
                "commande",
                plan.getSource().getName()
        );

        assertEquals(
                2,
                plan.getJoins().size()
        );

        DashboardJoin firstJoin =
                plan.getJoins().get(0);

        assertEquals(
                "client",
                firstJoin.getTable()
        );

        assertEquals(
                1,
                firstJoin.getConditions().size()
        );

        DashboardJoinCondition firstCondition =
                firstJoin.getConditions().get(0);

        assertEquals(
                "commande",
                firstCondition.getLeftTable()
        );

        assertEquals(
                "client_id",
                firstCondition.getLeftColumn()
        );

        assertEquals(
                "client",
                firstCondition.getRightTable()
        );

        assertEquals(
                "id",
                firstCondition.getRightColumn()
        );

        DashboardJoin secondJoin =
                plan.getJoins().get(1);

        assertEquals(
                "ville",
                secondJoin.getTable()
        );

        assertEquals(
                1,
                secondJoin.getConditions().size()
        );

        DashboardJoinCondition secondCondition =
                secondJoin.getConditions().get(0);

        assertEquals(
                "client",
                secondCondition.getLeftTable()
        );

        assertEquals(
                "ville_id",
                secondCondition.getLeftColumn()
        );

        assertEquals(
                "ville",
                secondCondition.getRightTable()
        );

        assertEquals(
                "id",
                secondCondition.getRightColumn()
        );

        assertEquals(
                2,
                plan.getSelections().size()
        );

        assertEquals(
                1,
                plan.getGroupBy().size()
        );

        assertEquals(
                "ville.nom",
                plan.getGroupBy().get(0)
        );
    }

    // =====================================================================
    // VISUALIZATIONS
    // =====================================================================

    private DashboardVisualization createCommandeByStatusVisualization() {

        DashboardVisualization visualization =
                new DashboardVisualization();

        visualization.setId(
                "sales-by-status"
        );

        visualization.setTitle(
                "Montant par statut"
        );

        visualization.setType(
                DashboardVisualizationType.BAR_VERTICAL
        );

        visualization.setDataSource(
                new DashboardDataSource(
                        "commande",
                        DashboardSourceType.TABLE
                )
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

        return visualization;
    }

    private DashboardVisualization createAmountByCityVisualization() {

        DashboardVisualization visualization =
                new DashboardVisualization();

        visualization.setId(
                "sales-by-city"
        );

        visualization.setTitle(
                "Montant par ville"
        );

        visualization.setType(
                DashboardVisualizationType.BAR_VERTICAL
        );

        visualization.setDataSource(
                new DashboardDataSource(
                        "commande",
                        DashboardSourceType.TABLE
                )
        );

        DashboardField dimension =
                new DashboardField(
                        "xAxis",
                        "ville.nom",
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

        return visualization;
    }

    // =====================================================================
    // TABLES
    // =====================================================================

    private TableMetadata createCommandeTable() {

        TableMetadata table =
                new TableMetadata();

        table.setTableName(
                "commande"
        );

        ColumnMetadata id =
                createColumn(
                        "id",
                        false,
                        false
                );

        ColumnMetadata montant =
                createColumn(
                        "montant",
                        true,
                        false
                );

        ColumnMetadata statut =
                createColumn(
                        "statut",
                        false,
                        true
                );

        ColumnMetadata clientId =
                createForeignKey(
                        "client_id",
                        "client",
                        "id"
                );

        table.setColumns(
                new ColumnMetadata[]{
                        id,
                        montant,
                        statut,
                        clientId
                }
        );

        return table;
    }

    private TableMetadata createClientTable() {

        TableMetadata table =
                new TableMetadata();

        table.setTableName(
                "client"
        );

        ColumnMetadata id =
                createColumn(
                        "id",
                        false,
                        false
                );

        ColumnMetadata nom =
                createColumn(
                        "nom",
                        false,
                        true
                );

        ColumnMetadata villeId =
                createForeignKey(
                        "ville_id",
                        "ville",
                        "id"
                );

        table.setColumns(
                new ColumnMetadata[]{
                        id,
                        nom,
                        villeId
                }
        );

        return table;
    }

    private TableMetadata createVilleTable() {

        TableMetadata table =
                new TableMetadata();

        table.setTableName(
                "ville"
        );

        ColumnMetadata id =
                createColumn(
                        "id",
                        false,
                        false
                );

        ColumnMetadata nom =
                createColumn(
                        "nom",
                        false,
                        true
                );

        table.setColumns(
                new ColumnMetadata[]{
                        id,
                        nom
                }
        );

        return table;
    }

    // =====================================================================
    // COLUMNS
    // =====================================================================

    private ColumnMetadata createColumn(
            String name,
            boolean numeric,
            boolean text
    ) {

        ColumnMetadata column =
                new ColumnMetadata();

        column.setName(
                name
        );

        column.setReferencedColumn(
                name
        );

        column.setNumeric(
                numeric
        );

        column.setText(
                text
        );

        return column;
    }

    private ColumnMetadata createForeignKey(
            String columnName,
            String referencedTable,
            String referencedPrimaryKey
    ) {

        ColumnMetadata column =
                createColumn(
                        columnName,
                        false,
                        false
                );

        column.setForeign(
                true
        );

        column.setReferencedTable(
                referencedTable
        );

        column.setReferencedPrimaryKeyColumn(
                referencedPrimaryKey
        );

        return column;
    }
}
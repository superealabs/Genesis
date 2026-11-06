package dashboard;

import org.junit.jupiter.api.Test;
import org.labs.genesis.dashboard.model.DashboardDataSource;
import org.labs.genesis.dashboard.model.DashboardEnums;
import org.labs.genesis.dashboard.query.DashboardQueryPlan;
import org.labs.genesis.dashboard.query.DashboardQuerySelection;
import org.labs.genesis.dashboard.technology.springapi.query.SpringApiDashboardQueryRenderer;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SpringApiDashboardQueryRendererTest {
    @Test
    void castsPostgresGeographyToGeometryForCoordinateExtraction() {
        DashboardQueryPlan plan = new DashboardQueryPlan();
        plan.setSource(new DashboardDataSource("clients", DashboardEnums.DashboardSourceType.TABLE));
        plan.setDatabaseName("PostgreSQL");

        DashboardQuerySelection latitude = coordinate("latitude", DashboardQuerySelection.CoordinateAxis.LATITUDE);
        DashboardQuerySelection longitude = coordinate("longitude", DashboardQuerySelection.CoordinateAxis.LONGITUDE);
        latitude.setNativeColumnType("geography");
        longitude.setNativeColumnType("geography");
        plan.getSelections().add(latitude);
        plan.getSelections().add(longitude);

        assertEquals(
                "SELECT ST_Y((clients.location)::geometry) AS latitude, "
                        + "ST_X((clients.location)::geometry) AS longitude FROM clients",
                SpringApiDashboardQueryRenderer.render(plan).getSql()
        );
    }

    private static DashboardQuerySelection coordinate(String alias, DashboardQuerySelection.CoordinateAxis axis) {
        DashboardQuerySelection selection = new DashboardQuerySelection();
        selection.setColumnName("clients.location");
        selection.setAlias(alias);
        selection.setCoordinateAxis(axis);
        return selection;
    }
}

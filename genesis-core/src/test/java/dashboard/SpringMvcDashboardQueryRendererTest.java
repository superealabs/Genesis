package dashboard;

import org.junit.jupiter.api.Test;
import org.labs.genesis.dashboard.model.DashboardDataSource;
import org.labs.genesis.dashboard.model.DashboardEnums;
import org.labs.genesis.dashboard.query.DashboardQueryPlan;
import org.labs.genesis.dashboard.query.DashboardQuerySelection;
import org.labs.genesis.dashboard.query.DashboardStatisticExpression;
import org.labs.genesis.dashboard.technology.springmvc.query.SpringMvcDashboardQueryRenderer;
import org.labs.genesis.dashboard.technology.springmvc.query.SpringMvcRenderedQuery;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SpringMvcDashboardQueryRendererTest {
    @Test
    void rendersSimpleAggregatedQuery() {

        DashboardQueryPlan plan =
                new DashboardQueryPlan();

        plan.setSource(
                new DashboardDataSource(
                        "commande",
                        DashboardEnums.DashboardSourceType.TABLE
                )
        );

        DashboardQuerySelection dimension =
                new DashboardQuerySelection();

        dimension.setColumnName(
                "commande.statut"
        );

        dimension.setAlias(
                "xAxis"
        );

        DashboardStatisticExpression statistic =
                new DashboardStatisticExpression(
                        DashboardEnums.StatisticType.SUM,
                        "commande.montant",
                        "yAxis",
                        false
                );

        DashboardQuerySelection measure =
                new DashboardQuerySelection();

        measure.setColumnName(
                "commande.montant"
        );

        measure.setAlias(
                "yAxis"
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

        SpringMvcRenderedQuery query =
                SpringMvcDashboardQueryRenderer.render(
                        plan
                );

        assertEquals(
                "SELECT commande.statut AS xAxis, "
                        + "SUM(commande.montant) AS yAxis "
                        + "FROM commande "
                        + "GROUP BY commande.statut",
                query.getSql()
        );

        assertEquals(
                List.of(
                        "xAxis",
                        "yAxis"
                ),
                query.getAliases()
        );
    }
}
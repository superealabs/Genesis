package dashboard;

import org.junit.jupiter.api.Test;
import org.labs.genesis.dashboard.model.DashboardField;
import org.labs.genesis.dashboard.query.DashboardStatisticExpression;
import org.labs.genesis.dashboard.rules.DashboardStatisticResolver;

import static org.junit.jupiter.api.Assertions.*;
import static org.labs.genesis.dashboard.model.DashboardEnums.*;

class DashboardStatisticResolverTest {
    @Test
    void shouldResolveSum() {
        DashboardField field = new DashboardField("total", "montant", DashboardFieldRole.MEASURE);
        field.setStatistic(StatisticType.SUM);

        DashboardStatisticExpression expression = DashboardStatisticResolver.resolve(field);
        assertNotNull(expression);
        assertEquals(StatisticType.SUM, expression.getStatistic());
        assertEquals("montant", expression.getColumnName());
        assertFalse(expression.isDistinct());
    }

    @Test
    void shouldResolveCountDistinct() {
        DashboardField field = new DashboardField("clients", "client_id", DashboardFieldRole.MEASURE);
        field.setStatistic(StatisticType.COUNT_DISTINCT);
        DashboardStatisticExpression expression = DashboardStatisticResolver.resolve(field);
        assertTrue(expression.isDistinct());
    }

    @Test
    void shouldCreateRowCount() {
        DashboardStatisticExpression expression = DashboardStatisticResolver.countRows("total");
        assertEquals(StatisticType.COUNT, expression.getStatistic());
        assertTrue(expression.targetsRows());
    }
}
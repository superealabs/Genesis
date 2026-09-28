package dashboard;

import org.junit.jupiter.api.Test;
import org.labs.genesis.connexion.model.ColumnMetadata;
import org.labs.genesis.dashboard.model.DashboardEnums.DashboardDataType;
import org.labs.genesis.dashboard.model.DashboardEnums.StatisticType;
import org.labs.genesis.dashboard.rules.DashboardDataTypeResolver;
import org.labs.genesis.dashboard.rules.DashboardStatisticRules;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class DashboardStatisticRulesTest {

    @Test
    void numericColumnSupportsNumericAggregations() {
        ColumnMetadata column = new ColumnMetadata();
        column.setNumeric(true);
        assertEquals(DashboardDataType.NUMERIC, DashboardDataTypeResolver.resolve(column));
        assertEquals(Set.of(StatisticType.COUNT, StatisticType.COUNT_DISTINCT, StatisticType.SUM, StatisticType.AVG, StatisticType.MIN, StatisticType.MAX), DashboardStatisticRules.getColumnStatistics(column));
    }

    @Test
    void textColumnOnlySupportsCountStatistics() {
        ColumnMetadata column = new ColumnMetadata();
        column.setText(true);
        assertEquals(DashboardDataType.TEXT, DashboardDataTypeResolver.resolve(column));
        assertEquals(Set.of(StatisticType.COUNT, StatisticType.COUNT_DISTINCT), DashboardStatisticRules.getColumnStatistics(column));
    }

    @Test
    void temporalColumnSupportsMinAndMax() {
        ColumnMetadata column = new ColumnMetadata();
        column.setDateTime(true);
        assertEquals(DashboardDataType.TEMPORAL, DashboardDataTypeResolver.resolve(column));
        assertTrue(DashboardStatisticRules.supports(column, StatisticType.MIN));
        assertTrue(DashboardStatisticRules.supports(column, StatisticType.MAX));
        assertFalse(DashboardStatisticRules.supports(column, StatisticType.SUM));
    }

    @Test
    void booleanColumnIsResolvedFromDatabaseType() {
        ColumnMetadata column = new ColumnMetadata();
        column.setColumnType("BOOLEAN");
        assertEquals(DashboardDataType.BOOLEAN, DashboardDataTypeResolver.resolve(column));
        assertEquals(Set.of(StatisticType.COUNT, StatisticType.COUNT_DISTINCT), DashboardStatisticRules.getColumnStatistics(column));
    }

    @Test
    void tableLevelOnlySupportsRowCountForNow() {
        assertEquals(Set.of(StatisticType.COUNT), DashboardStatisticRules.getTableStatistics());
    }
}

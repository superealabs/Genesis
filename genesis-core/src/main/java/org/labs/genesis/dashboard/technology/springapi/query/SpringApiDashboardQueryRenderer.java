package org.labs.genesis.dashboard.technology.springapi.query;

import org.labs.genesis.dashboard.query.DashboardQueryPlan;
import org.labs.genesis.dashboard.query.DashboardQuerySelection;
import org.labs.genesis.dashboard.technology.springmvc.query.SpringMvcDashboardQueryRenderer;
import org.labs.genesis.dashboard.technology.springmvc.query.SpringMvcRenderedQuery;

import java.util.Locale;

/** Adds API-only spatial expressions while preserving the MVC SQL renderer. */
public final class SpringApiDashboardQueryRenderer {
    private SpringApiDashboardQueryRenderer() {
    }

    public static SpringMvcRenderedQuery render(DashboardQueryPlan plan) {
        SpringMvcRenderedQuery rendered = SpringMvcDashboardQueryRenderer.render(plan);
        if (plan == null || plan.getSelections() == null || plan.getSelections().stream()
                .noneMatch(selection -> selection != null && selection.getCoordinateAxis() != null)) {
            return rendered;
        }

        String sql = rendered.getSql();
        String table = plan.getSource().getName();
        for (DashboardQuerySelection selection : plan.getSelections()) {
            if (selection == null || selection.getCoordinateAxis() == null) {
                continue;
            }
            String column = qualify(selection.getColumnName(), table);
            String alias = selection.getAlias();
            String current = column + " AS " + alias;
            sql = sql.replace(current, coordinateExpression(selection, column, plan.getDatabaseName()) + " AS " + alias);
        }
        return new SpringMvcRenderedQuery(sql, rendered.getParameters(), rendered.getLimit(), rendered.getAliases());
    }

    private static String coordinateExpression(
            DashboardQuerySelection selection,
            String column,
            String databaseName
    ) {
        if (databaseName == null || databaseName.isBlank()) {
            throw new IllegalArgumentException("Database engine is required for geographic Map coordinates");
        }
        boolean latitude = selection.getCoordinateAxis() == DashboardQuerySelection.CoordinateAxis.LATITUDE;
        String database = databaseName.toLowerCase(Locale.ROOT);
        String nativeType = selection.getNativeColumnType();
        if (database.contains("postgres")) {
            String geometry = "geography".equalsIgnoreCase(nativeType)
                    ? "(" + column + ")::geometry"
                    : column;
            return (latitude ? "ST_Y(" : "ST_X(") + geometry + ")";
        }
        if (database.contains("mysql") || database.contains("maria")) {
            return (latitude ? "ST_Y(" : "ST_X(") + column + ")";
        }
        if (database.contains("sql server") || database.contains("sqlserver")) {
            if ("geography".equalsIgnoreCase(nativeType)) {
                return column + (latitude ? ".Lat" : ".Long");
            }
            return column + (latitude ? ".STY()" : ".STX()");
        }
        if (database.contains("oracle")) {
            return column + ".SDO_POINT." + (latitude ? "Y" : "X");
        }
        throw new IllegalArgumentException("Geographic Map coordinates are not supported for database engine: " + databaseName);
    }

    private static String qualify(String column, String defaultTable) {
        if (column == null || column.isBlank()) {
            throw new IllegalArgumentException("Geographic column is required");
        }
        String value = column.trim();
        if (value.toUpperCase(Locale.ROOT).startsWith("COLUMN:")) {
            value = value.substring(7).trim();
        }
        return value.contains(".") ? value : defaultTable + "." + value;
    }
}

package org.labs.genesis.forms.ui.visualization;

import org.labs.genesis.config.ProjectGenerationContext;
import org.labs.genesis.dashboard.model.*;
import org.labs.genesis.dashboard.model.DashboardEnums.*;
import org.labs.genesis.forms.ui.visualization.model.QueryRole;
import org.labs.genesis.forms.ui.visualization.model.VisualizationParameter;

import java.util.Locale;

/** Maps the dashboard editor's visualization state to its serializable model. */
public final class DashboardVisualizationMapper {
    private DashboardVisualizationMapper() { }

    public static DashboardVisualization map(DashboardVisualComponent component, ProjectGenerationContext context) {
        DashboardVisualization result = new DashboardVisualization();
        result.setId(component.getDashboardId());
        result.setTitle(component.getConfig().getString("title", component.getVisualizationItem().name));
        result.setType(typeFor(component.getVisualizationItem().name));
        String source = component.getDataSourceName();
        DashboardSourceType sourceType = resolveSourceType(source, context);
        result.setDataSource(new DashboardDataSource(source, sourceType));
        for (VisualizationParameter parameter : component.getVisualizationItem().parameters) {
            QueryRole role = parameter.getRole();
            Object value = component.getConfig().getValue(parameter.getKey());
            if (role == null || role == QueryRole.FILTER || role == QueryRole.SORT || role == QueryRole.LIMIT
                    || value == null || value.toString().isBlank()) continue;
            DashboardFieldRole dashboardRole;
            try {
                dashboardRole = DashboardFieldRole.valueOf(role == QueryRole.COLUMNS ? "COLUMN" : role.name());
            } catch (IllegalArgumentException ignored) { continue; }
            DashboardField field = new DashboardField(parameter.getKey(), value.toString(), dashboardRole);
            if (dashboardRole == DashboardFieldRole.MEASURE) {
                String aggregation = component.getConfig().getString("aggregation", "NONE").trim().toUpperCase(Locale.ROOT).replace(' ', '_');
                try { field.setStatistic(StatisticType.valueOf(aggregation)); }
                catch (IllegalArgumentException ignored) { field.setStatistic(StatisticType.NONE); }
            }
            result.getFields().add(field);
        }
        Object limit = component.getConfig().getValue("limit");
        if (limit instanceof Number number) result.getQueryOptions().setLimit(number.intValue());
        mapSort(component, result);
        return result;
    }

    private static DashboardVisualizationType typeFor(String name) {
        String normalized = name.toUpperCase(Locale.ROOT).replace(' ', '_').replace('-', '_');
        for (DashboardVisualizationType type : DashboardVisualizationType.values())
            if (normalized.contains(type.name())) return type;
        if (normalized.contains("BAR")) return DashboardVisualizationType.BAR_VERTICAL;
        return DashboardVisualizationType.TABLE;
    }

    private static DashboardSourceType resolveSourceType(String source, ProjectGenerationContext context) {
        if (source == null || context == null) {
            return DashboardSourceType.TABLE;
        }

        return context.getAllTables()
                .stream()
                .filter(table -> table != null && table.getTableName() != null && matchesSource(table.getTableName(), source))
                .findFirst()
                .map(table -> Boolean.TRUE.equals(table.getIsView()) ? DashboardSourceType.VIEW : DashboardSourceType.TABLE)
                .orElse(DashboardSourceType.TABLE);
    }

    private static boolean matchesSource(String tableName, String sourceName) {
        if (tableName == null || sourceName == null) {
            return false;
        }
        String table = tableName.toLowerCase(Locale.ROOT);
        String source = sourceName.toLowerCase(Locale.ROOT);
        return source.equals(table) || source.endsWith("." + table);
    }

    private static void mapSort(DashboardVisualComponent component, DashboardVisualization result) {
        Object columnValue = component.getConfig().getValue("sortColumn");
        if (columnValue == null || columnValue.toString().isBlank()) {
            return;
        }
        String directionValue = component.getConfig().getString("sortDirection", "ASC");
        DashboardSortDirection direction = "DESC".equalsIgnoreCase(directionValue) ? DashboardSortDirection.DESC : DashboardSortDirection.ASC;
        result.getQueryOptions()
                .getSorts()
                .add(new DashboardSort(columnValue.toString(), direction));
    }
}

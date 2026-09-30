package org.labs.genesis.forms.ui.visualization;

import org.labs.genesis.dashboard.model.*;
import org.labs.genesis.dashboard.model.DashboardEnums.*;
import org.labs.genesis.forms.ui.visualization.model.QueryRole;
import org.labs.genesis.forms.ui.visualization.model.VisualizationParameter;

import java.util.Locale;

/** Maps the dashboard editor's visualization state to its serializable model. */
public final class DashboardVisualizationMapper {
    private DashboardVisualizationMapper() { }

    public static DashboardVisualization map(DashboardVisualComponent component) {
        DashboardVisualization result = new DashboardVisualization();
        result.setId(component.getDashboardId());
        result.setTitle(component.getConfig().getString("title", component.getVisualizationItem().name));
        result.setType(typeFor(component.getVisualizationItem().name));
        String source = component.getDataSourceName();
        result.setDataSource(new DashboardDataSource(source, DashboardSourceType.TABLE));
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
                String aggregation = component.getConfig().getString("aggregation", "SUM").trim().toUpperCase(Locale.ROOT).replace(' ', '_');
                try { field.setStatistic(StatisticType.valueOf(aggregation)); }
                catch (IllegalArgumentException ignored) { field.setStatistic(StatisticType.SUM); }
            }
            result.getFields().add(field);
        }
        Object limit = component.getConfig().getValue("limit");
        if (limit instanceof Number number) result.getQueryOptions().setLimit(number.intValue());
        return result;
    }

    private static DashboardVisualizationType typeFor(String name) {
        String normalized = name.toUpperCase(Locale.ROOT).replace(' ', '_').replace('-', '_');
        for (DashboardVisualizationType type : DashboardVisualizationType.values())
            if (normalized.contains(type.name())) return type;
        if (normalized.contains("BAR")) return DashboardVisualizationType.BAR_VERTICAL;
        return DashboardVisualizationType.TABLE;
    }
}

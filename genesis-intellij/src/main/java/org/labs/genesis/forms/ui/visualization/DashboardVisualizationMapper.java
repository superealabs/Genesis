package org.labs.genesis.forms.ui.visualization;

import org.labs.genesis.config.ProjectGenerationContext;
import org.labs.genesis.dashboard.model.*;
import org.labs.genesis.dashboard.model.DashboardEnums.*;
import org.labs.genesis.forms.ui.visualization.model.*;

import java.util.List;

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
        mapFields(result, component.getConfig(), component.getVisualizationItem());
        if (result.getType() == DashboardVisualizationType.MAP) {
            addOptionalMapColumn(result, component.getConfig(), "labelColumn");
        }
        mapQueryOptions(result, component.getConfig());
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

    private static DashboardFilter mapCondition(VisualizationFilterCondition condition) {
        if (condition == null || condition.getColumn() == null || condition.getColumn().isBlank()) {
            return null;
        }
        DashboardLogicalOperator relation = condition.getRelationToPrevious() == FilterLogicalOperator.OR
                        ? DashboardLogicalOperator.OR
                        : DashboardLogicalOperator.AND;
        return new DashboardFilter(condition.getColumn(), condition.getOperator(), condition.getValue(), relation);
    }

    private static DashboardFilterGroup mapGroup(VisualizationFilterGroup group) {
        if (group == null) {
            return null;
        }
        DashboardFilterGroup result = new DashboardFilterGroup();
        result.setOperator(
                group.getRelation()
                        == FilterLogicalOperator.OR
                        ? DashboardLogicalOperator.OR
                        : DashboardLogicalOperator.AND
        );
        for (VisualizationFilterNode child : group.getChildren()) {
            DashboardFilterNode mapped = mapFilterNode(child);
            if (mapped != null) {
                result.addChild(mapped);
            }
        }
        return result;
    }

    private static DashboardFilterNode mapFilterNode(VisualizationFilterNode node) {
        if (node instanceof VisualizationFilterCondition condition) {
            return mapCondition(condition);}
        if (node instanceof VisualizationFilterGroup group) {
            return mapGroup(group);
        }
        return null;
    }

    private static void mapFilters(DashboardVisualComponent component, DashboardVisualization result) {
        Object filtersValue = component.getConfig().getValue("filters");
        if (!(filtersValue instanceof List<?> filters)) {
            return;
        }
        for (Object item : filters) {
            if (!(item instanceof VisualizationFilterNode node)) {
                continue;
            }
            DashboardFilterNode mapped = mapFilterNode(node);
            if (mapped != null) {
                result.getQueryOptions()
                        .getFilters()
                        .add(mapped);
            }
        }
    }

    public static DashboardVisualization map(String source, VisualizationConfig config, VisualizationItem item) {
        DashboardVisualization result = new DashboardVisualization();
        result.setTitle(config.getString("title", item.name));
        result.setType(typeFor(item.name));
        result.setDataSource(new DashboardDataSource(source, DashboardSourceType.TABLE));
        mapFields(result, config, item);
        if (result.getType() == DashboardVisualizationType.MAP) {
            addOptionalMapColumn(result, config, "labelColumn");
        }
        mapQueryOptions(result, config);

        return result;
    }

    private static void mapFields(DashboardVisualization result, VisualizationConfig config, VisualizationItem item) {
        for (VisualizationParameter parameter : item.parameters) {
            QueryRole role = parameter.getRole();
            if (role == null
                    || role == QueryRole.FILTER
                    || role == QueryRole.SORT
                    || role == QueryRole.LIMIT) {
                continue;
            }
            Object value = config.getValue(parameter.getKey());
            if (value == null) {
                continue;
            }
            if (role == QueryRole.COLUMNS && value instanceof List<?> values) {
                mapColumns(result, values);
                continue;
            }
            if (value.toString().isBlank()) {
                continue;
            }
            DashboardFieldRole dashboardRole = toDashboardRole(role);
            if (dashboardRole == null) {
                continue;
            }
            DashboardField field = new DashboardField(parameter.getKey(), value.toString(), dashboardRole);
            if (dashboardRole == DashboardFieldRole.MEASURE) {
                field.setStatistic(resolveStatistic(config));
            }
            result.getFields().add(field);
        }
    }

    private static void addOptionalMapColumn(DashboardVisualization result, VisualizationConfig config, String key) {
        Object value = config.getValue(key);
        if (value == null || value.toString().isBlank()) {
            return;
        }
        result.getFields().add(new DashboardField(key, value.toString(), DashboardFieldRole.COLUMN));
    }

    private static void mapColumns(DashboardVisualization result, List<?> values) {
        for (Object value : values) {
            if (value == null || value.toString().isBlank()) {
                continue;
            }
            String column = value.toString();
            String key = simpleColumnName(column);
            result.getFields().add(new DashboardField(key, column, DashboardFieldRole.COLUMN));
        }
    }

    private static String simpleColumnName(String value
    ) {
        if (value == null) {
            return null;
        }
        String result = value.trim();
        int colon = result.lastIndexOf(':');
        if (colon >= 0) {
            result = result.substring(colon + 1);
        }
        int dot = result.lastIndexOf('.');
        if (dot >= 0) {
            result = result.substring(dot + 1);
        }
        return result;
    }

    private static DashboardFieldRole toDashboardRole(QueryRole role) {
        try {
            String name = role == QueryRole.COLUMNS ? "COLUMN" : role.name();
            return DashboardFieldRole.valueOf(name);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static StatisticType resolveStatistic(VisualizationConfig config) {
        String aggregation = config.getString("aggregation", "NONE")
                        .trim()
                        .toUpperCase(Locale.ROOT)
                        .replace(' ', '_');

        try {
            return StatisticType.valueOf(aggregation);

        } catch (IllegalArgumentException e) {
            return StatisticType.NONE;
        }
    }

    private static void mapQueryOptions(DashboardVisualization result, VisualizationConfig config) {
        Object limit = config.getValue("limit");
        if (limit instanceof Number number) {
            result.getQueryOptions().setLimit(number.intValue());
        }
        mapSort(config, result);
        mapFilters(config, result);
    }

    private static void mapSort(VisualizationConfig config, DashboardVisualization result) {
        Object columnValue = config.getValue("sortColumn");
        if (columnValue == null || columnValue.toString().isBlank()) {
            return;
        }
        String directionValue = config.getString("sortDirection", "ASC");
        DashboardSortDirection direction = "DESC".equalsIgnoreCase(directionValue) ? DashboardSortDirection.DESC : DashboardSortDirection.ASC;
        result.getQueryOptions()
                .getSorts()
                .add(new DashboardSort(columnValue.toString(), direction));
    }

    private static void mapFilters(VisualizationConfig config, DashboardVisualization result) {
        Object filtersValue = config.getValue("filters");
        if (!(filtersValue instanceof List<?> filters)) {
            return;
        }
        for (Object item : filters) {
            if (!(item instanceof VisualizationFilterNode node)) {
                continue;
            }
            DashboardFilterNode mapped = mapFilterNode(node);
            if (mapped != null) {
                result.getQueryOptions()
                        .getFilters()
                        .add(mapped);
            }
        }
    }

}

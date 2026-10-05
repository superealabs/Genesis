package org.labs.genesis.dashboard.technology.springmvc.metadata;

import org.labs.genesis.dashboard.generation.model.DashboardGenerationModel;
import org.labs.genesis.dashboard.generation.model.DashboardPageGenerationModel;
import org.labs.genesis.dashboard.generation.model.DashboardVisualizationGenerationModel;
import org.labs.genesis.dashboard.model.DashboardField;
import org.labs.genesis.dashboard.model.DashboardLayout;
import org.labs.genesis.dashboard.technology.springmvc.query.SpringMvcDashboardQueryRenderer;
import org.labs.genesis.dashboard.technology.springmvc.query.SpringMvcRenderedQuery;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class SpringMvcDashboardMetadataProvider {

    private SpringMvcDashboardMetadataProvider() {
    }

    public static HashMap<String, Object> getDashboardHashMap(DashboardGenerationModel dashboard) {
        HashMap<String, Object> metadata = new HashMap<>();
        boolean enabled = dashboard != null && dashboard.isEnabled() && !dashboard.isEmpty();

        metadata.put("dashboardEnabled", enabled);
        if (!enabled) {
            metadata.put("dashboardPages", new ArrayList<>());
            metadata.put("dashboardVisualizations", new ArrayList<>());
            metadata.put("dashboardQueries", new ArrayList<>());
            metadata.put("hasDashboardPages", false);
            metadata.put("hasDashboardVisualizations", false);
            return metadata;
        }
        List<Map<String, Object>> pages = new ArrayList<>();
        List<Map<String, Object>> visualizations = new ArrayList<>();
        List<Map<String, Object>> queries = new ArrayList<>();
        for (DashboardPageGenerationModel page : dashboard.getPages()) {
            if (page == null) {
                continue;
            }
            Map<String, Object> pageMetadata = buildPageMetadata(page, visualizations, queries);
            pages.add(pageMetadata);
        }
        metadata.put("dashboardPages", pages);
        metadata.put("dashboardVisualizations", visualizations);
        metadata.put("dashboardQueries", queries);
        metadata.put("hasDashboardPages", !pages.isEmpty());
        metadata.put("hasDashboardVisualizations", !visualizations.isEmpty());

        return metadata;
    }

    private static Map<String, Object> buildPageMetadata(DashboardPageGenerationModel page, List<Map<String, Object>> allVisualizations, List<Map<String, Object>> allQueries) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("id", valueOrEmpty(page.getId()));
        metadata.put("title", valueOrEmpty(page.getTitle()));
        metadata.put("idJava", javaString(page.getId()));
        metadata.put("titleJava", javaString(page.getTitle()));
        List<Map<String, Object>> pageVisualizations = new ArrayList<>();
        if (page.getVisualizations() != null) {
            for (DashboardVisualizationGenerationModel visualization : page.getVisualizations()) {
                if (visualization == null) {
                    continue;
                }
                Map<String, Object> visualizationMetadata = buildVisualizationMetadata(page, visualization);
                pageVisualizations.add(visualizationMetadata);
                allVisualizations.add(visualizationMetadata);
                allQueries.add(buildQueryMetadata(page, visualization)
                );
            }
        }
        metadata.put("visualizations", pageVisualizations);
        metadata.put("hasVisualizations", !pageVisualizations.isEmpty());

        return metadata;
    }

    private static Map<String, Object> buildVisualizationMetadata(DashboardPageGenerationModel page, DashboardVisualizationGenerationModel visualization) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("id", valueOrEmpty(visualization.getId()));
        metadata.put("title", valueOrEmpty(visualization.getTitle()));
        metadata.put("idJava", javaString(visualization.getId()));
        metadata.put("titleJava", javaString(visualization.getTitle()));
        String type = visualization.getType() == null
                        ? ""
                        : visualization.getType().name();
        metadata.put("type", type);
        metadata.put("typeLowerCase", type.toLowerCase(Locale.ROOT));
        metadata.put("pageId", valueOrEmpty(page.getId()));
        metadata.put("pageTitle", valueOrEmpty(page.getTitle()));
        addLayoutMetadata(metadata, visualization.getLayout());
        List<Map<String, Object>> fields = buildFieldsMetadata(visualization.getFields());
        metadata.put("fields", fields);
        metadata.put("hasFields", !fields.isEmpty());
        if (visualization.getOptions() != null) {
            metadata.put("options", visualization.getOptions());
        } else {
            metadata.put("options", new HashMap<>());
        }

        return metadata;
    }

    private static Map<String, Object> buildQueryMetadata(DashboardPageGenerationModel page, DashboardVisualizationGenerationModel visualization) {
        if (visualization.getQueryPlan() == null) {
            throw new IllegalArgumentException(
                    "Dashboard visualization has no query plan: " + visualization.getId()
            );
        }

        SpringMvcRenderedQuery renderedQuery = SpringMvcDashboardQueryRenderer.render(visualization.getQueryPlan());
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("id", valueOrEmpty(visualization.getId()));
        metadata.put("title", valueOrEmpty(visualization.getTitle()));
        metadata.put("pageId", valueOrEmpty(page.getId()));
        metadata.put("type", visualization.getType() == null ? "" : visualization.getType().name());
        metadata.put("sql", renderedQuery.getSql());
        metadata.put("sqlJava", javaString(renderedQuery.getSql()));
        metadata.put("parametersJava", renderParameters(renderedQuery.getParameters()));
        metadata.put("aliasesJava", renderAliases(renderedQuery.getAliases()));
        Integer limit = renderedQuery.getLimit();
        metadata.put("limit", limit);
        metadata.put("hasLimit", limit != null && limit > 0);
        metadata.put("limitJava", limit == null ? "null" : limit.toString());

        return metadata;
    }

    private static List<Map<String, Object>> buildFieldsMetadata(List<DashboardField> fields) {
        List<Map<String, Object>> result = new ArrayList<>();
        if (fields == null) {
            return result;
        }
        for (DashboardField field : fields) {
            if (field == null) {
                continue;
            }
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("key", valueOrEmpty(field.getKey()));
            metadata.put("columnName", valueOrEmpty(field.getColumnName()));
            metadata.put("role", field.getRole() == null ? "" : field.getRole().name());
            metadata.put("statistic", field.getStatistic() == null ? "" : field.getStatistic().name());
            result.add(metadata);
        }

        return result;
    }

    private static void addLayoutMetadata(Map<String, Object> metadata, DashboardLayout layout) {
        if (layout == null) {
            metadata.put("layoutX", 0);
            metadata.put("layoutY", 0);
            metadata.put("layoutWidth", 0);
            metadata.put("layoutHeight", 0);

            return;
        }
        metadata.put("layoutX", layout.getX());
        metadata.put("layoutY", layout.getY());
        metadata.put("layoutWidth", layout.getWidth());
        metadata.put("layoutHeight", layout.getHeight());
    }

    private static String renderAliases(List<String> aliases) {
        if (aliases == null || aliases.isEmpty()) {
            return "java.util.Arrays.asList()";
        }
        List<String> values = new ArrayList<>();
        for (String alias : aliases) {
            values.add(javaString(alias));
        }
        return "java.util.Arrays.asList(" + String.join(", ", values) + ")";
    }

    private static String renderParameters(List<Object> parameters) {
        if (parameters == null || parameters.isEmpty()) {
            return "java.util.Arrays.asList()";
        }
        List<String> values = new ArrayList<>();
        for (Object parameter : parameters) {
            values.add(javaLiteral(parameter));
        }
        return "java.util.Arrays.asList(" + String.join(
                ", ", values) + ")";
    }

    private static String javaLiteral(Object value) {
        if (value == null) {
            return "null";
        }
        if (value instanceof String || value instanceof Character) {
            return javaString(value.toString());
        }

        if (value instanceof Boolean || value instanceof Byte || value instanceof Short || value instanceof Integer) {
            return value.toString();
        }

        if (value instanceof Long) {
            return value + "L";
        }

        if (value instanceof Float) {
            return value + "F";
        }

        if (value instanceof Double) {
            return value + "D";
        }

        if (value instanceof BigDecimal decimal) {
            return "new java.math.BigDecimal(" + javaString(decimal.toPlainString()) + ")";
        }

        if (value instanceof BigInteger integer) {
            return "new java.math.BigInteger(" + javaString(integer.toString()) + ")";
        }

        if (value instanceof LocalDate date) {
            return "java.time.LocalDate.parse(" + javaString(date.toString()) + ")";
        }

        if (value instanceof LocalDateTime dateTime) {
            return "java.time.LocalDateTime.parse(" + javaString(dateTime.toString()) + ")";
        }

        if (value instanceof OffsetDateTime dateTime) {
            return "java.time.OffsetDateTime.parse(" + javaString(dateTime.toString()) + ")";
        }

        if (value instanceof Instant instant) {
            return "java.time.Instant.parse(" + javaString(instant.toString()) + ")";
        }

        return javaString(
                value.toString()
        );
    }

    private static String javaString(String value) {
        if (value == null) {
            return "null";
        }

        return "\"" + escapeJava(value) + "\"";
    }

    private static String escapeJava(String value) {
        return value
                .replace(
                        "\\",
                        "\\\\"
                )
                .replace(
                        "\"",
                        "\\\""
                )
                .replace(
                        "\r",
                        "\\r"
                )
                .replace(
                        "\n",
                        "\\n"
                )
                .replace(
                        "\t",
                        "\\t"
                );
    }

    private static String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }
}
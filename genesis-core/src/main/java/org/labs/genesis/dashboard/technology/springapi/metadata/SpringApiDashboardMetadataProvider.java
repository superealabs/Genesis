package org.labs.genesis.dashboard.technology.springapi.metadata;

import org.labs.genesis.dashboard.generation.model.DashboardGenerationModel;
import org.labs.genesis.dashboard.generation.model.DashboardPageGenerationModel;
import org.labs.genesis.dashboard.generation.model.DashboardVisualizationGenerationModel;
import org.labs.genesis.dashboard.model.DashboardField;
import org.labs.genesis.dashboard.technology.springapi.query.SpringApiDashboardQueryRenderer;
import org.labs.genesis.dashboard.technology.springmvc.query.SpringMvcRenderedQuery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class SpringApiDashboardMetadataProvider {
    private SpringApiDashboardMetadataProvider() {
    }

    public static HashMap<String, Object> getDashboardHashMap(DashboardGenerationModel dashboard) {
        HashMap<String, Object> metadata = new HashMap<>();
        List<Map<String, Object>> pages = new ArrayList<>();
        Set<String> pageSlugs = new HashSet<>();

        if (dashboard != null && dashboard.isEnabled() && dashboard.getPages() != null) {
            for (DashboardPageGenerationModel page : dashboard.getPages()) {
                if (page == null) continue;
                Map<String, Object> pageMetadata = new HashMap<>();
                String pageSlug = uniqueSlug(page.getTitle(), pageSlugs);
                pageMetadata.put("id", valueOrEmpty(page.getId()));
                pageMetadata.put("title", valueOrEmpty(page.getTitle()));
                pageMetadata.put("slug", pageSlug);
                pageMetadata.put("endpointPath", "/dashboard/" + pageSlug);
                pageMetadata.put("methodName", "loadPage" + toJavaName(pageSlug));

                List<Map<String, Object>> visualizations = new ArrayList<>();
                Set<String> graphSlugs = new HashSet<>();
                if (page.getVisualizations() != null) {
                    for (DashboardVisualizationGenerationModel visualization : page.getVisualizations()) {
                        if (visualization != null && visualization.getQueryPlan() != null) {
                            visualizations.add(buildVisualizationMetadata(visualization, graphSlugs));
                        }
                    }
                }
                pageMetadata.put("visualizations", visualizations);
                pages.add(pageMetadata);
            }
        }

        metadata.put("dashboardEnabled", !pages.isEmpty());
        metadata.put("dashboardPages", pages);
        return metadata;
    }

    private static Map<String, Object> buildVisualizationMetadata(
            DashboardVisualizationGenerationModel visualization,
            Set<String> graphSlugs) {
        SpringMvcRenderedQuery renderedQuery = SpringApiDashboardQueryRenderer.render(visualization.getQueryPlan());
        String graphSlug = uniqueSlug(visualization.getTitle(), graphSlugs);
        String sql = renderedQuery.getSql();

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("id", valueOrEmpty(visualization.getId()));
        metadata.put("title", valueOrEmpty(visualization.getTitle()));
        metadata.put("graphSlug", graphSlug);
        metadata.put("methodName", "query" + toJavaName(graphSlug));
        metadata.put("sqlJava", javaString(sql));
        metadata.put("parametersJava", renderParameters(renderedQuery.getParameters()));
        metadata.put("sqlNamedJava", javaString(toNamedParameters(sql, renderedQuery.getParameters().size())));
        metadata.put("argumentsJava", renderArguments(renderedQuery.getParameters()));
        metadata.put("aliasesJava", renderAliases(renderedQuery.getAliases()));
        metadata.put("limitJava", renderedQuery.getLimit() == null ? "null" : renderedQuery.getLimit().toString());
        metadata.put("parameterDeclarations", renderParameterDeclarations(renderedQuery.getParameters().size()));
        metadata.put("fields", renderFields(visualization.getFields()));
        return metadata;
    }

    private static String uniqueSlug(String value, Set<String> used) {
        String base = valueOrEmpty(value).toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-|-$", "");
        if (base.isBlank()) base = "dashboard";
        String candidate = base;
        int suffix = 2;
        while (!used.add(candidate)) candidate = base + "-" + suffix++;
        return candidate;
    }

    private static String toJavaName(String slug) {
        StringBuilder result = new StringBuilder();
        for (String part : slug.split("-")) {
            if (!part.isBlank()) {
                result.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
            }
        }
        return result.toString();
    }

    private static String toNamedParameters(String sql, int parameterCount) {
        StringBuilder result = new StringBuilder();
        int parameterIndex = 0;
        for (int index = 0; index < sql.length(); index++) {
            char character = sql.charAt(index);
            if (character == '?' && parameterIndex < parameterCount) {
                result.append(":p").append(parameterIndex++);
            } else {
                result.append(character);
            }
        }
        return result.toString();
    }

    private static String renderParameterDeclarations(int count) {
        List<String> declarations = new ArrayList<>();
        for (int index = 0; index < count; index++) {
            declarations.add("@Param(\"p" + index + "\") Object p" + index);
        }
        return String.join(", ", declarations);
    }

    private static String renderArguments(List<Object> parameters) {
        List<String> values = new ArrayList<>();
        for (Object parameter : parameters) values.add(javaLiteral(parameter));
        return String.join(", ", values);
    }

    private static String renderAliases(List<String> aliases) {
        if (aliases == null || aliases.isEmpty()) return "java.util.List.of()";
        List<String> values = new ArrayList<>();
        for (String alias : aliases) values.add(javaString(alias));
        return "java.util.List.of(" + String.join(", ", values) + ")";
    }

    private static String renderParameters(List<Object> parameters) {
        if (parameters == null || parameters.isEmpty()) return "java.util.List.of()";
        List<String> values = new ArrayList<>();
        for (Object parameter : parameters) values.add(javaLiteral(parameter));
        return "java.util.List.of(" + String.join(", ", values) + ")";
    }

    private static List<Map<String, Object>> renderFields(List<DashboardField> fields) {
        List<Map<String, Object>> result = new ArrayList<>();
        if (fields != null) {
            for (DashboardField field : fields) {
                Map<String, Object> value = new HashMap<>();
                value.put("key", valueOrEmpty(field.getKey()));
                value.put("columnName", valueOrEmpty(field.getColumnName()));
                value.put("label", valueOrEmpty(field.getLabel()));
                value.put("role", field.getRole() == null ? "" : field.getRole().name());
                result.add(value);
            }
        }
        return result;
    }

    private static String javaLiteral(Object value) {
        if (value == null) return "null";
        if (value instanceof String || value instanceof Character) return javaString(value.toString());
        if (value instanceof Boolean || value instanceof Number) return value.toString();
        return javaString(value.toString());
    }

    private static String javaString(String value) {
        return "\"" + valueOrEmpty(value)
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r") + "\"";
    }

    private static String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }
}

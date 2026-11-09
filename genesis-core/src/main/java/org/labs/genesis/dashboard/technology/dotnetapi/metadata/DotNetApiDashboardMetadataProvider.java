package org.labs.genesis.dashboard.technology.dotnetapi.metadata;

import org.labs.genesis.dashboard.generation.model.DashboardGenerationModel;
import org.labs.genesis.dashboard.generation.model.DashboardPageGenerationModel;
import org.labs.genesis.dashboard.generation.model.DashboardVisualizationGenerationModel;
import org.labs.genesis.dashboard.technology.springapi.query.SpringApiDashboardQueryRenderer;
import org.labs.genesis.dashboard.technology.springmvc.query.SpringMvcRenderedQuery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class DotNetApiDashboardMetadataProvider {
    private static final Pattern POSITIONAL_PARAMETER = Pattern.compile("\\?(\\d+)");

    private DotNetApiDashboardMetadataProvider() {
    }

    public static HashMap<String, Object> getDashboardHashMap(DashboardGenerationModel dashboard) {
        HashMap<String, Object> metadata = new HashMap<>();
        List<Map<String, Object>> pages = new ArrayList<>();
        Set<String> pageSlugs = new HashSet<>();

        if (dashboard != null && dashboard.isEnabled() && dashboard.getPages() != null) {
            for (DashboardPageGenerationModel page : dashboard.getPages()) {
                if (page == null) {
                    continue;
                }
                String pageSlug = uniqueSlug(page.getTitle(), pageSlugs);
                Map<String, Object> pageMetadata = new HashMap<>();
                pageMetadata.put("slug", pageSlug);
                pageMetadata.put("methodName", "LoadPage" + toPascalCase(pageSlug));

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
        SpringMvcRenderedQuery renderedQuery =
                SpringApiDashboardQueryRenderer.render(visualization.getQueryPlan());
        String graphSlug = uniqueSlug(visualization.getTitle(), graphSlugs);
        String parameterPrefix = isOracle(visualization.getQueryPlan().getDatabaseName()) ? ":" : "@";

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("graphSlug", graphSlug);
        metadata.put("sqlCs", csharpString(
                renderNamedParameters(renderedQuery.getSql(), parameterPrefix)));
        metadata.put("parametersCs", renderParameters(renderedQuery.getParameters()));
        metadata.put("aliasesCs", renderAliases(renderedQuery.getAliases()));
        metadata.put("limitCs", renderedQuery.getLimit() == null ? "null" : renderedQuery.getLimit().toString());
        metadata.put("parameterPrefix", parameterPrefix);
        return metadata;
    }

    private static String renderNamedParameters(String sql, String prefix) {
        Matcher matcher = POSITIONAL_PARAMETER.matcher(sql);
        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            int parameterIndex = Integer.parseInt(matcher.group(1)) - 1;
            matcher.appendReplacement(result, Matcher.quoteReplacement(prefix + "p" + parameterIndex));
        }
        matcher.appendTail(result);
        return result.toString();
    }

    private static boolean isOracle(String databaseName) {
        return databaseName != null && databaseName.toLowerCase(Locale.ROOT).contains("oracle");
    }

    private static String renderParameters(List<Object> parameters) {
        List<String> values = new ArrayList<>();
        if (parameters != null) {
            for (Object parameter : parameters) {
                values.add(csharpLiteral(parameter));
            }
        }
        return "new object?[] { " + String.join(", ", values) + " }";
    }

    private static String renderAliases(List<String> aliases) {
        List<String> values = new ArrayList<>();
        if (aliases != null) {
            for (String alias : aliases) {
                values.add(csharpString(alias));
            }
        }
        return "new string[] { " + String.join(", ", values) + " }";
    }

    private static String csharpLiteral(Object value) {
        if (value == null) {
            return "null";
        }
        if (value instanceof Boolean) {
            return value.toString().toLowerCase(Locale.ROOT);
        }
        if (value instanceof Number) {
            return value.toString();
        }
        return csharpString(value.toString());
    }

    private static String csharpString(String value) {
        return "\"" + (value == null ? "" : value)
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n")
                .replace("\t", "\\t") + "\"";
    }

    private static String uniqueSlug(String value, Set<String> used) {
        String base = value == null ? "" : value.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-|-$", "");
        if (base.isBlank()) {
            base = "dashboard";
        }
        String candidate = base;
        int suffix = 2;
        while (!used.add(candidate)) {
            candidate = base + "-" + suffix++;
        }
        return candidate;
    }

    private static String toPascalCase(String slug) {
        StringBuilder result = new StringBuilder();
        for (String part : slug.split("-")) {
            if (!part.isBlank()) {
                result.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
            }
        }
        return result.toString();
    }
}

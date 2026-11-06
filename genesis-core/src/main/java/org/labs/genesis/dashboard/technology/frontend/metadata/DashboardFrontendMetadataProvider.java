package org.labs.genesis.dashboard.technology.frontend.metadata;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.labs.genesis.dashboard.generation.model.DashboardGenerationModel;
import org.labs.genesis.dashboard.generation.model.DashboardPageGenerationModel;
import org.labs.genesis.dashboard.generation.model.DashboardVisualizationGenerationModel;
import org.labs.genesis.dashboard.query.DashboardQuerySelection;
import org.labs.genesis.dashboard.technology.springmvc.metadata.SpringMvcDashboardMetadataProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class DashboardFrontendMetadataProvider {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private DashboardFrontendMetadataProvider() {
    }

    public static HashMap<String, Object> getDashboardHashMap(DashboardGenerationModel dashboard) {
        HashMap<String, Object> metadata = SpringMvcDashboardMetadataProvider.getDashboardHashMap(dashboard);
        List<Map<String, Object>> pages = dashboard == null || !dashboard.isEnabled()
                ? new ArrayList<>()
                : pageMetadata(dashboard.getPages(), metadata);
        pages = preparePages(pages);
        for (int pageIndex = 0; pageIndex < pages.size(); pageIndex++) {
            Map<String, Object> page = pages.get(pageIndex);
            page.put("endpointPath", "/dashboard/" + page.get("slug"));
            String title = String.valueOf(page.get("title"));
            page.put("titleHtml", htmlEscape(title.isBlank() ? "Dashboard " + (pageIndex + 1) : title));
        }
        metadata.put("dashboardEnabled", !pages.isEmpty());
        metadata.put("dashboardPages", pages);
        metadata.put("dashboardInitialPageSlug", pages.isEmpty() ? "" : pages.get(0).get("slug"));
        metadata.put("dashboardInitialPageSlugLiteral", jsonLiteral(
                pages.isEmpty() ? "" : pages.get(0).get("slug")
        ));
        metadata.put("dashboardInitialPageEndpointPath", pages.isEmpty() ? "" : pages.get(0).get("endpointPath"));
        return metadata;
    }

    private static List<Map<String, Object>> pageMetadata(
            List<DashboardPageGenerationModel> dashboardPages,
            Map<String, Object> metadata) {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> pages = (List<Map<String, Object>>) metadata.get("dashboardPages");
        if (pages == null || dashboardPages == null) {
            return new ArrayList<>();
        }

        Set<String> pageSlugs = new HashSet<>();
        for (int pageIndex = 0; pageIndex < Math.min(pages.size(), dashboardPages.size()); pageIndex++) {
            DashboardPageGenerationModel page = dashboardPages.get(pageIndex);
            Map<String, Object> pageMetadata = pages.get(pageIndex);
            String slug = uniqueSlug(page == null ? null : page.getTitle(), pageSlugs);
            pageMetadata.put("slug", slug);
            addVisualizationSlugs(page, pageMetadata);
        }
        return pages;
    }

    private static void addVisualizationSlugs(
            DashboardPageGenerationModel page,
            Map<String, Object> pageMetadata) {
        if (page == null || page.getVisualizations() == null) {
            return;
        }
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> visualizations =
                (List<Map<String, Object>>) pageMetadata.get("visualizations");
        if (visualizations == null) {
            return;
        }

        Set<String> graphSlugs = new HashSet<>();
        int visualizationIndex = 0;
        for (DashboardVisualizationGenerationModel visualization : page.getVisualizations()) {
            if (visualization == null) {
                continue;
            }
            if (visualizationIndex < visualizations.size()) {
                Map<String, Object> visualizationMetadata = visualizations.get(visualizationIndex);
                visualizationMetadata.put("graphSlug", uniqueSlug(visualization.getTitle(), graphSlugs));
                visualizationMetadata.put("endpointPath", "/dashboard/" + pageMetadata.get("slug"));
                addGeographyCoordinateFields(visualization, visualizationMetadata);
                visualizationIndex++;
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> preparePages(List<Map<String, Object>> pages) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> page : pages) {
            Map<String, Object> generatedPage = new LinkedHashMap<>();
            generatedPage.put("title", page.get("title"));
            generatedPage.put("slug", page.get("slug"));

            List<Map<String, Object>> visualizations =
                    (List<Map<String, Object>>) page.get("visualizations");
            List<Map<String, Object>> generatedVisualizations = new ArrayList<>();
            if (visualizations != null) {
                for (Map<String, Object> visualization : visualizations) {
                    Map<String, Object> generatedVisualization = new LinkedHashMap<>();
                    generatedVisualization.put("title", visualization.get("title"));
                    generatedVisualization.put("type", visualization.get("type"));
                    generatedVisualization.put("graphSlug", visualization.get("graphSlug"));
                    generatedVisualization.put("pageSlug", page.get("slug"));
                    generatedVisualization.put("endpointPath", visualization.get("endpointPath"));
                    Map<String, Integer> layout = gridLayout(visualization);
                    generatedVisualization.put("gridLayout", layout);
                    generatedVisualization.put("layoutJson", jsonLiteral(layout));
                    generatedVisualization.put("layoutJsonHtml", htmlEscape(jsonLiteral(layout)));
                    generatedVisualization.put("options", visualization.get("options"));
                    generatedVisualization.put("titleHtml", htmlEscape(String.valueOf(visualization.get("title"))));

                    List<Map<String, Object>> fields =
                            (List<Map<String, Object>>) visualization.get("fields");
                    List<Map<String, Object>> runtimeFields = new ArrayList<>();
                    if (fields != null) {
                        for (Map<String, Object> field : fields) {
                            Map<String, Object> runtimeField = new LinkedHashMap<>();
                            runtimeField.put("role", field.get("role"));
                            runtimeField.put("key", field.get("key"));
                            runtimeField.put("columnName", field.get("columnName"));
                            runtimeField.put("label", field.get("label"));
                            runtimeFields.add(runtimeField);
                        }
                    }
                    generatedVisualization.put("fields", runtimeFields);
                    generatedVisualization.put("fieldsJson", jsonLiteral(runtimeFields));
                    generatedVisualization.put("fieldsJsonHtml", htmlEscape(String.valueOf(generatedVisualization.get("fieldsJson"))));
                    generatedVisualization.put("optionsJson", jsonLiteral(visualization.get("options")));
                    generatedVisualization.put("optionsJsonHtml", htmlEscape(String.valueOf(generatedVisualization.get("optionsJson"))));
                    generatedVisualizations.add(generatedVisualization);
                }
            }
            generatedPage.put("angularVisualizationsMarkup",
                    angularVisualizationsMarkup(generatedVisualizations));
            generatedPage.put("vueVisualizationsMarkup",
                    vueVisualizationsMarkup(generatedVisualizations));
            generatedPage.put("reactVisualizationsMarkup",
                    reactVisualizationsMarkup(generatedVisualizations));
            result.add(generatedPage);
        }
        return result;
    }

    private static String angularVisualizationsMarkup(List<Map<String, Object>> visualizations) {
        StringBuilder markup = new StringBuilder();
        for (Map<String, Object> visualization : visualizations) {
            markup.append("<app-dashboard-visualization\n")
                    .append("  title=\"").append(visualization.get("titleHtml")).append("\"\n")
                    .append("  type=\"").append(htmlEscape(String.valueOf(visualization.get("type")))).append("\"\n")
                    .append("  fieldsJson=\"").append(visualization.get("fieldsJsonHtml")).append("\"\n")
                    .append("  optionsJson=\"").append(visualization.get("optionsJsonHtml")).append("\"\n")
                    .append("  layoutJson=\"").append(visualization.get("layoutJsonHtml")).append("\"\n")
                    .append("  visualizationId=\"dashboard-")
                    .append(visualization.get("pageSlug")).append("-")
                    .append(visualization.get("graphSlug")).append("\"\n")
                    .append("  [active]=\"activePage === '").append(visualization.get("pageSlug"))
                    .append("'\"\n")
                    .append("  [rows]=\"rows('").append(visualization.get("pageSlug"))
                    .append("', '").append(visualization.get("graphSlug")).append("')\">\n")
                    .append("</app-dashboard-visualization>\n");
        }
        return markup.toString();
    }

    private static String vueVisualizationsMarkup(List<Map<String, Object>> visualizations) {
        StringBuilder markup = new StringBuilder();
        for (Map<String, Object> visualization : visualizations) {
            markup.append("<DashboardVisualization\n")
                    .append("  title=\"").append(visualization.get("titleHtml")).append("\"\n")
                    .append("  type=\"").append(htmlEscape(String.valueOf(visualization.get("type")))).append("\"\n")
                    .append("  graph-slug=\"").append(visualization.get("graphSlug")).append("\"\n")
                    .append("  visualization-id=\"dashboard-").append(visualization.get("pageSlug"))
                    .append("-").append(visualization.get("graphSlug")).append("\"\n")
                    .append("  :fields=\"").append(visualization.get("fieldsJsonHtml")).append("\"\n")
                    .append("  :options=\"").append(visualization.get("optionsJsonHtml")).append("\"\n")
                    .append("  :layout=\"").append(visualization.get("layoutJsonHtml")).append("\"\n")
                    .append("  :rows=\"pageData['").append(visualization.get("pageSlug"))
                    .append("']?.['").append(visualization.get("graphSlug")).append("'] ?? []\"\n")
                    .append("  :active=\"activePage === '").append(visualization.get("pageSlug"))
                    .append("'\" />\n");
        }
        return markup.toString();
    }

    private static String reactVisualizationsMarkup(List<Map<String, Object>> visualizations) {
        StringBuilder markup = new StringBuilder();
        for (Map<String, Object> visualization : visualizations) {
            String pageSlug = String.valueOf(visualization.get("pageSlug"));
            String graphSlug = String.valueOf(visualization.get("graphSlug"));
            markup.append("<DashboardVisualization\n")
                    .append("  title={").append(jsonLiteral(visualization.get("title"))).append("}\n")
                    .append("  type={").append(jsonLiteral(visualization.get("type"))).append("}\n")
                    .append("  visualizationId={").append(jsonLiteral("dashboard-" + pageSlug + "-" + graphSlug))
                    .append("}\n")
                    .append("  fields={").append(visualization.get("fieldsJson")).append("}\n")
                    .append("  options={").append(visualization.get("optionsJson")).append("}\n")
                    .append("  layout={").append(visualization.get("layoutJson")).append("}\n")
                    .append("  rows={pageData['").append(pageSlug).append("']?.['").append(graphSlug)
                    .append("'] ?? []}\n")
                    .append("  loaded={Boolean(pageData['").append(pageSlug).append("'])}\n")
                    .append("  active={activePage === '").append(pageSlug).append("'}\n")
                    .append("/>\n");
        }
        return markup.toString();
    }

    private static Map<String, Integer> gridLayout(Map<String, Object> visualization) {
        Object rawX = visualization.get("layoutX");
        Object rawY = visualization.get("layoutY");
        Object rawWidth = visualization.get("layoutWidth");
        Object rawHeight = visualization.get("layoutHeight");
        if (!(rawX instanceof Number x)
                || !(rawY instanceof Number y)
                || !(rawWidth instanceof Number width)
                || !(rawHeight instanceof Number height)
                || x.intValue() < 0
                || y.intValue() < 0
                || width.intValue() <= 0
                || height.intValue() <= 0) {
            return null;
        }

        Map<String, Integer> layout = new LinkedHashMap<>();
        layout.put("x", x.intValue());
        layout.put("y", y.intValue());
        layout.put("width", width.intValue());
        layout.put("height", height.intValue());
        return layout;
    }

    @SuppressWarnings("unchecked")
    private static void addGeographyCoordinateFields(
            DashboardVisualizationGenerationModel visualization,
            Map<String, Object> visualizationMetadata) {
        if (visualization.getQueryPlan() == null || visualizationMetadata.get("fields") == null) {
            return;
        }
        List<Map<String, Object>> fields =
                (List<Map<String, Object>>) visualizationMetadata.get("fields");
        List<Map<String, Object>> geographicFields = new ArrayList<>();
        for (DashboardQuerySelection selection : visualization.getQueryPlan().getSelections()) {
            if (selection == null || selection.getCoordinateAxis() == null) {
                continue;
            }
            Map<String, Object> field = new HashMap<>();
            field.put("key", selection.getAlias());
            field.put("columnName", selection.getColumnName());
            field.put("label", "");
            field.put("role", selection.getCoordinateAxis().name());
            field.put("statistic", "");
            geographicFields.add(field);
        }
        if (!geographicFields.isEmpty()) {
            fields.removeIf(field -> "GEOGRAPHY".equals(field.get("role")));
            fields.addAll(geographicFields);
        }
    }

    private static String jsonLiteral(Object value) {
        try {
            return OBJECT_MAPPER.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException(
                    "Unable to serialize frontend dashboard value",
                    exception
            );
        }
    }

    private static String htmlEscape(String value) {
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private static String uniqueSlug(String value, Set<String> used) {
        String base = value == null
                ? ""
                : value.toLowerCase(Locale.ROOT)
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
}

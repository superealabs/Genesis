package org.labs.genesis.config.langage;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.labs.genesis.config.Constantes;
import org.labs.utils.FileUtils;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Getter
@Setter
public class FrameworkMVC extends Framework {
    private View view;
    private List<ViewsTemplateEngine> viewsTemplateEngine;
    private List<ViewsTemplate> viewsTemplate;

    public void setViewsTemplateEngine() throws IOException {
        this.viewsTemplateEngine = Arrays.stream(FileUtils.fromYaml(ViewsTemplateEngine[].class, Constantes.VIEWS_TEMPLATE_ENGINE_YAML))
                .filter(vte -> vte.getFrameworkMvcId() == this.getId())
                .collect(Collectors.toList());
    }

    public void setViewsTemplate() throws IOException {
        this.viewsTemplate = Arrays.stream(FileUtils.fromYaml(ViewsTemplate[].class, Constantes.VIEWS_TEMPLATES_YAML))
                .collect(Collectors.toList());
    }

    public ViewsTemplateEngine findViewsTemplateEngineById(int idViewsTemplateEngine) {
        return viewsTemplateEngine.stream()
                .filter(viewsTemplateEngine -> viewsTemplateEngine.getId() == idViewsTemplateEngine)
                .findFirst()
                .orElse(null);
    }

    public ViewsTemplate findViewsTemplateById(int idViewsTemplate) {
        return viewsTemplate.stream()
                .filter(viewsTemplate -> viewsTemplate.getId() == idViewsTemplate)
                .findFirst()
                .orElse(null);
    }

    @Getter
    @Setter
    @ToString
    public static class View {
        private Boolean toGenerate;
        private String viewSavePath;
        private String listViewName;
        private String formViewName;
        private String detailViewName;
    }
}

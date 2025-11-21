package org.labs.genesis.config.langage.generator.sync.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.labs.genesis.config.ProjectGenerationContext;
import org.labs.genesis.config.langage.FrameworkMVC;
import org.labs.genesis.config.langage.ViewsTemplate;
import org.labs.genesis.config.langage.generator.project.ProjectGenerator;
import org.labs.genesis.frontend.FrontendLanguage;
import org.labs.genesis.frontend.generator.FrontendFramework;

import java.io.IOException;

@Getter
@Setter
@NoArgsConstructor
public class FrontendGenerationModel {
    private Boolean useFrontend;
    private int port;
    private int frameworkId;
    private int languageId;
    private int viewsId;

    public FrontendGenerationModel(ProjectGenerationContext context) {
        this.port = Integer.parseInt(context.getFrontendPort());
        this.useFrontend = context.isGenerateFrontendApp();
        if (context.getFrontendFramework() != null) {
            this.frameworkId = context.getFrontendFramework().getId();
        }
        else {
            this.frameworkId = 0;
        }
        if (context.getFrontendLanguage() != null) {
            this.languageId = context.getFrontendLanguage().getId();
        }
        else {
            this.languageId = 0;
        }
        if (context.getViewsTemplate() != null) {
            this.viewsId = context.getViewsTemplate().getId();
        }
        else {
            this.viewsId = 0;
        }
    }

    @JsonIgnore
    public String getPortString() {
        return port + "";
    }

    public void addToContext(ProjectGenerationContext context) throws IOException {
        FrontendLanguage language = ProjectGenerator.findFrontendLanguageById(getLanguageId());
        FrontendFramework framework = ProjectGenerator.findFrontendFrameworkById(getFrameworkId());
        ViewsTemplate viewsTemplate = null;
        if (getViewsId() > 0 && context.getFramework() instanceof FrameworkMVC mvc){
            mvc.setViewsTemplate();
            viewsTemplate = mvc.findViewsTemplateById(getViewsId());
        }
        context.setFrontendLanguage(language);
        context.setFrontendFramework(framework);
        context.setGenerateFrontendApp(getUseFrontend());
        context.setFrontendPort(getPortString());
        context.setViewsTemplate(viewsTemplate);
    }
}
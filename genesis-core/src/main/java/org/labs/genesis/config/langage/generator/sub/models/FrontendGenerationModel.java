package org.labs.genesis.config.langage.generator.sub.models;

import lombok.Getter;
import lombok.Setter;
import org.labs.genesis.config.ProjectGenerationContext;

@Getter
@Setter
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
}
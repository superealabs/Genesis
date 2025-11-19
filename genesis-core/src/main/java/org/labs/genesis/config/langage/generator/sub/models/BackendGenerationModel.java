package org.labs.genesis.config.langage.generator.sub.models;

import lombok.Getter;
import lombok.Setter;
import org.labs.genesis.config.ProjectGenerationContext;

import java.util.List;

@Getter
@Setter
public class BackendGenerationModel {
    private int languageId;
    private int port;
    private int frameworkId;
    private int projectId;
    private String groupId;
    private List<String> generationOptions;

    public BackendGenerationModel(ProjectGenerationContext context) {
        this.port = Integer.parseInt(context.getProjectPort());
        this.languageId = context.getLanguage().getId();
        this.frameworkId = context.getFramework().getId();
        this.projectId = context.getProject().getId();
        this.groupId = context.getGroupLink();
        this.generationOptions = context.getGenerationOptions();
    }
}
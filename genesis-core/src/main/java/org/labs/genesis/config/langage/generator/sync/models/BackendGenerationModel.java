package org.labs.genesis.config.langage.generator.sync.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.labs.genesis.config.ProjectGenerationContext;
import org.labs.genesis.config.langage.Framework;
import org.labs.genesis.config.langage.Language;
import org.labs.genesis.config.langage.Project;
import org.labs.genesis.config.langage.generator.project.ProjectGenerator;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
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

    public void addToContext(ProjectGenerationContext context) {
        Language language = ProjectGenerator.findLanguageById(getLanguageId());
        Framework framework = ProjectGenerator.findFrameworkById(getFrameworkId());
        Project project = ProjectGenerator.findProjectById(getProjectId());

        context.setProjectPort(getPortString());
        context.setFramework(framework);
        context.setLanguage(language);
        context.setProject(project);
        context.setGroupLink(getGroupId());
        context.setGenerationOptions(getGenerationOptions());
    }

    @JsonIgnore
    public String getPortString() {
        return port+"";
    }
}
package org.labs.genesis.config.langage.generator.sub.models;

import lombok.Getter;
import lombok.Setter;
import org.labs.genesis.config.ProjectGenerationContext;
import org.labs.genesis.connexion.model.RelationParameter;
import org.labs.genesis.connexion.model.TableMetadata;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class GenesisFileModel {
    private String projectName;
    private BackendGenerationModel webApi;
    private FrontendGenerationModel frontend;
    private Map<String, Object> languageConfiguration;
    private Map<String, Object> frameworkConfiguration;
    private DatabaseGenerationModel database;
    private List<TableGenerationModel> entities;
    private List<TableGenerationModel> views;
    private List<RelationParameter> relationsRules;

    public GenesisFileModel(ProjectGenerationContext context) {
        this.projectName = context.getProjectName();
        this.webApi = new BackendGenerationModel(context);
        this.languageConfiguration = context.getLanguageConfiguration();
        this.frameworkConfiguration = context.getFrameworkConfiguration();
        this.database = new DatabaseGenerationModel(context);
        this.relationsRules = context.getRelationParameters();
        this.frontend = new FrontendGenerationModel(context);
        if (this.relationsRules == null) {
            this.relationsRules = new ArrayList<>();
        }
        List<TableGenerationModel> entities = new ArrayList<>();
        for (TableMetadata tableMetadata : context.getEntityTables()){
            entities.add(tableMetadata.getGenerationModel());
        }
        this.entities = entities;
        List<TableGenerationModel> views = new ArrayList<>();
        for (TableMetadata tableMetadata : context.getViewTables()){
            views.add(tableMetadata.getGenerationModel());
        }
        this.views = views;
    }
}

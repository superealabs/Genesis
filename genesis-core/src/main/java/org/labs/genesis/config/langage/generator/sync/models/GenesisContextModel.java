package org.labs.genesis.config.langage.generator.sync.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.labs.genesis.config.ProjectGenerationContext;
import org.labs.genesis.connexion.model.RelationParameter;
import org.labs.genesis.connexion.model.TableMetadata;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public class GenesisContextModel {
    private String projectName;
    private BackendGenerationModel webApi;
    private FrontendGenerationModel frontend;
    private Map<String, Object> languageConfiguration;
    private Map<String, Object> frameworkConfiguration;
    private DatabaseGenerationModel database;
    private List<TableGenerationModel> entities;
    private List<TableGenerationModel> views;
    private List<RelationParameter> relationsRules;

    public void addTablesToContext(ProjectGenerationContext context) {
        List<TableMetadata> tables = new ArrayList<>();
        for (TableGenerationModel tableModel : entities) {
            TableMetadata tableMetadata = tableModel.generateTableMetadata();
            tableMetadata.setIsView(false);
            tableMetadata.setDatabase(context.getDatabase());
            tables.add(tableMetadata);
        }
        context.setEntityTables(tables);
    }

    public void addViewsToContext(ProjectGenerationContext context) {
        List<TableMetadata> tables = new ArrayList<>();
        for (TableGenerationModel tableModel : views) {
            TableMetadata tableMetadata = tableModel.generateTableMetadata();
            tableMetadata.setIsView(true);
            tableMetadata.setDatabase(context.getDatabase());
            tables.add(tableMetadata);
        }
        context.setViewTables(tables);
    }
}

package org.labs.genesis.config.langage.generator.sync.builder;

import org.labs.genesis.config.Constantes;
import org.labs.genesis.config.ProjectGenerationContext;
import org.labs.genesis.config.langage.generator.sync.models.*;
import org.labs.genesis.connexion.model.TableMetadata;
import org.labs.utils.FileUtils;

import java.util.ArrayList;
import java.util.List;

public class GenesisContextBuilder {
    public GenesisContextModel buildContextModel(ProjectGenerationContext context) {
        GenesisContextModel genesisContextModel = new GenesisContextModel();
        genesisContextModel.setProjectName(context.getProjectName());
        genesisContextModel.setWebApi(new BackendGenerationModel(context));
        genesisContextModel.setLanguageConfiguration(context.getLanguageConfiguration());
        genesisContextModel.setFrameworkConfiguration(context.getFrameworkConfiguration());
        genesisContextModel.setDatabase(new DatabaseGenerationModel(context));
        genesisContextModel.setRelationsRules(context.getRelationParameters());
        genesisContextModel.setFrontend(new FrontendGenerationModel(context));
        if (genesisContextModel.getRelationsRules() == null) {
            genesisContextModel.setRelationsRules(new ArrayList<>());
        }
        List<TableGenerationModel> entities = new ArrayList<>();
        for (TableMetadata tableMetadata : context.getEntityTables()){
            entities.add(tableMetadata.generateGenerationModel());
        }
        genesisContextModel.setEntities(entities);
        List<TableGenerationModel> views = new ArrayList<>();
        for (TableMetadata tableMetadata : context.getViewTables()){
            views.add(tableMetadata.generateGenerationModel());
        }
        genesisContextModel.setViews(views);
        return genesisContextModel;
    }

    public GenesisContextModel generateGenesisfile(ProjectGenerationContext context) throws Exception {
        GenesisContextModel genesisContextModel = buildContextModel(context);
        String finalContent = FileUtils.toJsonString(genesisContextModel);
        FileUtils.createFile(context.getDestinationFolder(),Constantes.GENESIS_CONTEXT_FILENAME,Constantes.GENESIS_CONTEXT_FILE_EXTENSION, finalContent);
        return genesisContextModel;
    }

    public ProjectGenerationContext buildProjectGenerationContext(GenesisContextModel contextModel) throws Exception {
        ProjectGenerationContext context = new ProjectGenerationContext();
        contextModel.getWebApi().addToContext(context);
        contextModel.getDatabase().addToContext(context);
        contextModel.getFrontend().addToContext(context);
        context.setProjectName(contextModel.getProjectName());
        context.setLanguageConfiguration(contextModel.getLanguageConfiguration());
        context.setFrameworkConfiguration(contextModel.getFrameworkConfiguration());
        context.setRelationParameters(contextModel.getRelationsRules());
        contextModel.addTablesToContext(context);
        contextModel.addViewsToContext(context);
        context.setEntityNamesFromTables(context.getEntityTables());
        context.setViewNamesFromTables(context.getViewTables());
        return context;
    }
}

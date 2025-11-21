package org.labs.genesis.config.langage.generator.sync;

import lombok.Getter;
import lombok.Setter;
import org.labs.genesis.config.ProjectGenerationContext;
import org.labs.genesis.config.langage.generator.project.ProjectGenerator;
import org.labs.genesis.config.langage.generator.sync.builder.DatabaseEvaluatorManagerBuilder;
import org.labs.genesis.config.langage.generator.sync.builder.GenesisContextBuilder;
import org.labs.genesis.config.langage.generator.sync.evaluators.DatabaseEvaluatorsManager;
import org.labs.genesis.config.langage.generator.sync.loader.GenesisContextLoader;
import org.labs.genesis.config.langage.generator.sync.models.GenesisContextModel;
import org.labs.genesis.config.langage.generator.sync.report.DatabaseReportManager;
import org.labs.genesis.connexion.Credentials;
import org.labs.genesis.connexion.Database;
import org.labs.genesis.connexion.model.TableMetadata;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
public class SyncGenerator extends ProjectGenerator {
    private ProjectGenerationContext evaluationContext;
    private DatabaseReportManager databaseReportManager;

    public SyncGenerator() {
        this.databaseReportManager = new DatabaseReportManager();
    }

    public GenesisContextModel loadGenesisFile(String projectPath) throws Exception {
        GenesisContextLoader contextLoader = new GenesisContextLoader();
        return contextLoader.loadContextModel(projectPath);
    }

    public ProjectGenerationContext loadProjectContext(String projectDirectory) throws Exception {
        GenesisContextModel contextModel = loadGenesisFile(projectDirectory);
        GenesisContextBuilder contextBuilder = new GenesisContextBuilder();
        ProjectGenerationContext projectGenerationContext = contextBuilder.buildProjectGenerationContext(contextModel);
        projectGenerationContext.setDestinationFolder(projectDirectory.substring(0, projectDirectory.lastIndexOf("/")));
        return projectGenerationContext;
    }

    public void evaluateDatabaseChanges(ProjectGenerationContext initialContext, EvaluationParameters evaluationParameters) throws Exception {
        this.evaluationContext = initialContext.duplicateWithNoTables();
        Connection connection = initialContext.getConnection();
        Database database = initialContext.getDatabase();
        Credentials credentials = initialContext.getCredentials();
        DatabaseEvaluatorsManager evaluator = DatabaseEvaluatorManagerBuilder.build(evaluationParameters);
        try (Connection connex = (connection != null) ? connection : database.getConnection(credentials)){
            evaluationContext.setConnection(connex);
            evaluationContext.setTables();
            if (evaluationParameters.isEvaluateTables()){
                evaluateEntitiesChanges(initialContext, evaluationContext, evaluator);
            }
            if (evaluationParameters.isEvaluateViews()){
                evaluateViewsChanges(initialContext, evaluationContext, evaluator);
            }
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void evaluateEntitiesChanges(ProjectGenerationContext initialContext, ProjectGenerationContext evaluationContext, DatabaseEvaluatorsManager evaluator) throws Exception {
        List<TableMetadata> initialTables = initialContext.getEntityTables();
        List<TableMetadata> targetTables = evaluationContext.getEntityTables();
        evaluator.evaluate(initialTables, targetTables, databaseReportManager);
    }

    public void evaluateViewsChanges(ProjectGenerationContext initialContext, ProjectGenerationContext evaluationContext, DatabaseEvaluatorsManager evaluator) throws Exception {
        List<TableMetadata> initialViews = initialContext.getViewTables();
        List<TableMetadata> targetTables = evaluationContext.getViewTables();
        evaluator.evaluate(initialViews, targetTables, databaseReportManager);
    }

    @Override
    protected void generateFullProject(ProjectGenerationContext context) throws Exception {
        if (evaluationContext == null) {
            throw new IllegalStateException("Evaluation context is not set. Please run evaluateDatabaseChanges before generating the project.");
        }
        super.generateFullProject(context);
    }

    @Override
    public List<TableMetadata> generateFullProjectComponents(ProjectGenerationContext context, Connection connex, boolean generateComponentOnly) throws Exception {
        evaluationContext.setGenerateFrontendSkeletons(false);
        evaluationContext.setGenerateFrontendStructure(false);
        List<TableMetadata> addedTables = databaseReportManager.getAddedTables();
        List<TableMetadata> updatedTables = databaseReportManager.getUpdatedTables();
        List<TableMetadata> deletedTables = databaseReportManager.getRemovedTables();

        List<TableMetadata> entitiesForGeneration = new ArrayList<>();
        if (addedTables != null) {
            entitiesForGeneration.addAll(addedTables);
        }
        if (updatedTables != null) {
            entitiesForGeneration.addAll(updatedTables);
        }
        super.generateFullProjectComponents(evaluationContext, entitiesForGeneration, generateComponentOnly);

        // Return the full list of tables after applying components updates
        Set<TableMetadata> allTables = new HashSet<>(context.getAllTables());
        allTables.addAll(addedTables);
        deletedTables.forEach(allTables::remove);
        return allTables.stream().toList();
    }
}

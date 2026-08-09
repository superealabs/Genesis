package org.labs.genesis.config.langage.generator.sync;

import lombok.Getter;
import lombok.Setter;
import org.labs.genesis.config.ProjectGenerationContext;
import org.labs.genesis.config.langage.Framework;
import org.labs.genesis.config.langage.Language;
import org.labs.genesis.config.langage.generator.framework.FrameworkMetadataProvider;
import org.labs.genesis.config.langage.generator.indicator.ProgressReporter;
import org.labs.genesis.config.langage.generator.sync.evaluators.IDatabaseEvaluator;
import org.labs.genesis.frontend.FrontendLanguage;
import org.labs.genesis.frontend.generator.FrontendFramework;
import org.labs.genesis.frontend.generator.frameworkFrontend.FrameworkFrontendMetadataProvider;
import org.labs.genesis.remover.APIRemover;
import org.labs.genesis.config.langage.generator.framework.GenesisGenerator;
import org.labs.genesis.config.langage.generator.project.ProjectGenerator;
import org.labs.genesis.config.langage.generator.sync.builder.DatabaseEvaluatorManagerBuilder;
import org.labs.genesis.config.langage.generator.sync.builder.GenesisContextBuilder;
import org.labs.genesis.config.langage.generator.sync.evaluators.DatabaseEvaluatorsManager;
import org.labs.genesis.config.langage.generator.sync.loader.GenesisContextLoader;
import org.labs.genesis.config.langage.generator.sync.models.GenesisContextModel;
import org.labs.genesis.config.langage.generator.sync.report.DatabaseReportManager;
import org.labs.genesis.config.langage.generator.sync.report.FrontendChangeReport;
import org.labs.genesis.config.langage.generator.sync.report.TableChangeReport;
import org.labs.genesis.config.langage.generator.sync.report.WebApiChangeReport;
import org.labs.genesis.connexion.Credentials;
import org.labs.genesis.connexion.Database;
import org.labs.genesis.connexion.model.TableMetadata;
import org.labs.genesis.frontend.generator.IFrontendGenerator;
import org.labs.genesis.remover.FrontendRemover;
import org.labs.genesis.remover.IAPIRemover;
import org.labs.genesis.remover.IFrontendRemover;
import org.labs.utils.FileUtils;

import java.sql.Connection;
import java.util.*;

import static org.labs.genesis.config.ProjectGenerationContext.*;
import static org.labs.genesis.config.ProjectGenerationContext.COMPONENT_CONTROLLER;

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
        try{
            projectGenerationContext.setDestinationFolder(projectDirectory.substring(0, projectDirectory.lastIndexOf("/")));
        }catch(Exception e){
            projectGenerationContext.setDestinationFolder(projectDirectory.substring(0, projectDirectory.lastIndexOf("\\")));
        }
        return projectGenerationContext;
    }

    public void evaluateDatabaseChanges(ProjectGenerationContext initialContext, IDatabaseEvaluator[] evaluators) throws Exception {
        if (this.evaluationContext == null) {
            initEvaluationContext(initialContext);
        }
        evaluateDatabaseChanges(initialContext, this.evaluationContext, new DatabaseEvaluatorsManager(evaluators), databaseReportManager);
    }
    public void evaluateDatabaseChanges(ProjectGenerationContext initialContext, ProjectGenerationContext evaluationContext, IDatabaseEvaluator[] evaluators, DatabaseReportManager databaseReportManager) throws Exception {
        evaluateDatabaseChanges(initialContext, evaluationContext, new DatabaseEvaluatorsManager(evaluators), databaseReportManager);
    }

    public void initEvaluationContext(ProjectGenerationContext initialContext) throws Exception {
        this.evaluationContext = initialContext.duplicateWithNoTables();
        clearReports();
        Connection connection = initialContext.getConnection();
        Database database = initialContext.getDatabase();
        Credentials credentials = initialContext.getCredentials();
        try (Connection connex = (connection != null) ? connection : database.getConnection(credentials)){
            evaluationContext.setConnection(connex);
            evaluationContext.setTables();
            this.evaluationContext.applyTableRelations();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void evaluateDatabaseChanges(ProjectGenerationContext initialContext) throws Exception {
        if (this.evaluationContext == null) {
            initEvaluationContext(initialContext);
        }
        evaluateDatabaseChanges(initialContext, this.evaluationContext, DatabaseEvaluatorManagerBuilder.build(), this.databaseReportManager);
    }

    public void clearReports() {
        this.databaseReportManager.clear();
    }

    public void evaluateDatabaseChanges(ProjectGenerationContext initialContext, ProjectGenerationContext evaluationContext, DatabaseEvaluatorsManager evaluator, DatabaseReportManager databaseReportManager) throws Exception {
        evaluationContext.setRelationParameters(initialContext.getRelationParameters());
        this.evaluationContext.applyTableRelations();
        evaluateEntitiesChanges(initialContext, evaluationContext, evaluator, databaseReportManager);
        evaluateViewsChanges(initialContext, evaluationContext, evaluator, databaseReportManager);
    }

    public void evaluateEntitiesChanges(ProjectGenerationContext initialContext, ProjectGenerationContext evaluationContext, DatabaseEvaluatorsManager evaluator, DatabaseReportManager databaseReportManager) throws Exception {
        List<TableMetadata> initialTables = initialContext.getEntityTables();
        List<TableMetadata> targetTables = evaluationContext.getEntityTables();
        evaluator.evaluate(initialTables, targetTables, databaseReportManager);
    }

    public void evaluateViewsChanges(ProjectGenerationContext initialContext, ProjectGenerationContext evaluationContext, DatabaseEvaluatorsManager evaluator, DatabaseReportManager databaseReportManager) throws Exception {
        List<TableMetadata> initialViews = initialContext.getViewTables();
        List<TableMetadata> targetTables = evaluationContext.getViewTables();
        evaluator.evaluate(initialViews, targetTables, databaseReportManager);
    }



    @Override
    protected void generateFullProject(ProjectGenerationContext context, ProgressReporter indicator) throws Exception {
        if (evaluationContext == null) {
            throw new IllegalStateException("Evaluation context is not set. Please run evaluateDatabaseChanges before generating the project.");
        }
        evaluationContext.setProjectName(evaluationContext.getProjectName());
        super.generateFullProject(context, indicator);
    }

    @Override
    public GenesisContextModel generateGenesisfile(ProjectGenerationContext context, ProgressReporter indicator) throws Exception {
        return super.generateGenesisfile(evaluationContext, indicator);
    }

    @Override
    public List<TableMetadata> generateFullProjectComponents(ProjectGenerationContext context, Connection connex, boolean generateComponentOnly, ProgressReporter indicator) throws Exception {
        indicator.setText("Setting up generation context for synchronization...");
        indicator.setFraction(0.2);
        evaluationContext.setGenerateFrontendSkeletons(false);
        evaluationContext.setGenerateFrontendStructure(false);
        evaluationContext.setGenerateProjectStructure(false);
        List<TableMetadata> addedTables = databaseReportManager.getAddedTables();
        List<TableMetadata> updatedTables = databaseReportManager.getUpdatedTables();
        List<TableMetadata> deletedTables = databaseReportManager.getRemovedTables();
        Set<TableMetadata> allTables = new HashSet<>(context.getAllTables());

        List<TableMetadata> entitiesForGeneration = new ArrayList<>();
        if (deletedTables != null && !deletedTables.isEmpty()){
            indicator.setText("Removing components for deleted tables...");
            indicator.setFraction(0.2);
            deletedTables.forEach(allTables::remove);
            removeFullProjectComponents(evaluationContext, deletedTables, generateComponentOnly);
        }
        if (addedTables != null && !addedTables.isEmpty()) {
            indicator.setText("Generating components for added tables...");
            indicator.setFraction(0.3);
            allTables.addAll(addedTables);
            entitiesForGeneration.addAll(addedTables);
        }
        if (updatedTables != null && !updatedTables.isEmpty()){
            indicator.setText("Updating components for modified tables...");
            indicator.setFraction(0.3);
            entitiesForGeneration.addAll(updatedTables);
        }
        super.generateFullProjectComponents(evaluationContext, entitiesForGeneration, generateComponentOnly, indicator);
        return allTables.stream().toList();
    }

    @Override
    public void generateFullProjectStrucutres(ProjectGenerationContext context, List<TableMetadata> allEntities, ProgressReporter indicator) throws Exception {
        super.generateFullProjectStrucutres(evaluationContext, allEntities, indicator);
    }

    @Override
    public void generateBackendComponents(ProjectGenerationContext context, GenesisGenerator genesisGenerator, TableMetadata tableMetadata, boolean generateComponentOnly) throws Exception {
        TableChangeReport report = databaseReportManager.getTableReport(tableMetadata.getTableName());
        WebApiChangeReport webApiChangeReport = report.getWebApiChangeReport();
        context.applyWebApiChangeReport(webApiChangeReport);
        super.generateBackendComponents(context, genesisGenerator, tableMetadata, generateComponentOnly);
    }

    @Override
    public void generateFrontendComponents(ProjectGenerationContext context, IFrontendGenerator frontendGenerator, TableMetadata tableMetadata, boolean generateComponentOnly) throws Exception {
        TableChangeReport report = databaseReportManager.getTableReport(tableMetadata.getTableName());
        FrontendChangeReport frontendChangeReport = report.getFrontendChangeReport();
        context.applyFrontendChangeReport(frontendChangeReport);
        super.generateFrontendComponents(context, frontendGenerator, tableMetadata, generateComponentOnly);
    }

    public void removeBackendComponents(ProjectGenerationContext context, IAPIRemover apiRemover, TableMetadata tableMetadata, boolean generateComponentOnly) throws Exception {
        String renderedDestinationFolder = engine.simpleRender(context.getDestinationFolder(), Map.of("projectName", context.getProjectName()));
        List<String> generationOptions = context.getGenerationOptions();
        Framework framework = context.getFramework();
        Language language = context.getLanguage();
        String projectName = context.getProjectName();
        String groupLink = context.getGroupLink();
        Map<String, Object> frameworkOptions = context.getFrameworkConfiguration();

        if (generationOptions.contains(COMPONENT_MODEL) && framework.getModel().getToGenerate()) {
            apiRemover.removeModel(framework, frameworkOptions, language, tableMetadata, renderedDestinationFolder, projectName, groupLink, generateComponentOnly);
        }

        if (generationOptions.contains(COMPONENT_DAO) && framework.getModelDao() != null && framework.getModelDao().getToGenerate()) {
            apiRemover.removeDao(framework, frameworkOptions, language, tableMetadata, renderedDestinationFolder, projectName, groupLink, generateComponentOnly);
        }

        if (generationOptions.contains(COMPONENT_SERVICE) && framework.getService() != null && framework.getService().getToGenerate()) {
            apiRemover.removeService(framework, frameworkOptions, language, tableMetadata, renderedDestinationFolder, projectName, groupLink, generateComponentOnly);
        }

        if (generationOptions.contains(COMPONENT_CONTROLLER) && framework.getController() != null && framework.getController().getToGenerate()) {
            apiRemover.removeController(framework, frameworkOptions, language, tableMetadata, renderedDestinationFolder, projectName, groupLink, generateComponentOnly);
        }

        HashMap<String, Object> tableHashMapData = FrameworkMetadataProvider.getHashMapIntermediaire(language, tableMetadata, framework, frameworkOptions, renderedDestinationFolder, projectName, groupLink);
        if (tableMetadata.getIsParent()){
            removeFilesEdits(framework.getMereFiles(),tableHashMapData);
        }
        if (tableMetadata.getIsChild()){
            removeFilesEdits(framework.getFilleFiles(),tableHashMapData);
        }
    }

    public void removeBackendComponents(ProjectGenerationContext context, TableMetadata tableMetadata, boolean generateComponentOnly) throws Exception {
        removeBackendComponents(context, new APIRemover(engine), tableMetadata, generateComponentOnly);
    }

    public void removeFrontendComponents(ProjectGenerationContext context, IFrontendRemover frontendRemover, TableMetadata tableMetadata, boolean generateComponentOnly) throws Exception {
        String webappFolder = FrameworkFrontendMetadataProvider.getWebappFolder(context);
        FrontendLanguage frontendLanguage=context.getFrontendLanguage();
        FrontendFramework frontendFramework=context.getFrontendFramework();
        if (frontendLanguage == null || frontendFramework == null) {
            return;
        }
        String projectName=context.getProjectName();

        if (context.getFrontendGenerationOptions() != null && context.getFrontendGenerationOptions().contains(ProjectGenerationContext.FRONTEND_COMPONENT)) {
            frontendRemover.removeComponent(frontendLanguage,frontendFramework,tableMetadata,webappFolder, projectName,  generateComponentOnly);
        }

        if (context.getFrontendGenerationOptions() != null && context.getFrontendGenerationOptions().contains(ProjectGenerationContext.FRONTEND_COMPONENT_SERVICE)) {
            frontendRemover.removeService(frontendLanguage, frontendFramework, tableMetadata, webappFolder, projectName, generateComponentOnly);
        }

        if (context.getFrontendGenerationOptions() != null && context.getFrontendGenerationOptions().contains(ProjectGenerationContext.FRONTEND_COMPONENT_MODEL)) {
            frontendRemover.removeModel(frontendLanguage, frontendFramework, tableMetadata, webappFolder, projectName, generateComponentOnly);
        }

        HashMap<String, Object> tableHashMapData = FrameworkFrontendMetadataProvider.getHashMapIntermediaire(tableMetadata, webappFolder, projectName);
        if (tableMetadata.getIsParent()){
            removeFilesEdits(frontendFramework.getMereFiles(),tableHashMapData);
        }
        if (tableMetadata.getIsChild()){
            removeFilesEdits(frontendFramework.getFilleFiles(),tableHashMapData);
        }
    }
    public void removeFrontendComponents(ProjectGenerationContext context, TableMetadata tableMetadata, boolean generateComponentOnly) throws Exception {
        removeFrontendComponents(context, new FrontendRemover(engine), tableMetadata, generateComponentOnly);
    }

    public  void removeFullProjectComponents(ProjectGenerationContext context, List<TableMetadata> toDeleteComponents ,boolean generateComponentOnly) throws Exception {
        APIRemover apiRemover = new APIRemover(SyncGenerator.engine);
        FrontendRemover frontendRemover = new FrontendRemover(SyncGenerator.engine);
        for (TableMetadata tableMetadata : toDeleteComponents) {
            removeBackendComponents(context, apiRemover, tableMetadata, generateComponentOnly);
            removeFrontendComponents(context, frontendRemover, tableMetadata, generateComponentOnly);
        }
    }
}

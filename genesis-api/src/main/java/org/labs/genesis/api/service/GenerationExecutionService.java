package org.labs.genesis.api.service;

import org.labs.genesis.api.dto.GenerationDtos;

import org.labs.genesis.api.dto.GenerationDtos.*;

import org.labs.genesis.api.exception.GenerationAlreadyRunningException;
import org.labs.utils.GenerationWriteScope;

import org.labs.genesis.api.dto.GenerationDtos.ExecutionBatch;
import org.labs.genesis.api.service.FrontendPlanningService.FrontendExecutionTask;
import org.labs.genesis.config.ProjectGenerationContext;
import org.labs.genesis.config.langage.FrameworkMVC;
import org.labs.genesis.config.langage.generator.project.ProjectGenerator;
import org.labs.genesis.frontend.generator.ViewsGenerator;
import org.labs.genesis.connexion.model.TableMetadata;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.labs.utils.FileUtils;
import org.labs.utils.enums.FilesUtilsMode;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class GenerationExecutionService {

    private final GenerationConfigurationSnapshotService snapshotService;
    private final GenerationPreflightService preflightService;
    private final GenerationStagingService stagingService;
    private final BackendBatchGenerationService backendService;
    private final GenerationExecutionContextFactory contextFactory;
    private final MvcSharedResourcesGenerationService mvcResources;
    private final GenerationOutputPathService outputPathService;
    private final ConsolidatedFrontendGenerationService consolidatedFrontendService;
    private final GenerationPublicationService publicationService;
    private final GitLocalService gitLocalService;
    private final ReentrantLock generationLock = new ReentrantLock();

    public GenerationExecutionService(
            GenerationConfigurationSnapshotService snapshotService,
            GenerationPreflightService preflightService,
            GenerationStagingService stagingService,
            BackendBatchGenerationService backendService,
            GenerationExecutionContextFactory contextFactory,
            MvcSharedResourcesGenerationService mvcResources,
            GenerationOutputPathService outputPathService,
            ConsolidatedFrontendGenerationService consolidatedFrontendService,
            GenerationPublicationService publicationService,
            GitLocalService gitLocalService
    ) {
        this.snapshotService = snapshotService;
        this.preflightService = preflightService;
        this.stagingService = stagingService;
        this.backendService = backendService;
        this.contextFactory = contextFactory;
        this.mvcResources = mvcResources;
        this.outputPathService = outputPathService;
        this.consolidatedFrontendService = consolidatedFrontendService;
        this.publicationService = publicationService;
        this.gitLocalService = gitLocalService;
    }

    private PreparedGeneration prepareGeneration(GenerationConfigurationSnapshot snapshot) throws Exception {
        var preflight = preflightService.prepare(snapshot);
        List<ExecutionBatch> backendBatches = preflight.executionPlan().batches();
        List<FrontendExecutionTask> frontendTasks = preflight.frontendTasks();
        if (backendBatches.isEmpty()) {
            throw new IllegalStateException("Le plan ne contient aucun composant à générer");
        }

        Path targetDirectory;
        GitLocalService.GitPlan gitPlan;
        boolean mvc;

        // Lire les paramètres du projet avant la génération.
        try (var execution = contextFactory.openConsolidated(snapshot)) {
            ProjectGenerationContext context = execution.context();
            targetDirectory = outputPathService.resolveProjectTarget(context);
            gitPlan = gitLocalService.plan(context, gitLocalService.configuration());
            mvc = context.getFramework() instanceof FrameworkMVC;
        }

        Path workspace = stagingService.createWorkspace();
        boolean prepared = false;
        Exception preparationFailure = null;

        try {
            try (GenerationWriteScope scope = GenerationWriteScope.open(workspace)) {

                List<Path> generatedRoots = new ArrayList<>();

                // 1. Génération des composants backend.
                generatedRoots.addAll(
                        backendService.generate(
                                backendBatches,
                                workspace,
                                snapshot
                        )
                );

                int nextBatchIndex = backendBatches.size();


                Path structureDirectory = stagingService.createBatchDirectory(workspace,nextBatchIndex++);

                try (var execution = contextFactory.openConsolidated(snapshot)) {

                    ProjectGenerationContext context = execution.context();

                    Path structureRoot = outputPathService.configureStaging(context,structureDirectory);

                    context.applyTableRelations();

                    ProjectGenerator generator = new ProjectGenerator();

                    if ("Django".equalsIgnoreCase(context.getFramework().getCoreFramework())) {
                        Map<String, Object> options = new HashMap<>(context.getFrameworkConfiguration());

                        // Ne pas lancer de processus système
                        // pendant la préparation temporaire.
                        options.put("createVenv", false);

                        context.setFrameworkConfiguration(options);
                    }


                    generator.generateBackendProjectStructure(context);

                    if (mvc) {
                        generator.generateMvcViewStructure(context);
                    }

                    if (Files.isDirectory(structureRoot)) {
                        try (var files = Files.walk(structureRoot)) {
                            if (files.anyMatch(Files::isRegularFile)) {
                                generatedRoots.add(structureRoot);
                            }
                        }
                    }
                }

                if (mvc) {

                    // 2. Génération des pages MVC complètes.
                    for (FrontendExecutionTask task : frontendTasks) {

                        if (!task.mvc() || !task.completeDisplay()) {
                            continue;
                        }

                        Path batchDirectory = stagingService.createBatchDirectory(workspace, nextBatchIndex++);

                        try (var execution = contextFactory.openConsolidated(snapshot)) {

                            ProjectGenerationContext context = execution.context();

                            context.applyTableRelations();

                            Path generatedRoot = outputPathService.configureStaging(context, batchDirectory);

                            TableMetadata entity = findEntity(context, task.entityName(), task.view());

                            ProjectGenerator generator = new ProjectGenerator();

                            generator.generateViewsComponents(context, new ViewsGenerator(ProjectGenerator.engine), entity, true);
                            generatedRoots.add(generatedRoot);
                        }
                    }

                    // 3. Générer une seule fois les ressources MVC.
                    boolean hasMvcPages = frontendTasks.stream()
                            .anyMatch(task -> task.mvc() && task.completeDisplay());

                    if (hasMvcPages) {
                        Path sharedDirectory = stagingService.createBatchDirectory(workspace, nextBatchIndex++);

                        try (var execution = contextFactory.openConsolidated(snapshot)) {

                            ProjectGenerationContext context = execution.context();

                            context.applyTableRelations();

                            Path generatedRoot = outputPathService.configureStaging(context, sharedDirectory);

                            mvcResources.generate(context, frontendTasks, sharedDirectory);

                            generatedRoots.add(generatedRoot);
                        }
                    }

                } else {

                    // 4. Frontend séparé : React, Vue, Angular, etc.
                    // Tous les composants partagent un seul contexte
                    // pour conserver les routes cumulées.

                    boolean hasFrontendTasks = frontendTasks.stream()
                            .anyMatch(task -> !task.mvc() && task.options() != null && !task.options().isEmpty());

                    if (hasFrontendTasks) {
                        Path frontendDirectory = stagingService.createBatchDirectory(workspace, nextBatchIndex++);

                        Path frontendRoot = consolidatedFrontendService.generate(frontendTasks, frontendDirectory, snapshot);

                        if (frontendRoot != null) {
                            generatedRoots.add(frontendRoot);
                        }
                    }
                }

                if (generatedRoots.isEmpty()) {
                    throw new IllegalStateException("Aucun fichier n'a été préparé");
                }

                PreparedGeneration result = new PreparedGeneration(
                        workspace,
                        targetDirectory,
                        gitPlan,
                        List.copyOf(generatedRoots),
                        backendBatches.size(),
                        frontendTasks.size()
                );

                prepared = true;
                return result;
            } catch (Exception exception) {
                preparationFailure = exception;
                throw exception;
            }
        } finally {
            if (!prepared) {
                try {
                    stagingService.deleteWorkspace(workspace);
                } catch (IOException cleanupException) {
                    if (preparationFailure != null) {
                        preparationFailure.addSuppressed(cleanupException);
                    } else {
                        throw cleanupException;
                    }
                }
            }
        }

    }

    /**
     * Consolide et publie les fichiers préparés.
     */
    private GenerationDtos.PublicationReport publishPreparedGeneration(PreparedGeneration prepared) throws Exception {
        if (prepared == null || prepared.generatedRoots().isEmpty()) {
            throw new IllegalStateException("Aucun fichier à publier");
        }

        return publicationService.consolidateAndPublish(
                prepared.generatedRoots(),
                prepared.workspace(),
                prepared.targetDirectory()
        );
    }

    /**
     * Point d'entrée de la génération.
     *
     * Préparation -> consolidation -> publication -> nettoyage.
     */
    public LaunchResult launch() throws Exception {
        if (!generationLock.tryLock()) {
            throw new GenerationAlreadyRunningException();
        }
        try {
            try (AutoCloseable mode = FileUtils.useExecutionMode(FilesUtilsMode.GENERATION)) {
                GenerationConfigurationSnapshot snapshot = snapshotService.capture();
                PreparedGeneration prepared = prepareGeneration(snapshot);
                Exception primaryFailure = null;
                try {
                    var publication = publishPreparedGeneration(prepared);
                    gitLocalService.init(prepared.gitPlan());
                    return new LaunchResult(true, prepared.backendBatchCount(), prepared.frontendTaskCount(), publication);
                } catch (Exception exception) {
                    primaryFailure = exception;
                    throw exception;
                } finally {
                    try {
                        stagingService.deleteWorkspace(prepared.workspace());
                    } catch (IOException cleanupException) {
                        if (primaryFailure != null) {
                            primaryFailure.addSuppressed(cleanupException);
                        } else {
                            throw cleanupException;
                        }
                    }
                }
            }
        } finally {
            generationLock.unlock();
        }
    }

    private TableMetadata findEntity(ProjectGenerationContext context, String entityName, boolean view) {
        List<TableMetadata> tables = view ? context.getViewTables() : context.getEntityTables();

        if (tables == null) {
            throw new IllegalStateException("Métadonnées indisponibles : " + entityName);
        }

        return tables.stream()
                .filter(table -> table.getTableName().equalsIgnoreCase(entityName))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Entité introuvable : " + entityName));
    }

    public record PreparedGeneration(
            Path workspace,
            Path targetDirectory,
            GitLocalService.GitPlan gitPlan,
            List<Path> generatedRoots,
            int backendBatchCount,
            int frontendTaskCount
    ) {
        public PreparedGeneration {
            generatedRoots = List.copyOf(generatedRoots);
        }
    }


}
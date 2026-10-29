package org.labs.genesis.api.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.labs.genesis.api.dto.GenerationDtos.ExecutionBatch;
import org.labs.genesis.api.exception.TableSelectionException;
import org.labs.genesis.config.ProjectGenerationContext;
import org.labs.genesis.config.langage.generator.framework.APIGenerator;
import org.labs.genesis.config.langage.generator.project.ProjectGenerator;
import org.labs.genesis.connexion.model.TableMetadata;
import org.springframework.stereotype.Service;

@Service
public class BackendBatchGenerationService {

    private final GenerationExecutionContextFactory contextFactory;
    private final GenerationStagingService stagingService;
    private final GenerationOutputPathService outputPathService;

    public BackendBatchGenerationService(GenerationExecutionContextFactory contextFactory, GenerationStagingService stagingService,  GenerationOutputPathService outputPathService) {
        this.contextFactory = contextFactory;
        this.stagingService = stagingService;
        this.outputPathService = outputPathService;
    }


    public List<Path> generate(List<ExecutionBatch> batches, Path stagingRoot, GenerationConfigurationSnapshot snapshot) throws Exception {
        if (batches == null || batches.isEmpty()) {
            throw new IllegalArgumentException("Aucun lot backend à générer");
        }
        List<Path> generatedRoots = new ArrayList<>();
        ProjectGenerator generator = new ProjectGenerator();
        APIGenerator backendGenerator = new APIGenerator(ProjectGenerator.engine);
        for (int index = 0; index < batches.size(); index++) {
            ExecutionBatch batch = batches.get(index);
            // 1. Créer un dossier temporaire propre au lot.
            Path batchDirectory = stagingService.createBatchDirectory(stagingRoot, index);
            // 2. Ouvrir un contexte et une connexion JDBC
            // indépendants pour le profil.
            try (var execution = contextFactory.open(batch.profileName(), snapshot)) {
                ProjectGenerationContext context = execution.context();
                // 3. Configurer le dossier temporaire et
                // récupérer le chemin réellement utilisé par Core.
                Path generatedRoot = outputPathService.configureStaging(context, batchDirectory);
                // 4. Ne conserver que les composants de ce lot.
                context.setGenerationOptions(mapComponents(batch.components()));
                // 5. Récupérer les métadonnées de l'entité.
                TableMetadata table = findEntity(context, batch.entityName(), batch.view());
                // 6. Appliquer les relations configurées.
                context.applyTableRelations();
                // 7. Générer les composants backend sans
                // les fichiers relationnels complémentaires.
                generator.generateBackendComponents(context, backendGenerator, table, false, true);
                // 8. Vérifier que Core a créé le dossier attendu.
                if (!Files.isDirectory(generatedRoot)) {
                    throw new IOException(
                            "Répertoire de génération introuvable pour "
                                    + batch.entityName()
                                    + " : "
                                    + generatedRoot
                    );
                }
                // 9. Conserver le chemin pour la consolidation.
                generatedRoots.add(generatedRoot);
            }
        }
        return List.copyOf(generatedRoots);
    }

    private TableMetadata findEntity(ProjectGenerationContext context, String entityName, boolean view) {
        List<TableMetadata> entities = view
                ? context.getViewTables()
                : context.getEntityTables();

        if (entities == null) {
            throw new TableSelectionException("Métadonnées indisponibles : " + entityName);
        }

        return entities.stream()
                .filter(entity -> entity.getTableName().equals(entityName))
                .findFirst()
                .orElseThrow(() -> new TableSelectionException("Entité introuvable dans le profil : " + entityName));
    }

    private List<String> mapComponents(List<String> components) {
        List<String> mapped = new ArrayList<>();
        for (String component : components) {
            String normalized = component.toLowerCase(Locale.ROOT);
            String value = switch (normalized) {
                case "model" ->
                        ProjectGenerationContext.COMPONENT_MODEL;
                case "dao", "repository" ->
                        ProjectGenerationContext.COMPONENT_DAO;
                case "service" ->
                        ProjectGenerationContext.COMPONENT_SERVICE;
                case "controller" ->
                        ProjectGenerationContext.COMPONENT_CONTROLLER;
                default -> throw new TableSelectionException(
                        "Composant inconnu : " + component
                );
            };
            mapped.add(value);
        }
        return mapped;
    }
}

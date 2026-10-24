package org.labs.genesis.api.service;

import org.labs.genesis.api.exception.TableSelectionException;
import org.labs.genesis.config.ProjectGenerationContext;
import org.labs.genesis.config.langage.FrameworkMVC;
import org.labs.genesis.config.langage.generator.project.ProjectGenerator;
import org.labs.genesis.connexion.model.TableMetadata;
import org.labs.genesis.frontend.generator.FontendGenerator;
import org.labs.genesis.frontend.generator.ViewsGenerator;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@Service
public class FrontendBatchGenerationService {

    private final GenerationExecutionContextFactory contextFactory;
    private final GenerationOutputPathService outputPathService;

    public FrontendBatchGenerationService(GenerationExecutionContextFactory contextFactory, GenerationOutputPathService outputPathService) {
        this.contextFactory = contextFactory;
        this.outputPathService = outputPathService;
    }

    /**
     * Génère une tâche frontend ou une vue MVC
     * dans un répertoire temporaire indépendant.
     *
     * @return le répertoire réel contenant les fichiers,
     *         ou null si la tâche ne nécessite aucune génération.
     */
    public Path generate(FrontendPlanningService.FrontendExecutionTask task, String profileName, Path stagingDirectory) throws Exception {
        Objects.requireNonNull(task, "La tâche frontend est obligatoire");
        Objects.requireNonNull(stagingDirectory, "Le dossier temporaire est obligatoire");

        // Aucune génération nécessaire.
        if (!task.mvc() && (task.options() == null || task.options().isEmpty())) {
            return null;
        }

        // L'affichage MVC exige tous les composants cumulés.
        if (task.mvc() && !task.completeDisplay()) {
            throw new IllegalStateException("Affichage MVC interdit : composants incomplets pour " + task.entityName());
        }

        try (var execution = contextFactory.open(profileName)) {
            ProjectGenerationContext context = execution.context();

            // Configurer le dossier temporaire et récupérer
            // le véritable chemin de génération de Core.
            Path generatedRoot = outputPathService.configureStaging(context, stagingDirectory);
            TableMetadata entity = findEntity(context, task.entityName(), task.view());
            // Appliquer les relations du profil.
            context.applyTableRelations();
            ProjectGenerator generator = new ProjectGenerator();
            if (task.mvc()) {
                generateMvcViews(generator, context, entity, task);
            } else {
                generateFrontend(generator, context, entity, task);
            }

            // Vérifier que Core a bien créé le dossier.
            if (!Files.isDirectory(generatedRoot)) {
                throw new IOException("Répertoire de génération introuvable pour " + task.entityName() + " : " + generatedRoot);
            }

            // Vérifier qu'au moins un fichier a été produit.
            try (Stream<Path> files = Files.walk(generatedRoot)) {
                boolean hasFiles = files.anyMatch(Files::isRegularFile);
                if (!hasFiles) {
                    throw new IOException("Aucun fichier frontend généré pour " + task.entityName());
                }
            }
            return generatedRoot;
        }
    }

    private void generateMvcViews(ProjectGenerator generator, ProjectGenerationContext context, TableMetadata entity, FrontendPlanningService.FrontendExecutionTask task) throws Exception {
        if (!(context.getFramework() instanceof FrameworkMVC)) {
            throw new IllegalStateException("Le framework sélectionné n'est pas MVC");
        }
        if (!task.completeDisplay()) {
            throw new IllegalStateException("Affichage MVC interdit pour " + task.entityName());
        }

        generator.generateViewsComponents(context, new ViewsGenerator(ProjectGenerator.engine), entity, true);
    }

    private void generateFrontend(ProjectGenerator generator, ProjectGenerationContext context, TableMetadata entity, FrontendPlanningService.FrontendExecutionTask task) throws Exception {

        if (!context.isGenerateFrontendApp() || context.getFrontendFramework() == null || context.getFrontendLanguage() == null) {
            throw new IllegalStateException("Frontend non configuré");
        }

        List<String> options = new ArrayList<>(task.options());
        // Protection supplémentaire contre un plan invalide.
        if (!task.completeDisplay() && options.contains(ProjectGenerationContext.FRONTEND_COMPONENT)) {
            throw new IllegalStateException("Affichage complet interdit pour " + task.entityName());
        }

        if (options.isEmpty()) {
            return;
        }

        // Les options sont propres à cette tâche.
        context.setFrontendGenerationOptions(options);
        generator.generateFrontendComponents(context, new FontendGenerator(ProjectGenerator.engine), entity, true, false);
    }

    private TableMetadata findEntity(ProjectGenerationContext context, String entityName, boolean view) {

        List<TableMetadata> entities = view
                        ? context.getViewTables()
                        : context.getEntityTables();

        if (entities == null) {
            throw new TableSelectionException("Métadonnées indisponibles : " + entityName);
        }
        return entities.stream()
                .filter(entity -> entity.getTableName().equalsIgnoreCase(entityName))
                .findFirst()
                .orElseThrow(() -> new TableSelectionException("Entité introuvable : " + entityName));
    }
}

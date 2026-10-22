package org.labs.genesis.api.service;

import org.labs.genesis.config.ProjectGenerationContext;
import org.labs.genesis.config.langage.FrameworkMVC;
import org.labs.genesis.config.langage.generator.project.ProjectGenerator;
import org.labs.genesis.connexion.model.TableMetadata;
import org.labs.genesis.frontend.generator.FontendGenerator;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@Service
public class ConsolidatedFrontendGenerationService {

    private final GenerationExecutionContextFactory contextFactory;
    private final GenerationOutputPathService outputPathService;

    public ConsolidatedFrontendGenerationService(
            GenerationExecutionContextFactory contextFactory,
            GenerationOutputPathService outputPathService
    ) {
        this.contextFactory = contextFactory;
        this.outputPathService = outputPathService;
    }

    public Path generate(
            List<FrontendExecutionTaskPlanner.FrontendExecutionTask> tasks,
            Path stagingDirectory,
            GenerationConfigurationSnapshot snapshot
    ) throws Exception {

        Objects.requireNonNull(tasks, "Tâches frontend obligatoires");
        Objects.requireNonNull(stagingDirectory, "Dossier temporaire obligatoire");
        Objects.requireNonNull(snapshot, "Instantané obligatoire");

        List<FrontendExecutionTaskPlanner.FrontendExecutionTask> selected =
                tasks.stream()
                        .filter(Objects::nonNull)
                        .filter(task -> !task.mvc())
                        .filter(task ->
                                task.options() != null
                                        && !task.options().isEmpty()
                        )
                        .toList();

        if (selected.isEmpty()) {
            return null;
        }

        // Un seul contexte JDBC et frontend pour toutes les entités.
        try (var execution = contextFactory.openConsolidated(snapshot)) {

            ProjectGenerationContext context = execution.context();

            if (context.getFramework() instanceof FrameworkMVC) {
                throw new IllegalStateException(
                        "Ce service est réservé aux frontends séparés"
                );
            }

            if (!context.isGenerateFrontendApp()
                    || context.getFrontendFramework() == null
                    || context.getFrontendLanguage() == null) {
                throw new IllegalStateException(
                        "Frontend non configuré"
                );
            }

            Path generatedRoot =
                    outputPathService.configureStaging(
                            context,
                            stagingDirectory
                    );

            // Appliquer les relations du contexte consolidé.
            context.applyTableRelations();

            ProjectGenerator generator = new ProjectGenerator();

            FontendGenerator frontendGenerator =
                    new FontendGenerator(ProjectGenerator.engine);

            // Initialiser la structure frontend une seule fois.
            ProjectGenerator.initFrontendProjectFiles(context);

            List<TableMetadata> eligibleDisplays = new ArrayList<>();

            // Les routes doivent être initialement vides.
            // Cette garantie doit provenir de l'isolation
            // réalisée dans GenerationExecutionContextFactory.

            for (var task : selected) {

                TableMetadata entity = findEntity(
                        context,
                        task.entityName(),
                        task.view()
                );

                List<String> options =
                        new ArrayList<>(task.options());

                // Ne jamais générer l'affichage complet
                // si les composants backend sont incomplets.
                if (!task.completeDisplay()
                        && options.contains(
                        ProjectGenerationContext.FRONTEND_COMPONENT
                )) {
                    throw new IllegalStateException(
                            "Affichage complet interdit pour "
                                    + task.entityName()
                    );
                }

                // Chaque entité conserve ses options frontend.
                context.setFrontendGenerationOptions(options);

                generator.generateFrontendComponents(
                        context,
                        frontendGenerator,
                        entity,
                        true,
                        false
                );

                if (task.completeDisplay()
                        && options.contains(
                        ProjectGenerationContext.FRONTEND_COMPONENT
                )) {
                    eligibleDisplays.add(entity);
                }
            }

            // Générer les fichiers communs une seule fois,
            // avec toutes les routes collectées.
            generator.generateFrontendSharedFiles(
                    context,
                    eligibleDisplays
            );

            // Vérifier le résultat physique.
            if (!Files.isDirectory(generatedRoot)) {
                throw new IOException(
                        "Le frontend n'a créé aucun répertoire : "
                                + generatedRoot
                );
            }

            try (Stream<Path> files = Files.walk(generatedRoot)) {
                if (files.noneMatch(Files::isRegularFile)) {
                    throw new IOException(
                            "Aucun fichier frontend généré"
                    );
                }
            }

            return generatedRoot;
        }
    }

    private TableMetadata findEntity(
            ProjectGenerationContext context,
            String name,
            boolean view
    ) {

        List<TableMetadata> entities =
                view
                        ? context.getViewTables()
                        : context.getEntityTables();

        if (entities == null) {
            throw new IllegalStateException(
                    "Métadonnées absentes pour " + name
            );
        }

        return entities.stream()
                .filter(entity ->
                        entity.getTableName().equalsIgnoreCase(name)
                )
                .findFirst()
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Entité introuvable : " + name
                        )
                );
    }
}
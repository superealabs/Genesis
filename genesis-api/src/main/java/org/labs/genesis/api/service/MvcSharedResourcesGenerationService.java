package org.labs.genesis.api.service;

import org.labs.genesis.config.ProjectGenerationContext;
import org.labs.genesis.config.langage.FrameworkMVC;
import org.labs.genesis.config.langage.generator.project.ProjectGenerator;
import org.labs.genesis.connexion.model.TableMetadata;
import org.labs.genesis.frontend.generator.ViewsGenerator;

import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
public class MvcSharedResourcesGenerationService {
    public List<String> generate(ProjectGenerationContext context, List<FrontendExecutionTaskPlanner.FrontendExecutionTask> tasks, Path stagingDirectory) throws Exception {
        Objects.requireNonNull(context, "Contexte obligatoire");
        Objects.requireNonNull(tasks, "Tâches obligatoires");
        Objects.requireNonNull(stagingDirectory, "Répertoire temporaire obligatoire");

        if (!(context.getFramework() instanceof FrameworkMVC framework)) {
            throw new IllegalArgumentException("Les ressources MVC nécessitent un framework MVC");
        }

        Set<EntityKey> eligible = new HashSet<>();

        for (var task : tasks) {
            if (task.mvc() && task.completeDisplay()) {
                eligible.add(new EntityKey(task.entityName(), task.view()));
            }
        }

        if (eligible.isEmpty()) {
            return List.of();
        }

        List<TableMetadata> entities = new ArrayList<>();

        if (context.getEntityTables() != null) {
            for (TableMetadata table : context.getEntityTables()) {
                if (eligible.contains(new EntityKey(table.getTableName(), false))) {
                    entities.add(table);
                }
            }
        }

        if (context.getViewTables() != null) {
            for (TableMetadata view : context.getViewTables()) {
                if (eligible.contains(new EntityKey(view.getTableName(), true))) {
                    entities.add(view);
                }
            }
        }

        if (entities.size() != eligible.size()) {
            throw new IllegalStateException("Certaines entités éligibles aux pages MVC " + "sont absentes des métadonnées");
        }

        Path staging = stagingDirectory.toAbsolutePath().normalize();

        Files.createDirectories(staging);

        // Important : le contexte reçu doit être dédié à cette
        // exécution, jamais le contexte global partagé.
        context.setDestinationFolder(staging.toString());

        ViewsGenerator viewsGenerator = new ViewsGenerator(ProjectGenerator.engine);

        TableMetadata[] allEntities = entities.toArray(new TableMetadata[0]);

        viewsGenerator.generateMainLayout(
                framework,
                context.getFrameworkConfiguration(),
                context.getLanguage(),
                context.getViewsTemplate(),
                allEntities,
                context.getDestinationFolder(),
                context.getProjectName(),
                context.getGroupLink()
        );

        viewsGenerator.generateErrorPage(
                framework,
                context.getFrameworkConfiguration(),
                context.getLanguage(),
                context.getViewsTemplate(),
                allEntities,
                context.getDestinationFolder(),
                context.getProjectName(),
                context.getGroupLink()
        );

        viewsGenerator.generateResources(
                framework,
                context.getFrameworkConfiguration(),
                context.getLanguage(),
                context.getViewsTemplate(),
                allEntities,
                context.getDestinationFolder(),
                context.getProjectName(),
                context.getGroupLink()
        );
        return List.of("mvc-main-layout", "mvc-error-page", "mvc-resources");
    }

    private record EntityKey(
            String name,
            boolean view
    ) {
    }
}

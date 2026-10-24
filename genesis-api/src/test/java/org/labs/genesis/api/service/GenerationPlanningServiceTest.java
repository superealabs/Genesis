package org.labs.genesis.api.service;

import org.junit.jupiter.api.Test;
import org.labs.genesis.api.dto.GenerationDtos.*;
import org.labs.genesis.api.dto.MetadataDtos.GenerationParameterRequest;
import org.labs.genesis.api.exception.TableSelectionException;
import org.labs.genesis.config.ProjectGenerationContext;
import org.labs.genesis.config.langage.generator.project.ProjectGenerator;
import java.util.List;
import static org.assertj.core.api.Assertions.*;

class GenerationPlanningServiceTest {
    private final GenerationPlanningService planner = new GenerationPlanningService();
    private final FrontendPlanningService frontend = new FrontendPlanningService();

    @Test
    void preservesProfileOwnershipDuplicatesAndTheTableViewBoundary() {
        var profiles = List.of(
                new GenerationParameterRequest("Model", List.of(" ville "), List.of("ville"), List.of("model")),
                new GenerationParameterRequest("Rest", List.of("ville"), List.of(), List.of("model", "repository", "service", "controller")));
        var plan = planner.plan(profiles);
        assertThat(plan.tasks()).containsExactly(
                new GenerationTask("Model", "ville", false, "model"),
                new GenerationTask("Model", "ville", true, "model"),
                new GenerationTask("Rest", "ville", false, "dao"),
                new GenerationTask("Rest", "ville", false, "service"),
                new GenerationTask("Rest", "ville", false, "controller"));
        assertThat(plan.skipped()).containsExactly(new SkippedTask("Rest", "ville", false, "model", "Model"));
        var batches = planner.prepare(profiles);
        assertThat(batches.batches()).containsExactly(
                new ExecutionBatch("Model", "ville", false, List.of("model")),
                new ExecutionBatch("Model", "ville", true, List.of("model")),
                new ExecutionBatch("Rest", "ville", false, List.of("dao", "service", "controller")));
    }

    @Test
    void cumulativeProfilesEnableOneFullScreenButNeverExpandPartialSelections() {
        var batches = List.of(new ExecutionBatch("A", "ville", false, List.of("model")),
                new ExecutionBatch("B", "ville", false, List.of("dao", "service", "controller")),
                new ExecutionBatch("A", "vue_ville", true, List.of("model")),
                new ExecutionBatch("B", "fragment", false, List.of("controller")));
        var context = context();
        var tasks = frontend.prepare(batches, context);
        assertThat(tasks).hasSize(2);
        assertThat(tasks.get(0).entityName()).isEqualTo("ville");
        assertThat(tasks.get(0).completeDisplay()).isTrue();
        assertThat(tasks.get(1).entityName()).isEqualTo("vue_ville");
        assertThat(tasks.get(1).completeDisplay()).isFalse();
        assertThat(tasks.get(1).options()).containsExactly(ProjectGenerationContext.FRONTEND_COMPONENT_MODEL);
        var partial = planner.prepare(List.of(new GenerationParameterRequest("OnlyDao", List.of("ville"), List.of(), List.of("dao"))));
        assertThat(partial.batches().get(0).components()).containsExactly("dao");
        assertThat(frontend.prepare(partial.batches(), context)).isEmpty();
    }

    @Test
    void resolvesTheFirstMatchingProfileForTheRequestedEntityKind() {
        var profiles = List.of(new GenerationParameterRequest("View", List.of(), List.of("ville"), List.of("model")),
                new GenerationParameterRequest("Table", List.of("ville"), List.of(), List.of("model")));
        assertThat(frontend.resolve(profiles, "ville", true)).isEqualTo("View");
        assertThat(frontend.resolve(profiles, "ville", false)).isEqualTo("Table");
        assertThatThrownBy(() -> frontend.resolve(profiles, "absent", false))
                .isInstanceOf(TableSelectionException.class).hasMessage("Aucun profil ne contient l'entité : absent");
    }

    @Test
    void preservesDifferentValidationErrorsForBackendAndFrontendPlanning() {
        assertThatThrownBy(() -> planner.plan(List.of())).isInstanceOf(TableSelectionException.class)
                .hasMessage("Aucun profil de génération enregistré");
        assertThatThrownBy(() -> frontend.resolve((List<ExecutionBatch>) null)).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Le plan d'exécution est obligatoire");
        assertThatThrownBy(() -> planner.plan(List.of(new GenerationParameterRequest("A", List.of("ville"), List.of(), List.of("unknown")))))
                .isInstanceOf(TableSelectionException.class).hasMessage("Composant inconnu : unknown");
    }

    private ProjectGenerationContext context() {
        var context = new ProjectGenerationContext();
        context.setFramework(ProjectGenerator.frameworks.get(1));
        context.setFrontendFramework(ProjectGenerator.frontendFrameworks.get(3));
        context.setFrontendLanguage(ProjectGenerator.frontendLanguage.get(1));
        context.setGenerateFrontendApp(true);
        context.setFrontendGenerationOptions(List.of(ProjectGenerationContext.FRONTEND_COMPONENT_MODEL,
                ProjectGenerationContext.FRONTEND_COMPONENT_SERVICE, ProjectGenerationContext.FRONTEND_COMPONENT));
        return context;
    }
}

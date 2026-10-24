package org.labs.genesis.api.service;

import org.labs.genesis.api.dto.GenerationDtos;

import org.junit.jupiter.api.Test;
import org.labs.genesis.api.dto.GenerationDtos.ExecutionBatch;
import org.labs.genesis.config.ProjectGenerationContext;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FrontendGenerationPolicyTest {
    @Test
    void partialSelectionsNeverEnableCompleteScreens() {
        List<String> components = List.of("model", "dao", "service", "controller");
        for (int selection = 1; selection < 15; selection++) {
            List<String> selected = new ArrayList<>();
            for (int index = 0; index < components.size(); index++) {
                if ((selection & (1 << index)) != 0) {
                    selected.add(components.get(index));
                }
            }
            var task = new FrontendPlanningService().resolve(
                    List.of(new ExecutionBatch("Partial", "customer", false, selected))).getFirst();
            assertThat(task.complete()).as(selected.toString()).isFalse();
            assertThat(task.frontendOptions()).doesNotContain(ProjectGenerationContext.FRONTEND_COMPONENT);
            assertThat(task.frontendOptions().contains(ProjectGenerationContext.FRONTEND_COMPONENT_MODEL))
                    .isEqualTo(selected.contains("model"));
            assertThat(task.frontendOptions().contains(ProjectGenerationContext.FRONTEND_COMPONENT_SERVICE))
                    .isEqualTo(selected.contains("service"));
        }
    }

    @Test
    void combinesProfilesOnlyForTheSameEntity() {
        var tasks = new FrontendPlanningService().resolve(List.of(
                new ExecutionBatch("Models", "customer", false, List.of("model", "dao")),
                new ExecutionBatch("Actions", "customer", false, List.of("service", "controller")),
                new ExecutionBatch("Models", "product", false, List.of("model"))));
        assertThat(tasks.getFirst().complete()).isTrue();
        assertThat(tasks.getFirst().frontendOptions()).contains(ProjectGenerationContext.FRONTEND_COMPONENT);
        assertThat(tasks.get(1).complete()).isFalse();
    }
}

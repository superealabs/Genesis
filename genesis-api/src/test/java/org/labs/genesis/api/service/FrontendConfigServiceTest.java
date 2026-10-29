package org.labs.genesis.api.service;

import org.labs.genesis.api.dto.FrontendDtos;

import org.labs.genesis.api.dto.FrontendDtos.*;

import org.junit.jupiter.api.Test;
import org.labs.genesis.api.context.GenerationContextStore;
import org.labs.genesis.api.exception.ProjectConfigException;
import org.labs.genesis.config.langage.generator.project.ProjectGenerator;

import static org.assertj.core.api.Assertions.*;

class FrontendConfigServiceTest {
    @Test
    void configuresEachFrontendWithoutMutatingTheRegistry() throws Exception {
        var store = new GenerationContextStore();
        new FrameworkService(store).select(1);
        var service = new FrontendConfigService(store, new FrontendFrameworkIsolationService(), new FrontendAssetService());
        for (int identifier : new int[]{1, 2, 3}) {
            var response = service.configure(new FrontendDtos.FrontendConfigRequest(identifier, null, null));
            assertThat(response.success()).isTrue();
            assertThat(store.getContext().isGenerateFrontendApp()).isTrue();
            assertThat(store.getContext().getFrontendFramework()).isNotSameAs(ProjectGenerator.frontendFrameworks.get(identifier));
            assertThat(store.getContext().getFrontendGenerationOptions()).hasSize(3);
        }
    }

    @Test
    void rejectsInvalidConfigurationBeforeChangingTheContext() throws Exception {
        var store = new GenerationContextStore();
        var service = new FrontendConfigService(store, new FrontendFrameworkIsolationService(), new FrontendAssetService());
        assertThatThrownBy(() -> service.configure(new FrontendDtos.FrontendConfigRequest(1, null, null)))
                .isInstanceOf(ProjectConfigException.class);
        new FrameworkService(store).select(1);
        assertThatThrownBy(() -> service.configure(new FrontendDtos.FrontendConfigRequest(99, null, null)))
                .isInstanceOf(ProjectConfigException.class);
        assertThatThrownBy(() -> service.configure(new FrontendDtos.FrontendConfigRequest(1, 999, null)))
                .isInstanceOf(ProjectConfigException.class);
        assertThatThrownBy(() -> service.configure(new FrontendDtos.FrontendConfigRequest(1, null, "70000")))
                .isInstanceOf(ProjectConfigException.class);
        assertThat(store.getContext().getFrontendFramework()).isNull();
    }
}

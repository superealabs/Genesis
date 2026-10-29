package org.labs.genesis.api.service;

import org.junit.jupiter.api.Test;
import org.labs.genesis.api.context.GenerationContextStore;
import org.labs.genesis.api.exception.ProjectConfigException;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class GenerationConfigurationSnapshotServiceTest {
    @Test
    void missingProfilesAreAConfigurationError() {
        var relations = mock(ProfileRelationService.class);
        var service = new GenerationConfigurationSnapshotService(
                new GenerationContextStore(), new GenerationProfilesStore(), relations);
        assertThatThrownBy(service::capture).isInstanceOf(ProjectConfigException.class)
                .hasMessageContaining("Aucun profil");
        verifyNoInteractions(relations);
    }
}

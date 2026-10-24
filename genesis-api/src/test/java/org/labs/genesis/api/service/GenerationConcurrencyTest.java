package org.labs.genesis.api.service;

import org.junit.jupiter.api.Test;
import org.labs.genesis.api.exception.GenerationAlreadyRunningException;
import java.util.concurrent.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class GenerationConcurrencyTest {
    @Test
    void rejectsConcurrentLaunchAndReleasesTheLockAfterPreparationFailure() throws Exception {
        var snapshots = mock(GenerationConfigurationSnapshotService.class);
        var entered = new CountDownLatch(1); var release = new CountDownLatch(1);
        var failure = new IllegalArgumentException("recipe snapshot failure");
        when(snapshots.capture()).thenAnswer(invocation -> {
            entered.countDown();
            if (!release.await(10, TimeUnit.SECONDS)) throw new AssertionError("Fixture timed out");
            throw failure;
        });
        var service = new GenerationExecutionService(snapshots, mock(GenerationPreflightService.class),
                mock(GenerationStagingService.class), mock(BackendBatchGenerationService.class),
                mock(GenerationExecutionContextFactory.class), mock(MvcSharedResourcesGenerationService.class),
                mock(GenerationOutputPathService.class), mock(ConsolidatedFrontendGenerationService.class),
                mock(GenerationPublicationService.class), mock(GitLocalService.class));
        try (var pool = Executors.newSingleThreadExecutor()) {
            var first = pool.submit(() -> { assertThatThrownBy(service::launch).isSameAs(failure); });
            try {
                assertThat(entered.await(10, TimeUnit.SECONDS)).isTrue();
                assertThatThrownBy(service::launch).isInstanceOf(GenerationAlreadyRunningException.class);
            } finally { release.countDown(); }
            first.get(10, TimeUnit.SECONDS);
            assertThatThrownBy(service::launch).isSameAs(failure);
        }
    }
}

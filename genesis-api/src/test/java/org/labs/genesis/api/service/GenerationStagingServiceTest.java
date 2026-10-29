package org.labs.genesis.api.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GenerationStagingServiceTest {
    @TempDir
    Path directory;

    @Test
    void consolidatesIdenticalSharedFilesOnlyOnce() throws Exception {
        Path first = Files.createDirectory(directory.resolve("first"));
        Path second = Files.createDirectory(directory.resolve("second"));
        Files.writeString(first.resolve("shared.java"), "shared content");
        Files.writeString(second.resolve("shared.java"), "shared content");
        var result = new GenerationStagingService().consolidate(List.of(first, second), directory.resolve("output"));
        assertThat(result.success()).isTrue();
        assertThat(result.consolidatedFiles()).containsExactly("shared.java");
    }

    @Test
    void rejectsDifferentContentsBeforePublishingAnything() throws Exception {
        Path first = Files.createDirectory(directory.resolve("first"));
        Path second = Files.createDirectory(directory.resolve("second"));
        Files.writeString(first.resolve("shared.java"), "first content");
        Files.writeString(second.resolve("shared.java"), "second content");
        Path output = directory.resolve("output");
        var result = new GenerationStagingService().consolidate(List.of(first, second), output);
        assertThat(result.success()).isFalse();
        assertThat(result.collisions()).containsExactly("shared.java");
        assertThat(output).doesNotExist();
    }
    @Test
    void rejectsFileDirectoryCollisionBeforeCreatingOutput() throws Exception {
        Path first = Files.createDirectory(directory.resolve("first"));
        Path second = Files.createDirectory(directory.resolve("second"));
        Files.writeString(first.resolve("assets"), "file");
        Files.createDirectory(second.resolve("assets"));
        Files.writeString(second.resolve("assets/logo.png"), "image");
        Path output = directory.resolve("output");
        var result = new GenerationStagingService().consolidate(List.of(first, second), output);
        assertThat(result.success()).isFalse();
        assertThat(result.collisions()).anyMatch(path -> path.contains("assets"));
        assertThat(output).doesNotExist();
    }
}

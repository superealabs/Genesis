package org.labs.genesis.api.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.assertj.core.api.Assertions.*;

class GeneratedFilesPublisherTest {
    @TempDir Path directory;

    @Test
    void preservesExistingFilesAndReportsThem() throws Exception {
        Path staging = Files.createDirectory(directory.resolve("staging"));
        Path target = Files.createDirectory(directory.resolve("target"));
        Files.writeString(staging.resolve("existing.txt"), "new");
        Files.writeString(staging.resolve("created.txt"), "created");
        Files.writeString(target.resolve("existing.txt"), "original");
        var result = new GeneratedFilesPublisher().publish(staging, target);
        assertThat(Files.readString(target.resolve("existing.txt"))).isEqualTo("original");
        assertThat(result.skippedExistingFiles()).containsExactly("existing.txt");
        assertThat(result.createdFiles()).containsExactly("created.txt");
    }

    @Test
    void rejectsDestinationSymlinksBeforeWritingAnyFile() throws Exception {
        Path staging = Files.createDirectory(directory.resolve("staging"));
        Path target = Files.createDirectory(directory.resolve("target"));
        Path outside = Files.createDirectory(directory.resolve("outside"));
        Files.writeString(staging.resolve("a.txt"), "new");
        Files.createDirectory(staging.resolve("z"));
        Files.writeString(staging.resolve("z/escape.txt"), "new");
        Files.createSymbolicLink(target.resolve("z"), outside);
        assertThatThrownBy(() -> new GeneratedFilesPublisher().publish(staging, target))
                .isInstanceOf(IOException.class);
        assertThat(target.resolve("a.txt")).doesNotExist();
        assertThat(outside.resolve("escape.txt")).doesNotExist();
    }

    @Test
    void rejectsSourceSymlinksBeforeCreatingDestination() throws Exception {
        Path staging = Files.createDirectory(directory.resolve("staging"));
        Path outside = Files.writeString(directory.resolve("outside.txt"), "original");
        Files.createSymbolicLink(staging.resolve("link.txt"), outside);
        Path target = directory.resolve("target");
        assertThatThrownBy(() -> new GeneratedFilesPublisher().publish(staging, target))
                .isInstanceOf(IOException.class);
        assertThat(target).doesNotExist();
    }

    @Test
    void rejectsOverlappingDirectories() throws Exception {
        Path staging = Files.createDirectory(directory.resolve("staging"));
        Files.writeString(staging.resolve("source.txt"), "original");
        assertThatThrownBy(() -> new GeneratedFilesPublisher().publish(staging, staging.resolve("nested")))
                .isInstanceOf(IOException.class);
        assertThat(staging.resolve("nested")).doesNotExist();
    }
}

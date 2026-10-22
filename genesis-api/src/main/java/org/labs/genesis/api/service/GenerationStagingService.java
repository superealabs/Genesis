package org.labs.genesis.api.service;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

@Service
public class GenerationStagingService {
    public Path createWorkspace() throws IOException {
        return Files.createTempDirectory("genesis-generation-").toRealPath();
    }

    public Path createBatchDirectory(Path workspace, int batchIndex) throws IOException {
        if (batchIndex < 0) {
            throw new IllegalArgumentException("L'indice du lot doit être positif");
        }
        Path root = workspace.toRealPath();
        return Files.createDirectory(root.resolve("batch-" + batchIndex));
    }

    public ConsolidationResult consolidate(List<Path> batchDirectories, Path destination) throws IOException {
        if (batchDirectories == null || batchDirectories.isEmpty()) {
            throw new IOException("Aucun lot à consolider");
        }
        Path target = destination.toAbsolutePath().normalize();
        if (Files.exists(target, LinkOption.NOFOLLOW_LINKS)) {
            throw new IOException("Le dossier de consolidation existe déjà");
        }

        // Préparation avant toute écriture.
        List<FileEntry> entries = new ArrayList<>();
        List<String> collisions = new ArrayList<>();
        java.util.Set<Path> occupied = new java.util.HashSet<>();

        for (Path directory : batchDirectories) {
            Path root = directory.toRealPath();
            try (Stream<Path> paths = Files.walk(root)) {
                for (Path source : paths.toList()) {
                    if (Files.isSymbolicLink(source)) {
                        throw new IOException("Lien symbolique interdit : " + source);
                    }

                    if (Files.isDirectory(source, LinkOption.NOFOLLOW_LINKS)) {
                        continue;
                    }

                    if (!Files.isRegularFile(source, LinkOption.NOFOLLOW_LINKS)) {
                        throw new IOException("Type de fichier interdit : " + source);
                    }

                    Path relative = root.relativize(source);

                    if (!occupied.add(relative)) {
                        collisions.add(relative.toString());
                    }

                    entries.add(new FileEntry(source, relative));
                }
            }
        }

        if (!collisions.isEmpty()) {
            return new ConsolidationResult(false, List.of(), List.copyOf(collisions));
        }
        if (entries.isEmpty()) {
            throw new IOException("Aucun fichier généré à consolider");
        }

        Set<Path> files = new java.util.HashSet<>(occupied);
        for (Path relative : files) {
            Path parent = relative.getParent();
            while (parent != null) {
                if (files.contains(parent)) {
                    collisions.add(relative + " (conflit fichier/répertoire)");
                    break;
                }
                parent = parent.getParent();
            }
        }

        Files.createDirectory(target);
        List<String> consolidated = new ArrayList<>();
        for (FileEntry entry : entries) {
            Path output = target.resolve(entry.relativePath()).normalize();

            if (!output.startsWith(target)) {
                throw new IOException("Chemin de sortie invalide");
            }

            Files.createDirectories(output.getParent());
            Files.copy(entry.source(), output);
            consolidated.add(entry.relativePath().toString());
        }

        return new ConsolidationResult(true, List.copyOf(consolidated), List.of());
    }

    public void deleteWorkspace(Path workspace) throws IOException {
        if (workspace == null) {
            return;
        }

        Path root = workspace.toAbsolutePath().normalize();

        // Ne jamais supprimer une racine du système.
        if (root.getParent() == null) {
            throw new IOException("Suppression de la racine du système interdite");
        }

        if (!Files.exists(root, LinkOption.NOFOLLOW_LINKS)) {
            return;
        }

        if (Files.isSymbolicLink(root) || !Files.isDirectory(root, LinkOption.NOFOLLOW_LINKS)) {
            throw new IOException("Espace temporaire invalide : " + root);
        }

        // Réservé aux workspaces que Genesis a créés.
        if (!root.getFileName().toString().startsWith("genesis-generation-")) {
            throw new IOException("Répertoire non reconnu comme workspace Genesis");
        }

        try (Stream<Path> paths = Files.walk(root)) {
            List<Path> entries = paths.sorted(java.util.Comparator.reverseOrder()).toList();
            for (Path entry : entries) {
                Files.delete(entry);
            }
        }
    }

    private record FileEntry(
            Path source,
            Path relativePath
    ) {
    }

    public record ConsolidationResult(
            boolean success,
            List<String> consolidatedFiles,
            List<String> collisions
    ) {
    }
}

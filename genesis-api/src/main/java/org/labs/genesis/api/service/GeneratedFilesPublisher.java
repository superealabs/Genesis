package org.labs.genesis.api.service;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Service
public class GeneratedFilesPublisher {

    public PublicationResult publish(Path stagingDirectory, Path targetDirectory) throws IOException {
        Path staging = stagingDirectory.toRealPath(LinkOption.NOFOLLOW_LINKS);
        Path target = targetDirectory.toAbsolutePath().normalize();

        if (!Files.isDirectory(staging, LinkOption.NOFOLLOW_LINKS) || Files.isSymbolicLink(staging)) {
            throw new IOException("Répertoire temporaire invalide");
        }

        if (target.startsWith(staging) || staging.startsWith(target)) {
            throw new IOException("Les répertoires temporaire et cible se chevauchent");
        }

        List<Path> sources;

        try (Stream<Path> walk = Files.walk(staging)) {
            sources = walk.sorted().toList();
        }

        // PHASE 1 : contrôler tous les chemins, sans écriture.
        for (Path source : sources) {
            if (Files.isSymbolicLink(source)) {
                throw new IOException("Lien symbolique interdit : " + source);
            }

            if (!Files.isDirectory(source, LinkOption.NOFOLLOW_LINKS) && !Files.isRegularFile(source, LinkOption.NOFOLLOW_LINKS)) {
                throw new IOException("Type de fichier non autorisé : " + source);
            }

            Path relative = staging.relativize(source);
            Path destination = target.resolve(relative).normalize();

            if (!destination.startsWith(target)) {
                throw new IOException("Chemin de publication invalide : " + relative);
            }

            verifyExistingAncestors(destination);

            if (Files.isDirectory(source, LinkOption.NOFOLLOW_LINKS)) {
                if (Files.exists(destination, LinkOption.NOFOLLOW_LINKS) && !Files.isDirectory(destination, LinkOption.NOFOLLOW_LINKS)) {
                    throw new IOException("Un fichier bloque le dossier : " + destination);
                }
            } else if (Files.exists(
                    destination, LinkOption.NOFOLLOW_LINKS
            ) && !Files.isRegularFile(
                    destination, LinkOption.NOFOLLOW_LINKS
            )) {
                throw new IOException(
                        "La destination n'est pas un fichier régulier : " + destination);
            }
        }

        // PHASE 2 : publier, sans remplacer les fichiers existants.
        List<String> created = new ArrayList<>();
        List<String> skipped = new ArrayList<>();

        try {
            for (Path source : sources) {
                Path relative = staging.relativize(source);
                if (relative.toString().isEmpty()) {
                    continue;
                }

                Path destination = target.resolve(relative).normalize();

                if (Files.isDirectory(source, LinkOption.NOFOLLOW_LINKS)) {
                    ensureSafeDirectories(target, destination);
                    continue;
                }

                ensureSafeDirectories(target, destination.getParent());

                if (Files.isRegularFile(destination, LinkOption.NOFOLLOW_LINKS)) {
                    skipped.add(relative.toString());
                    continue;
                }

                try {
                    Files.copy(source, destination);
                    created.add(relative.toString());
                } catch (FileAlreadyExistsException exception) {
                    // Ne jamais remplacer un fichier présent.
                    if (!Files.isRegularFile(destination, LinkOption.NOFOLLOW_LINKS)) {
                        throw new IOException("La destination a changé pendant la publication : " + destination, exception);
                    }
                    skipped.add(relative.toString());
                }
            }
        } catch (IOException exception) {
            throw new PartialPublicationException("La publication a échoué après la création de " + created.size() + " fichier(s)", created, skipped, exception);
        }

        return new PublicationResult(List.copyOf(created), List.copyOf(skipped));
    }

    private void verifyExistingAncestors(Path destination) throws IOException {
        Path current = destination.toAbsolutePath().getRoot();
        for (Path part : destination.toAbsolutePath()) {
            current = current.resolve(part);

            if (Files.exists(current, LinkOption.NOFOLLOW_LINKS) && Files.isSymbolicLink(current)) {
                throw new IOException("Lien symbolique dans la destination : " + current);
            }

            if (!current.equals(destination) && Files.exists(current, LinkOption.NOFOLLOW_LINKS) && !Files.isDirectory(current, LinkOption.NOFOLLOW_LINKS)) {
                throw new IOException("Un fichier bloque le chemin : " + current);
            }
        }
    }

    private void ensureSafeDirectories(Path root, Path directory) throws IOException {
        if (!directory.startsWith(root)) {
            throw new IOException("Répertoire hors destination");
        }

        Path current = root.getRoot();

        for (Path segment : root) {
            current = current.resolve(segment);
            verifyOrCreateDirectory(current);
        }

        Path relative = root.relativize(directory);

        for (Path segment : relative) {
            current = current.resolve(segment);
            verifyOrCreateDirectory(current);
        }
    }

    private void verifyOrCreateDirectory(Path directory) throws IOException {
        if (Files.exists(directory, LinkOption.NOFOLLOW_LINKS)) {
            if (Files.isSymbolicLink(directory) || !Files.isDirectory(directory, LinkOption.NOFOLLOW_LINKS)) {
                throw new IOException("Chemin non sécurisé : " + directory);
            }
        } else {
            try {
                Files.createDirectory(directory);
            } catch (FileAlreadyExistsException exception) {
                if (Files.isSymbolicLink(directory) || !Files.isDirectory(directory, LinkOption.NOFOLLOW_LINKS)) {
                    throw exception;
                }
            }
        }
    }

    public static class PartialPublicationException extends IOException {

        private final List<String> createdFiles;
        private final List<String> skippedExistingFiles;

        public PartialPublicationException(String message, List<String> createdFiles, List<String> skippedExistingFiles, Throwable cause) {
            super(message, cause);
            this.createdFiles = List.copyOf(createdFiles);
            this.skippedExistingFiles = List.copyOf(skippedExistingFiles);
        }

        public List<String> getCreatedFiles() {
            return createdFiles;
        }

        public List<String> getSkippedExistingFiles() {
            return skippedExistingFiles;
        }
    }

    public record PublicationResult(
            List<String> createdFiles,
            List<String> skippedExistingFiles
    ) {
    }
}
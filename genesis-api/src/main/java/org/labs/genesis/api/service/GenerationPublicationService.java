package org.labs.genesis.api.service;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

@Service
public class GenerationPublicationService {

    private final GenerationStagingService stagingService;
    private final GeneratedFilesPublisher filesPublisher;
    private volatile Path lastPublishedTarget;
    public Path getLastPublishedTarget() {
        return lastPublishedTarget;
    }

    public GenerationPublicationService(GenerationStagingService stagingService, GeneratedFilesPublisher filesPublisher) {
        this.stagingService = stagingService;
        this.filesPublisher = filesPublisher;
    }

    public PublicationReport consolidateAndPublish(List<Path> batchDirectories, Path workspace, Path targetDirectory) throws IOException {
        Objects.requireNonNull(batchDirectories, "Les dossiers des lots sont obligatoires");
        Objects.requireNonNull(workspace, "L'espace temporaire est obligatoire");
        Objects.requireNonNull(targetDirectory, "La destination est obligatoire");

        if (batchDirectories.isEmpty()) {
            throw new IOException("Aucun lot généré à publier");
        }

        Path workRoot = workspace.toRealPath();
        Path target = targetDirectory.toAbsolutePath().normalize();

        if (Files.isSymbolicLink(workRoot) || !Files.isDirectory(workRoot)) {
            throw new IOException("Espace de préparation invalide");
        }

        if (target.startsWith(workRoot) || workRoot.startsWith(target)) {
            throw new IOException("La destination ne peut pas se trouver " + "dans l'espace temporaire");
        }

        for (Path batchDirectory : batchDirectories) {
            if (Files.isSymbolicLink(batchDirectory)) {
                throw new IOException("Lien symbolique interdit : " + batchDirectory);
            }

            Path batch = batchDirectory.toRealPath(LinkOption.NOFOLLOW_LINKS);

            if (Files.isSymbolicLink(batchDirectory)
                    || !Files.isDirectory(batch)
                    || !batch.startsWith(workRoot)
                    || batch.equals(workRoot)) {
                throw new IOException("Dossier de lot invalide : " + batchDirectory);
            }
        }

        Path consolidated = workRoot.resolve("consolidated");

        if (Files.exists(consolidated, LinkOption.NOFOLLOW_LINKS)) {
            throw new IOException("Le dossier de consolidation existe déjà");
        }

        GenerationStagingService.ConsolidationResult result = stagingService.consolidate(batchDirectories, consolidated);

        if (!result.success()) {
            throw new GenerationCollisionException(result.collisions());
        }

        if (result.consolidatedFiles().isEmpty()) {
            throw new IOException("La génération n'a produit aucun fichier");
        }

        GeneratedFilesPublisher.PublicationResult publication = filesPublisher.publish(consolidated, target);

        lastPublishedTarget = target.toRealPath();

        return new PublicationReport(result.consolidatedFiles().size(), publication.createdFiles(), publication.skippedExistingFiles()
        );
    }

    public record PublicationReport(
            int preparedFileCount,
            List<String> createdFiles,
            List<String> skippedExistingFiles
    ) {
        public PublicationReport {
            createdFiles = List.copyOf(createdFiles);
            skippedExistingFiles = List.copyOf(skippedExistingFiles);
        }
    }

    public static class GenerationCollisionException extends IOException {
        private final List<String> collisions;
        public GenerationCollisionException(List<String> collisions) {
            super("Collisions entre fichiers générés : " + String.join(", ", collisions));
            this.collisions = List.copyOf(collisions);
        }
        public List<String> getCollisions() {
            return collisions;
        }
    }
}

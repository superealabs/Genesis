package org.labs.genesis.api.service;

import org.labs.genesis.config.ProjectGenerationContext;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;

@Service
public class GenerationOutputPathService {
    public Path configureStaging(ProjectGenerationContext context, Path batchRoot) throws IOException {
        Path root = batchRoot.toAbsolutePath().normalize();
        if (!Files.isDirectory(root) || Files.isSymbolicLink(root)) {
            throw new IOException("Le répertoire du lot est invalide");
        }

        context.setDestinationFolder(root.toString());

        Path generatedRoot = Path.of(context.getDestinationFolder()).toAbsolutePath().normalize();

        if (!generatedRoot.startsWith(root) || generatedRoot.equals(root)) {
            throw new IOException("Le chemin produit par Core sort du lot");
        }

        return generatedRoot;
    }

    public Path resolveProjectTarget(ProjectGenerationContext context) throws IOException {

        Path target = Path.of(context.getDestinationFolder()).toAbsolutePath().normalize();

        if (!target.isAbsolute()) {
            throw new IOException("Destination du projet invalide");
        }

        if (Files.exists(target, LinkOption.NOFOLLOW_LINKS) && (Files.isSymbolicLink(target) || !Files.isDirectory(target))) {
            throw new IOException("Le chemin cible n'est pas un répertoire valide");
        }

        return target;
    }
}

package org.labs.utils;

import java.io.IOException;
import java.nio.file.*;
import java.util.Objects;

public final class GenerationWriteScope implements AutoCloseable {

    private static final ThreadLocal<Path> ALLOWED_ROOT = new ThreadLocal<>();

    private final Path previous;
    private boolean closed;

    private GenerationWriteScope(Path root) {
        this.previous = ALLOWED_ROOT.get();
        ALLOWED_ROOT.set(root);
    }

    public static GenerationWriteScope open(Path workspace) throws IOException {
        Objects.requireNonNull(workspace, "Workspace obligatoire");

        Path root = workspace.toRealPath();

        if (Files.isSymbolicLink(workspace) || !Files.isDirectory(root)) {
            throw new IOException("Workspace invalide : " + workspace);
        }

        if (ALLOWED_ROOT.get() != null) {
            throw new IllegalStateException("Un périmètre d'écriture est déjà actif");
        }

        return new GenerationWriteScope(root);
    }

    public static Path check(Path requested) throws IOException {
        Objects.requireNonNull(requested, "Chemin obligatoire");

        Path result = requested.toAbsolutePath().normalize();

        Path root = ALLOWED_ROOT.get();

        // Aucun confinement activé :
        // comportement classique de Genesis Core.
        if (root == null) {
            return result;
        }

        if (!result.startsWith(root)) {
            throw new IOException("Écriture interdite hors workspace : " + result);
        }

        if (result.equals(root)) {
            throw new IOException(
                    "Modification de la racine du workspace interdite"
            );
        }

        // Vérifier tous les chemins existants, y compris
        // les parents, pour refuser les liens symboliques.
        Path current = result.getRoot();

        for (Path segment : result) {
            current = current.resolve(segment);

            if (Files.isSymbolicLink(current)) {
                throw new IOException("Lien symbolique interdit : " + current);
            }
        }

        return result;
    }

    @Override
    public void close() {
        if (closed) {
            return;
        }

        closed = true;

        if (previous == null) {
            ALLOWED_ROOT.remove();
        } else {
            ALLOWED_ROOT.set(previous);
        }
    }
}

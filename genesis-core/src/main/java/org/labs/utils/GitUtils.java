package org.labs.utils;

import org.labs.genesis.config.Constantes;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class GitUtils {

    private static final String GITHUB_API = "https://api.github.com";

    /**
     * Crée un repository GitHub pour l'utilisateur authentifié.
     */
    public static String createRemoteRepo(
            String token,
            String repoName,
            boolean isPrivate
    ) throws IOException, InterruptedException {

        try (HttpClient client = HttpClient.newHttpClient()) {

            String json = """
                    {
                        "name": "%s",
                        "private": %s
                    }
                    """.formatted(repoName, isPrivate);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(GITHUB_API + "/user/repos"))
                    .header("Authorization", "Bearer " + token)
                    .header("Accept", "application/vnd.github+json")
                    .header("Content-Type", "application/json")
                    .header("X-GitHub-Api-Version", "2022-11-28")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = client.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() < 200 ||
                    response.statusCode() >= 300) {

                throw new RuntimeException(
                        "Erreur GitHub (" +
                                response.statusCode() +
                                ") : " +
                                response.body()
                );
            }

            return response.body();
        }
    }

    /**
     * Initialise un repository Git local.
     */
    public static void gitInit(String projectPath)
            throws IOException, InterruptedException {

        executeGitCommand(
                projectPath,
                "git",
                "init"
        );

        executeGitCommand(
                projectPath,
                "git",
                "branch",
                "-M",
                "main"
        );
    }

    /**
     * Force la branche principale à être "main".
     */
    public static void gitSetMainBranch(String projectPath)
            throws IOException, InterruptedException {

        executeGitCommand(
                projectPath,
                "git",
                "branch",
                "-M",
                "main"
        );
    }

    /**
     * Ajoute tous les fichiers du projet.
     */
    public static void gitAdd(String projectPath)
            throws IOException, InterruptedException {

        executeGitCommand(
                projectPath,
                "git",
                "add",
                "."
        );
    }

    /**
     * Crée un commit.
     */
    public static void gitCommit(
            String projectPath,
            String message
    ) throws IOException, InterruptedException {

        executeGitCommand(
                projectPath,
                "git",
                "commit",
                "-m",
                message
        );
    }

    /**
     * Ajoute le repository distant comme "origin".
     */
    public static void gitRemote(
            String projectPath,
            String username,
            String repoName
    ) throws IOException, InterruptedException {

        String remoteUrl =
                "https://github.com/" +
                        username +
                        "/" +
                        repoName +
                        ".git";

        executeGitCommand(
                projectPath,
                "git",
                "remote",
                "add",
                "origin",
                remoteUrl
        );
    }

    /**
     * Ajoute le .gitignore correspondant au framework frontend.
     */
    public static void addFrontendGitIgnore(
            int frameworkId,
            String destinationPath
    ) throws IOException {

        String gitignorePath =
                Constantes.GITIGNORE_FRONTEND +
                        "-" +
                        frameworkId;

        FileUtils.copyFile(
                gitignorePath,
                destinationPath,
                ".gitignore"
        );
    }

    /**
     * Ajoute le .gitignore correspondant au framework backend.
     */
    public static void addBackendGitIgnore(
            int frameworkId,
            String destinationPath
    ) throws IOException {

        String gitignorePath =
                Constantes.GITIGNORE_BACKEND +
                        "-" +
                        frameworkId;

        FileUtils.copyFile(
                gitignorePath,
                destinationPath,
                ".gitignore"
        );
    }

    /**
     * Effectue un push vers GitHub avec le token.
     */
    public static void gitPush(
            String projectPath,
            String username,
            String token
    ) throws IOException, InterruptedException {

        ProcessBuilder processBuilder = new ProcessBuilder(
                "git",
                "push",
                "-u",
                "origin",
                "main"
        );

        processBuilder
                .directory(Paths.get(projectPath).toFile())
                .redirectErrorStream(true);

        /*
         * Git utilise ce script pour récupérer
         * automatiquement le username et le token.
         */
        Path askPassScript =
                Files.createTempFile("git-askpass-", ".sh");

        String script = """
                #!/bin/sh
                case "$1" in
                    *Username*) echo "%s" ;;
                    *Password*) echo "%s" ;;
                esac
                """.formatted(username, token);

        Files.writeString(
                askPassScript,
                script
        );

        askPassScript.toFile().setExecutable(true);

        processBuilder.environment().put(
                "GIT_ASKPASS",
                askPassScript.toAbsolutePath().toString()
        );

        processBuilder.environment().put(
                "GIT_TERMINAL_PROMPT",
                "0"
        );

        try {

            Process process = processBuilder.start();

            int exitCode = process.waitFor();

            if (exitCode != 0) {
                throw new RuntimeException(
                        "Git push échoué (code " +
                                exitCode +
                                ")"
                );
            }

        } finally {

            Files.deleteIfExists(askPassScript);
        }
    }

    /**
     * Exécute une commande Git.
     */
    private static void executeGitCommand(
            String projectPath,
            String... command
    ) throws IOException, InterruptedException {

        Process process = new ProcessBuilder(command)
                .directory(Paths.get(projectPath).toFile())
                .inheritIO()
                .start();

        int exitCode = process.waitFor();

        if (exitCode != 0) {

            throw new RuntimeException(
                    "Commande Git échouée : " +
                            String.join(" ", command)
            );
        }
    }
}
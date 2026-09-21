package org.labs.utils;

import org.labs.genesis.config.ProjectGenerationContext;
import org.labs.genesis.config.langage.FilesEdit;
import org.labs.genesis.config.langage.Framework;
import org.labs.genesis.frontend.generator.FrontendFramework;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GitUtils {

    private static final String GITHUB_API = "https://api.github.com";

    /**
     * Crée un repository GitHub pour l'utilisateur authentifié.
     */
    public static String createRemoteRepo(
            String token,
            String repoName,
            boolean isPrivate
    ) throws Exception {

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
            throws Exception {

        EnvironmentUtils.run(
                projectPath,
                null,
                EnvironmentUtils.getCommand("git"),
                "init"
        );

        EnvironmentUtils.run(
                projectPath,
                null,
                EnvironmentUtils.getCommand("git"),
                "branch",
                "-M",
                "main"
        );
    }

    /**
     * Force la branche principale à être "main".
     */
    public static void gitSetMainBranch(String projectPath)
            throws Exception {

        EnvironmentUtils.run(
                projectPath,
                null,
                EnvironmentUtils.getCommand("git"),
                "branch",
                "-M",
                "main"
        );
    }

    /**
     * Ajoute tous les fichiers du projet.
     */
    public static void gitAdd(String projectPath)
            throws Exception {

        EnvironmentUtils.run(
                projectPath,
                null,
                EnvironmentUtils.getCommand("git"),
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
    ) throws Exception {

        EnvironmentUtils.run(
                projectPath,
                null,
                EnvironmentUtils.getCommand("git"),
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
    ) throws Exception {

        String remoteUrl =
                "https://github.com/" +
                        username +
                        "/" +
                        repoName +
                        ".git";

        EnvironmentUtils.run(
                projectPath,
                null,
                EnvironmentUtils.getCommand("git"),
                "remote",
                "add",
                "origin",
                remoteUrl
        );
    }

    /**
     * Génère le .gitignore à partir d'un fichier YAML défini dans le framework,
     * ou utilise le fallback historique par ressource si aucun fichier YAML n'est fourni.
     */
    public static void generateGitIgnoreIfNeeded(
            FilesEdit gitIgnoreFile,
            String destinationPath
    ) throws IOException {
        if (gitIgnoreFile != null) {
            String content = gitIgnoreFile.getContent() == null ? "" : gitIgnoreFile.getContent();
            Path gitIgnoreTarget = Paths.get(destinationPath, gitIgnoreFile.getFileName());
            Files.writeString(gitIgnoreTarget, content);
        }
    }

    public static  FilesEdit get(List<FilesEdit> conditionalFiles, String fileName) {
        return conditionalFiles != null ? conditionalFiles.stream()
                .filter(file -> file != null && file.getFileName() != null)
                .filter(file -> file.getFileName().equalsIgnoreCase(fileName))
                .findFirst().orElse(null) : null;
    }

    /**
     * Effectue un push vers GitHub avec le token.
     */
    public static void gitPush(
            String projectPath,
            String username,
            String token
    ) throws Exception {
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
        
        Map<String, String> envs = new HashMap<>();
        envs.put(
                "GIT_ASKPASS",
                askPassScript.toAbsolutePath().toString()
        );

        envs.put(
                "GIT_TERMINAL_PROMPT",
                "0"
        );

        try {

            EnvironmentUtils.run(projectPath, envs,
                    EnvironmentUtils.getCommand("git"),
                    "push",
                    "-u",
                    "origin",
                    "main");

        } finally {

            Files.deleteIfExists(askPassScript);
        }
    }
}
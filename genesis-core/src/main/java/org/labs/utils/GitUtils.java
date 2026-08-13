package org.labs.utils;

import org.labs.genesis.config.ProjectGenerationContext;
import org.labs.genesis.config.docker.DockerConf;
import org.labs.genesis.config.tools.DockerConfiguration;
import org.labs.genesis.config.langage.FilesEdit;
import org.labs.genesis.config.langage.Framework;
import org.labs.genesis.frontend.generator.FrontendFramework;

import java.io.IOException;
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

    public static Map<String, Object> getVariables(
            ProjectGenerationContext context,
            DockerConfiguration config,
            Framework framework,
            FrontendFramework frontendFramework) {

        String nodeVersion = config.getLangVersion();

        String langVersion = String.valueOf(
                context.getLanguageConfiguration()
                        .getOrDefault("languageVersion", 21)
        );

        String backendPort = context.getProjectPort();
        String frontendPort = context.getFrontendPort();

        boolean isBackendDockerized = config.isBackendDockerized();
        boolean isFrontendDockerized = config.isFrontendDockerized();

        String backendContainer = config.getBackendContainer();
        String frontendContainer = config.getFrontendContainer();

        DockerConf backendDocker =
                framework != null ? framework.getDocker() : null;

        DockerConf frontendDocker =
                frontendFramework != null ? frontendFramework.getDocker() : null;

        List<DockerConf.Volume> volumes =
                getVolumes(backendDocker);

        List<DockerConf.Volume> frontendVolumes =
                getVolumes(frontendDocker);

        List<DockerConf.Environment> environments =
                getEnvironments(backendDocker);

        List<DockerConf.Environment> frontendEnvironments =
                getEnvironments(frontendDocker);

        Map<String, Object> variables = new HashMap<>();

        variables.put("projectName", context.getProjectName());
        variables.put("destinationFolder", context.getDestinationFolder());
        variables.put("versionNode", nodeVersion != null ? nodeVersion : 22);
        variables.put("versionLanguage", langVersion);
        variables.put("backendPort", backendPort != null ? backendPort : 8080);
        variables.put("frontendPort", frontendPort != null ? frontendPort : 4200);
        variables.put("isStructure", context.isGenerateFrontendApp());
        variables.put("backendDir", StringUtils.majStart(context.getProjectName()));
        variables.put( "frontendDir", StringUtils.majStart(context.getProjectName())
                        + StringUtils.majStart(context.getWebappFolder()));
        variables.put("hadFrontendEnvironments", !frontendEnvironments.isEmpty());
        variables.put("hadEnvironments", !environments.isEmpty());
        variables.put("volumes", toVolumeMaps(volumes));
        variables.put("frontendVolumes", toVolumeMaps(frontendVolumes));
        variables.put("hadVolumes", (!volumes.isEmpty() && isBackendDockerized)
                        || (!frontendVolumes.isEmpty() && isFrontendDockerized));
        variables.put("environments", toEnvironmentMaps(environments));
        variables.put("frontendEnvironments", toEnvironmentMaps(frontendEnvironments));
        variables.put("isBackendDockerized", isBackendDockerized);
        variables.put("isFrontendDockerized", isFrontendDockerized);
        variables.put("frontendContainer", frontendContainer != null ?
                        frontendContainer : "frontend");
        variables.put("backendContainer", backendContainer != null ?
                        backendContainer : "backend");

        System.out.println("- @ -" + variables);

        return variables;
    }


    private static List<DockerConf.Volume> getVolumes(
            DockerConf docker) {

        if (docker == null || docker.getVolumes() == null) {
            return List.of();
        }

        return docker.getVolumes();
    }


    private static List<DockerConf.Environment> getEnvironments(DockerConf docker) {

        if (docker == null || docker.getEnvironments() == null) {
            return List.of();
        }

        return docker.getEnvironments();
    }


    private static List<Map<String, Object>> toVolumeMaps(
            List<DockerConf.Volume> volumes) {

        return volumes.stream()
                .map(volume -> {
                    Map<String, Object> map = new HashMap<>();

                    map.put("name", volume.getName());
                    map.put("dir", volume.getDir());

                    return map;
                })
                .toList();
    }


    private static List<Map<String, Object>> toEnvironmentMaps(
            List<DockerConf.Environment> environments) {

        return environments.stream()
                .map(environment -> {
                    Map<String, Object> map = new HashMap<>();

                    map.put("name", environment.getName());
                    map.put("separator", environment.getSeparator());
                    map.put("value", environment.getValue());

                    return map;
                })
                .toList();
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
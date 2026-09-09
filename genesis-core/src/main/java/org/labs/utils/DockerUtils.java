package org.labs.utils;

import org.labs.genesis.config.Constantes;
import org.labs.genesis.config.ProjectGenerationContext;
import org.labs.genesis.config.docker.DockerConf;
import org.labs.genesis.config.langage.Framework;
import org.labs.genesis.config.tools.DockerConfiguration;
import org.labs.genesis.frontend.generator.FrontendFramework;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DockerUtils {
    public static boolean isDockerAvailable() {
        try {
            Process process = new ProcessBuilder(EnvironmentUtils.getCommand("docker"), "info")
                    .redirectErrorStream(true)
                    .start();

            return process.waitFor() == 0;

        } catch (Exception e) {
            return false;
        }
    }

    public static String formatCommand(String command, List<String> args) {
        Stream<String> commandParts = Arrays.stream(command.split("\\s+"));
        Stream<String> argParts = args != null
                ? args.stream()
                : Stream.empty();

        return Stream.concat(commandParts, argParts)
                .map(part -> "\"" + part + "\"")
                .collect(Collectors.joining(", "));
    }

    public static int getDefaultVersion(Framework framework) {
        if(framework == null)
            return 0;
        if(framework.getLanguageId() == Constantes.Java_ID)
          return 21;
        if(framework.getLanguageId() == Constantes.NET_ID)
            return 8;
        return 21;
    }

    public static Map<String, Object> getVariables(
            ProjectGenerationContext context,
            DockerConfiguration config,
            Framework framework,
            FrontendFramework frontendFramework) {

        String nodeVersion = config.getLangVersion();

        String langVersion = String.valueOf(
                context.getLanguageConfiguration()
                        .getOrDefault("languageVersion", getDefaultVersion(framework))
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

        DockerConf.Command command = config.getSelectedCommand();
        DockerConf.Command frontendCommand = config.getFrontendSelectedCommand();

        Map<String, Object> variables = new HashMap<>();
        boolean needVolume = false;
        boolean needFrontendVolume = false;

        if(command != null) {
            List<String> build = build = command.getBuild();
            variables.put("command", formatCommand(command.getCommand(), command.getArgs()));
            variables.put("build", build != null ? build : List.of());
            needVolume = command.isNeedVolume();
        }

        if(frontendCommand != null) {
            needFrontendVolume = frontendCommand.isNeedVolume();
            variables.put("commandFrontend", formatCommand(frontendCommand.getCommand(), frontendCommand.getArgs()));
        }

        variables.put("frontendNeedVolume", needFrontendVolume);
        variables.put("backendNeedVolume", needVolume);


        variables.put("hasFrontendVolume", needFrontendVolume
                || (!frontendVolumes.isEmpty() && isFrontendDockerized));
        variables.put("backendHasVolume", needVolume
                || (!volumes.isEmpty() && isBackendDockerized));

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
        variables.put("hasFrontendEnvironments", !frontendEnvironments.isEmpty());
        variables.put("hasBackendEnvironments", !environments.isEmpty());
        variables.put("backendVolumes", toVolumeMaps(volumes));
        variables.put("frontendVolumes", toVolumeMaps(frontendVolumes));
        variables.put("hasVolumes", (!volumes.isEmpty() && isBackendDockerized)
                || (!frontendVolumes.isEmpty() && isFrontendDockerized));
        variables.put("backendEnvironments", toEnvironmentMaps(environments));
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
}

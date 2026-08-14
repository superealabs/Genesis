package org.labs.utils;

import org.labs.genesis.config.ProjectGenerationContext;
import org.labs.genesis.config.docker.DockerConf;
import org.labs.genesis.config.langage.Framework;
import org.labs.genesis.config.tools.DockerConfiguration;
import org.labs.genesis.frontend.generator.FrontendFramework;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DockerUtils {
    public static boolean isDockerAvailable() {
        try {
            Process process = new ProcessBuilder("docker", "info")
                    .redirectErrorStream(true)
                    .start();

            return process.waitFor() == 0;

        } catch (Exception e) {
            return false;
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
}

package utils;

import org.labs.genesis.config.ProjectGenerationContext;
import org.labs.genesis.config.langage.Framework;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class UtilsTest {

    // Méthode utilitaire pour créer une route
    public static Map<String, Object> createRoute(String id, String uri, String path, String method) {
        Map<String, Object> route = new HashMap<>();
        route.put("id", id);
        route.put("uri", uri);
        route.put("path", path);
        route.put("method", method);
        return route;
    }


    // Méthode utilitaire pour créer un contexte de génération de base
    public static ProjectGenerationContext createBaseContext(
            String projectName,
            String groupLink,
            String projectPort,
            String destinationFolder,
            String projectDescription) {
        ProjectGenerationContext context = new ProjectGenerationContext();
        context.setProjectName(projectName);
        context.setGroupLink(groupLink);
        context.setProjectPort(projectPort);
        context.setDestinationFolder(destinationFolder);
        context.setProjectDescription(projectDescription);
        return context;
    }

    // Configuration du framework
    public static HashMap<String, Object> createFrameworkConfig(
            Framework framework,
            String logLevel,
            String frameworkVersion,
            String projectPort,
            boolean useCloud,
            boolean useEureka) {
        HashMap<String, Object> config = new HashMap<>();
        config.put("loggingLevel", logLevel);
        config.put("frameworkVersion", frameworkVersion);

        if (useEureka) {
            config.put("eurekaServerURL", "http://localhost:8761/eureka");
            config.put("projectNonSecurePort", projectPort);
        }

        framework.setUseCloud(useCloud);
        framework.setUseEurekaServer(useEureka);

        return config;
    }

    // Configuration du langage
    public static HashMap<String, Object> createLanguageConfig(String languageVersion) {
        HashMap<String, Object> config = new HashMap<>();
        config.put("languageVersion", languageVersion);
        return config;
    }

    // Vérification de la création du dossier
    public static void assertFolderCreated(String destinationFolder) {
        Path path = Path.of(destinationFolder);
        assertTrue(Files.exists(path) && Files.isDirectory(path),
                "Le dossier de destination n'existe pas.");
    }

}

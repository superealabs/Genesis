package project;

import org.junit.jupiter.api.Test;
import org.labs.genesis.config.Constantes;
import org.labs.genesis.config.ProjectGenerationContext;
import org.labs.genesis.config.langage.generator.project.ProjectGenerator;
import org.labs.genesis.connexion.Credentials;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GenesisCoreTest {
    private static Map<String, Object> createRoute(String id, String uri, String path, String method) {
        Map<String, Object> route = new HashMap<>();
        route.put("id", id);
        route.put("uri", uri);

        route.put("path", path);
        route.put("method", method);

        return route;
    }

    @Test
    void generateProjectSpring() {
        var credentials = new Credentials()
                .setHost("localhost")
                .setPort("5432")
                .setSchemaName("public")
                .setDatabaseName("mon_refuge_test")
                .setUser("nomena")
                .setPwd("root")
                .setTrustCertificate(true)
                .setUseSSL(true)
                .setAllowPublicKeyRetrieval(true);

        try {

            int databaseId = Constantes.PostgreSQL_ID;
            int languageId = Constantes.Java_ID;
            int frameworkId = Constantes.Spring_REST_API_ID;
            int projectId = Constantes.Maven_ID;

            var database = ProjectGenerator.databases.get(databaseId);
            var language = ProjectGenerator.languages.get(languageId);
            var framework = ProjectGenerator.frameworks.get(frameworkId);
            var project = ProjectGenerator.projects.get(projectId);

            String projectName = "MonRefuge";
            String groupLink = "org.labs";
            String projectPort = "8000";
            String logLevel = "INFO";
            String hibernateDdlAuto = "none";
            String projectDescription = "Mon Refuge - Backend";
            String frameworkVersion = "3.3.6";
            String languageVersion = "21";
            String destinationFolder = "generated/spring";

            ProjectGenerator projectGenerator = new ProjectGenerator();

            HashMap<String, Object> frameworkConfiguration = new HashMap<>();
            frameworkConfiguration.put("hibernateDdlAuto", hibernateDdlAuto);
            frameworkConfiguration.put("loggingLevel", logLevel);
            frameworkConfiguration.put("frameworkVersion", frameworkVersion);

            //===== USE EUREKA SERVER =======//
            framework.setUseCloud(false);
            framework.setUseEurekaServer(false);
            frameworkConfiguration.put("eurekaServerURL", "http://localhost:8761/eureka");
            frameworkConfiguration.put("projectNonSecurePort", projectPort);
            //==============================//

            HashMap<String, Object> languageConfiguration = new HashMap<>();
            languageConfiguration.put("languageVersion", languageVersion);

            List<String> generationOptions = List.of("Model", "DAO", "Service", "Controller");
            List<String> entityNames = new ArrayList<>();

            ProjectGenerationContext context = new ProjectGenerationContext();
            context.setDatabase(database);
            context.setLanguage(language);
            context.setFramework(framework);
            context.setProject(project);
            context.setCredentials(credentials);
            context.setDestinationFolder(destinationFolder);
            context.setProjectName(projectName);
            context.setGroupLink(groupLink);
            context.setProjectPort(projectPort);
            context.setProjectDescription(projectDescription);
            context.setLanguageConfiguration(languageConfiguration);
            context.setFrameworkConfiguration(frameworkConfiguration);
            context.setEntityNames(entityNames);
            context.setGenerationOptions(generationOptions);
            context.setGenerateProjectStructure(true);

            projectGenerator.generateProject(context);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void generateProjectNET() {
        var credentials = new Credentials()
                .setHost("localhost")
                .setPort("5432")
                .setSchemaName("public")
                .setDatabaseName("test_db")
                .setUser("nomena")
                .setPwd("root");

        try {
            int databaseId = Constantes.PostgreSQL_ID;
            int languageId = Constantes.CSharp_ID;
            int frameworkId = Constantes.NET_ID;
            int projectId = Constantes.ASP_ID;

            var database = ProjectGenerator.databases.get(databaseId);
            var language = ProjectGenerator.languages.get(languageId);
            var framework = ProjectGenerator.frameworks.get(frameworkId);
            var project = ProjectGenerator.projects.get(projectId);

            String projectName = "WebApiNet";
            String groupLink = "";
            String projectPort = "8080";
            String logLevel = "Information";
            String projectDescription = "An ASP.NET BEGIN Project";
            String frameworkVersion = "8.0";
            String languageVersion = "";
            String destinationFolder = "generated/dotnet";

            ProjectGenerator projectGenerator = new ProjectGenerator();

            HashMap<String, Object> frameworkConfiguration = new HashMap<>();
            frameworkConfiguration.put("loggingLevel", logLevel);
            frameworkConfiguration.put("frameworkVersion", frameworkVersion);

            //===== USE EUREKA SERVER =======//
            framework.setUseCloud(true);
            framework.setUseEurekaServer(true);
            frameworkConfiguration.put("eurekaServerURL", "http://localhost:8761/eureka");
            frameworkConfiguration.put("projectNonSecurePort", projectPort);
            //==============================//

            HashMap<String, Object> languageConfiguration = new HashMap<>();
            frameworkConfiguration.put("languageVersion", languageVersion);

            ProjectGenerationContext context = new ProjectGenerationContext();
            context.setDatabase(database);
            context.setLanguage(language);
            context.setFramework(framework);
            context.setProject(project);
            context.setCredentials(credentials);
            context.setDestinationFolder(destinationFolder);
            context.setProjectName(projectName);
            context.setGroupLink(groupLink);
            context.setProjectPort(projectPort);
            context.setProjectDescription(projectDescription);
            context.setLanguageConfiguration(languageConfiguration);
            context.setFrameworkConfiguration(frameworkConfiguration);

            projectGenerator.generateProject(context);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void generateProjectSpringEurekaServer() {
        try {

            int languageId = Constantes.Java_ID;
            int frameworkId = Constantes.Spring_Eureka_Server_ID;
            int projectId = Constantes.Maven_ID;

            var language = ProjectGenerator.languages.get(languageId);
            var framework = ProjectGenerator.frameworks.get(frameworkId);
            var project = ProjectGenerator.projects.get(projectId);

            String projectName = "TestEurekaServer";
            String groupLink = "labs.test";
            String projectPort = "8761";
            String logLevel = "INFO";
            String projectDescription = "Eureka Server Project For Testing Genesis API Generator";
            String frameworkVersion = "3.3.5";
            String languageVersion = "17";

            String destinationFolder = "generated/discovery";

            ProjectGenerator projectGenerator = new ProjectGenerator();

            HashMap<String, Object> frameworkConfiguration = new HashMap<>();
            frameworkConfiguration.put("loggingLevel", logLevel);
            frameworkConfiguration.put("frameworkVersion", frameworkVersion);

            HashMap<String, Object> languageConfiguration = new HashMap<>();
            languageConfiguration.put("languageVersion", languageVersion);

            ProjectGenerationContext context = new ProjectGenerationContext();
            context.setLanguage(language);
            context.setFramework(framework);
            context.setProject(project);
            context.setDestinationFolder(destinationFolder);
            context.setProjectName(projectName);
            context.setGroupLink(groupLink);
            context.setProjectPort(projectPort);
            context.setProjectDescription(projectDescription);
            context.setLanguageConfiguration(languageConfiguration);
            context.setFrameworkConfiguration(frameworkConfiguration);

            projectGenerator.generateProject(context);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void generateProjectSpringApiGateway() {
        try {

            int languageId = Constantes.Java_ID;
            int frameworkId = Constantes.Spring_Api_Gateway_ID;
            int projectId = Constantes.Maven_ID;

            var language = ProjectGenerator.languages.get(languageId);
            var framework = ProjectGenerator.frameworks.get(frameworkId);
            var project = ProjectGenerator.projects.get(projectId);

            String projectName = "TestApiGateway";
            String groupLink = "labs.test";
            String projectPort = "8090";
            String logLevel = "INFO";
            String projectDescription = "API Gateway Project For Testing Genesis API Generator";
            String frameworkVersion = "3.3.5";
            String languageVersion = "17";

            String destinationFolder = "generated/gateway";

            ProjectGenerator projectGenerator = new ProjectGenerator();

            HashMap<String, Object> frameworkConfiguration = new HashMap<>();
            frameworkConfiguration.put("loggingLevel", logLevel);
            frameworkConfiguration.put("frameworkVersion", frameworkVersion);

            //===== API GATEWAY ROUTES ======//
            List<Map<String, Object>> routes = new ArrayList<>();

            // Ajout des routes
            routes.add(createRoute("route1", "http://service1", "/path1", "GET"));
            routes.add(createRoute("route2", "http://service2", "/path2", "POST"));
            routes.add(createRoute("route3", "http://service3", "/path3", "PUT"));

            frameworkConfiguration.put("routes", routes);

            frameworkConfiguration.put("username", "admin");
            frameworkConfiguration.put("password", "admin");
            frameworkConfiguration.put("role", "user");


            //===== USE EUREKA SERVER =======//
            framework.setUseCloud(true);
            framework.setUseEurekaServer(true);
            frameworkConfiguration.put("eurekaServerURL", "http://localhost:8761/eureka");
            frameworkConfiguration.put("projectNonSecurePort", projectPort);
            //==============================//


            HashMap<String, Object> languageConfiguration = new HashMap<>();
            languageConfiguration.put("languageVersion", languageVersion);

            ProjectGenerationContext context = new ProjectGenerationContext();
            context.setLanguage(language);
            context.setFramework(framework);
            context.setProject(project);
            context.setDestinationFolder(destinationFolder);
            context.setProjectName(projectName);
            context.setGroupLink(groupLink);
            context.setProjectPort(projectPort);
            context.setProjectDescription(projectDescription);
            context.setLanguageConfiguration(languageConfiguration);
            context.setFrameworkConfiguration(frameworkConfiguration);

            projectGenerator.generateProject(context);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
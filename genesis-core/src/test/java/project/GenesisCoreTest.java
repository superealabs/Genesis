package project;

import org.junit.jupiter.api.Test;
import org.labs.genesis.config.Constantes;
import org.labs.genesis.config.ProjectGenerationContext;
import org.labs.genesis.config.langage.FrameworkMVC;
import org.labs.genesis.config.langage.InputTypeMapping;
import org.labs.genesis.config.langage.generator.project.ProjectGenerator;
import org.labs.genesis.connexion.Credentials;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

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
        var credentials = new Credentials().setHost("localhost").setPort("1522").setSchemaName("GENESIS").setDatabaseName("orcl").setUser("genesis").setPwd("root").setTrustCertificate(true).setUseSSL(true).setAllowPublicKeyRetrieval(true).setSID("ORCL").setDriverType("Oracle");
//
        var credentials = new Credentials().setSchemaName("").setHost("localhost").setPort("1521").setUser("C##SUPER").setPwd("super").setTrustCertificate(true).setUseSSL(true).setAllowPublicKeyRetrieval(true).setSID("ORCLBDD").setDriverType("thin");

        try {

            int databaseId = Constantes.Oracle_ID;//
            int languageId = Constantes.Java_ID;
            int frameworkId = Constantes.Spring_REST_API_ID;
            int projectId = Constantes.Maven_ID;
            int frontendLangageId=Constantes.TYPESCRIPT_ID;
            int frontendFrameworkId=Constantes.ANGULAR_ID;

            var database = ProjectGenerator.databases.get(databaseId);
            var language = ProjectGenerator.languages.get(languageId);
            var framework = ProjectGenerator.frameworks.get(frameworkId);
            var project = ProjectGenerator.projects.get(projectId);
            var frontendLangage=ProjectGenerator.frontendLanguage.get(frontendLangageId);
            var frontendFramework=ProjectGenerator.frontendFrameworks.get(frontendFrameworkId);

            String projectName = "oraTest";
            String groupLink = "org.labs";
            String projectPort = "8000";
            String logLevel = "INFO";
            String hibernateDdlAuto = "none";
            String projectDescription = "test";
            String frameworkVersion = "3.3.6";
            String languageVersion = "21";
            String destinationFolder = "D:\\tahiana\\test\\";

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
            languageConfiguration.put("frameworkCaching", "nom");

            List<String> generationOptions = List.of("Model", "DAO", "Service", "Controller");
            List<String> entityNames = new ArrayList<>();

            ProjectGenerationContext context = new ProjectGenerationContext();
            context.setDatabase(database);
            context.setLanguage(language);
            context.setFramework(framework);
            context.setFrontendFramework(frontendFramework);
            context.setFrontendLanguage(frontendLangage);
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
            context.setViewNames(new ArrayList<>());
            context.setGenerationOptions(generationOptions);
            context.setGenerateProjectStructure(true);

            context.setGenerateFrontendApp(false);


            projectGenerator.generateProject(context);

            // Assertion pour vérifier si le dossier existe
            Path path = Path.of(destinationFolder);
            assertTrue(Files.exists(path) && Files.isDirectory(path), "Le dossier de destination n'existe pas.");

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
                .setDatabaseName("test_keywords")
                .setUser("postgres")
                .setPwd("olafienby7")
                .setTrustCertificate(true)
                .setUseSSL(true)
                .setAllowPublicKeyRetrieval(true);

        try {
            int databaseId = Constantes.PostgreSQL_ID;
            int languageId = Constantes.CSharp_ID;
            int frameworkId = Constantes.NET_ID;
            int projectId = Constantes.ASP_ID;
            int frontendLangageId=Constantes.TYPESCRIPT_ID;
            int frontendFrameworkId=Constantes.REACT_ID;

            var database = ProjectGenerator.databases.get(databaseId);
            var language = ProjectGenerator.languages.get(languageId);
            var framework = ProjectGenerator.frameworks.get(frameworkId);
            var project = ProjectGenerator.projects.get(projectId);
            var frontendLangage=ProjectGenerator.frontendLanguage.get(frontendLangageId);
            var frontendFramework=ProjectGenerator.frontendFrameworks.get(frontendFrameworkId);

            List<String> generationOptions = List.of("Model", "DAO", "Service", "Controller");
            String projectName = "kw";
            String groupLink = "";
            String projectPort = "8080";
            String logLevel = "Information";
            String projectDescription = "An ASP.NET BEGIN Project";
            String frameworkVersion = "8.0";
            String languageVersion = "";
            String destinationFolder = "../generated/dotnet";

            ProjectGenerator projectGenerator = new ProjectGenerator();

            HashMap<String, Object> frameworkConfiguration = new HashMap<>();
            frameworkConfiguration.put("loggingLevel", logLevel);
            frameworkConfiguration.put("frameworkVersion", frameworkVersion);

            //===== USE EUREKA SERVER =======//
            framework.setUseCloud(false);
            framework.setUseEurekaServer(false);
            frameworkConfiguration.put("eurekaServerURL", "http://localhost:8761/eureka");
            frameworkConfiguration.put("projectNonSecurePort", projectPort);
            //==============================//

            HashMap<String, Object> languageConfiguration = new HashMap<>();
            frameworkConfiguration.put("languageVersion", languageVersion);
            List<String> entityNames = new ArrayList<>();
            ProjectGenerationContext context = new ProjectGenerationContext();
            context.setDatabase(database);
            context.setLanguage(language);
            context.setFramework(framework);
            context.setFrontendFramework(frontendFramework);
            context.setFrontendLanguage(frontendLangage);
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
            context.setViewNames(new ArrayList<>());
            context.setGenerateFrontendApp(false);

            projectGenerator.generateProject(context);


            // Assertion pour vérifier si le dossier existe
            Path path = Path.of(destinationFolder);
            assertTrue(Files.exists(path) && Files.isDirectory(path), "Le dossier de destination n'existe pas.");

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

            String destinationFolder = "../generated/discovery";

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
            context.setGenerateFrontendApp(false);

            projectGenerator.generateProject(context);

            // Assertion pour vérifier si le dossier existe
            Path path = Path.of(destinationFolder);
            assertTrue(Files.exists(path) && Files.isDirectory(path), "Le dossier de destination n'existe pas.");

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

            String destinationFolder = "../generated/gateway";

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
            context.setGenerateFrontendApp(false);

            projectGenerator.generateProject(context);

            // Assertion pour vérifier si le dossier existe
            Path path = Path.of(destinationFolder);
            assertTrue(Files.exists(path) && Files.isDirectory(path), "Le dossier de destination n'existe pas.");

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @Test
    void generateProjectDotnetMvc() {
        var credentials = new Credentials()
                .setHost("localhost")
                .setPort("5432")
                .setSchemaName("")
                .setDatabaseName("genesis")
                .setUser("postgres")
                .setPwd("root")
                .setTrustCertificate(true)
                .setUseSSL(true)
                .setAllowPublicKeyRetrieval(true)
                .setSID("")
                .setDriverType("");

        try {
            int databaseId = Constantes.PostgreSQL_ID;
            int languageId = Constantes.CSharp_ID;
            int frameworkId = Constantes.DOTNET_MVC_ID;
            int projectId = Constantes.ASP_ID;

            var database = ProjectGenerator.databases.get(databaseId);
            var language = ProjectGenerator.languages.get(languageId);
            var framework = ProjectGenerator.frameworks.get(frameworkId);
            var project = ProjectGenerator.projects.get(projectId);

            if (framework instanceof FrameworkMVC) {
                FrameworkMVC frameworkMvc = (FrameworkMVC) framework;

                frameworkMvc.setViewsTemplateEngine();
                frameworkMvc.setViewsTemplate();
            }

            framework.setFrameworkSecurities();

            List<String> generationOptions = List.of("Model", "DAO", "Service", "Controller");
            String projectName = "new";
            String groupLink = "";
            String projectPort = "8080";
            String logLevel = "Information";
            String projectDescription = "An ASP.NET BEGIN Project";
            String frameworkVersion = "8.0";
            String languageVersion = "";
            String destinationFolder = "E:/stage/dotnet mvc/hotfix";

            int viewsTemplateEngineId = Constantes.Razor_ID;
            int viewsTemplateId = Constantes.Template_1_ID;

            ProjectGenerator projectGenerator = new ProjectGenerator();

            HashMap<String, Object> frameworkConfiguration = new HashMap<>();
            frameworkConfiguration.put("loggingLevel", logLevel);
            frameworkConfiguration.put("frameworkVersion", frameworkVersion);
            frameworkConfiguration.put("templateEngineId", viewsTemplateEngineId);
            frameworkConfiguration.put("templateId", viewsTemplateId);

            //===== USE EUREKA SERVER =======//
            framework.setUseCloud(false);
            framework.setUseEurekaServer(false);
            frameworkConfiguration.put("eurekaServerURL", "http://localhost:8761/eureka");
            frameworkConfiguration.put("projectNonSecurePort", projectPort);
            //==============================//

            HashMap<String, Object> languageConfiguration = new HashMap<>();
            frameworkConfiguration.put("languageVersion", languageVersion);
            List<String> entityNames = new ArrayList<>();
            List<String> viewNames = new ArrayList<>();
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
            context.setViewNames(viewNames);
            context.setGenerationOptions(generationOptions);
            context.setGenerateProjectStructure(true);

            projectGenerator.generateProject(context);

            // Assertion pour vérifier si le dossier existe
            Path path = Path.of(destinationFolder);
            assertTrue(Files.exists(path) && Files.isDirectory(path), "Le dossier de destination n'existe pas.");

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void generateProjectDjango() {
        var credentials = new Credentials().setHost("localhost").setPort("3306").setSchemaName("public").setDatabaseName("genesis").setUser("root").setPwd("root").setTrustCertificate(true).setUseSSL(true).setAllowPublicKeyRetrieval(true);

        try {
            int databaseId = Constantes.MySQL_ID;
            int languageId = Constantes.Python_ID;
            int frameworkId = Constantes.Django_ID;
            int projectId = Constantes.Django_Project_ID;

            var database = ProjectGenerator.databases.get(databaseId);
            var language = ProjectGenerator.languages.get(languageId);
            var framework = ProjectGenerator.frameworks.get(frameworkId);
            var project = ProjectGenerator.projects.get(projectId);

            String projectName = "TestDjangoProject";
            String groupLink = "labs.test";
            String projectPort = "8000";
            String logLevel = "INFO";
            String projectDescription = "Django Project For Testing Genesis API Generator";
            String frameworkVersion = "4.2.1";
            String languageVersion = "3.10";

            String destinationFolder = "../generated/django";

            ProjectGenerator projectGenerator = new ProjectGenerator();

            HashMap<String, Object> frameworkConfiguration = new HashMap<>();
            frameworkConfiguration.put("loggingLevel", logLevel);
            frameworkConfiguration.put("frameworkVersion", frameworkVersion);

            HashMap<String, Object> languageConfiguration = new HashMap<>();
            languageConfiguration.put("languageVersion", languageVersion);

            List<String> generationOptions = List.of("Model");
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

            // Assertion pour vérifier si le dossier existe
            Path path = Path.of(destinationFolder);
            assertTrue(Files.exists(path) && Files.isDirectory(path), "Le dossier de destination n'existe pas.");

            System.out.println("Credentials All good");

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
package project;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.labs.genesis.config.Constantes;
import org.labs.genesis.config.ProjectGenerationContext;
import org.labs.genesis.config.langage.generator.project.ProjectGenerator;
import org.labs.genesis.connexion.Credentials;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static utils.TestUtils.*;

public class GenesisCoreTest {
    private ProjectGenerator projectGenerator;

    @BeforeEach
    void setUp() {
        projectGenerator = new ProjectGenerator();
    }

    // Méthode utilitaire pour créer les credentials de base de données
    private Credentials createDatabaseCredentials(String databaseName) {
        return new Credentials()
                .setHost("localhost")
                .setPort("5432")
                .setSchemaName("public")
                .setDatabaseName(databaseName)
                .setUser("nomena")
                .setPwd("root");
    }

    @Test
    void generateProjectSpring() {
        var credentials = new Credentials().setHost("localhost").setPort("1522").setSchemaName("GENESIS").setDatabaseName("orcl").setUser("genesis").setPwd("root").setTrustCertificate(true).setUseSSL(true).setAllowPublicKeyRetrieval(true).setSID("ORCL").setDriverType("Oracle");

        try {
            // Configuration initiale
            var credentials = createDatabaseCredentials("genesis");
/*          credentials.setTrustCertificate(true)
                    .setUseSSL(true)
                    .setAllowPublicKeyRetrieval(true);
*/
            // Récupération des composants du projet
            var database = ProjectGenerator.databases.get(Constantes.PostgreSQL_ID);
            var language = ProjectGenerator.languages.get(Constantes.Java_ID);
            var framework = ProjectGenerator.frameworks.get(Constantes.Spring_REST_API_ID);
            var project = ProjectGenerator.projects.get(Constantes.Maven_ID);

            // Création du contexte de base
            ProjectGenerationContext context = createBaseContext(
                    "TestWebApiRe",
                    "org.labs",
                    "8000",
                    "../generated/spring",
                    "test web api"
            );

            // Configuration framework spécifique
            HashMap<String, Object> frameworkConfig = createFrameworkConfig(
                    framework,
                    "INFO",
                    "3.3.6",
                    "8000",
                    false,
                    false
            );
            frameworkConfig.put("hibernateDdlAuto", "none");

            // Configuration du framework et du langage
            context.setFrameworkConfiguration(frameworkConfig);
            context.setLanguageConfiguration(createLanguageConfig("21"));

            // Configuration des options de génération
            context.setGenerationOptions(List.of("Model", "DAO", "Service", "Controller"));
            context.setEntityNames(new ArrayList<>());
            context.setGenerateProjectStructure(true);

            // Configuration des composants
            int databaseId = Constantes.Oracle_ID;
            int languageId = Constantes.Java_ID;
            int frameworkId = Constantes.Spring_REST_API_ID;
            int projectId = Constantes.Maven_ID;

            var database = ProjectGenerator.databases.get(databaseId);
            var language = ProjectGenerator.languages.get(languageId);
            var framework = ProjectGenerator.frameworks.get(frameworkId);
            var project = ProjectGenerator.projects.get(projectId);

            String projectName = "Popol";
            String groupLink = "org.labs";
            String projectPort = "8000";
            String logLevel = "INFO";
            String hibernateDdlAuto = "none";
            String projectDescription = "test";
            String frameworkVersion = "3.3.6";
            String languageVersion = "21";
            String destinationFolder = "../generated/spring";

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

            // Génération du projet
            projectGenerator.generateProject(context);
            assertFolderCreated("../generated/spring/");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    
    @Test
    void generateProjectNET() {
        try {
            // Configuration initiale
            var credentials = createDatabaseCredentials("scenema");
            credentials.setPort("3306")
                    .setUser("root")
                    .setPwd("Nomena321@")
                    .setTrustCertificate(true)
                    .setUseSSL(true)
                    .setAllowPublicKeyRetrieval(true);

            // Récupération des composants du projet
            var database = ProjectGenerator.databases.get(Constantes.MySQL_ID);
            var language = ProjectGenerator.languages.get(Constantes.CSharp_ID);
            var framework = ProjectGenerator.frameworks.get(Constantes.NET_ID);
            var project = ProjectGenerator.projects.get(Constantes.ASP_ID);

            // Création du contexte de base
            ProjectGenerationContext context = createBaseContext(
                    "WebApiScenema",
                    "",
                    "8080",
                    "../generated/dotnet",
                    "An ASP.NET BEGIN Project"
            );

            // Configuration framework spécifique
            HashMap<String, Object> frameworkConfig = createFrameworkConfig(
                    framework,
                    "Information",
                    "8.0",
                    "8080",
                    true,
                    false
            );

            // Configuration du framework et du langage
            context.setFrameworkConfiguration(frameworkConfig);
            context.setLanguageConfiguration(createLanguageConfig(""));

            // Configuration des options de génération
            context.setGenerationOptions(List.of("Model", "DAO", "Service", "Controller"));
            context.setEntityNames(new ArrayList<>());

            // Configuration des composants
            context.setDatabase(database);
            context.setLanguage(language);
            context.setFramework(framework);
            context.setProject(project);
            context.setCredentials(credentials);

            // Génération du projet
            projectGenerator.generateProject(context);
            assertFolderCreated("../generated/dotnet");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /*@Test
    void generateProjectSpringEurekaServer() {
        try {
            // Récupération des composants du projet
            var language = ProjectGenerator.languages.get(Constantes.Java_ID);
            var framework = ProjectGenerator.frameworks.get(Constantes.Spring_Eureka_Server_ID);
            var project = ProjectGenerator.projects.get(Constantes.Maven_ID);

            // Création du contexte de base
            ProjectGenerationContext context = createBaseContext(
                    "TestEurekaServer",
                    "labs.test",
                    "8761",
                    "../generated/discovery",
                    "Eureka Server Project For Testing Genesis API Generator"
            );

            // Configuration du framework et du langage
            context.setFrameworkConfiguration(createFrameworkConfig(
                    framework,
                    "INFO",
                    "3.3.5",
                    "8761",
                    false,
                    false
            ));
            context.setLanguageConfiguration(createLanguageConfig("17"));

            // Configuration des composants
            context.setLanguage(language);
            context.setFramework(framework);
            context.setProject(project);

            // Génération du projet
            projectGenerator.generateProject(context);
            assertFolderCreated("../generated/discovery");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void generateProjectSpringApiGateway() {
        try {
            // Récupération des composants du projet
            var language = ProjectGenerator.languages.get(Constantes.Java_ID);
            var framework = ProjectGenerator.frameworks.get(Constantes.Spring_Api_Gateway_ID);
            var project = ProjectGenerator.projects.get(Constantes.Maven_ID);

            // Création du contexte de base
            ProjectGenerationContext context = createBaseContext(
                    "TestApiGateway",
                    "labs.test",
                    "8090",
                    "../generated/gateway",
                    "API Gateway Project For Testing Genesis API Generator"
            );

            // Configuration framework spécifique
            HashMap<String, Object> frameworkConfig = createFrameworkConfig(
                    framework,
                    "INFO",
                    "3.3.5",
                    "8090",
                    true,
                    true
            );

            // Configuration des routes de l'API Gateway
            List<Map<String, Object>> routes = new ArrayList<>();
            routes.add(createRoute("route1", "http://service1", "/path1", "GET"));
            routes.add(createRoute("route2", "http://service2", "/path2", "POST"));
            routes.add(createRoute("route3", "http://service3", "/path3", "PUT"));
            frameworkConfig.put("routes", routes);

            // Configuration des credentials de l'API Gateway
            frameworkConfig.put("username", "admin");
            frameworkConfig.put("password", "admin");
            frameworkConfig.put("role", "user");

            // Configuration du framework et du langage
            context.setFrameworkConfiguration(frameworkConfig);
            context.setLanguageConfiguration(createLanguageConfig("17"));

            // Configuration des composants
            context.setLanguage(language);
            context.setFramework(framework);
            context.setProject(project);

            // Génération du projet
            projectGenerator.generateProject(context);
            assertFolderCreated("../generated/gateway");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
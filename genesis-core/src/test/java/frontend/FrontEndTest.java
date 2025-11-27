package frontend;


import org.junit.jupiter.api.Test;
import org.labs.genesis.config.Constantes;
import org.labs.genesis.config.ProjectGenerationContext;
import org.labs.genesis.config.langage.generator.project.ProjectGenerator;
import org.labs.genesis.connexion.Credentials;
import org.labs.genesis.connexion.model.RelationParameter;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class FrontEndTest {
    @Test
    public void testCollectionYamlData()
    {
        ProjectGenerator projectGenerator = new ProjectGenerator();
    }

    @Test
    void generateProjetDotNetVueJS() {
        var credentials = new Credentials()
                .setHost("localhost")
                .setPort("5432")
                .setSchemaName("public")
                .setDatabaseName("fanamby")
                .setUser("chan_alex")
                .setPwd("chanalex")
                .setTrustCertificate(true).
                setUseSSL(true).
                setAllowPublicKeyRetrieval(true);
        try {
            int databaseId = Constantes.PostgreSQL_ID;
            int languageId = Constantes.CSharp_ID;
            int frameworkId = Constantes.NET_ID;
            int projectId = Constantes.ASP_ID;
            int frontendLangageId=Constantes.TYPESCRIPT_ID;
            int frontendFrameworkId=Constantes.VUE_JS_ID;

            var database = ProjectGenerator.databases.get(databaseId);
            var language = ProjectGenerator.languages.get(languageId);
            var framework = ProjectGenerator.frameworks.get(frameworkId);
            var project = ProjectGenerator.projects.get(projectId);
            var frontendLangage=ProjectGenerator.frontendLanguage.get(frontendLangageId);
            var frontendFramework=ProjectGenerator.frontendFrameworks.get(frontendFrameworkId);

            List<String> generationOptions = List.of("Model", "DAO", "Service", "Controller");
            String projectName = "FanambyPresence";
            String groupLink = "mg.akademia";
            String projectPort = "8080";
            String logLevel = "Information";
            String projectDescription = "An ASP.NET BEGIN Project";
            String frameworkVersion = "8.0";
            String languageVersion = "";
            String destinationFolder = "/home/itu-chan-alex/Stage/generated";

            ProjectGenerator projectGenerator = new ProjectGenerator();

            HashMap<String, Object> frameworkConfiguration = new HashMap<>();
            frameworkConfiguration.put("loggingLevel", logLevel);
            frameworkConfiguration.put("frameworkVersion", frameworkVersion);

            //======SECURITY CONFIG ==========//
            frameworkConfiguration.put("securityType", ".NET Security - JWT");

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
            context.setGenerateFrontendApp(true);
            context.setFrontendPort(frontendFramework.getDefaultPort());

            projectGenerator.generateProject(context);


            // Assertion pour vérifier si le dossier existe
            Path path = Path.of(destinationFolder);
            assertTrue(Files.exists(path) && Files.isDirectory(path), "Le dossier de destination n'existe pas.");

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @Test
    void generateProjetJavaVueJS() {
        var credentials = new Credentials()
                .setHost("localhost")
                .setPort("5432")
                .setSchemaName("public")
                .setDatabaseName("bakery")
                .setUser("chan_alex")
                .setPwd("chanalex")
                .setTrustCertificate(true).
                setUseSSL(true).
                setAllowPublicKeyRetrieval(true);
        try {
            int databaseId = Constantes.PostgreSQL_ID;
            int languageId = Constantes.Java_ID;
            int frameworkId = Constantes.Spring_REST_API_ID;
            int projectId = Constantes.Maven_ID;
            int frontendLanguageId = Constantes.TYPESCRIPT_ID;
            int frontendFrameworkId = Constantes.VUE_JS_ID;

            var database = ProjectGenerator.databases.get(databaseId);
            var language = ProjectGenerator.languages.get(languageId);
            var framework = ProjectGenerator.frameworks.get(frameworkId);
            var project = ProjectGenerator.projects.get(projectId);
            var frontendLangage=ProjectGenerator.frontendLanguage.get(frontendLanguageId);
            var frontendFramework=ProjectGenerator.frontendFrameworks.get(frontendFrameworkId);

            List<String> generationOptions = List.of("Model", "DAO", "Service", "Controller");
            String projectName = "relations";
            String groupLink = "mg.akademia";
            String projectPort = "8000";
            String logLevel = "INFO";
            String hibernateDdlAuto = "none";
            String projectDescription = "test";
            String frameworkVersion = "3.3.6";
            String languageVersion = "21";
            String destinationFolder = "/home/itu-chan-alex/Stage/generated";
            List<RelationParameter> relations = new ArrayList<>();
            relations.add(new RelationParameter("sales", "sales_details", false, false));


            ProjectGenerator projectGenerator = new ProjectGenerator();

            HashMap<String, Object> frameworkConfiguration = new HashMap<>();
            frameworkConfiguration.put("hibernateDdlAuto", hibernateDdlAuto);
            frameworkConfiguration.put("loggingLevel", logLevel);
            frameworkConfiguration.put("frameworkVersion", frameworkVersion);

            //======SECURITY CONFIG ==========//
            frameworkConfiguration.put("securityType", "Spring Security - JWT");

            //===== USE EUREKA SERVER =======//
            framework.setUseCloud(false);
            framework.setUseEurekaServer(false);
            frameworkConfiguration.put("eurekaServerURL", "http://localhost:8761/eureka");
            frameworkConfiguration.put("projectNonSecurePort", projectPort);
            //==============================//

            HashMap<String, Object> languageConfiguration = new HashMap<>();
            languageConfiguration.put("languageVersion", languageVersion);
            languageConfiguration.put("frameworkCaching", "nom");
            List<String> entityNames = new ArrayList<>();

            ProjectGenerationContext context = new ProjectGenerationContext();
            context.setLanguage(language);
            context.setFramework(framework);
            context.setFrontendFramework(frontendFramework);
            context.setFrontendLanguage(frontendLangage);
            context.setProject(project);
            context.setCredentials(credentials);
            context.setDatabase(database);
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
            context.setGenerateFrontendApp(true);
            context.setFrontendPort(frontendFramework.getDefaultPort());
            context.setRelationParameters(relations);

            projectGenerator.generateProject(context);

            Path path = Path.of(destinationFolder);
            assertTrue(Files.exists(path) && Files.isDirectory(path), "Le dossier de destination n'existe pas.");

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

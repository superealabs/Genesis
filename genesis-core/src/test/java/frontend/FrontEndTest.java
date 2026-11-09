package frontend;


import org.junit.jupiter.api.Test;
import org.labs.genesis.config.Constantes;
import org.labs.genesis.config.ProjectGenerationContext;
import org.labs.genesis.config.langage.FilesEdit;
import org.labs.genesis.config.langage.generator.project.ProjectGenerator;
import org.labs.genesis.connexion.Credentials;
import org.labs.genesis.connexion.model.RelationParameter;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FrontEndTest {
    @Test
    public void testCollectionYamlData()
    {
        ProjectGenerator projectGenerator = new ProjectGenerator();
    }

    @Test
    void dashboardFilesAreConfiguredForEachFrontend() {
        new ProjectGenerator();

        for (int frameworkId : List.of(
                Constantes.ANGULAR_ID,
                Constantes.VUE_JS_ID,
                Constantes.REACT_ID
        )) {
            var framework = ProjectGenerator.frontendFrameworks.get(frameworkId);
            assertNotNull(framework);
            assertNotNull(framework.getDashboardFiles());
            assertTrue(framework.getDashboardFiles().stream()
                    .anyMatch(file -> file.getFileName().toLowerCase().startsWith("dashboard")));
            assertTrue(framework.getDashboardFiles().stream()
                    .allMatch(file -> file.getDestinationPath() != null && !file.getDestinationPath().isBlank()),
                    "Every dashboard file must have a destination path for frontend " + frameworkId);
        }
    }

    @Test
    void dashboardTemplatesRenderStaticConfiguredPages() throws Exception {
        new ProjectGenerator();
        Map<String, Object> metadata = dashboardTemplateMetadata();

        assertStaticDashboardTemplate(Constantes.ANGULAR_ID, "dashboard.component", "html",
                "Sales overview", "<app-dashboard-visualization", 3, metadata);
        assertStaticDashboardTemplate(Constantes.VUE_JS_ID, "DashboardView", "vue",
                "Sales overview", "<DashboardVisualization", 3, metadata);
        assertStaticDashboardTemplate(Constantes.REACT_ID, "DashboardPage", "tsx",
                "Sales overview", "<DashboardVisualization", 3, metadata);

        var angularComponent = ProjectGenerator.frontendFrameworks.get(Constantes.ANGULAR_ID)
                .getDashboardFiles().stream()
                .filter(file -> "dashboard.component".equals(file.getFileName())
                        && "ts".equals(file.getExtension()))
                .findFirst()
                .orElseThrow();
        assertTrue(ProjectGenerator.engine.render(angularComponent.getContent(), metadata)
                .contains("templateUrl: './dashboard.component.html'"));

        for (int frameworkId : List.of(Constantes.ANGULAR_ID, Constantes.VUE_JS_ID, Constantes.REACT_ID)) {
            var files = ProjectGenerator.frontendFrameworks.get(frameworkId).getDashboardFiles();
            assertTrue(files.stream().noneMatch(file -> "dashboard-data".equals(file.getFileName())),
                    "Dashboard page configuration must not be emitted as a runtime data module.");
            assertTrue(files.stream().anyMatch(file ->
                            file.getFileName().toLowerCase().contains("visualization")),
                    "Each frontend must generate its reusable visualization component.");
            assertNoNestedDashboardDirectives(files);
        }
    }

    private void assertStaticDashboardTemplate(
            int frameworkId,
            String fileName,
            String extension,
            String expectedPageTitle,
            String expectedVisualizationTag,
            int expectedVisualizationCount,
            Map<String, Object> metadata
    ) throws Exception {
        var files = ProjectGenerator.frontendFrameworks.get(frameworkId).getDashboardFiles();
        var dashboardFile = files.stream()
                .filter(file -> fileName.equals(file.getFileName()) && extension.equals(file.getExtension()))
                .findFirst()
                .orElseThrow();

        String rendered = ProjectGenerator.engine.render(dashboardFile.getContent(), metadata);

        assertTrue(rendered.contains(expectedPageTitle), "Configured page title is missing: " + fileName);
        assertTrue(countOccurrences(rendered, expectedVisualizationTag) == expectedVisualizationCount,
                "Configured visualization markup was not generated statically: " + fileName);
        assertTrue(!rendered.contains("{{#"));
        assertTrue(!rendered.contains("${"));
        assertTrue(!rendered.contains("dashboardPages.map"));
        assertTrue(!rendered.contains("dashboard-data"));
    }

    private Map<String, Object> dashboardTemplateMetadata() {
        Map<String, Object> salesPage = Map.of(
                "title", "Sales overview",
                "titleHtml", "Sales overview",
                "slug", "sales",
                "endpointPath", "/dashboard/sales",
                "angularVisualizationsMarkup", "<app-dashboard-visualization></app-dashboard-visualization>\n"
                        + "<app-dashboard-visualization></app-dashboard-visualization>",
                "vueVisualizationsMarkup", "<DashboardVisualization />\n<DashboardVisualization />",
                "reactVisualizationsMarkup", "<DashboardVisualization />\n<DashboardVisualization />"
        );
        Map<String, Object> customerPage = Map.of(
                "title", "Customers",
                "titleHtml", "Customers",
                "slug", "customers",
                "endpointPath", "/dashboard/customers",
                "angularVisualizationsMarkup", "<app-dashboard-visualization></app-dashboard-visualization>",
                "vueVisualizationsMarkup", "<DashboardVisualization />",
                "reactVisualizationsMarkup", "<DashboardVisualization />"
        );
        return Map.of(
                "dashboardPages", List.of(salesPage, customerPage),
                "dashboardInitialPageSlug", "sales",
                "dashboardInitialPageSlugLiteral", "\"sales\"",
                "dashboardInitialPageEndpointPath", "/dashboard/sales"
        );
    }

    private void assertNoNestedDashboardDirectives(List<FilesEdit> dashboardFiles) {
        java.util.regex.Pattern directive = java.util.regex.Pattern.compile(
                "\\{\\{#(?:each|for|if)\\b|\\{\\{/(?:each|for|if)\\}\\}"
        );
        for (FilesEdit dashboardFile : dashboardFiles) {
            String content = dashboardFile.getContent();
            int depth = 0;
            java.util.regex.Matcher matcher = directive.matcher(content);
            while (matcher.find()) {
                if (matcher.group().startsWith("{{#")) {
                    depth++;
                    assertTrue(depth <= 1,
                            "Dashboard templates must not nest Genesis control directives: "
                                    + dashboardFile.getFileName());
                } else {
                    depth--;
                }
            }
            assertTrue(depth == 0,
                    "Dashboard template has unbalanced Genesis control directives: "
                            + dashboardFile.getFileName());
        }
    }

    private int countOccurrences(String value, String search) {
        int count = 0;
        int offset = 0;
        while ((offset = value.indexOf(search, offset)) >= 0) {
            count++;
            offset += search.length();
        }
        return count;
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
                .setDatabaseName("fanamby")
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
            String projectName = "FanambyPresence";
            String groupLink = "org.example";
            String projectPort = "8000";
            String logLevel = "INFO";
            String hibernateDdlAuto = "none";
            String projectDescription = "test";
            String frameworkVersion = "3.3.6";
            String languageVersion = "21";
            String destinationFolder = "/home/itu-chan-alex/Stage/generated";
            List<RelationParameter> relations = new ArrayList<>();
//            "relationsRules" : [ {
//                "parentTable" : "match_foot",
//                        "childTable" : "composition",
//                        "mandatory" : false,
//                        "hasForm" : true
//            }, {
//                "parentTable" : "match_foot",
//                        "childTable" : "presence_match",
//                        "mandatory" : false,
//                        "hasForm" : true
//            }, {
//                "parentTable" : "entrainement",
//                        "childTable" : "presence_entrainement",
//                        "mandatory" : false,
//                        "hasForm" : true
//            } ]
            relations.add(new RelationParameter("entrainement", "presence_entrainement", false, true));
            relations.add(new RelationParameter("match_foot", "composition", false, true));
            relations.add(new RelationParameter("match_foot", "presence_match", false, true));


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

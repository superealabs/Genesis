package org.labs.genesis.config.langage.generator.project;

import org.labs.genesis.config.Constantes;
import org.labs.genesis.config.ProjectGenerationContext;
import org.labs.genesis.config.langage.FilesEdit;
import org.labs.genesis.config.langage.Framework;
import org.labs.genesis.config.langage.Language;
import org.labs.genesis.config.langage.Project;
import org.labs.genesis.config.langage.generator.framework.APIGenerator;
import org.labs.genesis.config.langage.generator.framework.GenesisGenerator;
import org.labs.genesis.connexion.Credentials;
import org.labs.genesis.connexion.Database;
import org.labs.genesis.connexion.model.TableMetadata;
import org.labs.genesis.engine.GenesisTemplateEngine;
import org.labs.utils.FileUtils;

import java.io.IOException;
import java.sql.Connection;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.labs.genesis.config.ProjectGenerationContext.*;
import static org.labs.genesis.config.langage.generator.framework.FrameworkMetadataProvider.getHashMapDaoGlobal;
import static org.labs.genesis.config.langage.generator.project.ProjectMetadataProvider.getInitialHashMap;
import static org.labs.genesis.config.langage.generator.project.ProjectMetadataProvider.getProjectFilesEditsHashMap;

public class ProjectGenerator {

    public static final Map<Integer, Project> projects;
    public static final Map<Integer, Database> databases;
    public static final Map<Integer, Language> languages;
    public static final Map<Integer, Framework> frameworks;
    public static final GenesisTemplateEngine engine;

    static {
        try {
            engine = new GenesisTemplateEngine();

            databases = Arrays.stream(FileUtils.fromJson(Database[].class, Constantes.DATABASE_JSON))
                    .collect(Collectors.toMap(Database::getId, database -> database));

            languages = Arrays.stream(FileUtils.fromJson(Language[].class, Constantes.LANGUAGE_JSON))
                    .collect(Collectors.toMap(Language::getId, language -> language));

            projects = Arrays.stream(FileUtils.fromYaml(Project[].class, Constantes.PROJECT_YAML))
                    .collect(Collectors.toMap(Project::getId, project -> project));

            frameworks = Arrays.stream(FileUtils.fromYaml(Framework[].class, Constantes.FRAMEWORK_YAML))
                    .collect(Collectors.toMap(Framework::getId, framework -> framework));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public ProjectGenerator() {
    }

    public static void renderAndCopyFiles(List<Project.ProjectFiles> projectFiles, HashMap<String, Object> initializeHashMap) throws IOException {
        for (Project.ProjectFiles projectFile : projectFiles) {
            String sourceFilePath = projectFile.getSourcePath() + projectFile.getFileName();
            String destinationFilePathSimple = projectFile.getDestinationPath() + projectFile.getFileName();
            String destinationFilePath = engine.simpleRender(destinationFilePathSimple, initializeHashMap);

            System.out.println("Rendering and copying file:");
            System.out.println("Source: " + sourceFilePath);
            System.out.println("Rendered destination: " + destinationFilePath);
            System.out.println();
            FileUtils.copyFile(sourceFilePath, destinationFilePath, "");
        }
    }

    public static void renderAndCopyFolders(List<Project.ProjectFolders> projectFolders, HashMap<String, Object> initializeHashMap) throws IOException {
        for (Project.ProjectFolders projectFolder : projectFolders) {
            String sourceFolderPath = projectFolder.getSourcePath();
            String destinationFolderPath = engine.simpleRender(projectFolder.getDestinationPath() + projectFolder.getFolderName(), initializeHashMap);

            System.out.println("Rendering and copying folder:");
            System.out.println("Source folder: " + sourceFolderPath);
            System.out.println("Rendered destination folder: " + destinationFolderPath);

            FileUtils.copyDirectory(sourceFolderPath, destinationFolderPath);
        }
    }

    public static void renderFilesEdits(List<FilesEdit> filesEdits, HashMap<String, Object> initializeHashMap) throws Exception {
        for (FilesEdit projectFile : filesEdits) {
            String destinationFilePath = engine.simpleRender(projectFile.getDestinationPath(), initializeHashMap);
            String fileName = engine.render(projectFile.getFileName(), initializeHashMap);
            String content = engine.render(projectFile.getContent(), initializeHashMap);
            String extension = projectFile.getExtension();

            // ALT rendering for specific placeholders
            content = engine.simpleRenderAlt(content, Map.of("spring-cloud.version", "${spring-cloud.version}"));
            content = engine.simpleRenderAlt(content, Map.of("spring.application.name", "${spring.application.name}"));
            content = engine.simpleRenderAlt(content, Map.of("server.port", "${server.port}"));
            content = engine.simpleRenderAlt(content, Map.of("spring.datasource.url", "${spring.datasource.url}"));
            content = engine.simpleRenderAlt(content, Map.of("spring.datasource.url", "${spring.datasource.url}"));
            content = engine.simpleRenderAlt(content, Map.of("HOSTNAME", "${HOSTNAME}"));
            content = engine.simpleRenderAlt(content, Map.of("server.port", "${server.port}"));
            content = engine.simpleRenderAlt(content, Map.of("spring.cloud.client.ip-address", "${spring.cloud.client.ip-address}"));
            content = engine.simpleRenderAlt(content, Map.of("security.user.username:admin", "${security.user.username:admin}"));
            content = engine.simpleRenderAlt(content, Map.of("security.user.password:admin", "${security.user.password:admin}"));
            content = engine.simpleRenderAlt(content, Map.of("security.user.role:admin", "${security.user.role:USER}"));

            System.out.println("\nEditing file:");
            System.out.println("Rendered destination path: " + destinationFilePath);
            System.out.println("Rendered file name: " + fileName);
            System.out.println("Extension: " + extension);

            FileUtils.createFile(destinationFilePath, fileName, extension, content);
            System.out.println("File edited and created successfully: " + fileName + "\n");
        }
    }

    private void generateProjectFiles(ProjectGenerationContext context, List<TableMetadata> entities) throws Exception {
        HashMap<String, Object> initializeHashMap = getInitialHashMap(
                context.getDestinationFolder(),
                context.getProjectName(),
                context.getGroupLink()
        );

        HashMap<String, Object> projectFilesEditsHashMap = getProjectFilesEditsHashMap(
                context.getDestinationFolder(),
                context.getProjectName(),
                context.getGroupLink(),
                context.getProjectPort(),
                context.getDatabase(),
                context.getCredentials(),
                context.getLanguage(),
                context.getProjectDescription(),
                context.getLanguageConfiguration(),
                context.getFramework(),
                context.getFrameworkConfiguration()
        );

        if (context.getFramework().getUseDB())
            projectFilesEditsHashMap.putAll(getHashMapDaoGlobal(context.getFramework(), entities, context.getProjectName()));

        renderAndCopyFiles(context.getProject().getProjectFiles(), initializeHashMap);
        renderAndCopyFolders(context.getProject().getProjectFolders(), initializeHashMap);
        renderFilesEdits(context.getProject().getProjectFilesEdits(), projectFilesEditsHashMap);
        renderFilesEdits(context.getFramework().getAdditionalFiles(), projectFilesEditsHashMap);
    }

    public void generateBackendComponents(ProjectGenerationContext context,
                                          GenesisGenerator genesisGenerator,
                                          TableMetadata tableMetadata,
                                          boolean generateComponentOnly) throws Exception {

        String renderedDestinationFolder = engine.simpleRender(context.getDestinationFolder(), Map.of("projectName", context.getProjectName()));
        System.out.println("Generating backend components for project: " + context.getProjectName() + " at rendered destination: " + renderedDestinationFolder);
        System.out.println("The entity: " + tableMetadata.getTableName() + "\n");

        // S'assurer que le répertoire de destination existe
        FileUtils.createDirectory(renderedDestinationFolder);

        List<String> generationOptions = context.getGenerationOptions();
        Framework framework = context.getFramework();
        Language language = context.getLanguage();
        String projectName = context.getProjectName();
        String groupLink = context.getGroupLink();

        if (generationOptions.contains(COMPONENT_MODEL) && framework.getModel().getToGenerate()) {
            System.out.println("Generating " + COMPONENT_MODEL + " component...");
            genesisGenerator.generateModel(framework, language, tableMetadata, renderedDestinationFolder, projectName, groupLink, generateComponentOnly);
        }

        if (generationOptions.contains(COMPONENT_DAO) && framework.getModelDao().getToGenerate()) {
            System.out.println("Generating " + COMPONENT_DAO + " component..." + tableMetadata.getClassName());
            genesisGenerator.generateDao(framework, language, tableMetadata, renderedDestinationFolder, projectName, groupLink, generateComponentOnly);
        }

        if (generationOptions.contains(COMPONENT_SERVICE) && framework.getService().getToGenerate()) {
            System.out.println("Generating " + COMPONENT_SERVICE + " component...");
            genesisGenerator.generateService(framework, language, tableMetadata, renderedDestinationFolder, projectName, groupLink, generateComponentOnly);
        }

        if (generationOptions.contains(COMPONENT_CONTROLLER) && framework.getController().getToGenerate()) {
            System.out.println("Generating " + COMPONENT_CONTROLLER + " component...");
            genesisGenerator.generateController(framework, language, tableMetadata, renderedDestinationFolder, projectName, groupLink, generateComponentOnly);
        }

        System.out.println("Backend component generation completed for project: " + projectName);
    }

    public void generateProject(ProjectGenerationContext context) throws Exception {
        if (context.isGenerateProjectStructure()) {
            // Générer le projet complet
            generateFullProject(context);
        } else {
            // Générer uniquement les composants
            generateComponentsOnly(context);
        }
    }

    private void generateFullProject(ProjectGenerationContext context) throws Exception {
        Database database = context.getDatabase();
        Framework framework = context.getFramework();
        Credentials credentials = context.getCredentials();
        Connection connection = context.getConnection();
        Language language = context.getLanguage();

        if (framework.getUseDB()) {
            try (Connection connex = (connection != null) ? connection : database.getConnection(credentials)) {
                List<TableMetadata> entities = database.getEntitiesByNames(context.getEntityNames(), connex, credentials, language);
                GenesisGenerator genesisGenerator = new APIGenerator(ProjectGenerator.engine);

                for (TableMetadata tableMetadata : entities) {
                    generateBackendComponents(
                            context,
                            genesisGenerator,
                            tableMetadata,
                            false
                    );
                }

                generateProjectFiles(context, entities);

            } catch (Exception e) {
                throw new RuntimeException("\nError in generateFullProject : \n" + e);
            }
        } else {
            generateProjectFiles(context, null);
        }
    }

    private void generateComponentsOnly(ProjectGenerationContext context) {
        Database database = context.getDatabase();
        Framework framework = context.getFramework();
        Credentials credentials = context.getCredentials();
        Connection connection = context.getConnection();
        Language language = context.getLanguage();

        if (framework.getUseDB()) {
            try (Connection connex = (connection != null) ? connection : database.getConnection(credentials)) {
                List<TableMetadata> entities = database.getEntitiesByNames(context.getEntityNames(), connex, credentials, language);
                GenesisGenerator genesisGenerator = new APIGenerator(ProjectGenerator.engine);

                for (TableMetadata tableMetadata : entities) {
                    generateBackendComponents(
                            context,
                            genesisGenerator,
                            tableMetadata,
                            true
                    );
                }
            } catch (Exception e) {
                throw new RuntimeException("\nError in generateComponentsOnly : \n" + e);
            }
        }
    }
}

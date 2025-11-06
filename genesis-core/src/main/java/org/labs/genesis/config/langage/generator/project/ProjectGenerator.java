package org.labs.genesis.config.langage.generator.project;

import org.labs.genesis.config.Constantes;
import org.labs.genesis.config.ProjectGenerationContext;
import org.labs.genesis.config.langage.*;
import org.labs.genesis.config.langage.generator.framework.APIGenerator;
import org.labs.genesis.config.langage.generator.framework.FrameworkMetadataProvider;
import org.labs.genesis.config.langage.generator.framework.GenesisGenerator;
import org.labs.genesis.frontend.generator.ViewsGenerator;
import org.labs.genesis.connexion.Credentials;
import org.labs.genesis.connexion.Database;
import org.labs.genesis.connexion.model.TableMetadata;
import org.labs.genesis.engine.GenesisTemplateEngine;
import org.labs.genesis.frontend.FrontendLanguage;
import org.labs.genesis.frontend.generator.FontendGenerator;
import org.labs.genesis.frontend.generator.FrontendFramework;
import org.labs.genesis.frontend.generator.IFrontendGenerator;
import org.labs.genesis.frontend.generator.IViewsGenerator;
import org.labs.genesis.frontend.generator.frameworkFrontend.FrameworkFrontendMetadataProvider;
import org.labs.genesis.frontend.generator.model.InterfaceLang;
import org.labs.utils.FileUtils;

import java.io.IOException;
import java.sql.Connection;
import java.util.*;
import java.util.stream.Collectors;

import static org.labs.genesis.config.ProjectGenerationContext.*;
import static org.labs.genesis.config.langage.generator.framework.FrameworkMetadataProvider.*;
import static org.labs.genesis.config.langage.generator.project.ProjectMetadataProvider.getInitialHashMap;
import static org.labs.genesis.config.langage.generator.project.ProjectMetadataProvider.getProjectFilesEditsHashMap;

public class ProjectGenerator {

    public static final Map<Integer, Project> projects;
    public static final Map<Integer, Database> databases;
    public static final Map<Integer, Language> languages;
    public static final Map<Integer, Framework> frameworks;
    public static final Map<Integer, LlmApiConfig> llmApiConfigs;
    public static final GenesisTemplateEngine engine;
    public static final Map<Integer, FrontendLanguage> frontendLanguage;
    public static final Map<Integer, FrontendFramework> frontendFrameworks;
    public static final Map<Integer, InterfaceLang> langs;

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
                    .peek(framework -> {
                        try {
                            framework.setFrameworkSecurities();
                            framework.setFrameworkCaching();
                        } catch (IOException e) {
                            throw new RuntimeException("Error while initializing frameworkSecurities for Framework ID: " + framework.getId(), e);
                        }
                    })
                    .collect(Collectors.toMap(Framework::getId, framework -> framework));

            // Load MVC
            Map<Integer, FrameworkMVC> mvcFrameworks = Arrays.stream(FileUtils.fromYaml(FrameworkMVC[].class, Constantes.FRAMEWORK_MVC_YAML))
                    .peek(framework -> {
                        try {
                            framework.setFrameworkSecurities();
                            framework.setViewsTemplate();
                        }
                        catch (IOException e) { throw new RuntimeException("Error initializing frameworkMvc components for ID: " + framework.getId(), e); }
                    })
                    .collect(Collectors.toMap(Framework::getId, framework -> framework));

            // Ajouter tout dans la map principale
            frameworks.putAll(mvcFrameworks);

            llmApiConfigs = Arrays.stream(FileUtils.fromJson(LlmApiConfig[].class, Constantes.LLM_API_CONFIG_JSON))
                    .collect(Collectors.toMap(LlmApiConfig::getId, llmApiConfig -> llmApiConfig));

            frontendLanguage = Arrays.stream(FileUtils.fromJson(FrontendLanguage[].class, Constantes.FRONTEND_LANGUAGE_JSON))
                    .collect(Collectors.toMap(FrontendLanguage::getId, frontLang -> frontLang));

            langs = Arrays.stream(FileUtils.fromYaml(InterfaceLang[].class, Constantes.LANGS_YAML))
                    .collect(Collectors.toMap(InterfaceLang::getId, lang -> lang));

            frontendFrameworks = Arrays.stream(FileUtils.fromYaml(FrontendFramework[].class, Constantes.FRONTEND_FRAMEWORK_YAML))
                    .collect(Collectors.toMap(FrontendFramework::getId, frontFr -> frontFr));


        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public ProjectGenerator() {
    }

    public static void renderAndCopyFiles(List<Project.ProjectFiles> projectFiles, HashMap<String, Object> initializeHashMap) throws Exception {
        for (Project.ProjectFiles projectFile : projectFiles) {
            String sourceFilePath = projectFile.getSourcePath() + projectFile.getFileName();
            String destinationFilePathSimple = projectFile.getDestinationPath() + projectFile.getFileName();
            String destinationFilePath = engine.render(destinationFilePathSimple, initializeHashMap);

            System.out.println("Rendering and copying file:");
            System.out.println("Source: " + sourceFilePath);
            System.out.println("Rendered destination: " + destinationFilePath);
            System.out.println();
            FileUtils.copyFile(sourceFilePath, destinationFilePath, "");
        }
    }

    public static void initFrontendProjectFiles(ProjectGenerationContext context){
        FrontendFramework frontendFramework = context.getFrontendFramework();
        String destinationFilePath = FrameworkFrontendMetadataProvider.getWebappFolder(context);
        String sourcePath = Constantes.FRONTEND_SKELLETTON_DIRECTORY + frontendFramework.getInitPath();
        try {
            FileUtils.copyDirectory(sourcePath,destinationFilePath);
        }
        catch (IOException e) {
            throw new RuntimeException("Error while initializing frontend project files: " + destinationFilePath, e);
        }
    }

    public static void renderAndCopyFolders(List<Project.ProjectFolders> projectFolders, HashMap<String, Object> initializeHashMap) throws Exception {
        for (Project.ProjectFolders projectFolder : projectFolders) {
            String sourceFolderPath = projectFolder.getSourcePath();
            String destinationFolderPath = engine.render(projectFolder.getDestinationPath() + projectFolder.getFolderName(), initializeHashMap);

            System.out.println("Rendering and copying folder:");
            System.out.println("Source folder: " + sourceFolderPath);
            System.out.println("Rendered destination folder: " + destinationFolderPath);

            FileUtils.copyDirectory(sourceFolderPath, destinationFolderPath);
        }
    }

    public static void renderFilesEdits(List<FilesEdit> filesEdits, HashMap<String, Object> initializeHashMap) throws Exception {
        for (FilesEdit projectFile : filesEdits) {
            String destinationFilePath = engine.render(projectFile.getDestinationPath(), initializeHashMap);
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

    private void generateFrontentProjectFiles(ProjectGenerationContext context, List<TableMetadata> entities) throws Exception {
        if (!context.isGenerateFrontendApp()){
            return;
        }
        HashMap<String, Object> finalRenderData = FrameworkFrontendMetadataProvider.getGlobalComponentsHashMap(entities, context);
        String securityType = (String) context.getFrameworkConfiguration().get("securityType");
        Optional<FrameworkSecurity> selectedSecurityOption = context.getFramework().getSelectedSecurityByName(securityType);
        renderFilesEdits(context.getFrontendFramework().getAdditionalFiles(),finalRenderData);
        selectedSecurityOption.ifPresent(security -> {
            try {
                HashMap<String, Object> securityMap=FrameworkFrontendMetadataProvider.getHashMapForSecurity(securityType,context);
                finalRenderData.putAll(securityMap);
                renderFilesEdits(context.getFrontendFramework().getAuthenticationFiles(),finalRenderData);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        IFrontendGenerator frontendGenerator = new FontendGenerator(ProjectGenerator.engine);
        frontendGenerator.generateRessources(context, entities);
    }

    private static void generateViewsFiles (ProjectGenerationContext context, ViewsTemplate viewsTemplate) throws Exception {
        HashMap<String, Object> initializeHashMap = getInitialHashMap(
                context.getDestinationFolder(),
                context.getProjectName(),
                context.getGroupLink()
        );

        FrameworkMVC frameworkMVC = (FrameworkMVC) context.getFramework();
        HashMap<String, Object> frontendHashMap = FrameworkFrontendMetadataProvider.getLayoutHashMap(frameworkMVC.getFrontendLayout());
        frontendHashMap.putAll(FrameworkMetadataProvider.getGeneralViewHashMap(frameworkMVC));

        frontendHashMap.putAll(initializeHashMap);

        renderAndCopyFolders(frameworkMVC.getView().getTemplateEngineFolders(), frontendHashMap);
        renderAndCopyFiles(viewsTemplate.getTemplateFiles(), frontendHashMap);
        renderFilesEdits(viewsTemplate.getTemplateFilesEdits(), frontendHashMap);
        renderFilesEdits(frameworkMVC.getView().getTemplateEngineFilesEdits(), frontendHashMap);
    }

    private void generateProjectFiles(ProjectGenerationContext context, List<TableMetadata> entities) throws Exception {
        HashMap<String, Object> initializeHashMap = getInitialHashMap(
                context.getDestinationFolder(),
                context.getProjectName(),
                context.getGroupLink()
        );
        System.out.println("Generating PROJECT FILESSS 1");

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
        System.out.println("Generating PROJECT FILESSS 2");

        if (context.getFramework().getUseDB()) {
            var mapDaoGlobal = getHashMapDaoGlobal(context.getFramework(), entities, context.getProjectName());
            projectFilesEditsHashMap.putAll(mapDaoGlobal);
        }
        System.out.println("Generating PROJECT FILESSS 3");

        List<FilesEdit> projectFilesEdits;

        if (context.getFramework() instanceof FrameworkMVC mvcFramework) {
            List<String> excludeFilesEdits = mvcFramework.getExcludeProjectFilesEdits();
            projectFilesEdits = context.getProject().getProjectFilesEdits().stream()
                    .filter(file -> !excludeFilesEdits.contains(file.getFileType()))
                    .toList();
            if (mvcFramework.getProjectBranding().hasFavicon() ||
                    mvcFramework.getProjectBranding().useFaviconLink() ||
                    mvcFramework.getProjectBranding().hasLogo() ||
                    mvcFramework.getProjectBranding().useLogoLink()) {
                projectFilesEditsHashMap.putAll(FrameworkFrontendMetadataProvider.getBrandingHashMap(mvcFramework.getProjectBranding()));
            }
        } else {
            projectFilesEdits = new ArrayList<>(context.getProject().getProjectFilesEdits());
        }

        renderAndCopyFiles(context.getProject().getProjectFiles(), projectFilesEditsHashMap);
        renderAndCopyFolders(context.getProject().getProjectFolders(), initializeHashMap);
        renderFilesEdits(projectFilesEdits, projectFilesEditsHashMap);
        renderFilesEdits(context.getFramework().getAdditionalFiles(), projectFilesEditsHashMap);

        String securityType = (String) context.getFrameworkConfiguration().get("securityType");
        System.out.println(securityType + " SECURITYYY");
        Optional<FrameworkSecurity> selectedSecurityOption = context.getFramework().getSelectedSecurityByName(securityType);

        selectedSecurityOption.ifPresent(security -> {
            try {
                renderFilesEdits(security.getSecurityFiles(), projectFilesEditsHashMap);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        String cacheProvider = (String) context.getFrameworkConfiguration().get("cacheProvider");
        Optional<FrameworkCaching> selectedCacheProviderOption = context.getFramework().getSelectedCacheProviderByName(cacheProvider);

        selectedCacheProviderOption.ifPresent(frameworkCaching -> {
            try {
                renderFilesEdits(frameworkCaching.getConfigFiles(), projectFilesEditsHashMap);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void generateFrontendComponents(ProjectGenerationContext context,
                                           IFrontendGenerator frontendGenerator,
                                           TableMetadata tableMetadata,
                                           boolean generateComponentOnly) throws Exception {
         // Create missing Webapp folder
        String webappFolder = FrameworkFrontendMetadataProvider.getWebappFolder(context);
        FileUtils.createDirectory(webappFolder);

        Database database=context.getDatabase();
        FrontendLanguage frontendLanguage=context.getFrontendLanguage();
        FrontendFramework frontendFramework=context.getFrontendFramework();
        String projectName=context.getProjectName();
        String securityType = (String) context.getFrameworkConfiguration().get("securityType");

        tableMetadata.setColumnsFrontendTypes(frontendLanguage, database);
        frontendGenerator.generateComponent(securityType,database,frontendLanguage,frontendFramework,tableMetadata,webappFolder, projectName,  generateComponentOnly);
        frontendGenerator.generateService(database,frontendLanguage,frontendFramework,tableMetadata,webappFolder, projectName, generateComponentOnly);
        frontendGenerator.generateModel(database,frontendLanguage,frontendFramework,tableMetadata,webappFolder, projectName, generateComponentOnly);
        return;
    }

    public void generateViewsComponents (ProjectGenerationContext context,
                                         IViewsGenerator viewsGenerator,
                                         TableMetadata tableMetadata,
                                         boolean generateComponentOnly) throws Exception {

        String renderedDestinationFolder = engine.simpleRender(context.getDestinationFolder(), Map.of("projectName", context.getProjectName()));
        System.out.println("Generating views components for project: " + context.getProjectName() + " at rendered destination: " + renderedDestinationFolder);
        System.out.println("The entity: " + tableMetadata.getTableName() + "\n");

        // S'assurer que le répertoire de destination existe
        FileUtils.createDirectory(renderedDestinationFolder);

        FrameworkMVC framework = (FrameworkMVC) context.getFramework();
        Language language = context.getLanguage();
        String projectName = context.getProjectName();
        Map<String, Object> frameworkOptions = context.getFrameworkConfiguration();
        ViewsTemplate viewsTemplate = context.getViewsTemplate();
        String groupLink = context.getGroupLink();

        viewsGenerator.generateViews(framework,
                frameworkOptions,
                language,
                viewsTemplate,
                tableMetadata,
                context.getDestinationFolder(),
                projectName,
                groupLink
        );

        System.out.println("Views components generation completed for project: " + projectName);
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
        Map<String, Object> frameworkOptions = context.getFrameworkConfiguration();

        if (generationOptions.contains(COMPONENT_MODEL) && framework.getModel().getToGenerate()) {
            System.out.println("Generating " + COMPONENT_MODEL + " component...");
            genesisGenerator.generateModel(framework, frameworkOptions, language, tableMetadata, renderedDestinationFolder, projectName, groupLink, generateComponentOnly);
        }

        if (generationOptions.contains(COMPONENT_DAO) && framework.getModelDao().getToGenerate()) {
            System.out.println("Generating " + COMPONENT_DAO + " component..." + tableMetadata.getClassName());
            genesisGenerator.generateDao(framework, frameworkOptions, language, tableMetadata, renderedDestinationFolder, projectName, groupLink, generateComponentOnly);
        }

        if (generationOptions.contains(COMPONENT_SERVICE) && framework.getService().getToGenerate()) {
            System.out.println("Generating " + COMPONENT_SERVICE + " component...");
            genesisGenerator.generateService(framework, frameworkOptions, language, tableMetadata, renderedDestinationFolder, projectName, groupLink, generateComponentOnly);
        }

        if (generationOptions.contains(COMPONENT_CONTROLLER) && framework.getController().getToGenerate()) {
            System.out.println("Generating " + COMPONENT_CONTROLLER + " component...");
            genesisGenerator.generateController(framework, frameworkOptions, language, tableMetadata, renderedDestinationFolder, projectName, groupLink, generateComponentOnly);
        }

        HashMap<String, Object> tableHashMapData = FrameworkMetadataProvider.getHashMapIntermediaire(tableMetadata, framework, frameworkOptions, renderedDestinationFolder, projectName, groupLink);
        if (tableMetadata.getIsParent()){
            renderFilesEdits(framework.getMereFiles(),tableHashMapData);
        }
        if (tableMetadata.getIsChild()){
            renderFilesEdits(framework.getFilleFiles(),tableHashMapData);
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
    private  void generateFullBackendProject(ProjectGenerationContext context, List<TableMetadata> entities, List<TableMetadata> views) throws Exception {
        GenesisGenerator genesisGenerator = new APIGenerator(ProjectGenerator.engine);
        for (TableMetadata tableMetadata : entities) {
            generateBackendComponents(
                    context,
                    genesisGenerator,
                    tableMetadata,
                    false
            );
        }

        for (TableMetadata tableMetadata : views) {
            generateBackendComponents(
                    context,
                    genesisGenerator,
                    tableMetadata,
                    false
            );
        }
    }
    private  void generateFullFrontendProject(ProjectGenerationContext context, List<TableMetadata> entities, List<TableMetadata> views) throws Exception {
        if (!context.isGenerateFrontendApp()) { return; }
        IFrontendGenerator frontendGenerator = new FontendGenerator(ProjectGenerator.engine);
        initFrontendProjectFiles(context);
        for (TableMetadata tableMetadata : entities) {
            generateFrontendComponents(
                    context,
                    frontendGenerator,
                    tableMetadata,
                    false
            );
        }
        for (TableMetadata tableMetadata : views) {
            generateFrontendComponents(
                    context,
                    frontendGenerator,
                    tableMetadata,
                    false
            );
        }
    }
    private void generateFullViewsComponents(ProjectGenerationContext context, List<TableMetadata> entities, List<TableMetadata> views) throws Exception {
        IViewsGenerator viewsGenerator = new ViewsGenerator(ProjectGenerator.engine);
        FrameworkMVC framework = (FrameworkMVC) context.getFramework();
        Language language = context.getLanguage();

        Map<String, Object> frameworkOptions = context.getFrameworkConfiguration();
        ViewsTemplate viewsTemplate = context.getViewsTemplate();
        String groupLink = context.getGroupLink();

        List<TableMetadata> allEntities = new ArrayList<>();
        allEntities.addAll(entities);
        allEntities.addAll(views);

        viewsGenerator.generateMainLayout(framework, frameworkOptions, language, viewsTemplate, allEntities.toArray(new TableMetadata[0]), context.getDestinationFolder(), context.getProjectName(), groupLink);
        viewsGenerator.generateErrorPage(framework, frameworkOptions, language, viewsTemplate, allEntities.toArray(new TableMetadata[0]), context.getDestinationFolder(), context.getProjectName(), groupLink);
        viewsGenerator.generateResources(framework, frameworkOptions, language, viewsTemplate, allEntities.toArray(new TableMetadata[0]), context.getDestinationFolder(), context.getProjectName(), groupLink);
        for (TableMetadata tableMetadata : entities) {
            generateViewsComponents(
                    context,
                    viewsGenerator,
                    tableMetadata,
                    false
            );
        }
        for (TableMetadata tableMetadata : views) {
            generateViewsComponents(
                    context,
                    viewsGenerator,
                    tableMetadata,
                    false
            );
        }
    }
    private void generateFullProject(ProjectGenerationContext context) throws Exception {
        Database database = context.getDatabase();
        Framework framework = context.getFramework();
        Credentials credentials = context.getCredentials();
        useRealSidAndDriverType(database,credentials);
        Connection connection = context.getConnection();
        Language language = context.getLanguage();

        if (framework.getUseDB()) {
            try (Connection connex = (connection != null) ? connection : database.getConnection(credentials)) {

                List<TableMetadata> entities = context.getEntityTables();
                List<TableMetadata> views = context.getViewTables();

                generateFullBackendProject(context, entities, views);
                if (framework instanceof FrameworkMVC) {
                    generateFullViewsComponents(context, entities, views);
                } else {
                    generateFullFrontendProject(context, entities, views);
                }

                List<TableMetadata> allEntities = new ArrayList<>();
                allEntities.addAll(entities);
                allEntities.addAll(views);

                generateProjectFiles(context, allEntities);
                if (framework instanceof FrameworkMVC) {
                    ViewsTemplate viewsTemplate = context.getViewsTemplate();
                    generateViewsFiles(context, viewsTemplate);
                } else {
                    generateFrontentProjectFiles(context, allEntities);
                }


            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("\nError in generateFullProject : \n" + e);
            }
        } else {
            generateProjectFiles(context, null);
        }
    }

    private void useRealSidAndDriverType(Database database,Credentials credentials)
    {
        if (credentials == null) {
            return;
        }
        if(credentials.getSID()!=null)
        {
            database.setSid(credentials.getSID());
        }
        if(credentials.getDriverType()!=null)
        {
            database.setDriverType(credentials.getDriverType());
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
                List<TableMetadata> entities = database.getEntitiesByNames(context.getEntityNames(), connex, credentials, language, framework);
                List<TableMetadata> views = database.getViewsByNames(context.getViewNames(), connex, credentials, language, framework);
                GenesisGenerator genesisGenerator = new APIGenerator(ProjectGenerator.engine);

                for (TableMetadata tableMetadata : entities) {
                    generateBackendComponents(
                            context,
                            genesisGenerator,
                            tableMetadata,
                            true
                    );
                }

                for (TableMetadata tableMetadata : views) {
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

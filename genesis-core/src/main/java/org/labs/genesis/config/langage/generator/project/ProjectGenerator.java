package org.labs.genesis.config.langage.generator.project;

import org.labs.genesis.config.Constantes;
import org.labs.genesis.config.ProjectGenerationContext;
import org.labs.genesis.config.git.GitConfiguration;
import org.labs.genesis.config.langage.*;
import org.labs.genesis.config.langage.generator.framework.APIGenerator;
import org.labs.genesis.config.langage.generator.framework.FrameworkMetadataProvider;
import org.labs.genesis.config.langage.generator.framework.GenesisGenerator;
import org.labs.genesis.config.langage.generator.indicator.NoOpProgressReporter;
import org.labs.genesis.config.langage.generator.indicator.ProgressReporter;
import org.labs.genesis.config.langage.generator.sync.builder.GenesisContextBuilder;
import org.labs.genesis.config.langage.generator.sync.models.GenesisContextModel;
import org.labs.genesis.connexion.model.RelationParameter;
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
import org.labs.utils.GitUtils;
import org.labs.utils.StringUtils;

import java.io.File;
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
                            framework.setFrameworkCaching();
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
        if ( !context.isGenerateFrontendSkeletons() || frontendFramework == null) {
            return;
        }
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
        if (projectFolders != null){
            for (Project.ProjectFolders projectFolder : projectFolders) {
                String sourceFolderPath = projectFolder.getSourcePath();
                String destinationFolderPath = engine.render(projectFolder.getDestinationPath() + projectFolder.getFolderName(), initializeHashMap);

                System.out.println("Rendering and copying folder:");
                System.out.println("Source folder: " + sourceFolderPath);
                System.out.println("Rendered destination folder: " + destinationFolderPath);

                FileUtils.copyDirectory(sourceFolderPath, destinationFolderPath);
            }
        }
    }
    public static void removeFilesEdits(List<FilesEdit> filesEdits, HashMap<String, Object> initializeHashMap) throws Exception {
        for (FilesEdit projectFile : filesEdits) {
            String destinationFilePath = engine.render(projectFile.getDestinationPath(), initializeHashMap);
            String fileName = engine.render(projectFile.getFileName(), initializeHashMap);
            String extension = projectFile.getExtension();
            FileUtils.deleteFile(destinationFilePath, fileName, extension);
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

            FileUtils.createOrMergeFile((String)initializeHashMap.get("destinationFolder"),destinationFilePath, fileName, extension, content);
            System.out.println("File edited and created successfully: " + fileName + "\n");
        }
    }

    public static Framework findFrameworkById(int frameworkId) {
        Framework framework = ProjectGenerator.frameworks.get(frameworkId);
        if (frameworkId > 0 && framework == null) {
            throw new RuntimeException("Framework not found: " + frameworkId);
        }
        return framework;
    }

    public static FrontendFramework findFrontendFrameworkById(int frameworkId) {
        FrontendFramework framework = ProjectGenerator.frontendFrameworks.get(frameworkId);
        if (frameworkId > 0 && framework == null) {
            throw new RuntimeException("Frontend Framework not found: " + frameworkId);
        }
        return framework;
    }

    public static Project findProjectById(int projectId) {
        Project project = ProjectGenerator.projects.get(projectId);
        if (projectId > 0 && project == null) {
            throw new RuntimeException("Project not found: " + projectId);
        }
        return  project;
    }

    public static Language findLanguageById(int languageId) {
        Language language = ProjectGenerator.languages.get(languageId);
        if (languageId > 0 && language == null) {
            throw new RuntimeException("Language with id " + languageId + " not found.");
        }
        return language;
    }

    public static FrontendLanguage findFrontendLanguageById(int languageId) {
        FrontendLanguage language = ProjectGenerator.frontendLanguage.get(languageId);
        if (languageId > 0 && language == null) {
            throw new RuntimeException("Frontend Language with id " + languageId + " not found.");
        }
        return language;
    }

    public static Database findDatabaseById(int databaseId) {
        Database database = ProjectGenerator.databases.get(databaseId);
        if (databaseId > 0 && database == null) {
            throw new RuntimeException("Database not found: " + databaseId);
        }
        return database;
    }

    private void generateFrontendProjectFiles(ProjectGenerationContext context, List<TableMetadata> entities, ProgressReporter indicator) throws Exception {
        if (!context.isGenerateFrontendApp() || !context.isGenerateFrontendStructure()) {
            return;
        }
        FrontendFramework frontendFramework = context.getFrontendFramework();
        if (frontendFramework == null) {
            return;
        }
        HashMap<String, Object> finalRenderData = FrameworkFrontendMetadataProvider.getGlobalComponentsHashMap(entities, context);
        String securityType = (String) context.getFrameworkConfiguration().get("securityType");
        Optional<FrameworkSecurity> selectedSecurityOption = context.getFramework().getSelectedSecurityByName(securityType);
        indicator.setFraction(0.73);
        selectedSecurityOption.ifPresent(security -> {
            try {
                HashMap<String, Object> securityMap=FrameworkFrontendMetadataProvider. getHashMapForSecurity(securityType,context);
                finalRenderData.putAll(securityMap);
                if (frontendFramework.getAuthenticationFiles() != null) {
                    indicator.setText2("generating frontend authentication files");
                    renderFilesEdits(frontendFramework.getAuthenticationFiles(),finalRenderData);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        indicator.setFraction(0.76);
        if (frontendFramework.getAdditionalFiles() != null) {
            indicator.setText2("generating frontend additional files");
            renderFilesEdits(frontendFramework.getAdditionalFiles(),finalRenderData);
        }
        indicator.setFraction(0.85);
        indicator.setText2("generating frontend ressources");
        IFrontendGenerator frontendGenerator = new FontendGenerator(ProjectGenerator.engine);
        frontendGenerator.generateRessources(context, entities);
    }

    private void generateViewsFiles (ProjectGenerationContext context, ViewsTemplate viewsTemplate) throws Exception {
        if (!context.isGenerateViewsTemplates()){
            return;
        }
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

    protected void generateProjectFiles(ProjectGenerationContext context, List<TableMetadata> entities, ProgressReporter indicator) throws Exception {
        if (!context.isGenerateProjectStructure()){
            return;
        }
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
            if (context.getFramework().getModelDao() != null) {
                var mapDaoGlobal = getHashMapDaoGlobal(context.getFramework(), entities, context.getProjectName());
                projectFilesEditsHashMap.putAll(mapDaoGlobal);
            } else {
                // For frameworks without ModelDao (like Django), still generate entitiesImports and entitiesAll
                var entitiesMetadata = FrameworkMetadataProvider.generateEntitiesImportsAndAll(entities);
                projectFilesEditsHashMap.putAll(entitiesMetadata);
                // Add entities list for template loops (e.g., {{#each entities}})
                List<String> entityNames = new ArrayList<>();
                for (TableMetadata entity : entities) {
                    entityNames.add(entity.getClassName());
                }
                projectFilesEditsHashMap.put("entities", entityNames);
            }
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

        indicator.setText2("renderinng project files");
        indicator.setFraction(0.62);
        renderAndCopyFiles(context.getProject().getProjectFiles(), projectFilesEditsHashMap);
        indicator.setFraction(0.63);
        renderAndCopyFiles(context.getProject().getProjectFiles(), initializeHashMap);
        indicator.setFraction(0.64);
        renderAndCopyFolders(context.getProject().getProjectFolders(), initializeHashMap);
        indicator.setFraction(0.65);
        indicator.setText2("generation project files");
        renderFilesEdits(projectFilesEdits, projectFilesEditsHashMap);
        indicator.setFraction(0.67);
        indicator.setText2("generation framework additional files");
        renderFilesEdits(context.getFramework().getAdditionalFiles(), projectFilesEditsHashMap);

        String securityType = (String) context.getFrameworkConfiguration().get("securityType");
        System.out.println(securityType + " SECURITYYY");

        indicator.setFraction(0.68);
        if (securityType != null && !securityType.isBlank()) {
            Optional<FrameworkSecurity> selectedSecurityOption = context.getFramework()
                    .getFrameworkSecurities()
                    .stream()
                    .filter(fs -> fs.getName().equalsIgnoreCase(securityType))
                    .findFirst();

            selectedSecurityOption.ifPresent(security -> {
                try {
                    indicator.setText2("generating framework security files");
                    renderFilesEdits(security.getSecurityFiles(), projectFilesEditsHashMap);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }
        indicator.setFraction(0.69);
        String cacheProvider = (String) context.getFrameworkConfiguration().get("cacheProvider");
        Optional<FrameworkCaching> selectedCacheProviderOption = context.getFramework().getSelectedCacheProviderByName(cacheProvider);

        selectedCacheProviderOption.ifPresent(frameworkCaching -> {
            try {
                indicator.setText2("generating framework caching files");
                renderFilesEdits(frameworkCaching.getConfigFiles(), projectFilesEditsHashMap);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        indicator.setFraction(0.7);
        // Post-setup for Django: create venv and install requirements
        try {
            if (context.getFramework().getCoreFramework().equalsIgnoreCase("Django")) {
                indicator.setText2("generating django virtual environnement");
                // Vérifier si l'utilisateur a choisi de créer le venv
                Map<String, Object> frameworkConfig = context.getFrameworkConfiguration();
                boolean createVenv = true; // Par défaut, créer le venv si l'option n'est pas spécifiée
                if (frameworkConfig != null && frameworkConfig.containsKey("createVenv")) {
                    Object createVenvValue = frameworkConfig.get("createVenv");
                    if (createVenvValue instanceof Boolean) {
                        createVenv = (Boolean) createVenvValue;
                    } else if (createVenvValue instanceof String) {
                        createVenv = Boolean.parseBoolean((String) createVenvValue);
                    }
                }

                if (createVenv) {
                    String projectPath = engine.simpleRender(context.getDestinationFolder() + "/" + context.getProjectName(),
                            Map.of("projectName", context.getProjectName()));
                    setupDjangoEnvironment(projectPath);
                } else {
                    System.out.println("ℹ️  Création du venv ignorée (option désactivée par l'utilisateur)");
                }
            }
        } catch (Exception e) {
            System.err.println("   ⚠️  Post-setup Django échoué: " + e.getMessage());
        }
    }

    /**
     * Configure a Python virtual environment and installs requirements for Django projects
     */
    private void setupDjangoEnvironment(String projectPath) throws Exception {
        System.out.println("🐍 Configuration de l'environnement Django...");
        File projectDir = new File(projectPath);
        if (!projectDir.exists()) {
            System.out.println("   ⚠️  Dossier projet introuvable: " + projectPath);
            return;
        }

        // 1) Create venv
        if (!createVirtualEnvironment(projectPath)) {
            System.out.println("   ⚠️  Impossible de créer le venv. Étape ignorée.");
            return;
        }

        // 2) Ensure manage.py is executable
        File manage = new File(projectPath + "/manage.py");
        if (manage.exists()) {
            try { manage.setExecutable(true); } catch (Exception ignored) {}
        }

        // 3) Install requirements using venv pip
        File pipFile = new File(projectPath + "/venv/bin/pip");
        if (!pipFile.exists()) {
            System.out.println("   ⚠️  pip introuvable dans le venv, installation des deps ignorée.");
            return;
        }

        File requirements = new File(projectPath + "/requirements.txt");
        if (!requirements.exists()) {
            System.out.println("   ℹ️  Aucun requirements.txt trouvé, rien à installer.");
            return;
        }

        System.out.println("   📦 Installation des dépendances à partir de requirements.txt...");
        ProcessBuilder pipInstall = new ProcessBuilder(pipFile.getAbsolutePath(), "install", "-r", "requirements.txt");
        pipInstall.directory(projectDir);
        pipInstall.redirectErrorStream(true);
        Process p = pipInstall.start();
        try (java.io.BufferedReader r = new java.io.BufferedReader(new java.io.InputStreamReader(p.getInputStream()))) {
            String line;
            while ((line = r.readLine()) != null) {
                System.out.println("   " + line);
            }
        }
        int code = p.waitFor();
        if (code == 0) {
            System.out.println("   ✅ Dépendances installées avec succès");
        } else {
            System.out.println("   ⚠️  pip install a retourné le code: " + code);
        }
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
        if (frontendLanguage == null || frontendFramework == null) {
            return;
        }
        String projectName=context.getProjectName();
        String securityType = (String) context.getFrameworkConfiguration().get("securityType");

        tableMetadata.setColumnsFrontendTypes(frontendLanguage, database);
        if (context.getFrontendGenerationOptions() != null && context.getFrontendGenerationOptions().contains(ProjectGenerationContext.FRONTEND_COMPONENT)) {
            frontendGenerator.generateComponent(securityType,database,frontendLanguage,frontendFramework,tableMetadata,webappFolder, projectName,  generateComponentOnly);
        }

        if (context.getFrontendGenerationOptions() != null && context.getFrontendGenerationOptions().contains(ProjectGenerationContext.FRONTEND_COMPONENT_SERVICE)) {
            frontendGenerator.generateService(securityType, database, frontendLanguage, frontendFramework, tableMetadata, webappFolder, projectName, generateComponentOnly);
        }

        if (context.getFrontendGenerationOptions() != null && context.getFrontendGenerationOptions().contains(ProjectGenerationContext.FRONTEND_COMPONENT_MODEL)) {
            frontendGenerator.generateModel(securityType, database, frontendLanguage, frontendFramework, tableMetadata, webappFolder, projectName, generateComponentOnly);
        }

        HashMap<String, Object> tableHashMapData = FrameworkFrontendMetadataProvider.getHashMapIntermediaire(tableMetadata, webappFolder, projectName);
        if (tableMetadata.getIsParent()){
            renderFilesEdits(frontendFramework.getMereFiles(),tableHashMapData);
        }
        if (tableMetadata.getIsChild()){
            renderFilesEdits(frontendFramework.getFilleFiles(),tableHashMapData);
        }
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

        // Ensure a default ViewsTemplate for Django (Template 2) when none provided (works for plugin too)
        if (framework instanceof FrameworkMVC && context.getViewsTemplate() == null) {
            FrameworkMVC mvc = (FrameworkMVC) framework;
            try {
                // Prefer a robust ID check
                if (framework.getId() == org.labs.genesis.config.Constantes.Django_ID) {
                    mvc.setViewsTemplate();
                    ViewsTemplate vt = mvc.findViewsTemplateById(2);
                    if (vt != null) {
                        context.setViewsTemplate(vt);
                    }
                }
            } catch (Exception ignored) { }
        }
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

        // Liste des tables Django par défaut à exclure
        Set<String> djangoDefaultTables = Set.of(
            "auth_group", "auth_group_permissions", "auth_permission",
            "auth_user", "auth_user_groups", "auth_user_user_permissions",
            "django_admin_log", "django_content_type", "django_migrations",
            "django_session", "authgroup", "authgrouppermission", "authpermission",
            "authuser", "authusergroup", "authuseruserpermission",
            "djangoadminlog", "djangocontenttype", "djangomigration", "djangosession"
        );

        // Exclure les tables Django par défaut
        if (djangoDefaultTables.contains(tableMetadata.getTableName().toLowerCase())) {
            System.out.println("Skipping Django default table: " + tableMetadata.getTableName());
            return;
        }

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

        if (generationOptions.contains(COMPONENT_DAO) && framework.getModelDao() != null && framework.getModelDao().getToGenerate()) {
            System.out.println("Generating " + COMPONENT_DAO + " component..." + tableMetadata.getClassName());
            genesisGenerator.generateDao(framework, frameworkOptions, language, tableMetadata, renderedDestinationFolder, projectName, groupLink, generateComponentOnly);
        }

        if (generationOptions.contains(COMPONENT_SERVICE) && framework.getService() != null && framework.getService().getToGenerate()) {
            System.out.println("Generating " + COMPONENT_SERVICE + " component...");
            genesisGenerator.generateService(framework, frameworkOptions, language, tableMetadata, renderedDestinationFolder, projectName, groupLink, generateComponentOnly);
        }

        if (generationOptions.contains(COMPONENT_CONTROLLER) && framework.getController() != null && framework.getController().getToGenerate()) {
            System.out.println("Generating " + COMPONENT_CONTROLLER + " component...");
            genesisGenerator.generateController(framework, frameworkOptions, language, tableMetadata, renderedDestinationFolder, projectName, groupLink, generateComponentOnly);
        }

        HashMap<String, Object> tableHashMapData = FrameworkMetadataProvider.getHashMapIntermediaire(language, tableMetadata, framework, frameworkOptions, renderedDestinationFolder, projectName, groupLink);
        if (tableMetadata.getIsParent()){
            renderFilesEdits(framework.getMereFiles(),tableHashMapData);
        }
        if (tableMetadata.getIsChild()){
            renderFilesEdits(framework.getFilleFiles(),tableHashMapData);
        }

        System.out.println("Backend component generation completed for project: " + projectName);
    }

    public void generateProject(ProjectGenerationContext context) throws Exception {
        generateProject(context, new NoOpProgressReporter());
    }

    public void initGit(String path, boolean isCreateRemote, String repoName, String userName, String token) throws Exception {
        GitUtils.gitInit(path);
        GitUtils.gitAdd(path);
        GitUtils.gitCommit(path, "Initialisation du projet");

        if(repoName != null && !repoName.isBlank() && userName != null && !userName.isBlank()) {
            GitUtils.gitRemote(path, userName, repoName);
        }

        if(isCreateRemote) {
            try {
                GitUtils.createRemoteRepo(token, repoName, false);
            } catch (Exception e) {}
            GitUtils.gitPush(path, userName, token);
        }
    }

    public void initGit(ProjectGenerationContext context) throws Exception {
        GitConfiguration config = context.getGitConfiguration();
        if(!config.isUseGit()) return;

        Framework framework = context.getFramework();
        FrontendFramework frontendFramework = context.getFrontendFramework();
        if (framework == null) {
            return;
        }

        String projectPath = engine.simpleRender(context.getDestinationFolder(), Map.of("projectName", context.getProjectName()));
        String frontendPath = FrameworkFrontendMetadataProvider.getWebappFolder(context);

        FilesEdit backendGitIgnoreFile = GitUtils.getGitIgnore(framework.getConditionalFiles());
        String backendPath = backendGitIgnoreFile != null ? engine.simpleRender(backendGitIgnoreFile.getDestinationPath(),
                Map.of("projectName", context.getProjectName(), "destinationFolder", context.getDestinationFolder()))
            : projectPath + "/" + StringUtils.majStart(context.getProjectName());

        GitUtils.generateGitIgnoreIfNeeded(backendGitIgnoreFile, backendPath);

        if(context.isGenerateProjectStructure()) {
            if (frontendFramework == null) {
                return;
            }
            FilesEdit frontendGitIgnoreFile = GitUtils.getGitIgnore(frontendFramework.getConditionalFiles());
            GitUtils.generateGitIgnoreIfNeeded(frontendGitIgnoreFile, frontendPath);
        }

        if(config.isSeparateRepositories()) {
            initGit(backendPath, config.isCreateRemoteRepository(), config.getBackendRepositoryName(),
                        config.getGithubUsername(), config.getGithubToken());
            if(context.isGenerateProjectStructure()) {
                initGit(frontendPath, config.isCreateRemoteRepository(), config.getFrontendRepositoryName(),
                        config.getGithubUsername(), config.getGithubToken());
            }
        } else {
            initGit(projectPath, config.isCreateRemoteRepository(), config.getRepositoryName(),
                    config.getGithubUsername(), config.getGithubToken());
        }
    }

    public void generateProject(ProjectGenerationContext context, ProgressReporter indicator) throws Exception {
        if (context.isGenerateProjectStructure()) {
            generateFullProject(context, indicator);
            generateGenesisfile(context, indicator);
        } else {
            generateComponentsOnly(context, indicator);
        }
        initGit(context);
    }
    private  void generateFullBackendProject(ProjectGenerationContext context, List<TableMetadata> entities, boolean generateComponentOnly, ProgressReporter indicator) throws Exception {
        GenesisGenerator genesisGenerator = new APIGenerator(ProjectGenerator.engine);
        indicator.setProgress(0.4,"Generating backend components");
        int i = 0;
        for (TableMetadata tableMetadata : entities) {
            indicator.setText2("Generating files for entity: " + tableMetadata.getTableName());
            if (i == entities.size() - 1) {
                indicator.setFraction(0.45);
            }
            generateBackendComponents(
                    context,
                    genesisGenerator,
                    tableMetadata,
                    generateComponentOnly
            );
            i++;
        }
    }
    private  void generateFullFrontendProject(ProjectGenerationContext context, List<TableMetadata> entities, ProgressReporter indicator) throws Exception {
        if (!context.isGenerateFrontendApp()) { return; }
        IFrontendGenerator frontendGenerator = new FontendGenerator(ProjectGenerator.engine);
        initFrontendProjectFiles(context);
        indicator.setProgress(0.5,"Generating frontend components");
        int i=0;
        for (TableMetadata tableMetadata : entities) {
            indicator.setText2("Generating files for entity: " + tableMetadata.getTableName());
            if (i == entities.size() - 1) {
                indicator.setFraction(0.55);
            }
            generateFrontendComponents(
                    context,
                    frontendGenerator,
                    tableMetadata,
                    false
            );
            i++;
        }
    }
    private void generateFullViewsComponents(ProjectGenerationContext context, List<TableMetadata> allEntities) throws Exception {
        IViewsGenerator viewsGenerator = new ViewsGenerator(ProjectGenerator.engine);
        FrameworkMVC framework = (FrameworkMVC) context.getFramework();
        Language language = context.getLanguage();

        Map<String, Object> frameworkOptions = context.getFrameworkConfiguration();
        ViewsTemplate viewsTemplate = context.getViewsTemplate();
        String groupLink = context.getGroupLink();


        viewsGenerator.generateMainLayout(framework, frameworkOptions, language, viewsTemplate, allEntities.toArray(new TableMetadata[0]), context.getDestinationFolder(), context.getProjectName(), groupLink);
        viewsGenerator.generateErrorPage(framework, frameworkOptions, language, viewsTemplate, allEntities.toArray(new TableMetadata[0]), context.getDestinationFolder(), context.getProjectName(), groupLink);
        viewsGenerator.generateResources(framework, frameworkOptions, language, viewsTemplate, allEntities.toArray(new TableMetadata[0]), context.getDestinationFolder(), context.getProjectName(), groupLink);
        for (TableMetadata tableMetadata : allEntities) {
            generateViewsComponents(
                    context,
                    viewsGenerator,
                    tableMetadata,
                    false
            );
        }
    }
    protected void generateFullProject(ProjectGenerationContext context, ProgressReporter indicator) throws Exception {
        indicator.setProgress(0.1,"Checking generation settings...");
        Database database = context.getDatabase();
        Framework framework = context.getFramework();
        Credentials credentials = context.getCredentials();
        useRealSidAndDriverType(database,credentials);
        Connection connection = context.getConnection();

        if (framework.getUseDB()) {
            try (Connection connex = (connection != null) ? connection : database.getConnection(credentials)) {
                List<TableMetadata> allEntities = generateFullProjectComponents(context, connex, false, indicator);
                generateFullProjectStrucutres(context, allEntities, indicator);
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("\nError in generateFullProject : \n" + e);
            }
        } else {
            generateProjectFiles(context, null,indicator);
        }
    }

    public List<TableMetadata> generateFullProjectComponents(ProjectGenerationContext context, Connection connex, boolean generateComponentOnly, ProgressReporter indicator) throws Exception {
        indicator.setProgress(0.2,"Fetching data from database...");
        if (context.getEntityTables() == null || context.getEntityTables().isEmpty()) {
            indicator.setText2("Fetching tables");
            context.setEntityTables(connex);
        }
        if (context.getViewTables() == null || context.getViewTables().isEmpty()) {
            indicator.setText("Fetching views");
            context.setViewTables(connex);
        }
        indicator.setText2("Build all entities table");
        indicator.setFraction(0.25);
        List<TableMetadata> allEntities = context.getAllTables();
        generateFullProjectComponents(context, allEntities, generateComponentOnly, indicator);
        return allEntities;
    }
    public void generateFullProjectComponents(ProjectGenerationContext context, List<TableMetadata> allEntities, boolean generateComponentOnly, ProgressReporter indicator) throws Exception {
        indicator.setProgress(0.3,"Setup relation parameters");
        context.applyTableRelations();
        indicator.setFraction(0.35);
        generateFullBackendProject(context, allEntities, generateComponentOnly, indicator);
        if (context.getFramework() instanceof FrameworkMVC) {
            generateFullViewsComponents(context, allEntities);
        } else {
            generateFullFrontendProject(context, allEntities, indicator);
        }
    }
    public void generateFullProjectStrucutres(ProjectGenerationContext context, List<TableMetadata> allEntities, ProgressReporter indicator) throws Exception {
        indicator.setProgress(0.6, "Generating project structures");
        if (allEntities == null || allEntities.isEmpty()) {
            allEntities = context.getAllTables();
        }
        generateProjectFiles(context, allEntities, indicator);
        if (context.getFramework() instanceof FrameworkMVC) {
            ViewsTemplate viewsTemplate = context.getViewsTemplate();
            generateViewsFiles(context, viewsTemplate);
        } else {
            generateFrontendProjectFiles(context, allEntities, indicator);
        }
    }
    protected void useRealSidAndDriverType(Database database,Credentials credentials)
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

    private boolean createVirtualEnvironment(String projectPath) {
        try {
            System.out.println("🐍 Création d'un nouvel environnement virtuel Python...");

            // Supprimer l'ancien venv s'il existe
            File oldVenv = new File(projectPath + "/venv");
            if (oldVenv.exists()) {
                System.out.println("   🗑️  Suppression de l'ancien environnement virtuel...");
                FileUtils.deleteDirectory(oldVenv);
            }

            // Créer un nouvel environnement virtuel
            ProcessBuilder venvBuilder = new ProcessBuilder("python3", "-m", "venv", "venv");
            venvBuilder.directory(new File(projectPath));
            venvBuilder.redirectErrorStream(true);

            Process venvProcess = venvBuilder.start();

            // Lire la sortie
            java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(venvProcess.getInputStream())
            );

            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("   " + line);
            }

            int exitCode = venvProcess.waitFor();

            if (exitCode == 0) {
                System.out.println("   ✅ Environnement virtuel créé avec succès");

                // Donner les permissions d'exécution
                File venvPython = new File(projectPath + "/venv/bin/python");
                if (venvPython.exists()) {
                    venvPython.setExecutable(true);
                    File venvDir = new File(projectPath + "/venv/bin");
                    File[] pythonFiles = venvDir.listFiles((dir, name) -> name.startsWith("python"));
                    if (pythonFiles != null) {
                        for (File pythonFile : pythonFiles) {
                            pythonFile.setExecutable(true);
                        }
                    }
                    System.out.println("   ✅ Permissions d'exécution accordées");
                }

                return true;
            } else {
                System.err.println("   ❌ Erreur lors de la création de l'environnement virtuel (code: " + exitCode + ")");
                return false;
            }

        } catch (Exception e) {
            System.err.println("   ❌ Erreur lors de la création de l'environnement virtuel: " + e.getMessage());
            return false;
        }
    }

    private List<TableMetadata> generateComponentsOnly(ProjectGenerationContext context, ProgressReporter indicator) {
        Database database = context.getDatabase();
        Framework framework = context.getFramework();
        Credentials credentials = context.getCredentials();
        Connection connection = context.getConnection();
        List<TableMetadata> allEntities = new ArrayList<>();
        if (framework.getUseDB()) {
            try (Connection connex = (connection != null) ? connection : database.getConnection(credentials)) {
                allEntities = generateFullProjectComponents(context, connex, true, indicator);
            } catch (Exception e) {
                throw new RuntimeException("\nError in generateComponentsOnly : \n" + e);
            }
        }
        return allEntities;
    }

    public GenesisContextModel generateGenesisfile(ProjectGenerationContext context, ProgressReporter indicator) throws Exception {
        indicator.setProgress(0.9, "Finalisation generation","generating genesis file context");
        GenesisContextBuilder contextBuilder = new GenesisContextBuilder();
        return contextBuilder.generateGenesisfile(context);
    }
}

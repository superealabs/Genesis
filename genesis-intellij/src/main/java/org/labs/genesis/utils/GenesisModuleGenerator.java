package org.labs.genesis.utils;

import org.labs.genesis.config.ProjectGenerationContext;
import org.labs.genesis.config.ide.IdeConfiguration;
import org.labs.genesis.config.ide.IdeConfigurationLoader;
import org.labs.genesis.config.ide.ImlTemplate;
import org.labs.genesis.config.langage.Project;
import org.labs.genesis.engine.GenesisTemplateEngine;
import org.labs.utils.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class GenesisModuleGenerator {

    private static final GenesisTemplateEngine engine = new GenesisTemplateEngine();

    public static void generateModulesXml(Path rootProjectFolder, ProjectGenerationContext context) {
        if (rootProjectFolder == null || context == null) {
            return;
        }

        IdeConfiguration ideConfig = IdeConfigurationLoader.getIdeConfiguration();
        if (ideConfig == null) {
            return;
        }

        String rawProjectName = context.getProjectName() != null ? context.getProjectName() : "Project";
        String backendFolderName = StringUtils.toPascalCase(rawProjectName);
        String frontendFolderName = backendFolderName + StringUtils.toPascalCase(
                context.getWebappFolder() != null ? context.getWebappFolder() : "webapp"
        );

        Path backendFolder = rootProjectFolder.resolve(backendFolderName);
        if (!Files.exists(backendFolder) && Files.exists(rootProjectFolder.resolve(rawProjectName))) {
            backendFolder = rootProjectFolder.resolve(rawProjectName);
            backendFolderName = rawProjectName;
        }

        boolean hasFrontend = context.isGenerateFrontendApp();
        Path frontendFolder = rootProjectFolder.resolve(frontendFolderName);

        String javaVersion = "21";
        if (context.getLanguageConfiguration() != null && context.getLanguageConfiguration().containsKey("languageVersion")) {
            javaVersion = context.getLanguageConfiguration().get("languageVersion").toString();
        }

        // 1. Generate Backend .iml file
        String backendModuleType = resolveBackendModuleType(context);
        generateImlFile(backendFolder, backendFolderName, backendModuleType, javaVersion, ideConfig);

        // 2. Generate Frontend .iml file and Root .iml file if frontend exists
        if (hasFrontend && Files.exists(frontendFolder)) {
            generateImlFile(frontendFolder, frontendFolderName, "WEB_MODULE", javaVersion, ideConfig);
            generateImlFile(rootProjectFolder, backendFolderName, "WEB_MODULE", javaVersion, ideConfig);
        }

        // 3. Generate .idea/modules.xml
        generateModulesXmlFile(rootProjectFolder, backendFolderName, hasFrontend && Files.exists(frontendFolder) ? frontendFolderName : null, ideConfig);

        // 4. Generate .idea/misc.xml for Maven / .NET / Python / Node integration
        generateMiscXmlFile(rootProjectFolder, backendFolderName, backendModuleType, javaVersion, ideConfig);
    }

    private static String resolveBackendModuleType(ProjectGenerationContext context) {
        Project project = context.getProject();
        if (project != null && project.getName() != null) {
            String name = project.getName().toLowerCase();
            if (name.contains("maven") || name.contains("gradle") || name.contains("spring")) {
                return "JAVA_MODULE";
            }
            if (name.contains("msbuild") || name.contains(".net") || name.contains("c#") || name.contains("csharp")) {
                return "CSHARP_MODULE";
            }
            if (name.contains("django") || name.contains("python")) {
                return "PYTHON_MODULE";
            }
            if (name.contains("nodejs") || name.contains("nest")) {
                return "WEB_MODULE";
            }
        }
        return "WEB_MODULE";
    }

    private static void generateImlFile(Path folder, String moduleName, String moduleType, String javaVersion, IdeConfiguration ideConfig) {
        if (folder == null) return;
        try {
            if (!Files.exists(folder)) {
                Files.createDirectories(folder);
            }
            Path imlFile = folder.resolve(moduleName + ".iml");

            ImlTemplate template = IdeConfigurationLoader.findImlTemplate(ideConfig, moduleType);
            if (template == null) {
                template = IdeConfigurationLoader.findImlTemplate(ideConfig, "WEB_MODULE");
            }
            if (template != null) {
                Map<String, Object> vars = new HashMap<>();
                vars.put("javaVersion", javaVersion);
                String content = engine.render(template.getContent(), vars);
                Files.writeString(imlFile, content);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void generateModulesXmlFile(Path rootFolder, String backendModuleName, String frontendModuleName, IdeConfiguration ideConfig) {
        try {
            Path ideaFolder = rootFolder.resolve(".idea");
            if (!Files.exists(ideaFolder)) {
                Files.createDirectories(ideaFolder);
            }
            Path modulesXmlFile = ideaFolder.resolve("modules.xml");

            Map<String, Object> vars = new HashMap<>();
            vars.put("rootModuleName", backendModuleName);
            vars.put("backendModuleName", backendModuleName);
            vars.put("hasFrontend", frontendModuleName != null);
            vars.put("frontendModuleName", frontendModuleName != null ? frontendModuleName : "");

            String renderedContent = engine.render(ideConfig.getModulesTemplate().getContent(), vars);
            Files.writeString(modulesXmlFile, renderedContent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void generateMiscXmlFile(Path rootFolder, String backendModuleName, String moduleType, String javaVersion, IdeConfiguration ideConfig) {
        if (ideConfig.getMiscTemplate() == null) return;
        try {
            Path ideaFolder = rootFolder.resolve(".idea");
            if (!Files.exists(ideaFolder)) {
                Files.createDirectories(ideaFolder);
            }
            Path miscXmlFile = ideaFolder.resolve("misc.xml");

            Map<String, Object> vars = new HashMap<>();
            vars.put("backendModuleName", backendModuleName);
            vars.put("isMaven", "JAVA_MODULE".equals(moduleType));
            vars.put("isDotNet", "CSHARP_MODULE".equals(moduleType));
            vars.put("javaVersion", javaVersion);

            String renderedContent = engine.render(ideConfig.getMiscTemplate().getContent(), vars);
            Files.writeString(miscXmlFile, renderedContent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

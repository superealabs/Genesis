package org.labs.genesis.utils;

import com.intellij.ide.impl.ProjectUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import org.labs.genesis.config.ProjectGenerationContext;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class GenesisProjectOpener {

    public static void openGeneratedProject(Project currentProject, ProjectGenerationContext context) {
        if (context == null) {
            return;
        }

        Path targetFolder = resolveTargetFolder(context);
        if (targetFolder == null || !Files.exists(targetFolder)) {
            return;
        }

        // 1. Generate .idea/dataSources.xml for automatic Database Tool Window integration
        GenesisDataSourceGenerator.generateDataSourcesXml(targetFolder, context);

        // 2. Generate .idea/modules.xml and .iml files for Backend & Frontend modules
        GenesisModuleGenerator.generateModulesXml(targetFolder, context);

        // 2. Ask user if they want to open the generated project in IntelliJ IDEA
        int answer = Messages.showYesNoDialog(
                currentProject,
                "Project generated successfully at:\n" + targetFolder.toAbsolutePath() + "\n\nDo you want to open it in a new IntelliJ window?",
                "Open Generated Project",
                "Open Project",
                "Cancel",
                Messages.getQuestionIcon()
        );

        if (answer == Messages.YES) {
            try {
                ProjectUtil.openOrImport(targetFolder, currentProject, true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static Path resolveTargetFolder(ProjectGenerationContext context) {
        String destinationFolder = context.getDestinationFolder();
        String projectName = context.getProjectName();

        if (destinationFolder == null || destinationFolder.isEmpty()) {
            return null;
        }

        Path destPath = Paths.get(destinationFolder);

        // If both frontend and backend exist, open the parent container folder
        if (context.isGenerateFrontendApp()) {
            if (Files.exists(destPath)) {
                return destPath;
            }
        }

        // If backend only, check for destinationFolder/projectName
        if (projectName != null && !projectName.isEmpty()) {
            Path backendSubfolder = destPath.resolve(projectName);
            if (Files.exists(backendSubfolder)) {
                return backendSubfolder;
            }
        }

        return destPath;
    }
}

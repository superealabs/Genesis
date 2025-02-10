package org.labs.genesis.wizards;

import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.openapi.options.ConfigurationException;
import org.labs.genesis.config.ProjectGenerationContext;
import org.labs.genesis.config.langage.Framework;
import org.labs.genesis.config.langage.Language;
import org.labs.genesis.config.langage.Project;
import org.labs.genesis.forms.InitializationForm;

import javax.swing.*;
import java.util.Map;

public class InitializationWizardStep extends ModuleWizardStep {
    public final SpecificConfigurationWizardStep specificConfigurationWizardStep;
    private final InitializationForm newProjectPanel;
    private final ProjectGenerationContext projectGenerationContext;

    public InitializationWizardStep(ProjectGenerationContext projectGenerationContext, SpecificConfigurationWizardStep specificConfigurationWizardStep) {
        newProjectPanel = new InitializationForm();
        this.projectGenerationContext = projectGenerationContext;
        this.specificConfigurationWizardStep = specificConfigurationWizardStep;
    }

    @Override
    public JComponent getComponent() {
        return newProjectPanel.getMainPanel();
    }

    @Override
    public void updateDataModel() {
        // Récupérer les valeurs depuis le formulaire
        String projectName = newProjectPanel.getProjectNameField().getText().trim();
        String groupId = newProjectPanel.getGroupIdField().getText().trim();
        String location = newProjectPanel.getLocationField().getText().trim();
        Language language = (Language) newProjectPanel.getLanguageOptions().getSelectedItem();
        String languageVersion = (String) newProjectPanel.getLanguageVersionOptions().getSelectedItem();
        String frameworkVersion = (String) newProjectPanel.getFrameworkVersionOptions().getSelectedItem();
        Framework framework = (Framework) newProjectPanel.getFrameworkOptions().getSelectedItem();
        Project buildTool = (Project) newProjectPanel.getBuildToolOptions().getSelectedItem();

        assert languageVersion != null;
        assert frameworkVersion != null;
        Map<String, Object> languageConfiguration = Map.of(
                "languageVersion", languageVersion,
                "frameworkVersion", frameworkVersion
        );
        projectGenerationContext.setLanguageConfiguration(languageConfiguration);

        projectGenerationContext
                .setProjectName(projectName)
                .setDestinationFolder(location)
                .setLanguage(language)
                .setFramework(framework)
                .setProject(buildTool)
                .setGroupLink(groupId);

        specificConfigurationWizardStep.onFrameworkSelected(framework);
    }

    @Override
    public boolean validate() throws ConfigurationException {
        // Retrieve selected values
        String projectName = newProjectPanel.getProjectNameField().getText().trim();
        String groupId = newProjectPanel.getGroupIdField().getText().trim();
        String location = newProjectPanel.getLocationField().getText().trim();
        Language language = (Language) newProjectPanel.getLanguageOptions().getSelectedItem();
        String languageVersion = (String) newProjectPanel.getLanguageVersionOptions().getSelectedItem();
        String frameworkVersion = (String) newProjectPanel.getFrameworkVersionOptions().getSelectedItem();
        String framework = (String) newProjectPanel.getCoreFrameworkOptions().getSelectedItem();
        Framework projectType = (Framework) newProjectPanel.getFrameworkOptions().getSelectedItem();
        Project buildTool = (Project) newProjectPanel.getBuildToolOptions().getSelectedItem();

        // Validate the project name
        if (projectName.isEmpty()) {
            throw new ConfigurationException("The project name cannot be empty.");
        }

        // Validate the group id
        if (groupId.isEmpty()) {
            assert projectType != null;
            if (projectType.getWithGroupId()) {
                throw new ConfigurationException("The group id cannot be empty.");
            }
        }

        if (!projectName.matches("^[a-zA-Z0-9_]+$")) {
            throw new ConfigurationException("""
                        The project name must be a single word containing only letters, numbers, and underscores.
                        No special characters or spaces are allowed.
                    """);
        }

        // Check that the location path is not empty
        if (location.isEmpty()) {
            throw new ConfigurationException("The location path cannot be empty.");
        }

        // Check that a language is selected
        if (language == null) {
            throw new ConfigurationException("Please select a language.");
        }

        // Check that a language version is selected
//        if (languageVersion == null || languageVersion.equals("Not applicable")) {
//            throw new ConfigurationException("Please select a language version.");
//        }
        if (languageVersion == null) {
            throw new ConfigurationException("Please select a language version.");
        }

        // Check that a framework version is selected
        if (frameworkVersion == null) {
            throw new ConfigurationException("Please select a framework version.");
        }

        // Check that a framework is selected
        if (framework == null) {
            throw new ConfigurationException("Please select a framework.");
        }

        // Check that a project type is selected
        if (projectType == null) {
            throw new ConfigurationException("Please select a project type.");
        }

        // Check that a build tool is selected
        if (buildTool == null) {
            throw new ConfigurationException("Please select a build tool.");
        }

        // If all validations pass, return true
        return true;
    }


}

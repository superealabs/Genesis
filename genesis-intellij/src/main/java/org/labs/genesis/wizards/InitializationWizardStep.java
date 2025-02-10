package org.labs.genesis.wizards;

import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.openapi.options.ConfigurationException;
import org.labs.genesis.config.ProjectGenerationContext;
import org.labs.genesis.config.langage.Framework;
import org.labs.genesis.config.langage.Language;
import org.labs.genesis.config.langage.Project;
import org.labs.genesis.forms.InitializationForm;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

public class InitializationWizardStep extends ModuleWizardStep {
    public final SpecificConfigurationWizardStep specificConfigurationWizardStep;
    private final InitializationForm newProjectPanel;
    private final ProjectGenerationContext projectGenerationContext;
    // added as attributes to avoid repetition
    private final String projectName;
    private final String groupId;
    private final String location;
    private final Language language;
    private final String languageVersion;
    private final String frameworkVersion;
    private final Framework framework;
    private final Project buildTool;
    private final Framework projectType;

    public InitializationWizardStep(ProjectGenerationContext projectGenerationContext, SpecificConfigurationWizardStep specificConfigurationWizardStep) {
        newProjectPanel = new InitializationForm();
        this.projectGenerationContext = projectGenerationContext;
        this.specificConfigurationWizardStep = specificConfigurationWizardStep;
        // initialization of the added attributes
        this.projectName = newProjectPanel.getProjectNameField().getText().trim();
        this.groupId = newProjectPanel.getGroupIdField().getText().trim();
        this.location = newProjectPanel.getLocationField().getText().trim();
        this.language = (Language) newProjectPanel.getLanguageOptions().getSelectedItem();
        this.languageVersion = (String) newProjectPanel.getLanguageVersionOptions().getSelectedItem();
        this.frameworkVersion = (String) newProjectPanel.getFrameworkVersionOptions().getSelectedItem();
        this.framework = (Framework) newProjectPanel.getFrameworkOptions().getSelectedItem();
        this.buildTool = (Project) newProjectPanel.getBuildToolOptions().getSelectedItem();
        this.projectType = (Framework) newProjectPanel.getFrameworkOptions().getSelectedItem();
    }

    @Override
    public JComponent getComponent() {
        return newProjectPanel.getMainPanel();
    }

    @Override
    public void updateDataModel() {
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
        // declared the validation steps as a map to avoid hardcoded if's
        HashMap<Boolean, ConfigurationException> validationMap=new HashMap<>(){{
            put(projectName.isEmpty(), new ConfigurationException("The project name cannot be empty."));
//                put(groupId.isEmpty() && projectType.getWithGroupId(), new ConfigurationException("The group id cannot be empty."));
            put(!projectName.matches("^[a-zA-Z0-9_]+$"), new ConfigurationException("""
                        The project name must be a single word containing only letters, numbers, and underscores.
                        No special characters or spaces are allowed.
                    """));
            put(location.isEmpty(), new ConfigurationException("The location path cannot be empty."));
            put(language == null, new ConfigurationException("Please select a language."));
            put(languageVersion == null, new ConfigurationException("Please select a language version."));
            put(frameworkVersion == null, new ConfigurationException("Please select a framework version."));
            put(framework == null, new ConfigurationException("Please select a framework."));
            put(projectType == null, new ConfigurationException("Please select a project type."));
            put(buildTool == null, new ConfigurationException("Please select a build tool."));
        }};
        for(Map.Entry<Boolean, ConfigurationException> e:validationMap.entrySet()){
            if(e.getKey()){
                throw e.getValue();
            }
        }

        // Validate the group id
        if (groupId.isEmpty()) {
            assert projectType != null;
            if (projectType.getWithGroupId()) {
                throw new ConfigurationException("The group id cannot be empty.");
            }
        }

        // If all validations pass, return true
        return true;
    }


}

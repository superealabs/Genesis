package org.labs.genesis.wizards;

import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.ui.Messages;
import org.labs.genesis.config.ProjectGenerationContext;
import org.labs.genesis.config.langage.Framework;
import org.labs.genesis.config.langage.FrameworkMVC;
import org.labs.genesis.config.langage.Language;
import org.labs.genesis.config.langage.Project;
import org.labs.genesis.forms.InitializationForm;

import javax.swing.*;
import java.util.*;

public class InitializationWizardStep extends ModuleWizardStep {
    public final SpecificConfigurationWizardStep specificConfigurationWizardStep;
    public final FrontendConfigurationWizardStep frontendConfigurationWizardStep;
    private final InitializationForm newProjectPanel;
    private final ProjectGenerationContext projectGenerationContext;
    private final List<ProjectGenerationContext> listProjectGenerationContext ;
    // added as attributes to avoid repetition
    private String projectName;
    private String groupId;
    private String location;
    private Language language;
    private String languageVersion;
    private String frameworkVersion;
    private Framework framework;
    private Project buildTool;
    private Framework projectType;

    public InitializationWizardStep(ProjectGenerationContext projectGenerationContext, List<ProjectGenerationContext> listProjectGenerationContext, SpecificConfigurationWizardStep specificConfigurationWizardStep, FrontendConfigurationWizardStep frontendConfigurationWizardStep) {
        newProjectPanel = new InitializationForm();
        this.projectGenerationContext = projectGenerationContext;
        this.listProjectGenerationContext = listProjectGenerationContext;
        this.specificConfigurationWizardStep = specificConfigurationWizardStep;
        this.frontendConfigurationWizardStep = frontendConfigurationWizardStep;

       initFormActions();
    }

    @Override
    public JComponent getComponent() {
        return newProjectPanel.getMainPanel();
    }

    private void initializeAttributes(String projectName, String groupId, String location, Language language, String languageVersion, String frameworkVersion, Framework framework, Project buildTool) {
        this.projectName = projectName;
        this.groupId = groupId;
        this.location = location;
        this.language = language;
        this.languageVersion = languageVersion;
        this.frameworkVersion = frameworkVersion;
        this.framework = framework;
        this.buildTool = buildTool;
    }

    @Override
    public void updateDataModel() {
        initializeAttributes(
                newProjectPanel.getProjectNameField().getText().trim(),
                newProjectPanel.getGroupIdField().getText().trim(),
                newProjectPanel.getLocationField().getText().trim(),
                (Language) newProjectPanel.getLanguageOptions().getSelectedItem(),
                (String) newProjectPanel.getLanguageVersionOptions().getSelectedItem(),
                (String) newProjectPanel.getFrameworkVersionOptions().getSelectedItem(),
                (Framework) newProjectPanel.getFrameworkOptions().getSelectedItem(),
                (Project) newProjectPanel.getBuildToolOptions().getSelectedItem()
        );
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
        projectGenerationContext.setGenerateFrontendApp(framework.getUseFrontendApp());
        if (framework instanceof FrameworkMVC) {
            frontendConfigurationWizardStep.onFrameworkMVCSelected((FrameworkMVC) framework);
        } else {
            frontendConfigurationWizardStep.onFrameworkSelected();
        }
    }

    @Override
    public boolean validate() throws ConfigurationException {
        initializeAttributes(
                newProjectPanel.getProjectNameField().getText().trim(),
                newProjectPanel.getGroupIdField().getText().trim(),
                newProjectPanel.getLocationField().getText().trim(),
                (Language) newProjectPanel.getLanguageOptions().getSelectedItem(),
                (String) newProjectPanel.getLanguageVersionOptions().getSelectedItem(),
                (String) newProjectPanel.getFrameworkVersionOptions().getSelectedItem(),
                (Framework) newProjectPanel.getFrameworkOptions().getSelectedItem(),
                (Project) newProjectPanel.getBuildToolOptions().getSelectedItem()
        );
        this.projectType = (Framework) newProjectPanel.getFrameworkOptions().getSelectedItem();
        // declared the validation steps as a map to avoid hardcoded if's
        HashMap<Boolean, ConfigurationException> validationMap = new HashMap<>() {{
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
        for (Map.Entry<Boolean, ConfigurationException> e : validationMap.entrySet()) {
            if (e.getKey()) {
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

    private void checkDuplicateProjectName(String projectName) {
        for (ProjectGenerationContext ctx : listProjectGenerationContext) {
            if (ctx.getProjectName().equals(projectName)) {
                Messages.showErrorDialog(
                        newProjectPanel.getMainPanel(),
                        "Duplicate project name detected: " + projectName,
                        "Error"
                );
                throw new IllegalArgumentException("Error: Duplicate project name detected: " + projectName);
            }
        }
    }

    private void initFormActions() {
        newProjectPanel.getValidateButton().addActionListener(e -> {
            String newName = newProjectPanel.getProjectNameField().getText();
            checkDuplicateProjectName(newName);
            validateAndAddProject();
        });
        newProjectPanel.getResetButton().addActionListener(e -> {
            resetAll();
        });
    }
    private void updateProjectCounterLabel() {
        int count = listProjectGenerationContext.size();
        newProjectPanel.getNumberProject().setText(String.valueOf(count));
    }
    //Add multi ProjectGenerationContext
    private void validateAndAddProject() {
        try {
            if (validate()) {
                ProjectGenerationContext newProjectGenerationContext = new ProjectGenerationContext() ;
                initializeAttributes(
                        newProjectPanel.getProjectNameField().getText().trim(),
                        newProjectPanel.getGroupIdField().getText().trim(),
                        newProjectPanel.getLocationField().getText().trim(),
                        (Language) newProjectPanel.getLanguageOptions().getSelectedItem(),
                        (String) newProjectPanel.getLanguageVersionOptions().getSelectedItem(),
                        (String) newProjectPanel.getFrameworkVersionOptions().getSelectedItem(),
                        (Framework) newProjectPanel.getFrameworkOptions().getSelectedItem(),
                        (Project) newProjectPanel.getBuildToolOptions().getSelectedItem()
                );
                assert languageVersion != null;
                assert frameworkVersion != null;
                Map<String, Object> languageConfiguration = Map.of(
                        "languageVersion", languageVersion,
                        "frameworkVersion", frameworkVersion
                );
                newProjectGenerationContext.setLanguageConfiguration(languageConfiguration);

                newProjectGenerationContext
                        .setProjectName(projectName)
                        .setDestinationFolder(location)
                        .setLanguage(language)
                        .setFramework(framework)
                        .setProject(buildTool)
                        .setGroupLink(groupId);

                specificConfigurationWizardStep.onFrameworkSelected(framework);
                newProjectGenerationContext.setGenerateFrontendApp(framework.getUseFrontendApp());
                if (framework instanceof FrameworkMVC) {
                    frontendConfigurationWizardStep.onFrameworkMVCSelected((FrameworkMVC) framework);
                } else {
                    frontendConfigurationWizardStep.onFrameworkSelected();
                }
                //add new ProjectGenerationContext
                listProjectGenerationContext.add(newProjectGenerationContext);
                updateProjectCounterLabel();

            }
        } catch (ConfigurationException ex) {
            JOptionPane.showMessageDialog(newProjectPanel.getMainPanel(), ex.getMessage(), "Validation Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void resetAll() {
        // Reset form
        newProjectPanel.getProjectNameField().setText("");
        newProjectPanel.getGroupIdField().setText("");
        newProjectPanel.getLocationField().setText("");
        newProjectPanel.getLanguageOptions().setSelectedIndex(0);
        newProjectPanel.getLanguageVersionOptions().setSelectedIndex(0);
        newProjectPanel.getFrameworkOptions().setSelectedIndex(0);
        newProjectPanel.getFrameworkVersionOptions().setSelectedIndex(0);
        newProjectPanel.getBuildToolOptions().setSelectedIndex(0);

        listProjectGenerationContext.clear();
        updateProjectCounterLabel();

    }

}

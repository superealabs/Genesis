package org.labs.genesis.wizards;

import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.openapi.options.ConfigurationException;
import org.labs.genesis.config.tools.DockerConfiguration;
import org.labs.genesis.context.GenerationContextManager;
import org.labs.genesis.forms.DockerConfigurationForm;

import javax.swing.*;

public class DockerConfigurationWizardStep extends ModuleWizardStep {

    private final DockerConfigurationForm form;
    private final GenerationContextManager generationContextManager;

    public DockerConfigurationWizardStep(
            GenerationContextManager generationContextManager
    ) {
        this.generationContextManager = generationContextManager;
        this.form = new DockerConfigurationForm();
    }

    @Override
    public JComponent getComponent() {
        return form.getMainPanel();
    }

    @Override
    public boolean isStepVisible() {
        return generationContextManager
                .getContext()
                .getGenerationProcess()
                .isGenerateProjectProcess();
    }

    @Override
    public void updateDataModel() {

        DockerConfiguration config =
                new DockerConfiguration();


        // =========================================================
        // DOCKER
        // =========================================================

        boolean useDocker =
                form.getConfigureDockerCheckBox()
                        .isSelected();

        config.setUseDocker(useDocker);


        if (!useDocker) {

            generationContextManager
                    .getContext()
                    .setDockerConfiguration(config);

            return;
        }


        // =========================================================
        // DOCKERIZED SERVICES
        // =========================================================

        boolean frontendDockerized =
                form.getDockerFrontendRadioButton().isSelected()
                        || form.getDockerBothRadioButton().isSelected();

        boolean backendDockerized =
                form.getDockerBackendRadioButton().isSelected()
                        || form.getDockerBothRadioButton().isSelected();


        config.setFrontendDockerized(
                frontendDockerized
        );

        config.setBackendDockerized(
                backendDockerized
        );


        // =========================================================
        // FRONTEND CONTAINER
        // =========================================================

        if (frontendDockerized) {

            config.setFrontendContainer(
                    form.getFrontendContainerNameField()
                            .getText()
                            .trim()
            );
        }


        // =========================================================
        // BACKEND CONTAINER
        // =========================================================

        if (backendDockerized) {

            config.setBackendContainer(
                    form.getBackendContainerNameField()
                            .getText()
                            .trim()
            );
        }


        // =========================================================
        // SAVE
        // =========================================================

        generationContextManager
                .getContext()
                .setDockerConfiguration(config);
    }


    @Override
    public boolean validate() throws ConfigurationException {

        if (!form.getConfigureDockerCheckBox()
                .isSelected()) {

            return true;
        }


        boolean frontendDockerized =
                form.getDockerFrontendRadioButton().isSelected()
                        || form.getDockerBothRadioButton().isSelected();

        boolean backendDockerized =
                form.getDockerBackendRadioButton().isSelected()
                        || form.getDockerBothRadioButton().isSelected();


        // =========================================================
        // FRONTEND CONTAINER
        // =========================================================

        if (frontendDockerized) {

            String container =
                    form.getFrontendContainerNameField()
                            .getText()
                            .trim();

            if (container.isEmpty()) {

                throw new ConfigurationException(
                        "Frontend container name cannot be empty."
                );
            }
        }


        // =========================================================
        // BACKEND CONTAINER
        // =========================================================

        if (backendDockerized) {

            String container =
                    form.getBackendContainerNameField()
                            .getText()
                            .trim();

            if (container.isEmpty()) {

                throw new ConfigurationException(
                        "Backend container name cannot be empty."
                );
            }
        }


        return true;
    }
}
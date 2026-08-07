package org.labs.genesis.wizards;

import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.openapi.options.ConfigurationException;
import org.labs.genesis.config.git.GitConfiguration;
import org.labs.genesis.context.GenerationContextManager;
import org.labs.genesis.forms.GitConfigurationForm;

import javax.swing.*;

public class GitConfigurationWizardStep extends ModuleWizardStep {

    private final GitConfigurationForm form;
    private final GenerationContextManager generationContextManager;

    public GitConfigurationWizardStep(
            GenerationContextManager generationContextManager
    ) {
        this.generationContextManager = generationContextManager;
        this.form = new GitConfigurationForm();
    }

    @Override
    public JComponent getComponent() {
        return form.getMainPanel();
    }

    @Override
    public boolean isStepVisible() {
        return this.generationContextManager.getContext().getGenerationProcess().isGenerateProjectProcess();
    }

    @Override
    public void updateDataModel() {

        GitConfiguration config = new GitConfiguration();

        config.setUseGit(
                form.getUseGitCheckBox().isSelected()
        );

        if (!config.isUseGit()) {
            generationContextManager
                    .getContext()
                    .setGitConfiguration(config);

            return;
        }

        boolean separate =
                form.getSeparateRepositoriesRadioButton()
                        .isSelected();

        config.setSeparateRepositories(separate);

        if (separate) {

            config.setBackendRepositoryName(
                    form.getBackendRepositoryNameField()
                            .getText()
                            .trim()
            );

            config.setFrontendRepositoryName(
                    form.getFrontendRepositoryNameField()
                            .getText()
                            .trim()
            );

        } else {

            config.setRepositoryName(
                    form.getRepositoryNameField()
                            .getText()
                            .trim()
            );
        }

        boolean createRemote =
                form.getCreateRemoteRepositoryCheckBox()
                        .isSelected();

        config.setCreateRemoteRepository(createRemote);

        if (createRemote) {

            config.setGithubUsername(
                    form.getGithubUsernameField()
                            .getText()
                            .trim()
            );

            config.setGithubToken(
                    new String(
                            form.getGithubTokenField()
                                    .getPassword()
                    ).trim()
            );
        }

        generationContextManager
                .getContext()
                .setGitConfiguration(config);
    }

    @Override
    public boolean validate() throws ConfigurationException {

        if (!form.getUseGitCheckBox().isSelected()) {
            return true;
        }

        boolean separate =
                form.getSeparateRepositoriesRadioButton()
                        .isSelected();

        if (separate) {

            String backend =
                    form.getBackendRepositoryNameField()
                            .getText()
                            .trim();

            String frontend =
                    form.getFrontendRepositoryNameField()
                            .getText()
                            .trim();

            if (backend.isEmpty()) {
                throw new ConfigurationException(
                        "Backend repository name cannot be empty."
                );
            }

            if (frontend.isEmpty()) {
                throw new ConfigurationException(
                        "Frontend repository name cannot be empty."
                );
            }

        } else {

            String repository =
                    form.getRepositoryNameField()
                            .getText()
                            .trim();

            if (repository.isEmpty()) {
                throw new ConfigurationException(
                        "Repository name cannot be empty."
                );
            }
        }

        if (form.getCreateRemoteRepositoryCheckBox().isSelected()) {

            if (form.getGithubUsernameField()
                    .getText()
                    .trim()
                    .isEmpty()) {

                throw new ConfigurationException(
                        "GitHub username cannot be empty."
                );
            }

            if (form.getGithubTokenField()
                    .getPassword()
                    .length == 0) {

                throw new ConfigurationException(
                        "GitHub Personal Access Token cannot be empty."
                );
            }
        }

        return true;
    }
}
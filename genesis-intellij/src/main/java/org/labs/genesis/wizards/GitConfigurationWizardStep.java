package org.labs.genesis.wizards;

import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.openapi.options.ConfigurationException;
import org.labs.genesis.config.tools.GitConfiguration;
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
        return generationContextManager
                .getContext()
                .getGenerationProcess()
                .isGenerateProjectProcess();
    }

    @Override
    public void updateDataModel() {

        GitConfiguration config = new GitConfiguration();

        // =========================================================
        // GIT
        // =========================================================

        boolean useGit =
                form.getUseGitCheckBox().isSelected();

        config.setUseGit(useGit);

        if (!useGit) {

            generationContextManager
                    .getContext()
                    .setGitConfiguration(config);

            return;
        }


        // =========================================================
        // REPOSITORY STRUCTURE
        // =========================================================

        boolean separate =
                form.getSeparateRepositoriesRadioButton()
                        .isSelected();

        config.setSeparateRepositories(separate);


        // =========================================================
        // REPOSITORY NAMES
        // =========================================================

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


        // =========================================================
        // REMOTE REPOSITORY
        // =========================================================

        boolean createRemote =
                form.getCreateRemoteRepositoryCheckBox()
                        .isSelected();

        config.setCreateRemoteRepository(createRemote);


        if (createRemote) {

            // -----------------------------------------------------
            // GitHub username
            // -----------------------------------------------------

            config.setGithubUsername(
                    form.getGithubUsernameField()
                            .getText()
                            .trim()
            );


            // -----------------------------------------------------
            // Private repository
            // -----------------------------------------------------

            config.setRepositoryPrivate(
                    form.getPrivateRepositoryCheckBox()
                            .isSelected()
            );


            // -----------------------------------------------------
            // GitHub token
            // -----------------------------------------------------

            config.setGithubToken(
                    new String(
                            form.getGithubTokenField()
                                    .getPassword()
                    ).trim()
            );
        }


        // =========================================================
        // SAVE
        // =========================================================

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

        boolean createRemote =
                form.getCreateRemoteRepositoryCheckBox()
                        .isSelected();


        // =========================================================
        // REPOSITORY NAMES
        // =========================================================

        boolean repositoryConfigured = false;


        if (separate) {

            String backend =
                    form.getBackendRepositoryNameField()
                            .getText()
                            .trim();

            String frontend =
                    form.getFrontendRepositoryNameField()
                            .getText()
                            .trim();


            repositoryConfigured =
                    !backend.isEmpty()
                            || !frontend.isEmpty();


            // Remote => les deux sont obligatoires
            if (createRemote) {

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
            }

        } else {

            String repository =
                    form.getRepositoryNameField()
                            .getText()
                            .trim();


            repositoryConfigured =
                    !repository.isEmpty();


            if (createRemote && repository.isEmpty()) {

                throw new ConfigurationException(
                        "Repository name cannot be empty."
                );
            }
        }


        // =========================================================
        // GITHUB USERNAME
        // =========================================================

        String username =
                form.getGithubUsernameField()
                        .getText()
                        .trim();


        /*
         * Le username est obligatoire si :
         *
         * - un repository est configuré
         * OU
         * - le remote est activé
         */

        if ((createRemote || repositoryConfigured)
                && username.isEmpty()) {

            throw new ConfigurationException(
                    "GitHub username cannot be empty."
            );
        }


        // =========================================================
        // GITHUB TOKEN
        // =========================================================

        if (createRemote) {

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
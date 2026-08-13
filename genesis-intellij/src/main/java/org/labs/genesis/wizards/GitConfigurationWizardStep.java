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

            config.setUseDocker(
                    form.getConfigureDockerCheckBox().isSelected()
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

            config.setGithubUsername(
                    form.getGithubUsernameField()
                            .getText()
                            .trim()
            );

            boolean createRemote =
                    form.getCreateRemoteRepositoryCheckBox()
                            .isSelected();

            config.setCreateRemoteRepository(createRemote);

            if (createRemote) {
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

            boolean createRemote =
                    form.getCreateRemoteRepositoryCheckBox()
                            .isSelected();

            // ---------------------------------------------------------
            // Vérification des noms de repositories
            // ---------------------------------------------------------

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

                // Si l'un des deux est renseigné, on considère
                // que la configuration du repository a commencé.
                repositoryConfigured =
                        !backend.isEmpty() || !frontend.isEmpty();

                // Si le remote est activé, les deux sont obligatoires.
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

                // Si le remote est activé, le repository est obligatoire.
                if (createRemote && repository.isEmpty()) {

                    throw new ConfigurationException(
                            "Repository name cannot be empty."
                    );
                }
            }

            // ---------------------------------------------------------
            // GitHub username
            // ---------------------------------------------------------

            String username =
                    form.getGithubUsernameField()
                            .getText()
                            .trim();

            /*
             * Le username devient obligatoire :
             *
             * 1. si le remote est activé
             * OU
             * 2. si un repository a été renseigné.
             */
            if ((createRemote || repositoryConfigured)
                    && username.isEmpty()) {

                throw new ConfigurationException(
                        "GitHub username cannot be empty."
                );
            }

            // ---------------------------------------------------------
            // GitHub PAT
            // ---------------------------------------------------------

            /*
             * Le PAT est obligatoire uniquement lorsque
             * le remote est activé.
             */
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
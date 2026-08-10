package org.labs.genesis.forms;

import lombok.Getter;

import javax.swing.*;

@Getter
public class GitConfigurationForm {

    private JPanel mainPanel;

    private JCheckBox useGitCheckBox;

    private JLabel repositoryModeLabel;
    private JRadioButton singleRepositoryRadioButton;
    private JRadioButton separateRepositoriesRadioButton;

    private JLabel repositoryNameLabel;
    private JTextField repositoryNameField;

    private JLabel backendRepositoryNameLabel;
    private JTextField backendRepositoryNameField;

    private JLabel frontendRepositoryNameLabel;
    private JTextField frontendRepositoryNameField;

    private JCheckBox createRemoteRepositoryCheckBox;

    private JLabel githubUsernameLabel;
    private JTextField githubUsernameField;

    private JLabel githubTokenLabel;
    private JPasswordField githubTokenField;


    public GitConfigurationForm() {

        // ---------------------------------------------------------
        // Repository mode
        // ---------------------------------------------------------

        ButtonGroup repositoryGroup = new ButtonGroup();

        repositoryGroup.add(singleRepositoryRadioButton);
        repositoryGroup.add(separateRepositoriesRadioButton);


        // ---------------------------------------------------------
        // Listeners
        // ---------------------------------------------------------

        useGitCheckBox.addActionListener(e ->
                refreshVisibility()
        );

        singleRepositoryRadioButton.addActionListener(e ->
                refreshVisibility()
        );

        separateRepositoriesRadioButton.addActionListener(e ->
                refreshVisibility()
        );

        createRemoteRepositoryCheckBox.addActionListener(e ->
                refreshVisibility()
        );


        // ---------------------------------------------------------
        // Default values
        // ---------------------------------------------------------

        useGitCheckBox.setSelected(false);

        singleRepositoryRadioButton.setSelected(true);

        createRemoteRepositoryCheckBox.setSelected(false);


        // ---------------------------------------------------------
        // Initial visibility
        // ---------------------------------------------------------

        refreshVisibility();
    }


    private void refreshVisibility() {

        boolean useGit =
                useGitCheckBox.isSelected();


        // =========================================================
        // GIT CONFIGURATION
        // =========================================================

        repositoryModeLabel.setVisible(useGit);

        singleRepositoryRadioButton.setVisible(useGit);

        separateRepositoriesRadioButton.setVisible(useGit);


        // =========================================================
        // REPOSITORY MODE
        // =========================================================

        boolean separate =
                separateRepositoriesRadioButton.isSelected();


        // ---------------------------------------------------------
        // Single repository
        // ---------------------------------------------------------

        repositoryNameLabel.setVisible(
                useGit && !separate
        );

        repositoryNameField.setVisible(
                useGit && !separate
        );


        // ---------------------------------------------------------
        // Separate repositories
        // ---------------------------------------------------------

        backendRepositoryNameLabel.setVisible(
                useGit && separate
        );

        backendRepositoryNameField.setVisible(
                useGit && separate
        );

        frontendRepositoryNameLabel.setVisible(
                useGit && separate
        );

        frontendRepositoryNameField.setVisible(
                useGit && separate
        );


        // =========================================================
        // REMOTE CONFIGURATION
        // =========================================================

        /*
         * Le checkbox de configuration du remote est disponible
         * dès que Git est activé.
         */
        createRemoteRepositoryCheckBox.setVisible(useGit);


        // =========================================================
        // GITHUB USERNAME
        // =========================================================

        /*
         * Le username GitHub est visible dès que Git est activé.
         *
         * Il ne dépend PAS du checkbox "Configure remote repository".
         */
        githubUsernameLabel.setVisible(useGit);

        githubUsernameField.setVisible(useGit);


        // =========================================================
        // GITHUB PAT
        // =========================================================

        /*
         * Le PAT est visible uniquement si :
         *
         * 1. Git est activé
         * 2. Le remote est configuré
         */
        boolean createRemote =
                useGit &&
                        createRemoteRepositoryCheckBox.isSelected();

        githubTokenLabel.setVisible(createRemote);

        githubTokenField.setVisible(createRemote);


        // =========================================================
        // REFRESH UI
        // =========================================================

        mainPanel.revalidate();
        mainPanel.repaint();
    }
}
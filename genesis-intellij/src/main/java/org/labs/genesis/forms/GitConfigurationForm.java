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

    private JLabel githubUsernameLabel;
    private JTextField githubUsernameField;

    private JCheckBox createRemoteRepositoryCheckBox;

    private JLabel githubTokenLabel;
    private JPasswordField githubTokenField;

    private JCheckBox privateRepositoryCheckBox;


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

        privateRepositoryCheckBox.setSelected(false);


        // ---------------------------------------------------------
        // Initial visibility
        // ---------------------------------------------------------

        refreshVisibility();
    }


    private void refreshVisibility() {

        boolean useGit =
                useGitCheckBox.isSelected();


        // =========================================================
        // REPOSITORY MODE
        // =========================================================

        repositoryModeLabel.setVisible(useGit);

        singleRepositoryRadioButton.setVisible(useGit);

        separateRepositoriesRadioButton.setVisible(useGit);


        boolean separate =
                separateRepositoriesRadioButton.isSelected();


        // =========================================================
        // SINGLE REPOSITORY
        // =========================================================

        repositoryNameLabel.setVisible(
                useGit && !separate
        );

        repositoryNameField.setVisible(
                useGit && !separate
        );


        // =========================================================
        // SEPARATE REPOSITORIES
        // =========================================================

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
        // GITHUB USERNAME
        // =========================================================

        // Le username est demandé dès qu'un repository est configuré.
        githubUsernameLabel.setVisible(useGit);

        githubUsernameField.setVisible(useGit);


        // =========================================================
        // REMOTE REPOSITORY
        // =========================================================

        createRemoteRepositoryCheckBox.setVisible(useGit);


        boolean createRemote =
                useGit &&
                        createRemoteRepositoryCheckBox.isSelected();


        // =========================================================
        // TOKEN
        // =========================================================

        githubTokenLabel.setVisible(createRemote);

        githubTokenField.setVisible(createRemote);


        // =========================================================
        // PRIVATE REPOSITORY
        // =========================================================

        privateRepositoryCheckBox.setVisible(createRemote);


        // =========================================================
        // REFRESH
        // =========================================================

        mainPanel.revalidate();
        mainPanel.repaint();
    }
}
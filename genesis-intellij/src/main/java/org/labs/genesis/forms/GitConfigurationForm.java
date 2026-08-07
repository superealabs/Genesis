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

        ButtonGroup repositoryGroup = new ButtonGroup();

        repositoryGroup.add(singleRepositoryRadioButton);
        repositoryGroup.add(separateRepositoriesRadioButton);

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

        useGitCheckBox.setSelected(false);
        singleRepositoryRadioButton.setSelected(true);
        createRemoteRepositoryCheckBox.setSelected(false);

        refreshVisibility();
    }


    private void refreshVisibility() {

        boolean useGit =
                useGitCheckBox.isSelected();

        repositoryModeLabel.setVisible(useGit);
        singleRepositoryRadioButton.setVisible(useGit);
        separateRepositoriesRadioButton.setVisible(useGit);

        boolean separate =
                separateRepositoriesRadioButton.isSelected();

        repositoryNameLabel.setVisible(
                useGit && !separate
        );

        repositoryNameField.setVisible(
                useGit && !separate
        );

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

        createRemoteRepositoryCheckBox.setVisible(useGit);

        boolean createRemote =
                useGit &&
                        createRemoteRepositoryCheckBox.isSelected();

        githubUsernameLabel.setVisible(createRemote);
        githubUsernameField.setVisible(createRemote);

        githubTokenLabel.setVisible(createRemote);
        githubTokenField.setVisible(createRemote);

        mainPanel.revalidate();
        mainPanel.repaint();
    }
}
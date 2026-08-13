package org.labs.genesis.forms;

import lombok.Getter;

import javax.swing.*;

@Getter
public class DockerConfigurationForm {

    private JPanel mainPanel;

    private JCheckBox configureDockerCheckBox;

    private JLabel dockerGenerationModeLabel;

    private JRadioButton dockerFrontendRadioButton;
    private JRadioButton dockerBackendRadioButton;
    private JRadioButton dockerBothRadioButton;

    private JLabel frontendContainerNameLabel;
    private JTextField frontendContainerNameField;

    private JLabel backendContainerNameLabel;
    private JTextField backendContainerNameField;


    public DockerConfigurationForm() {

        // ---------------------------------------------------------
        // Docker mode
        // ---------------------------------------------------------

        ButtonGroup dockerGroup = new ButtonGroup();

        dockerGroup.add(dockerFrontendRadioButton);
        dockerGroup.add(dockerBackendRadioButton);
        dockerGroup.add(dockerBothRadioButton);


        // ---------------------------------------------------------
        // Listeners
        // ---------------------------------------------------------

        configureDockerCheckBox.addActionListener(e ->
                refreshVisibility()
        );

        dockerFrontendRadioButton.addActionListener(e ->
                refreshVisibility()
        );

        dockerBackendRadioButton.addActionListener(e ->
                refreshVisibility()
        );

        dockerBothRadioButton.addActionListener(e ->
                refreshVisibility()
        );


        // ---------------------------------------------------------
        // Default values
        // ---------------------------------------------------------

        configureDockerCheckBox.setSelected(false);

        dockerBothRadioButton.setSelected(true);

        frontendContainerNameField.setText("frontend");

        backendContainerNameField.setText("backend");


        // ---------------------------------------------------------
        // Initial visibility
        // ---------------------------------------------------------

        refreshVisibility();
    }


    private void refreshVisibility() {

        boolean useDocker =
                configureDockerCheckBox.isSelected();


        // =========================================================
        // DOCKER GENERATION MODE
        // =========================================================

        dockerGenerationModeLabel.setVisible(useDocker);

        dockerFrontendRadioButton.setVisible(useDocker);

        dockerBackendRadioButton.setVisible(useDocker);

        dockerBothRadioButton.setVisible(useDocker);


        // =========================================================
        // DOCKERIZED SERVICES
        // =========================================================

        boolean frontendDockerized =
                useDocker &&
                        (
                                dockerFrontendRadioButton.isSelected()
                                        || dockerBothRadioButton.isSelected()
                        );

        boolean backendDockerized =
                useDocker &&
                        (
                                dockerBackendRadioButton.isSelected()
                                        || dockerBothRadioButton.isSelected()
                        );


        // =========================================================
        // FRONTEND CONTAINER
        // =========================================================

        frontendContainerNameLabel.setVisible(
                frontendDockerized
        );

        frontendContainerNameField.setVisible(
                frontendDockerized
        );


        // =========================================================
        // BACKEND CONTAINER
        // =========================================================

        backendContainerNameLabel.setVisible(
                backendDockerized
        );

        backendContainerNameField.setVisible(
                backendDockerized
        );


        // =========================================================
        // REFRESH UI
        // =========================================================

        mainPanel.revalidate();
        mainPanel.repaint();
    }
}
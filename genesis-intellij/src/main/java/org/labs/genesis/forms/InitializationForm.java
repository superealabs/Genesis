package org.labs.genesis.forms;

import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import lombok.Getter;
import org.labs.genesis.config.langage.ConfigurationMetadata;
import org.labs.genesis.config.langage.Framework;
import org.labs.genesis.config.langage.Language;
import org.labs.genesis.config.langage.Project;
import org.labs.genesis.config.langage.generator.project.ProjectGenerator;

import javax.swing.*;
import java.util.List;

@Getter
public class InitializationForm {
    private JPanel mainPanel;
    private JTextField projectNameField;
    private TextFieldWithBrowseButton locationField;
    private JComboBox<Language> languageOptions;
    private JComboBox<String> languageVersionOptions;
    private JComboBox<String> coreFrameworkOptions;
    private JComboBox<Project> buildToolOptions;
    private JLabel languageVersionLabel;
    private JComboBox<Framework> frameworkOptions;
    private JComboBox<String> frameworkVersionOptions;
    private JLabel nameLabel;
    private JLabel locationLabel;
    private JLabel languageLabel;
    private JLabel frameworkLabel;
    private JLabel buildToolLabel;
    private JLabel projectType;
    private JLabel frameworkVersionLabel;
    private JLabel groupIdLabel;
    private JTextField groupIdField;

    public InitializationForm() {
        // Créez un FileChooserDescriptor pour sélectionner un seul dossier
        FileChooserDescriptor folderChooserDescriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor();
        folderChooserDescriptor.withTitle("Select Folder");
        folderChooserDescriptor.withDescription("Choose a directory for the project location");

        // Ajoutez un écouteur de sélection de dossier au champ de texte
        locationField.addBrowseFolderListener(
                null, // Projet (null si non applicable)
                folderChooserDescriptor
        );

        groupIdLabel.setEnabled(false);
        groupIdField.setEnabled(false);

        // Populate initial options and set default selections
        populateLanguageOptions();
        initializeDefaultSelections();

        // Add listeners for dependent comboboxes
        addListeners();
    }

    private void addListeners() {
        languageOptions.addActionListener(e -> {
            Language selectedLanguage = (Language) languageOptions.getSelectedItem();
            if (selectedLanguage != null) {
                populateLanguageVersionOptions(selectedLanguage);
                populateCoreFrameworkOptions(selectedLanguage);
            }
        });

        coreFrameworkOptions.addActionListener(e -> addCoreFrameworkListener());

        frameworkOptions.addActionListener(e -> {
            Framework selectedFramework = (Framework) frameworkOptions.getSelectedItem();
            if (selectedFramework != null) {
                populateFrameworkVersionOptions(selectedFramework);
            }
        });
    }

    private void addCoreFrameworkListener() {
        String selectedCoreFramework = (String) coreFrameworkOptions.getSelectedItem();
        if (selectedCoreFramework != null) {
            populateBuildToolOptions(selectedCoreFramework);
            populateFrameworkOptions(selectedCoreFramework);

            if (frameworkOptions.getItemCount() > 0) {
                frameworkOptions.setSelectedIndex(0); // Automatically select the first framework
                Framework selectedFramework = (Framework) frameworkOptions.getSelectedItem();
                if (selectedFramework != null) {
                    populateFrameworkVersionOptions(selectedFramework); // Automatically populate and select the first version
                }
            }
        }
    }


    private void initializeDefaultSelections() {
        if (languageOptions.getItemCount() > 0) {
            languageOptions.setSelectedIndex(0);
            Language defaultLanguage = (Language) languageOptions.getSelectedItem();
            if (defaultLanguage != null) {
                populateLanguageVersionOptions(defaultLanguage);
                populateCoreFrameworkOptions(defaultLanguage);
            }
        }

        if (coreFrameworkOptions.getItemCount() > 0) {
            coreFrameworkOptions.setSelectedIndex(0);
            addCoreFrameworkListener();
        }
    }


    private void populateLanguageOptions() {
        List<Language> languages = ProjectGenerator.languages.values().stream().toList();
        for (Language language : languages) {
            languageOptions.addItem(language);
        }
    }

    private void populateCoreFrameworkOptions(Language language) {
        coreFrameworkOptions.removeAllItems();
        List<String> frameworks = ProjectGenerator.frameworks.values().stream()
                .filter(f -> f.getLanguageId() == language.getId())
                .map(Framework::getCoreFramework)
                .distinct()
                .toList();

        for (String framework : frameworks) {
            coreFrameworkOptions.addItem(framework);
        }

        if (coreFrameworkOptions.getItemCount() > 0) {
            coreFrameworkOptions.setSelectedIndex(0);
        }
    }

    private void populateFrameworkOptions(String coreFramework) {
        frameworkOptions.removeAllItems();
        List<Framework> frameworks = ProjectGenerator.frameworks.values().stream()
                .filter(f -> f.getCoreFramework().equalsIgnoreCase(coreFramework))
                .distinct()
                .toList();

        for (Framework framework : frameworks) {
            frameworkOptions.addItem(framework);
        }

        if (frameworkOptions.getItemCount() > 0) {
            frameworkOptions.setSelectedIndex(0);
            Framework selectedFramework = (Framework) frameworkOptions.getSelectedItem();
            assert selectedFramework != null;
            if (selectedFramework.getWithGroupId() != null && selectedFramework.getWithGroupId()) {
                groupIdLabel.setEnabled(true);
                groupIdField.setEnabled(true);
                groupIdField.setText("org.example");
            } else {
                groupIdLabel.setEnabled(false);
                groupIdField.setEnabled(false);
                groupIdField.setText("");
            }
        }
    }

    private void populateBuildToolOptions(String framework) {
        buildToolOptions.removeAllItems();
        for (Project project : ProjectGenerator.projects.values()) {
            if (project.getCoreFrameworks().contains(framework)) {
                buildToolOptions.addItem(project);
            }
        }

        if (buildToolOptions.getItemCount() > 0) {
            buildToolOptions.setSelectedIndex(0);
        }
    }

    private void populateLanguageVersionOptions(Language language) {
        languageVersionOptions.removeAllItems();
        for (ConfigurationMetadata config : language.getConfigurations()) {
            if ("languageVersion".equals(config.getVariableName())) {
                for (String version : config.getOptions()) {
                    languageVersionOptions.addItem(version);
                }
                break;
            }
        }

        enableOptions(languageVersionOptions.getItemCount() > 0, languageVersionOptions, languageVersionLabel);
    }

    private void populateFrameworkVersionOptions(Framework framework) {
        frameworkVersionOptions.removeAllItems();
        for (ConfigurationMetadata config : framework.getConfigurations()) {
            if ("frameworkVersion".equals(config.getVariableName())) {
                for (String version : config.getOptions()) {
                    frameworkVersionOptions.addItem(version);
                }
                break;
            }
        }

        enableOptions(frameworkVersionOptions.getItemCount() > 0, frameworkVersionOptions, frameworkVersionLabel);

        // Automatically select the first version if available
        if (frameworkVersionOptions.getItemCount() > 0) {
            frameworkVersionOptions.setSelectedIndex(0);
        }
    }


    private void enableOptions(boolean hasItems, JComboBox<String> comboBox, JLabel label) {
        if (hasItems) {
            label.setEnabled(true);
            comboBox.setEnabled(true);
            if (comboBox.getItemCount() > 0) {
                comboBox.setSelectedIndex(0);
            }
        } else {
            label.setEnabled(false);
            comboBox.setEnabled(false);
            comboBox.removeAllItems();
            comboBox.addItem("Not applicable");
            comboBox.setSelectedIndex(0);
        }
    }


}
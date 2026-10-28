package org.labs.genesis.forms;

import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.ui.components.labels.LinkLabel;
import lombok.Getter;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.labs.genesis.config.ProjectGenerationContext;
import org.labs.genesis.config.langage.generator.project.LlmApiClient;
import org.labs.genesis.config.langage.generator.project.LlmApiConfig;
import org.labs.genesis.config.langage.generator.project.ProjectGenerator;
import org.labs.genesis.connexion.Database;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

@Getter
public class ScriptDatabaseConfigurationForm {

    private final ProjectGenerationContext projectGenerationContext;
    private final LlmApiClient llmApiClient;

    private JPanel mainPanel;
    private JLabel databaseLabel;
    private JComboBox<Database> dmsOptions;
    private TextFieldWithBrowseButton locationField;
    private RTextScrollPane rTextScrollPane;
    private RSyntaxTextArea sqlTextArea;
    private JCheckBox useLLMCheckBox;
    private JCheckBox addDBSchemaCheckBox;
    private JComboBox<LlmApiConfig> llmModelComboBox;
    private JCheckBox usePersonalAccessTokenCheckBox;
    private JLabel tokenLabel;
    private JPasswordField tokenApiField;
    private JTextArea promptTextArea;
    private LinkLabel<String> generateScriptLinkLabel;

    public ScriptDatabaseConfigurationForm(ProjectGenerationContext projectGenerationContext) {
        this.projectGenerationContext = projectGenerationContext;
        this.llmApiClient = new LlmApiClient();

        populateDmsOptions();
        populateLlmModelComboBox();
        configureUseLLMCheckBox();
        configureUsePersonalAccessTokenCheckBox();
        addLocationFieldListener();
        setupListeners();

        // Mettre à jour le context avec la base sélectionnée par défaut
        if (this.projectGenerationContext != null && getSelectedDatabase() != null) {
            this.projectGenerationContext.setDatabase(getSelectedDatabase());
        }

        // Écouter les changements de sélection de base de données
        dmsOptions.addActionListener(e -> {
            if (this.projectGenerationContext != null && getSelectedDatabase() != null) {
                this.projectGenerationContext.setDatabase(getSelectedDatabase());
            }
        });

        useLLMCheckBox.setSelected(false);
        usePersonalAccessTokenCheckBox.setSelected(false);
        addDBSchemaCheckBox.setSelected(false);
        llmModelComboBox.setEnabled(false);
        usePersonalAccessTokenCheckBox.setEnabled(false);
        addDBSchemaCheckBox.setEnabled(false);
        tokenApiField.setEnabled(false);
        promptTextArea.setEnabled(false);
        generateScriptLinkLabel.setEnabled(false);
    }

    public Database getSelectedDatabase() {
        return (Database) dmsOptions.getSelectedItem();
    }

    private void populateDmsOptions() {
        if (dmsOptions == null) return;
        dmsOptions.removeAllItems();
        for (Database database : ProjectGenerator.databases.values()) {
            dmsOptions.addItem(database);
        }
        if (dmsOptions.getItemCount() > 0) {
            dmsOptions.setSelectedIndex(0);
            if (this.projectGenerationContext != null) {
                this.projectGenerationContext.setDatabase((Database) dmsOptions.getSelectedItem());
            }
        }
    }

    private void populateLlmModelComboBox() {
        if (llmModelComboBox == null) return;
        List<LlmApiConfig> llmApiConfigList = ProjectGenerator.llmApiConfigs.values().stream().toList();
        for (LlmApiConfig llmApiConfig : llmApiConfigList) {
            llmModelComboBox.addItem(llmApiConfig);
        }
    }

    private void addLocationFieldListener() {
        FileChooserDescriptor fileChooserDescriptor = FileChooserDescriptorFactory.createSingleFileNoJarsDescriptor()
                .withTitle("Select SQL File")
                .withDescription("Choose a .sql file to load into the editor");

        locationField.addBrowseFolderListener(null, fileChooserDescriptor);
        locationField.getTextField().getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                onLocationFieldChanged();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                onLocationFieldChanged();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                onLocationFieldChanged();
            }
        });
    }

    private void onLocationFieldChanged() {
        String filePath = locationField.getText();
        if (filePath == null || filePath.isEmpty()) return;

        File file = new File(filePath);
        if (!file.exists() || !file.isFile()) return;

        try {
            String content = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
            SwingUtilities.invokeLater(() -> {
                sqlTextArea.setText(content);
                sqlTextArea.setCaretPosition(0);
                rTextScrollPane.revalidate();
                rTextScrollPane.repaint();
                mainPanel.revalidate();
                mainPanel.repaint();
            });
        } catch (IOException ex) {
            SwingUtilities.invokeLater(() -> Messages.showErrorDialog(mainPanel, "Error reading file: " + ex.getMessage(), "Error"));
        }
    }

    private void configureUseLLMCheckBox() {
        useLLMCheckBox.addActionListener(e -> {
            boolean selected = useLLMCheckBox.isSelected();
            llmModelComboBox.setEnabled(selected);
            usePersonalAccessTokenCheckBox.setEnabled(selected);
            usePersonalAccessTokenCheckBox.setSelected(false);
            addDBSchemaCheckBox.setEnabled(selected);
            addDBSchemaCheckBox.setSelected(false);
            tokenApiField.setEnabled(false);
            promptTextArea.setEnabled(selected);
            generateScriptLinkLabel.setEnabled(selected);
            locationField.setEnabled(!selected);
        });
    }

    private void configureUsePersonalAccessTokenCheckBox() {
        usePersonalAccessTokenCheckBox.addActionListener(e -> {
            boolean selected = usePersonalAccessTokenCheckBox.isSelected();
            tokenApiField.setEnabled(selected);
            this.llmApiClient.setUseCustomApiKey(selected);
        });
    }

    private void setupListeners() {
        if (generateScriptLinkLabel != null) {
            generateScriptLinkLabel.setListener((LinkLabel<String> source, String data) -> generateSQLFromLLM(), null);
        }
    }

    private void generateSQLFromLLM() {
        try {
            LlmApiConfig llmApiConfig = (LlmApiConfig) this.llmModelComboBox.getSelectedItem();
            if (llmApiConfig == null) return;

            this.llmApiClient.setDefaultModel(llmApiConfig.getModel());
            this.llmApiClient.setApiUrl(llmApiConfig.getApiUrl());
            if (this.llmApiClient.getUseCustomApiKey()) {
                this.llmApiClient.setApiKey(new String(this.tokenApiField.getPassword()).trim());
            } else {
                this.llmApiClient.setApiKeyFromFile();
            }

            String prompt = this.promptTextArea.getText();
            if (prompt == null || prompt.trim().isEmpty()) {
                Messages.showErrorDialog(mainPanel, "The prompt cannot be empty", "Error");
                return;
            }

            Database selectedDb = getSelectedDatabase();
            if (selectedDb == null) {
                Messages.showErrorDialog(mainPanel, "Please select a database type first.", "Error");
                return;
            }
            this.projectGenerationContext.setDatabase(selectedDb);

            String scriptGenerated = this.llmApiClient.generateSQL(
                    this.projectGenerationContext,
                    prompt,
                    false
            );
            this.sqlTextArea.setText(scriptGenerated);
            Messages.showInfoMessage(mainPanel, "SQL generation successful!", "Success");
        } catch (Exception e) {
            Messages.showErrorDialog(mainPanel, "Failed to generate SQL script: " + e.getMessage(), "Error");
        }
    }
}

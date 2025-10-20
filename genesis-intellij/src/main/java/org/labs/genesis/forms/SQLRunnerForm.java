package org.labs.genesis.forms;

import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.ui.components.labels.LinkLabel;
import lombok.Getter;
import org.labs.genesis.config.ProjectGenerationContext;
import org.labs.genesis.config.langage.generator.project.LlmApiClient;
import org.labs.genesis.config.langage.generator.project.LlmApiConfig;
import org.labs.genesis.config.langage.generator.project.ProjectGenerator;
import org.labs.genesis.connexion.SQLRunner;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.RTextScrollPane;

@Getter
public class SQLRunnerForm {
    private final ProjectGenerationContext projectGenerationContext;
    private JPanel mainPanel;
    private TextFieldWithBrowseButton locationField;
    private LinkLabel<String> executeLinkLabel;
    private JPasswordField tokenApiField;
    private JLabel nameLabel;
    private JCheckBox usePersonalAccessTokenCheckBox;
    private JCheckBox useLLMCheckBox;
    private JComboBox<LlmApiConfig> llmModelComboBox;
    private JTextArea promptTextArea;
    private LinkLabel<String> generateScriptLinkLabel;
    private RTextScrollPane rTextScrollPane;
    private RSyntaxTextArea sqlTextArea;
    private JCheckBox addDBSchemaCheckBox;
    private final LlmApiClient llmApiClient;
    
    public SQLRunnerForm(ProjectGenerationContext projectGenerationContext) {
        this.projectGenerationContext = projectGenerationContext;
        this.llmApiClient = new LlmApiClient();
        populateLlmModelComboBox();
        configureUseLLMCheckBox();
        configureUsePersonalAccessTokenCheckBox();
        addLocationFieldListener();
        setupListeners();
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

    private void addLocationFieldListener() {
        // Crée le FileChooserDescriptor pour sélectionner un seul fichier .sql
        FileChooserDescriptor fileChooserDescriptor = FileChooserDescriptorFactory.createSingleFileDescriptor()
                .withTitle("Select SQL File")
                .withDescription("Choose a .sql file to load into the editor")
                .withFileFilter(file -> {
                    String extension = file.getExtension();
                    return true;
                    //returetrieve table namesrn extension != null && extension.equalsIgnoreCase("sql");
                });

        // Ajoute le sélecteur de fichiers au champ (sans les paramètres dépréciés)
        locationField.addBrowseFolderListener(null, fileChooserDescriptor);
        // Initialisation du bouton de sélection de fichier
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
            sqlTextArea.setText(content);
        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(mainPanel, "Error reading file: " + ex.getMessage());
        }
    }

    private void setupListeners() {
        assert executeLinkLabel != null;
        executeLinkLabel.setListener((LinkLabel<String> source, String data) -> executeSQLCommand(), null);
        assert generateScriptLinkLabel != null;
        generateScriptLinkLabel.setListener((LinkLabel<String> source, String data) -> generateSQLFromLLM(), null);
    }

    private boolean validateCreateCommand() {
        String sqlCommand=this.sqlTextArea.getText();
        if(!sqlCommand.toUpperCase().contains("CREATE")){
            Messages.showErrorDialog(
                    mainPanel,
                    "Command must be a CREATE statement",
                    "Error"
            );
            return false;
        }
        return true;
    }

    private void executeSQLCommand() {

        try {
            if (!validateCreateCommand()) return;
            SQLRunner.execute(this.projectGenerationContext.getConnection(), this.sqlTextArea.getText());
            Messages.showInfoMessage(
                    mainPanel,
                    "SQL execution successful!",
                    "Success"
            );
        } catch (IllegalStateException e) {
            Messages.showErrorDialog(
                    mainPanel,
                    e.getMessage(),
                    "Error"
            );
        } catch (Exception e) {
            Messages.showErrorDialog(
                    mainPanel,
                    "Failed to execute SQL command: " + e.getMessage(),
                    "Error"
            );
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

    private void populateLlmModelComboBox() {
        List<LlmApiConfig> llmApiConfigList = ProjectGenerator.llmApiConfigs.values().stream().toList();
        for (LlmApiConfig llmApiConfig : llmApiConfigList) {
            llmModelComboBox.addItem(llmApiConfig);
        }
    }

    private boolean validateIaPrompt() {
        String prompt=this.promptTextArea.getText();
        if(prompt.isEmpty()){
            Messages.showErrorDialog(
                    mainPanel,
                    "The prompt can not be empty",
                    "Error"
            );
            return false;
        }
        return true;
    }

    private void generateSQLFromLLM() {
        try {
            LlmApiConfig llmApiConfig = (LlmApiConfig) this.llmModelComboBox.getSelectedItem();
            this.llmApiClient.setDefaultModel(llmApiConfig.getModel());
            this.llmApiClient.setApiUrl(llmApiConfig.getApiUrl());
            if(this.llmApiClient.getUseCustomApiKey()) {
                this.llmApiClient.setApiKey(this.tokenApiField.getText().trim());
            } else {
                this.llmApiClient.setApiKeyFromFile();
            }
            if (!validateIaPrompt()) return;
            String scriptGenerated = this.llmApiClient.generateSQL(this.projectGenerationContext,
                    this.promptTextArea.getText(),
                    addDBSchemaCheckBox.isSelected());
            this.sqlTextArea.setText(scriptGenerated);
            Messages.showInfoMessage(
                    mainPanel,
                    "SQL generation successful!",
                    "Success"
            );
        } catch (IllegalStateException e) {
            Messages.showErrorDialog(
                    mainPanel,
                    e.getMessage(),
                    "Error"
            );
        } catch (Exception e) {
            Messages.showErrorDialog(
                    mainPanel,
                    "Failed to generate SQL script: " + e.getMessage(),
                    "Error"
            );
        }
    }

}

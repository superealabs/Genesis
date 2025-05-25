package org.labs.genesis.forms;

import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.ui.components.labels.LinkLabel;
import lombok.Getter;
import org.labs.genesis.config.ProjectGenerationContext;
import org.labs.genesis.connexion.SQLRunner;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

@Getter
public class SQLRunnerForm {
    private final ProjectGenerationContext projectGenerationContext;
    private JPanel mainPanel;
    private TextFieldWithBrowseButton locationField;
    private LinkLabel<String> executeLinkLabel;
    private JTextArea textArea1;

    public SQLRunnerForm(ProjectGenerationContext projectGenerationContext) {
        this.projectGenerationContext = projectGenerationContext;
        addLocationFieldListener();
        setupListeners();
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
            textArea1.setText(content);
        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(mainPanel, "Error reading file: " + ex.getMessage());
        }
    }

    private void setupListeners() {
        assert executeLinkLabel != null;
        executeLinkLabel.setListener((LinkLabel<String> source, String data) -> executeSQLCommand(), null);
    }

    private boolean validateCreateCommand() {
        String sqlCommand=this.textArea1.getText();
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
            SQLRunner.execute(this.projectGenerationContext.getConnection(), this.textArea1.getText());
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
}

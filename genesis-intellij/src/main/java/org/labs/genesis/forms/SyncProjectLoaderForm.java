package org.labs.genesis.forms;

import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.TextBrowseFolderListener;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import lombok.Getter;
import org.labs.genesis.config.Constantes;
import javax.swing.*;
import java.awt.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Getter
public class SyncProjectLoaderForm {
    private JPanel mainPanel;
    private TextFieldWithBrowseButton folderField;
    private JLabel statusLabel;
    private static final String GENESIS_CONTEXT_FILE = Constantes.GENESIS_CONTEXT_FILE;

    public SyncProjectLoaderForm() {
        setupFolderSelector();
        setupStatusLabel();
        setupListeners();
    }

    private void setupFolderSelector() {
        FileChooserDescriptor folderChooserDescriptor =
                FileChooserDescriptorFactory.createSingleFolderDescriptor()
                        .withTitle("Select Project Folder")
                        .withDescription("Choose a directory containing genesis-context file");

        folderField.addBrowseFolderListener(
                new TextBrowseFolderListener(folderChooserDescriptor, (Project) null) {
                }
        );

        folderField.setPreferredSize(new Dimension(400, folderField.getPreferredSize().height));
    }

    private void setupStatusLabel() {
        statusLabel.setText("");
    }

    private void setupListeners() {
        folderField.getTextField().getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                validateFolder();
            }
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                validateFolder();
            }
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                validateFolder();
            }
        });
    }

    public boolean validateFolder() {
        String folderPath = folderField.getText();
        boolean valid = false;

        if (folderPath.trim().isEmpty()) {
            updateStatus("", Color.BLACK);
        }

        Path folder = Paths.get(folderPath);
        Path contextFile = folder.resolve(GENESIS_CONTEXT_FILE);

        if (Files.exists(contextFile) && Files.isRegularFile(contextFile)) {
            updateStatus("✓ Genesis context found", new Color(0, 128, 0));
            return true;
        } else {
            updateStatus("⚠ Genesis context file not found. Please select a valid project directory.",
                    new Color(255, 140, 0));
        }
        return valid;
    }

    private void updateStatus(String message, Color color) {
        statusLabel.setText(message);
        statusLabel.setForeground(color);
    }

    public boolean hasValidGenesisContext() {
        String folderPath = folderField.getText();
        if (folderPath.trim().isEmpty()) {
            return false;
        }

        Path folder = Paths.get(folderPath);
        Path contextFile = folder.resolve(GENESIS_CONTEXT_FILE);
        return Files.exists(contextFile) && Files.isRegularFile(contextFile);
    }

    public Path getGenesisContextPath() {
        if (!hasValidGenesisContext()) {
            return null;
        }
        return Paths.get(folderField.getText()).resolve(GENESIS_CONTEXT_FILE);
    }

    public void showValidationError() {
        Messages.showErrorDialog(
                mainPanel,
                "Please select a valid directory containing a " + GENESIS_CONTEXT_FILE + " file.",
                "Invalid Project Directory"
        );
    }
}
package org.labs.genesis.action.apjwizard.forms;

import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.labels.LinkLabel;
import lombok.Getter;
import org.labs.genesis.apj.filetype.ApjFile;
import org.labs.genesis.apj.generator.ApjFileGenerator;
import org.labs.genesis.apj.utilitaire.UtilDBDynamique;
import org.labs.genesis.config.langage.Language;
import org.labs.genesis.config.langage.generator.project.ProjectGenerator;

import java.io.File;
import java.sql.Connection;
import java.util.List;
import javax.swing.*;
import static org.labs.genesis.apj.ApjGenerationContext.*;

@Getter
public class PropertiesForm {
    private JPanel mainPanel;
    private JComboBox<ApjFile> fileApjOptions;
    private TextFieldWithBrowseButton jarDir;
    private TextFieldWithBrowseButton libDir;
    private TextFieldWithBrowseButton location;
    private LinkLabel<String> testConnectionButton;
    private JLabel connectionStatusLabel;

    public PropertiesForm() {
        setupFolderChoosers();
        addTestConnectionButtonListener();
        populateApjFileOptions();
    }

    public void populateApjFileOptions() {
        List<ApjFile> apjFiles = ApjFileGenerator.apjFileMap.values().stream().toList();
        for (ApjFile apjFile : apjFiles) {
            fileApjOptions.addItem(apjFile);
        }
    }

    private void setupFolderChoosers() {
        setupFolderChooser(libDir, "Select Lib Directory", "Choose the lib directory for your project");
        setupFolderChooser(jarDir, "Select Project JAR Directory", "Choose project JARs");
        setupFolderChooser(location, "Select Project Location", "Choose location for your file");
    }

    private void setupFolderChooser(TextFieldWithBrowseButton field, String title, String description) {
        FileChooserDescriptor chooser = FileChooserDescriptorFactory.createSingleFolderDescriptor();
        chooser.withTitle(title);
        chooser.withDescription(description);
        field.addBrowseFolderListener(null, chooser);
    }

    private void addTestConnectionButtonListener() {
        testConnectionButton.setListener((source, data) -> {
            File socobisJarFile = new File(jarDir.getText());
            File libDirectory = new File(libDir.getText());

            try (Connection conn = UtilDBDynamique.GetConn(socobisJarFile, libDirectory)) {
                Messages.showInfoMessage(
                        mainPanel,
                        "Connection successful!",
                        "Success"
                );
                connectionStatusLabel.setText("<html>Connection successful!</html>");
                connectionStatusLabel.setForeground(JBColor.GREEN);
            } catch (Exception ex) {
                Messages.showErrorDialog(
                        mainPanel,
                        "Connection failed:\n" + ex.getMessage(),
                        "Error"
                );
                connectionStatusLabel.setText("<html>Connection failed:<br>" + ex.getMessage() + "</html>");
                connectionStatusLabel.setForeground(JBColor.RED);
            }
        }, null);
    }

}

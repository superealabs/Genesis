package org.labs.genesis.action.apjwizard.forms;

import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.labels.LinkLabel;
import lombok.Getter;
import org.labs.genesis.action.apjwizard.forms.helper.ProgressUtils;
import org.labs.genesis.apj.filetype.ApjFile;
import org.labs.genesis.apj.generator.ApjFileGenerator;
import org.labs.genesis.apj.utilitaire.UtilDBDynamique;

import java.sql.Connection;
import java.util.List;
import javax.swing.*;

@Getter
public class PropertiesForm {
    private JPanel mainPanel;
    private JComboBox<ApjFile> fileApjOptions;
    private TextFieldWithBrowseButton jarDir;
    private TextFieldWithBrowseButton libDir;
    private TextFieldWithBrowseButton location;
    private LinkLabel<String> testConnectionButton;
    private JLabel connectionStatusLabel;
    private JCheckBox sansBaseCheckBox;

    public PropertiesForm() {
        populateApjFileOptions();
        initConfig();
    }

    public void initConfig() {
        sansBaseCheckBox.addItemListener(e -> {
            boolean selected = sansBaseCheckBox.isSelected();
            testConnectionButton.setEnabled(!selected);
            connectionStatusLabel.setEnabled(!selected);
        });
    }

    public void populateApjFileOptions() {
        List<ApjFile> apjFiles = ApjFileGenerator.apjFileMap.values().stream().toList();
        for (ApjFile apjFile : apjFiles) {
            fileApjOptions.addItem(apjFile);
        }
    }

    public void setupFolderChoosers(Project project) {
        setupFolderChooser(project,libDir, "Select Lib Directory", "Choose the lib directory for your project");
        setupFolderChooser(project,jarDir, "Select Project JAR Directory", "Choose project JARs");
        setupFolderChooser(project,location, "Select Project Location", "Choose location for your file");
    }

    private void setupFolderChooser(Project project,TextFieldWithBrowseButton field, String title, String description) {
        FileChooserDescriptor chooser = FileChooserDescriptorFactory.createSingleFolderDescriptor();
        chooser.withTitle(title);
        chooser.withDescription(description);
        field.addBrowseFolderListener(project, chooser);
    }

    public void addTestConnectionButtonListener(Project project) {
        testConnectionButton.setListener((source, data) -> {
            String jarPath = jarDir.getText();
            String libPath = libDir.getText();

            try {
                ProgressUtils.runWithProgress(project, "Testing Database Connection...", indicator -> {
                    try (Connection conn = UtilDBDynamique.GetConn(jarPath, libPath)) {
                        ProgressUtils.updateProgress(indicator, "Connection successful", 1.0);
                    } catch (Exception e) {
                        throw new ConfigurationException(e.getMessage());
                    }
                });
                Messages.showInfoMessage(mainPanel, "Connection successful!", "Success");
                connectionStatusLabel.setText("<html>Connection successful!</html>");
                connectionStatusLabel.setForeground(JBColor.GREEN);
            } catch (Exception ex) {
                Messages.showErrorDialog(mainPanel, "Connection failed:\n" + ex.getMessage(), "Error");
                connectionStatusLabel.setText("<html>Connection failed:<br>" + ex.getMessage() + "</html>");
                connectionStatusLabel.setForeground(JBColor.RED);
            }
        }, null);
    }

}

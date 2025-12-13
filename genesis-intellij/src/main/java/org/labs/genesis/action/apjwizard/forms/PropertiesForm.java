package org.labs.genesis.action.apjwizard.forms;

import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeManager;
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

import java.awt.*;
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
    private TextFieldWithBrowseButton racineProjetField;
    private TextFieldWithBrowseButton racinePageField;

    public PropertiesForm() {
        populateApjFileOptions();
        initConfig();
        configureFileApjRenderer();
    }

    public void initConfig() {
        sansBaseCheckBox.addItemListener(e -> {
            boolean selected = sansBaseCheckBox.isSelected();
            testConnectionButton.setEnabled(!selected);
            connectionStatusLabel.setEnabled(!selected);
        });
    }

    private void configureFileApjRenderer() {
        fileApjOptions.setRenderer(new ListCellRenderer<ApjFile>() {
            private final DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();

            @Override
            public Component getListCellRendererComponent(
                    JList<? extends ApjFile> list,
                    ApjFile apjFile,
                    int index,
                    boolean isSelected,
                    boolean cellHasFocus) {

                JLabel label = (JLabel) defaultRenderer.getListCellRendererComponent(
                        list, apjFile, index, isSelected, cellHasFocus);

                if (apjFile != null) {
                    FileType jsp = FileTypeManager.getInstance().getFileTypeByExtension(apjFile.getExtension());
                    Icon icon = jsp.getIcon();
                    label.setText(apjFile.toString());
                    label.setIcon(icon);
                }

                return label;
            }
        });
    }


    public void populateApjFileOptions() {
        List<ApjFile> apjFiles = ApjFileGenerator.apjFileMap.values().stream().toList();
        for (ApjFile apjFile : apjFiles) {
            fileApjOptions.addItem(apjFile);
        }
    }

    public void setupFolderChoosers(Project project) {
        setupFolderChooser(project, libDir, "Sélectionner le dossier lib", "Choisissez le dossier lib pour votre projet");
        setupFolderChooser(project, jarDir, "Sélectionner le dossier jar du projet", "Choisissez le dossier jar du projet");
        setupFolderChooser(project, location, "Sélectionner l’emplacement du projet", "Choisissez l’emplacement pour votre fichier");
        setupFolderChooser(project, racineProjetField, "Sélectionner la racine du projet Java", "Choisissez le dossier racine contenant le code Java");
        setupFolderChooser(project, racinePageField, "Sélectionner la racine des pages",  "Choisissez le dossier contenant vos pages");
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
                ProgressUtils.runWithProgress(project, "Test de la connexion à la base de données…", indicator -> {
                    try (Connection conn = UtilDBDynamique.GetConn(jarPath, libPath)) {
                        ProgressUtils.updateProgress(indicator, "Connexion réussie", 1.0);
                    } catch (Exception e) {
                        throw new ConfigurationException(e.getMessage());
                    }
                });
                Messages.showInfoMessage(mainPanel, "Connexion réussie!", "Succès");
                connectionStatusLabel.setText("<html>Connexion réussie!</html>");
                connectionStatusLabel.setForeground(JBColor.GREEN);
            } catch (Exception ex) {
                Messages.showErrorDialog(mainPanel, "Échec de la connexion :\n" + ex.getMessage(), "Erreur");
                connectionStatusLabel.setText("<html>Échec de la connexion :<br>" + ex.getMessage() + "</html>");
                connectionStatusLabel.setForeground(JBColor.RED);
            }
        }, null);
    }

}

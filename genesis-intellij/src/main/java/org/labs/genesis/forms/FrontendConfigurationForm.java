package org.labs.genesis.forms;

import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.ui.components.JBList;
import lombok.Getter;
import org.labs.genesis.config.langage.generator.project.ProjectGenerator;
import org.labs.genesis.frontend.FrontendLanguage;
import org.labs.genesis.frontend.generator.FrontendFramework;
import org.labs.genesis.frontend.generator.model.InterfaceLang;
import org.labs.genesis.frontend.generator.model.ProjectBranding;
import org.labs.genesis.listener.ColorPicker;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

@Getter
public class FrontendConfigurationForm {
    private JPanel mainPanel;
    private JComboBox<FrontendLanguage> frontendLanguageOptions;
    private JComboBox<FrontendFramework> frontendFrameworkOptions;
    private JLabel languageLabel;
    private JLabel frameworkLabel;
    private JComboBox<String> navbarSelect;
    private JTextField primaryColorField;
    private JButton primaryColorPickerButton;
    private JLabel primaryColorLabel;
    private JTextField secondaryColorField;
    private JLabel secondaryColorLabel;
    private JButton secondaryColorPickerButton;
    private TextFieldWithBrowseButton logoFileField;
    private JTextArea cssTextArea;
    private TextFieldWithBrowseButton faviconFileField;
    private JTextField logoLinkField;
    private JTextField faviconLinkField;
    private JBList<InterfaceLang> interfaceLangOptions;

    private File logoFile;
    private File faviconFile;

    public FrontendConfigurationForm() {
        initializeListners();
        initializeOptions();
    }

    private void initializeListners(){
        primaryColorPickerButton.addActionListener(new ColorPicker(primaryColorField ,Color.decode("#537cc2")));
        secondaryColorPickerButton.addActionListener(new ColorPicker(secondaryColorField ,new Color(0x53,0x7c,0xc2,0x26)));

        FileChooserDescriptor faviconChooserDescriptor = FileChooserDescriptorFactory.createSingleFileDescriptor()
                .withTitle("Select favicon File")
                .withDescription("Choose a .ico file to load into the editor")
                .withFileFilter(file -> {
                    String extension = file.getExtension();
                    return extension.equals("ico") || extension.equals("svg");
                });

        FileChooserDescriptor logoChooserDescriptor = FileChooserDescriptorFactory.createSingleFileDescriptor()
                .withTitle("Select logo File")
                .withDescription("Choose image or icon file to load into the editor")
                .withFileFilter(file -> {
                    String extension = file.getExtension();
                    return extension.equals("png") || extension.equals("jpeg") || extension.equals("jpg") || extension.equals("svg") || extension.equals("ico");
                });

        logoFileField.addBrowseFolderListener(null, logoChooserDescriptor);
        logoFileField.getTextField().getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent documentEvent) {
                onLogoFileChange();
            }

            @Override
            public void removeUpdate(DocumentEvent documentEvent) {
                onLogoFileChange();
            }

            @Override
            public void changedUpdate(DocumentEvent documentEvent) {
                onLogoFileChange();
            }
        });
        logoLinkField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                onLogoLinkChange();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                onLogoLinkChange();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                onLogoLinkChange();
            }
        });

        faviconFileField.addBrowseFolderListener(null, faviconChooserDescriptor);
        faviconFileField.getTextField().getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent documentEvent) {
                onFaviconFileChange();
            }

            @Override
            public void removeUpdate(DocumentEvent documentEvent) {
                onFaviconFileChange();
            }

            @Override
            public void changedUpdate(DocumentEvent documentEvent) {
                onFaviconFileChange();
            }
        });
        faviconLinkField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent documentEvent) {
                onFaviconLinkChange();
            }

            @Override
            public void removeUpdate(DocumentEvent documentEvent) {
                onFaviconLinkChange();
            }

            @Override
            public void changedUpdate(DocumentEvent documentEvent) {
                onFaviconLinkChange();
            }
        });
    }
    private void onLogoFileChange() {
        String filePath = logoFileField.getText();
        if (filePath == null || filePath.isEmpty()) return;
        File file = new File(filePath);
        if (!file.exists() || !file.isFile()) return;
        logoFile = file;
    }
    private void onLogoLinkChange() {
        String text = logoLinkField.getText();
        boolean hasText = text != null && !text.trim().isEmpty();
        // disable the file chooser if link is typed
        logoFileField.setEnabled(!hasText);
    }

    private void onFaviconFileChange() {
        String filePath = faviconFileField.getText();
        if (filePath == null || filePath.isEmpty()) return;
        File file = new File(filePath);
        if (!file.exists() || !file.isFile()) return;
        faviconFile = file;
    }
    private void onFaviconLinkChange() {
        String text = faviconLinkField.getText();
        boolean hasText = text != null && !text.trim().isEmpty();
        // disable the file chooser if link is typed
        faviconFileField.setEnabled(!hasText);
    }

    private  void initializeOptions(){
        populateLanguageOptions();
        populateFrameworkOptions((FrontendLanguage) frontendLanguageOptions.getSelectedItem());
        interfaceLangOptions.setListData(ProjectGenerator.langs.values().toArray(new InterfaceLang[0]));
        navbarSelect.setModel(new DefaultComboBoxModel<>(new String[]{"Sidebar","Topbar"}));
    }

    private void populateLanguageOptions() {
        List<FrontendLanguage> frontendLanguages = ProjectGenerator.frontendLanguage.values().stream().toList();
        for (FrontendLanguage frontendLanguage : frontendLanguages) {
            frontendLanguageOptions.addItem(frontendLanguage);
        }
        frontendLanguageOptions.setSelectedIndex(0);
    }

    private void populateFrameworkOptions(FrontendLanguage frontendLanguage) {
        frontendFrameworkOptions.removeAllItems();
        List<FrontendFramework> frameworks = ProjectGenerator.frontendFrameworks.values().stream()
                .filter(f -> f.getLanguageId() == frontendLanguage.getId())
                .distinct()
                .toList();

        for (FrontendFramework framework : frameworks) {
            frontendFrameworkOptions.addItem(framework);
        }

        if (frontendFrameworkOptions.getItemCount() > 0) {
            frontendFrameworkOptions.setSelectedIndex(0);
        }
    }
}

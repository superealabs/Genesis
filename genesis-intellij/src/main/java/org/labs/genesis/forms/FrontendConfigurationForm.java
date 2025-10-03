package org.labs.genesis.forms;

import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import lombok.Getter;
import org.labs.genesis.config.langage.Framework;
import org.labs.genesis.config.langage.FrameworkMVC;
import org.labs.genesis.config.langage.ViewsTemplateEngine;
import org.labs.genesis.listener.ColorPicker;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.io.File;

@Getter
public class FrontendConfigurationForm {
    private JPanel mainPanel;
    private JLabel templateEngine;
    private JComboBox<ViewsTemplateEngine> viewsTemplateEngineOptions;
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
        templateEngine.setVisible(false);
        viewsTemplateEngineOptions.setVisible(false);
    }

    private void configureMVCOptions(FrameworkMVC frameworkMVC) {
        templateEngine.setVisible(true);
        viewsTemplateEngineOptions.setVisible(true);

        viewsTemplateEngineOptions.removeAllItems();
        frameworkMVC.getViewsTemplateEngine()
                .forEach(te -> viewsTemplateEngineOptions.addItem(te));
    }

    public void updateFormWithFrameworkMVCOptions(FrameworkMVC frameworkMVC) {
        configureMVCOptions(frameworkMVC);
    }
}

package org.labs.genesis.forms;

import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.ui.components.JBList;
import lombok.Getter;
import org.labs.genesis.listener.ColorPicker;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

@Getter
public class FrontendConfigurationForm {
    private JPanel mainPanel;
    private JComboBox languageSelect;
    private JComboBox frameworkSelect;
    private JLabel languageLabel;
    private JLabel frameworkLabel;
    private JComboBox navbarSelect;
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

    public FrontendConfigurationForm() {
        primaryColorPickerButton.addActionListener(new ColorPicker(primaryColorField ,Color.decode("#537cc2")));
        secondaryColorPickerButton.addActionListener(new ColorPicker(secondaryColorField ,new Color(0x53,0x7c,0xc2,0x26)));

        FileChooserDescriptor faviconChooserDescriptor = FileChooserDescriptorFactory.createSingleFileDescriptor()
                .withTitle("Select favicon File")
                .withDescription("Choose a .ico file to load into the editor")
                .withFileFilter(file -> {
                    String extension = file.getExtension();
                    return extension.equals("ico") || extension.equals(".ico");
                });

        FileChooserDescriptor logoChooserDescriptor = FileChooserDescriptorFactory.createSingleFileDescriptor()
                .withTitle("Select logo File")
                .withDescription("Choose image or icon file to load into the editor")
                .withFileFilter(file -> {
                    String extension = file.getExtension();
                    return extension.equals("png") || extension.equals("jpeg") || extension.equals("jpg") || extension.equals("ico");
                });

        logoFileField.addBrowseFolderListener(null, logoChooserDescriptor);
        faviconFileField.addBrowseFolderListener(null, faviconChooserDescriptor);
    }
}

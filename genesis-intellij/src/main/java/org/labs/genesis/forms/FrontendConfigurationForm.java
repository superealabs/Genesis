package org.labs.genesis.forms;

import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.ui.components.JBList;
import lombok.Getter;

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
    private JTextField logoLinkField;
    private TextFieldWithBrowseButton logoFileField;
    private JTextArea cssTextArea;
    private TextFieldWithBrowseButton faviconFileField;
    private JTextField faviconLinkField;

    public FrontendConfigurationForm() {
        primaryColorPickerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                JColorChooser colorChooser = new JColorChooser();
                Color color = JColorChooser.showDialog(null, "Pick a color", Color.BLACK);
            }
        });
    }
}

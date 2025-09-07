package org.labs.genesis.forms;

import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.ui.components.JBList;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class FrontendConfigurationForm {
    private JPanel mainPanel;
    private JComboBox languageSelect;
    private JComboBox frameworkSelect;
    private JLabel languageLabel;
    private JLabel frameworkLabel;
    private JComboBox comboBox1;
    private JTextField primaryColorField;
    private JButton colorPickerButton;
    private JLabel primaryColorLabel;
    private JTextField secondaryColorField;
    private JLabel secondaryColorLabel;
    private JButton button1;
    private JTextField textField1;
    private TextFieldWithBrowseButton locationField;
    private JBList tableNamesList;

    public FrontendConfigurationForm() {
        colorPickerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                JColorChooser colorChooser = new JColorChooser();
                Color color = JColorChooser.showDialog(null, "Pick a color", Color.BLACK);
            }
        });
    }
}

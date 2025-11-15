package org.labs.genesis.action.apjwizard.forms;

import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import lombok.Getter;

import javax.swing.*;

@Getter
public class PropertiesForm {
    private JPanel mainPanel;
    private JComboBox fileApjType;
    private TextFieldWithBrowseButton jarDir;
    private TextFieldWithBrowseButton libDir;
    private TextFieldWithBrowseButton location;


}

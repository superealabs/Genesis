package org.labs.genesis.action.apjwizard.forms;

import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import lombok.Getter;

import javax.swing.*;
import static org.labs.genesis.config.ApjGenerationContext.*;

@Getter
public class PropertiesForm {
    private JPanel mainPanel;
    private JComboBox<String> fileApjType;
    private TextFieldWithBrowseButton jarDir;
    private TextFieldWithBrowseButton libDir;
    private TextFieldWithBrowseButton location;

    public PropertiesForm() {
        fileApjType.addItem(PAGE_RECHERCHE);
        fileApjType.addItem(PAGE_INSERT);
    }
}

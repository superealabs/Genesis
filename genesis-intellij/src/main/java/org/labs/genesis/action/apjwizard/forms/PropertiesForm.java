package org.labs.genesis.action.apjwizard.forms;

import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
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

        FileChooserDescriptor folderChooser = FileChooserDescriptorFactory.createSingleFolderDescriptor();
        folderChooser.withTitle("Select Lib Directory");
        folderChooser.withDescription("Choose the lib directory for your project");
        libDir.addBrowseFolderListener(null, folderChooser);

        FileChooserDescriptor jarChooser = FileChooserDescriptorFactory.createSingleFolderDescriptor();
        jarChooser.withTitle("Select Project JAR Directory");
        jarChooser.withDescription("Choose project JARs ");
        jarDir.addBrowseFolderListener(null, jarChooser);

        FileChooserDescriptor locationChooser = FileChooserDescriptorFactory.createSingleFolderDescriptor();
        locationChooser.withTitle("Select Project Location");
        locationChooser.withDescription("Choose location for your file");
        location.addBrowseFolderListener(null, locationChooser);
    }
}

package org.labs.genesis.forms;

import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import lombok.Getter;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.ui.TextBrowseFolderListener;
import com.intellij.openapi.project.Project;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.labs.genesis.config.langage.Framework;
import org.labs.genesis.config.langage.generator.project.ProjectGenerator;
import org.labs.genesis.config.langage.generator.ruleToCode.YamlData;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;

@Getter
public class RuleToCodeForm {
    private JPanel mainPanel;
    private TextFieldWithBrowseButton folderField;

    private RSyntaxTextArea yamlContentArea;
    private RTextScrollPane yamlScrollPane;

    private JButton showRuleButton; // 🔹 le nouveau bouton
    private JButton generateYamlButton;
    private JComboBox<String> selectFramework;
    private Map<String, Integer> frameworkIds = new HashMap<>() ;
    private JComboBox<Framework> frameworkOptions;

    public RuleToCodeForm() {


        //Add selector folder
        frameworkOptions = new JComboBox<>();
        FileChooserDescriptor folderChooserDescriptor =
                FileChooserDescriptorFactory.createSingleFolderDescriptor()
                        .withTitle("Select Folder")
                        .withDescription("Choose a directory for the project location");
        folderField.addBrowseFolderListener(
                new TextBrowseFolderListener(folderChooserDescriptor, (Project) null) {
                }
        );
        //Configure size field
        folderField.setPreferredSize(new Dimension(400, folderField.getPreferredSize().height));

        populateFramework();
        setupListeners();


        // 🔹 Listener du bouton
        showRuleButton.addActionListener(e -> {
                showRulePanel();
        });

    }
    private void showRulePanel() {
        JDialog ruleDialog = new JDialog((Frame) null, "Rule", true);
        ruleDialog.setSize(400, 250);
        ruleDialog.setLocationRelativeTo(mainPanel);

        JTextArea ruleTextArea = new JTextArea();
        ruleTextArea.setEditable(false);
        ruleTextArea.setText(
                "rule :\n" +
                " - name: Name rule\n" +
                        "   entity: Entity\n" +
                        "   description: Description\n" +
                        "   throw: Description\n" +
                        "   depends_on: [Entity1, Entity2, ... ]\n" +
                        "\n" +
                " - name: Name rule\n" +
                        "   entity: Entity\n" +
                        "   description: Description\n" +
                        "   throw: Description\n" +
                        "   depends_on: [Entity1, Entity2, ... ]\n"
        );

        JScrollPane scrollPane = new JScrollPane(ruleTextArea);
        ruleDialog.add(scrollPane);

        ruleDialog.setVisible(true);
    }

    private void populateFramework() {
        Set<String> seen = new HashSet<>();
        List<Framework> frameworkList = ProjectGenerator.frameworks.values().stream().toList();
        for (Framework framework : frameworkList) {
            if (seen.add(framework.getCoreFramework())) {
                selectFramework.addItem(framework.getCoreFramework());
                frameworkIds.put(framework.getCoreFramework(), framework.getId());
            }
        }
    }

    private void setupListeners() {

        YamlData yamlData = new YamlData();
        // ---------------- Generate YAML ----------------
        generateYamlButton.addActionListener(e -> {
            try {
                String[] meta = new String[0];
                String yamlMeta = "" ;
                String folderPath = folderField.getText();
                Path folderBaseProject = Paths.get(folderPath);
                Integer frameworkId = frameworkIds.get( (String) selectFramework.getSelectedItem() );

                if ( frameworkId == 1 ) {
                    meta = yamlData.extractGroupAndArtifact(folderBaseProject , frameworkId );
                    yamlMeta = yamlData.extractMetaData( folderBaseProject, meta[0], meta[1], frameworkId );
                }
                if ( frameworkId == 2 ) {
                    yamlMeta = yamlData.extractMetaData( folderBaseProject, "", yamlData.getProjectName(folderBaseProject), frameworkId );
                }

                yamlContentArea.setText(yamlMeta);
            } catch (Exception ex) {
                Messages.showErrorDialog(
                        mainPanel,
                        "Error generating YAML: " + ex.getMessage(),
                        "Error"
                );
            }
        });
    }



}

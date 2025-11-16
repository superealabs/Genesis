package org.labs.genesis.action.apjwizard.forms;

import com.intellij.ui.JBColor;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.table.JBTable;
import lombok.Getter;
import lombok.Setter;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

@Getter
@Setter
public class PageRechercheForm {
    private JPanel mainPanel;
    private JTextField nomField;
    private JTextField mappingField;
    private JTextField nomTableField;
    private JTextField titreField;
    private JLabel nomLabel;
    private JPanel generalPanel;
    private JLabel nomTableLabel;
    private JLabel titreLabel;
    private JPanel propertiesPanel;
    private JScrollPane scrollProperties;
    private JTabbedPane tabPane;
    private JPanel filtrePanel;
    private JPanel recapitulationPanel;
    private JPanel tableauPanel;
    private JScrollPane scrollFiltre;
    private JBTable filtreTable;
    private DefaultTableModel filtreTableModel;

    public PageRechercheForm() {
        scrollProperties.setBorder(BorderFactory.createEmptyBorder());
        scrollProperties.setViewportBorder(null);

        filtreTableModel = new DefaultTableModel(new Object[]{"Attribut", "Libellé"}, 0);
        filtreTable = new JBTable(filtreTableModel);
        scrollFiltre.setViewportView(filtreTable);
        scrollFiltre.setBorder(BorderFactory.createEmptyBorder());
        scrollFiltre.setViewportBorder(null);

        ToolbarDecorator decorator = ToolbarDecorator.createDecorator(filtreTable)
                .setAddAction(anActionButton -> { })
                .setRemoveAction(anActionButton -> {
                    int selectedRow = filtreTable.getSelectedRow();
                    if (selectedRow >= 0) {
                        filtreTableModel.removeRow(selectedRow);
                    }
                });
        JPanel decoratorPanel = decorator.createPanel();
        decoratorPanel.setBorder(BorderFactory.createEmptyBorder());
        decoratorPanel.setBorder(BorderFactory.createMatteBorder(0, 1, 1, 1, JBColor.border()));
        filtrePanel.removeAll();
        filtrePanel.setLayout(new BorderLayout());
        filtrePanel.add(decoratorPanel, BorderLayout.CENTER);
    }


}

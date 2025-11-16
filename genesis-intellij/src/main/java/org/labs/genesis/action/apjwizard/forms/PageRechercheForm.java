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
    private JScrollPane scrollRecap;
    private JScrollPane scrollTableau;
    private JBTable filtreTable;
    private JBTable recapTable;
    private JBTable tableauTable;
    private DefaultTableModel filtreTableModel;
    private DefaultTableModel recapTableModel;
    private DefaultTableModel tableauTableModel;

    public void createDecorator(JPanel panel, JBTable table, DefaultTableModel tableModel){
        ToolbarDecorator decorator = ToolbarDecorator.createDecorator(table)
                .setAddAction(anActionButton -> { })
                .setRemoveAction(anActionButton -> {
                    int selectedRow = table.getSelectedRow();
                    if (selectedRow >= 0) {
                        tableModel.removeRow(selectedRow);
                    }
                });
        JPanel decoPanel = decorator.createPanel();
        decoPanel.setBorder(BorderFactory.createEmptyBorder());
        decoPanel.setBorder(BorderFactory.createMatteBorder(0, 1, 1, 1, JBColor.border()));
        panel.removeAll();
        panel.setLayout(new BorderLayout());
        panel.add(decoPanel, BorderLayout.CENTER);
    }

    public PageRechercheForm() {
        if (scrollProperties != null) {
            scrollProperties.setBorder(BorderFactory.createEmptyBorder());
            scrollProperties.setViewportBorder(null);
        }

        // Table filtre
        filtreTableModel = new DefaultTableModel(new Object[]{"Champ", "Libellé"}, 0);
        filtreTable = new JBTable(filtreTableModel);
        scrollFiltre.setViewportView(filtreTable);
        scrollFiltre.setBorder(BorderFactory.createEmptyBorder());
        scrollFiltre.setViewportBorder(null);
        createDecorator(filtrePanel, filtreTable, filtreTableModel);

        // --- Recap ---
        recapTableModel = new DefaultTableModel(new Object[]{"Colonne", "Libellé"}, 0);
        recapTable = new JBTable(recapTableModel);
        scrollRecap.setViewportView(recapTable);
        scrollRecap.setBorder(BorderFactory.createEmptyBorder());
        scrollRecap.setViewportBorder(null);
        createDecorator(recapitulationPanel, recapTable, recapTableModel);

        // --- Tableau ---
        tableauTableModel = new DefaultTableModel(new Object[]{"Colonne", "Libellé"}, 0);
        tableauTable = new JBTable(tableauTableModel);
        scrollTableau.setViewportView(tableauTable);
        scrollTableau.setBorder(BorderFactory.createEmptyBorder());
        scrollTableau.setViewportBorder(null);
        createDecorator(tableauPanel, tableauTable, tableauTableModel);

    }


}

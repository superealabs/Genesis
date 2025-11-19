package org.labs.genesis.action.apjwizard.forms;

import com.intellij.openapi.actionSystem.*;
import com.intellij.ui.table.JBTable;
import lombok.Getter;
import lombok.Setter;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.*;
import java.awt.*;
import java.util.List;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.ide.util.TreeClassChooser;
import com.intellij.ide.util.TreeClassChooserFactory;
import org.jetbrains.annotations.NotNull;
import org.labs.genesis.action.apjwizard.forms.helper.TableToolbarHelper;
import org.labs.genesis.action.apjwizard.forms.popup.FieldSelectionDialog;
import org.labs.genesis.action.apjwizard.forms.popup.TableTreeChooser;

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
    private JButton chooseClassButton;
    private JPanel mappingPanel;
    private JButton chooseTableButton;
    private JBTable filtreTable;
    private JBTable recapTable;
    private JBTable tableauTable;
    private DefaultTableModel filtreTableModel;
    private DefaultTableModel recapTableModel;
    private DefaultTableModel tableauTableModel;
    private List<String> availableFiltreFields;
    private List<String> availableIntervalFields;
    private List<String> availableColSommeFields;
    private List<String> availableColonneFields;

    public PageRechercheForm() {
        initializeUI();
        initFiltreTable();
        initRecapTable();
        initTableauTable();
    }

    private void initializeUI() {
        if (scrollProperties != null) {
            scrollProperties.setBorder(BorderFactory.createEmptyBorder());
            scrollProperties.setViewportBorder(null);
        }
        chooseClassButton.setBorder(UIManager.getBorder("TextField.border"));
        chooseClassButton.setContentAreaFilled(true);
        chooseClassButton.setFocusPainted(true);
        chooseClassButton.setBackground(mappingField.getBackground());
        nomTableField.setEditable(false);
    }

    private void initFiltreTable() {
        filtreTableModel = new DefaultTableModel(new Object[]{"Champ", "Libellé"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 1;
            }
        };
        filtreTable = new JBTable(filtreTableModel);
        scrollFiltre.setViewportView(filtreTable);
        scrollFiltre.setBorder(BorderFactory.createEmptyBorder());
        scrollFiltre.setViewportBorder(null);
        DefaultActionGroup filtreGroup = new DefaultActionGroup();
        filtreGroup.add(new AnAction("Simple") {
            @Override public void actionPerformed(@NotNull AnActionEvent e) {
                showAddFieldsDialogAndAddRows(filtreTableModel);
            }
        });
        filtreGroup.add(new AnAction("Intervalle") {
            @Override public void actionPerformed(@NotNull AnActionEvent e) {
                showAddFieldsDialogAndAddRows(filtreTableModel);
            }
        });
        TableToolbarHelper.builder()
            .table(filtreTable)
            .panel(filtrePanel)
            .addActionGroup(filtreGroup)
            .removeAction(() -> removeSelectedRow(filtreTable, filtreTableModel))
            .build().init();
    }

    private void initRecapTable() {
        recapTableModel = new DefaultTableModel(new Object[]{"Colonne", "Libellé"}, 0);
        recapTable = new JBTable(recapTableModel);
        scrollRecap.setViewportView(recapTable);
        scrollRecap.setBorder(BorderFactory.createEmptyBorder());
        scrollRecap.setViewportBorder(null);
        TableToolbarHelper.builder()
            .table(recapTable)
            .panel(recapitulationPanel)
            .addAction((t) -> showAddFieldsDialogAndAddRows(recapTableModel))
            .removeAction(() -> removeSelectedRow(recapTable, recapTableModel))
            .build().init();
    }

    private void initTableauTable() {
        tableauTableModel = new DefaultTableModel(new Object[]{"Colonne", "Libellé","Lien"}, 0);
        tableauTable = new JBTable(tableauTableModel);
        scrollTableau.setViewportView(tableauTable);
        scrollTableau.setBorder(BorderFactory.createEmptyBorder());
        scrollTableau.setViewportBorder(null);
        TableToolbarHelper.builder()
            .table(tableauTable)
            .panel(tableauPanel)
            .addAction((t) -> showAddFieldsDialogAndAddRows(tableauTableModel))
            .removeAction(() -> removeSelectedRow(tableauTable, tableauTableModel))
            .build().init();
    }


    private void removeSelectedRow(JBTable table, DefaultTableModel model) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0) {
            model.removeRow(selectedRow);
        }
    }

    public void showClassChooser(Project project) {
        chooseClassButton.addActionListener(e -> {
            TreeClassChooser chooser = TreeClassChooserFactory.getInstance(project)
                    .createAllProjectScopeChooser("Select Class");
            chooser.showDialog();
            PsiClass selectedClass = chooser.getSelected();
            if (selectedClass != null) {
                mappingField.setText(selectedClass.getQualifiedName());
                // Code pour récupérer les champs de la classe et les stocker dans availableFields
            }
        });
    }

    public void showTableTree(List<String> tables, List<String> views) {
        TableTreeChooser chooser = new TableTreeChooser(mainPanel, tables, views);
        String table = chooser.showDialog();
        if (table != null) {
            nomTableField.setText(table);
        }
    }

    private void showAddFieldsDialogAndAddRows(DefaultTableModel tableModel) {
        FieldSelectionDialog dialog = new FieldSelectionDialog(mainPanel, availableFiltreFields);
        dialog.show();

        List<String> selectedFields = dialog.getSelected();
        if (selectedFields == null || selectedFields.isEmpty()) return;

        for (String field : selectedFields) {
            boolean exists = false;
            for (int r = 0; r < tableModel.getRowCount(); r++) {
                Object v = tableModel.getValueAt(r, 0);
                if (v != null && v.toString().equals(field)) {
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                tableModel.addRow(new Object[]{field, ""});
            }
        }
    }



}

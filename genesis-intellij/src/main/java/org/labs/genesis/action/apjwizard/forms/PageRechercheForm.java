package org.labs.genesis.action.apjwizard.forms;

import com.intellij.openapi.actionSystem.*;
import com.intellij.ui.table.JBTable;
import lombok.Getter;
import lombok.Setter;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.lang.reflect.Field;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.ide.util.TreeClassChooser;
import com.intellij.ide.util.TreeClassChooserFactory;
import org.jetbrains.annotations.NotNull;
import org.labs.genesis.action.apjwizard.forms.helper.TableToolbarHelper;
import org.labs.genesis.action.apjwizard.forms.popup.FieldSelectionDialog;
import org.labs.genesis.action.apjwizard.forms.popup.TableTreeChooser;
import org.labs.genesis.action.apjwizard.forms.renderer.TableRenderer;
import org.labs.genesis.action.apjwizard.forms.tablehandler.TableRowTransferHandler;
import org.labs.genesis.action.apjwizard.forms.tablehandler.TableauTableModel;
import org.labs.genesis.apj.ApjGenerationContext;
import org.labs.genesis.apj.utilitaire.ApjField;
import org.labs.genesis.apj.utilitaire.UtilClassLoader;
import org.labs.utils.StringUtils;

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
    private LinkedHashMap<String, ApjField> availableRecapFieldsMap = new LinkedHashMap<>();
    private LinkedHashMap<String, ApjField> availableTabFieldsMap = new LinkedHashMap<>();
    private List<ApjField> apjFields;

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
        filtreTableModel = new TableauTableModel(new Object[]{"Champ", "Libellé"});
        filtreTable = new JBTable(filtreTableModel);
        filtreTable.setDefaultRenderer(Object.class, new TableRenderer());
        scrollFiltre.setViewportView(filtreTable);
        scrollFiltre.setBorder(BorderFactory.createEmptyBorder());
        scrollFiltre.setViewportBorder(null);
        DefaultActionGroup filtreGroup = new DefaultActionGroup();
        filtreGroup.add(new AnAction("Simple") {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {

            }
        });
        filtreGroup.add(new AnAction("Intervalle") {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {

            }
        });
        TableToolbarHelper.builder()
            .table(filtreTable)
            .panel(filtrePanel)
            .addActionGroup(filtreGroup)
            .build().init();
    }

    private void initRecapTable() {
        recapTableModel = new TableauTableModel(new Object[]{"Colonne", "Libellé"});
        recapTable = new JBTable(recapTableModel);
        recapTable.setDefaultRenderer(Object.class, new TableRenderer());
        scrollRecap.setViewportView(recapTable);
        scrollRecap.setBorder(BorderFactory.createEmptyBorder());
        scrollRecap.setViewportBorder(null);
        TableToolbarHelper.builder()
            .table(recapTable)
            .panel(recapitulationPanel)
            .addAction((t) -> showAddFieldsRecapAndAddRows(recapTableModel))
            .removeAction(() -> removeSelectedRow(recapTable, recapTableModel, availableRecapFieldsMap))
            .build().init();
    }

    private void showAddFieldsRecapAndAddRows(DefaultTableModel tableModel) {
        FieldSelectionDialog dialog = new FieldSelectionDialog(mainPanel, new ArrayList<>(availableRecapFieldsMap.keySet()));
        dialog.show();
        List<String> selectedFields = dialog.getSelected();
        if (selectedFields == null || selectedFields.isEmpty()) return;

        for (String fieldName : selectedFields) {
            ApjField field = availableRecapFieldsMap.remove(fieldName);
            if (field == null) continue;
            tableModel.addRow(new Object[]{fieldName, "Somme de " + fieldName});
        }
    }

    private void initTableauTable() {
        tableauTableModel = new TableauTableModel(new Object[]{"Colonne", "Libellé", "Lien", "AttLien"});
        tableauTable = new JBTable(tableauTableModel);
        tableauTable.setDefaultRenderer(Object.class, new TableRenderer());
        tableauTable.setDragEnabled(true);
        tableauTable.setDropMode(DropMode.INSERT_ROWS);
        tableauTable.setTransferHandler(new TableRowTransferHandler(tableauTable));
        scrollTableau.setViewportView(tableauTable);
        scrollTableau.setBorder(BorderFactory.createEmptyBorder());
        scrollTableau.setViewportBorder(null);
        TableToolbarHelper.builder()
            .table(tableauTable)
            .panel(tableauPanel)
            .addAction((t) -> showAddFieldsTabAndAddRows(tableauTableModel))
            .removeAction(() -> removeSelectedRow(tableauTable, tableauTableModel, availableTabFieldsMap))
            .build().init();
    }

    private void showAddFieldsTabAndAddRows(DefaultTableModel tableModel) {
        FieldSelectionDialog dialog = new FieldSelectionDialog(mainPanel, new ArrayList<>(availableTabFieldsMap.keySet()));
        dialog.show();
        List<String> selectedFields = dialog.getSelected();
        if (selectedFields == null || selectedFields.isEmpty()) return;

        for (String fieldName : selectedFields) {
            ApjField field = availableTabFieldsMap.remove(fieldName);
            if (field == null) continue;
            tableModel.addRow(new Object[]{fieldName, StringUtils.majStart(fieldName),null,fieldName});
        }
    }

    private void removeSelectedRow(JBTable table, DefaultTableModel model, LinkedHashMap<String, ApjField> map) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) return;

        String fieldName = table.getValueAt(selectedRow, 0).toString();
        ApjField apjField = new ApjField();
        apjField.setNom(fieldName);
        map.put(fieldName, apjField);
        model.removeRow(selectedRow);
    }

    public void showClassChooser(Project project, ApjGenerationContext context) {
        chooseClassButton.addActionListener(e -> {
            TreeClassChooser chooser = TreeClassChooserFactory.getInstance(project)
                    .createAllProjectScopeChooser("Select Class");
            chooser.showDialog();
            PsiClass selectedClass = chooser.getSelected();
            if (selectedClass != null) {
                mappingField.setText(selectedClass.getQualifiedName());
                try {
                    URLClassLoader loader = UtilClassLoader.buildLoader(context.getProjectJarDir(), context.getLibDir());
                    Class<?> cls = loader.loadClass(mappingField.getText());
                    List<Field> fields = UtilClassLoader.listFieldsStopClassMAPTable(cls);
                    List<ApjField> apjFields = ApjField.javaFieldsToApjFields(fields);
                    loadAvailableRecapFields(apjFields);
                    loadAvailableTabFields(apjFields);
                } catch (Exception ignored) {

                }
            }
        });
    }

    private void loadAvailableRecapFields(List<ApjField> fields) {
        availableRecapFieldsMap.clear();
        for (ApjField field : fields) {
            if (field.isSummable()) {
                availableRecapFieldsMap.put(field.getNom(), field);
            }
        }
    }

    private void loadAvailableTabFields(List<ApjField> fields) {
        availableTabFieldsMap.clear();
        for (ApjField field : fields) {
            availableTabFieldsMap.put(field.getNom(), field);
        }
    }

    public void showTableTree(String[] tables, String[] views) {
        TableTreeChooser chooser = new TableTreeChooser(mainPanel, tables, views);
        String table = chooser.showDialog();
        if (table != null) {
            nomTableField.setText(table);
        }
    }
}

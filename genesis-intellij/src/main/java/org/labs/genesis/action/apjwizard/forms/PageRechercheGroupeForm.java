package org.labs.genesis.action.apjwizard.forms;

import com.intellij.ide.util.TreeClassChooser;
import com.intellij.ide.util.TreeClassChooserFactory;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.ui.table.JBTable;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.labs.genesis.action.apjwizard.forms.helper.TableToolbarHelper;
import org.labs.genesis.action.apjwizard.forms.popup.FieldSelectionDialog;
import org.labs.genesis.action.apjwizard.forms.popup.ListDetailsDialog;
import org.labs.genesis.action.apjwizard.forms.popup.ListeStringDialog;
import org.labs.genesis.action.apjwizard.forms.popup.TableTreeChooser;
import org.labs.genesis.action.apjwizard.forms.renderer.TableRenderer;
import org.labs.genesis.action.apjwizard.forms.tablehandler.BasicTableModel;
import org.labs.genesis.action.apjwizard.forms.tablehandler.FilterTableModel;
import org.labs.genesis.action.apjwizard.forms.tablehandler.TableRowTransferHandler;
import org.labs.genesis.action.apjwizard.forms.tablehandler.TableauTableModel;
import org.labs.genesis.apj.ApjGenerationContext;
import org.labs.genesis.apj.component.ApjField;
import org.labs.genesis.apj.utilitaire.ConstantesApj;
import org.labs.genesis.apj.utilitaire.UtilClassLoader;
import org.labs.genesis.config.langage.generator.project.LlmApiClient;
import org.labs.utils.StringUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.lang.reflect.Field;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

@Getter
@Setter
public class PageRechercheGroupeForm {
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
    private JComboBox comboBox1;
    private JBTable filtreTable;
    private JBTable recapTable;
    private JBTable tableauTable;
    private DefaultTableModel filtreTableModel;
    private DefaultTableModel recapTableModel;
    private DefaultTableModel tableauTableModel;
    private List<String> availableFiltreFields;
    private LinkedHashMap<String, ApjField> allFieldsMap = new LinkedHashMap<>();
    private LinkedHashMap<String, ApjField> availableRecapFieldsMap = new LinkedHashMap<>();
    private LinkedHashMap<String, ApjField> availableTabFieldsMap = new LinkedHashMap<>();
    private LinkedHashMap<String, ApjField> availableFilterFieldsMap = new LinkedHashMap<>();
    private LinkedHashMap<String, ApjField> availableBetweenFilterFieldsMap = new LinkedHashMap<>();
    private LinkedHashMap<String, ApjField> availableListFilterFieldsMap = new LinkedHashMap<>();
    private LinkedHashMap<String, ApjField> availableListStringFilterFieldsMap = new LinkedHashMap<>();
    private LinkedHashMap<String, ApjField> availableOuiNonFilterFieldsMap = new LinkedHashMap<>();
    private List<ApjField> apjFields;
    private ApjField[] dataFiltre;
    private ApjField[] dataRecap;
    private ApjField[] dataTableau;
    private String[] listeCrt;
    private String[] listeInt;
    private final ApjGenerationContext context;
    private final Project project;

    public PageRechercheGroupeForm(ApjGenerationContext context, Project project) {
        this.context = context;
        this.project = project;
        initializeUI();
        
        initFiltreTable();
        initRecapTable();
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
    }

    private JBTable initTable(JBTable table,DefaultTableModel tableModel,JScrollPane scroll){
        table = new JBTable(tableModel);
        table.setDefaultRenderer(Object.class, new TableRenderer());
        table.setDragEnabled(true);
        table.setDropMode(DropMode.INSERT_ROWS);
        table.setTransferHandler(new TableRowTransferHandler(table));
        scroll.setViewportView(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setViewportBorder(null);
        return table;
    }

    private void fixSizeColumn(JBTable table,int row,int size) {
        table.getColumnModel().getColumn(row).setPreferredWidth(size);
        table.getColumnModel().getColumn(row).setMinWidth(size);
        table.getColumnModel().getColumn(row).setMaxWidth(size);
    }

    private void addUpdateGroup(DefaultActionGroup group, String name,DefaultTableModel tableModel) {
        group.add(new AnAction(name) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                showAddFieldsFilterAndAddRows(name,tableModel);
            }
        });
    }

    private void initFiltreTable() {
        filtreTableModel = new FilterTableModel(new Object[]{"Champ", "Libellé", "Type","Détails"});
        filtreTable = initTable(filtreTable, filtreTableModel, scrollFiltre);
        filtreTable.getEmptyText().setText("Aucune ligne à afficher");
        filtreTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        fixSizeColumn(filtreTable,0,130);
        fixSizeColumn(filtreTable,1,140);
        fixSizeColumn(filtreTable,2,100);

        DefaultActionGroup filtreGroup = new DefaultActionGroup();
        addUpdateGroup(filtreGroup,ConstantesApj.SIMPLE,filtreTableModel);
        addUpdateGroup(filtreGroup,ConstantesApj.INTERVALLE,filtreTableModel);
        addUpdateGroup(filtreGroup,ConstantesApj.LISTE,filtreTableModel);
        addUpdateGroup(filtreGroup,ConstantesApj.LISTE_STRING,filtreTableModel);
        addUpdateGroup(filtreGroup,ConstantesApj.OUI_NON,filtreTableModel);

        TableToolbarHelper.builder()
            .table(filtreTable)
            .panel(filtrePanel)
            .addActionGroup(filtreGroup)
            .removeAction(() -> removeSelectedFilterRow(filtreTable, filtreTableModel))
            .customButtonText("Générer les libellés via l'IA")
            .customButtonAction(() -> askAI(filtreTableModel, filtreTable))
            .build().init();
    }

    private void showAddFieldsFilterAndAddRows(String type,DefaultTableModel tableModel) {
        List<String> selectedFields = getSelectedFields(type);
        if (selectedFields == null || selectedFields.isEmpty()) return;

        String details = null;
        boolean withDetail = false;
        if (type.equalsIgnoreCase(ConstantesApj.LISTE)){
            ListDetailsDialog listeDialog = new ListDetailsDialog(mainPanel, context, project);
            listeDialog.showDialog();
            String result = listeDialog.getDetails();
            if (result == null) return;
            details = result;
            withDetail = true;
        } else if (type.equalsIgnoreCase(ConstantesApj.LISTE_STRING)){
            ListeStringDialog listeStringDialog = new ListeStringDialog(mainPanel);
            listeStringDialog.showDialog();
            String result = listeStringDialog.getDetails();
            if (result == null) return;
            details = result;
            withDetail = true;
        }

        for (String fieldName : selectedFields) {
            availableBetweenFilterFieldsMap.remove(fieldName);
            availableOuiNonFilterFieldsMap.remove(fieldName);
            availableListStringFilterFieldsMap.remove(fieldName);
            availableListFilterFieldsMap.remove(fieldName);
            ApjField field = availableFilterFieldsMap.remove(fieldName);
            if (field == null) continue;
            if (type.equalsIgnoreCase(ConstantesApj.INTERVALLE)) {
                tableModel.addRow(new Object[]{fieldName+"1", StringUtils.majStart(fieldName)+" min",type});
                tableModel.addRow(new Object[]{fieldName+"2", StringUtils.majStart(fieldName)+" max",type});
                continue;
            }
            if (withDetail) {
                tableModel.addRow(new Object[]{fieldName, StringUtils.majStart(fieldName),details,type});
                continue;
            }
            tableModel.addRow(new Object[]{fieldName, StringUtils.majStart(fieldName),type});
        }
    }

    private List<String> getSelectedFields(String type) {
        LinkedHashMap<String, ApjField> map = availableFilterFieldsMap;
        switch (type){
            case ConstantesApj.INTERVALLE -> map = availableBetweenFilterFieldsMap;
            case ConstantesApj.OUI_NON -> map = availableOuiNonFilterFieldsMap;
            case ConstantesApj.LISTE -> map = availableListFilterFieldsMap;
            case ConstantesApj.LISTE_STRING -> map = availableListStringFilterFieldsMap;
        }
        FieldSelectionDialog dialog = new FieldSelectionDialog(mainPanel, new ArrayList<>(map.keySet()));
        dialog.show();
        return dialog.getSelected();
    }

    private void removeSelectedFilterRow(JBTable table, DefaultTableModel model) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) return;

        String fieldName = table.getValueAt(selectedRow, 2).toString();
        if ((fieldName.endsWith("1") || fieldName.endsWith("2"))) {
            String baseName = fieldName.substring(0, fieldName.length() - 1);
            if (allFieldsMap.containsKey(baseName)) {
                availableBetweenFilterFieldsMap.put(baseName, allFieldsMap.get(baseName));
                availableFilterFieldsMap.put(baseName, allFieldsMap.get(baseName));
                availableOuiNonFilterFieldsMap.put(baseName, allFieldsMap.get(baseName));
                availableListFilterFieldsMap.put(baseName, allFieldsMap.get(baseName));
                availableListStringFilterFieldsMap.put(baseName, allFieldsMap.get(baseName));
                for (int i = model.getRowCount() - 1; i >= 0; i--) {
                    Object v = model.getValueAt(i, 0);
                    if (v == null) continue;
                    String name = v.toString();
                    if (name.equals(baseName + "1") || name.equals(baseName + "2")) {
                        model.removeRow(i);
                    }
                }
                return;
            }
        }
        ApjField apjField = new ApjField();
        apjField.setNom(fieldName);
        availableFilterFieldsMap.put(fieldName, apjField);
        availableBetweenFilterFieldsMap.put(fieldName, apjField);
        availableOuiNonFilterFieldsMap.put(fieldName, apjField);
        availableListFilterFieldsMap.put(fieldName, apjField);
        availableListStringFilterFieldsMap.put(fieldName, apjField);
        model.removeRow(selectedRow);
    }


    private void initRecapTable() {
        recapTableModel = new BasicTableModel(new Object[]{"Colonne", "Libellé"});
        recapTable = initTable(recapTable, recapTableModel, scrollRecap);
        recapTable.getEmptyText().setText("Aucune ligne à afficher");
        recapTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        fixSizeColumn(recapTable,0,170);
        TableToolbarHelper.builder()
            .table(recapTable)
            .panel(recapitulationPanel)
            .addAction((t) -> showAddFieldsRecapAndAddRows(recapTableModel))
            .removeAction(() -> removeSelectedRow(recapTable, recapTableModel, availableRecapFieldsMap))
            .customButtonText("Générer les libellés via l'IA")
            .customButtonAction(() -> askAI(recapTableModel, recapTable))
            .build().init();
    }

    private void askAI(DefaultTableModel model, JBTable table) {
        int[] selectedRows = table.getSelectedRows();
        ApjField[] fields = new ApjField[selectedRows.length];
        for (int i = 0; i < selectedRows.length; i++) {
            String nom = String.valueOf(model.getValueAt(selectedRows[i], 0));
            fields[i] = new ApjField();
            fields[i].setNom(nom);
        }
        String mapping = this.getMappingField().getText();
        LlmApiClient llmClient = new LlmApiClient();
        String[] libelles = new String[selectedRows.length];
        try {
            libelles = llmClient.askForLabel(mapping, fields,ConstantesApj.STANDARD);
        } catch (Exception ignored) {

        }
        for (int i = 0; i < libelles.length; i++) {
            model.setValueAt(libelles[i], selectedRows[i], 1);
        }
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
        tableauTable = initTable(tableauTable, tableauTableModel, scrollTableau);
        tableauTable.getEmptyText().setText("Aucune ligne à afficher");
        tableauTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        fixSizeColumn(tableauTable,0,130);
        fixSizeColumn(tableauTable,1,140);
        fixSizeColumn(tableauTable,2,150);
        TableToolbarHelper.builder()
            .table(tableauTable)
            .panel(tableauPanel)
            .addAction((t) -> showAddFieldsTabAndAddRows(tableauTableModel))
            .removeAction(() -> removeSelectedRow(tableauTable, tableauTableModel, availableTabFieldsMap))
            .customButtonText("Générer les libellés via l'IA")
            .customButtonAction(() -> askAI(tableauTableModel, tableauTable))
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
                    loadFieldsMap(apjFields);
                    removeAllRows(filtreTableModel);
                    removeAllRows(recapTableModel);
                    removeAllRows(tableauTableModel);
                } catch (Exception ignored) {

                }
            }
        });
    }

    private void removeAllRows(DefaultTableModel model) {
        model.setRowCount(0);
    }

    private void loadFieldsMap(List<ApjField> fields) {
        allFieldsMap.clear();
        availableFilterFieldsMap.clear();
        availableBetweenFilterFieldsMap.clear();
        availableRecapFieldsMap.clear();
        availableTabFieldsMap.clear();
        availableListFilterFieldsMap.clear();
        availableListStringFilterFieldsMap.clear();
        availableOuiNonFilterFieldsMap.clear();

        for (ApjField field : fields) {
            allFieldsMap.put(field.getNom(), field);
            availableFilterFieldsMap.put(field.getNom(), field);
            availableTabFieldsMap.put(field.getNom(), field);
            availableListFilterFieldsMap.put(field.getNom(), field);
            availableListStringFilterFieldsMap.put(field.getNom(), field);
            if (field.isRangeable()) {
                availableBetweenFilterFieldsMap.put(field.getNom(), field);
            }
            if (field.isSummable()) {
                availableRecapFieldsMap.put(field.getNom(), field);
                availableOuiNonFilterFieldsMap.put(field.getNom(), field);

            }
        }
    }

    public void showTableTree(String[] tables, String[] views) {
        TableTreeChooser chooser = new TableTreeChooser(mainPanel, tables, views);
        String table = chooser.showDialog();
        if (table != null) {
            nomTableField.setText(table);
        }
    }

    public void fillDataTables(){
        this.setDataTableau();
        this.setDataFiltre();
        this.setDataRecap();
    }

    private ApjField[] getDataTable(JTable table) {
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        int rowCount = model.getRowCount();
        ApjField[] fields = new ApjField[rowCount];
        for (int i = 0; i < rowCount; i++) {
            String nom = String.valueOf(model.getValueAt(i, 0));
            String libelle = String.valueOf(model.getValueAt(i, 1));
            ApjField f = new ApjField();
            f.setNom(nom);
            f.setLibelle(libelle);
            fields[i] = f;
        }
        return fields;
    }


    private void setDataRecap(){
        this.dataRecap = getDataTable(recapTable);
    }

    private void setDataFiltre(){
        List<String> listeCrt = new ArrayList<>();
        List<String> listeInt = new ArrayList<>();
        DefaultTableModel model = (DefaultTableModel) filtreTable.getModel();
        int rowCount = model.getRowCount();
        ApjField[] fields = new ApjField[rowCount];
        for (int i = 0; i < rowCount; i++) {
            String type = String.valueOf(model.getValueAt(i, 2));
            String nom = String.valueOf(model.getValueAt(i, 0));
            String libelle = String.valueOf(model.getValueAt(i, 1));
            String details = String.valueOf(model.getValueAt(i, 3));
            ApjField f = new ApjField();
            f.setNom(nom);
            f.setLibelle(libelle);
            f.setType(type);
            f.setDetails(details);
            fields[i] = f;
            String baseName = nom.substring(0, nom.length() - 1);
            if (nom.endsWith("2") && allFieldsMap.containsKey(baseName)) continue;
            if (nom.endsWith("1") && allFieldsMap.containsKey(baseName)) {
                listeCrt.add(baseName);
                listeInt.add(baseName);
            } else {
                listeCrt.add(nom);
            }
        }
        this.listeCrt = listeCrt.toArray(new String[0]);
        this.listeInt = listeInt.toArray(new String[0]);
        this.dataFiltre = fields;
    }

    private void setDataTableau(){
        DefaultTableModel model = (DefaultTableModel) tableauTable.getModel();
        int rowCount = model.getRowCount();
        ApjField[] fields = new ApjField[rowCount];
        for (int i = 0; i < rowCount; i++) {
            String nom = String.valueOf(model.getValueAt(i, 0));
            String libelle = String.valueOf(model.getValueAt(i, 1));
            String lien = String.valueOf(model.getValueAt(i, 2));
            String attLien = String.valueOf(model.getValueAt(i, 3));
            ApjField f = new ApjField();
            f.setNom(nom);
            f.setLibelle(libelle);
            f.setLien(lien);
            f.setAttLien(attLien);
            fields[i] = f;
        }
        this.dataTableau = fields;
    }

}

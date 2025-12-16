package org.labs.genesis.action.apjwizard.forms;

import com.intellij.ide.util.TreeClassChooser;
import com.intellij.ide.util.TreeClassChooserFactory;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.ui.table.JBTable;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.labs.genesis.action.apjwizard.forms.helper.ProgressUtils;
import org.labs.genesis.action.apjwizard.forms.helper.TableToolbarHelper;
import org.labs.genesis.action.apjwizard.forms.popup.*;
import org.labs.genesis.action.apjwizard.forms.renderer.TableRenderer;
import org.labs.genesis.action.apjwizard.forms.tablehandler.BasicTableModel;
import org.labs.genesis.action.apjwizard.forms.tablehandler.FilterTableModel;
import org.labs.genesis.action.apjwizard.forms.tablehandler.TableRowTransferHandler;
import org.labs.genesis.apj.ApjGenerationContext;
import org.labs.genesis.apj.component.ApjField;
import org.labs.genesis.apj.utilitaire.ConstantesApj;
import org.labs.genesis.apj.utilitaire.Database;
import org.labs.genesis.apj.utilitaire.UtilClassLoader;
import org.labs.genesis.apj.utilitaire.UtilDBDynamique;
import org.labs.genesis.config.langage.generator.project.LlmApiClient;
import org.labs.utils.StringUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.lang.reflect.Field;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.util.*;

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
    private JTabbedPane tabPane;
    private JPanel filtrePanel;
    private JPanel recapitulationPanel;
    private JScrollPane scrollFiltre;
    private JScrollPane scrollRecap;
    private JButton chooseClassButton;
    private JPanel mappingPanel;
    private JButton chooseTableButton;
    private JComboBox<String> colGrColField;
    private JComboBox<String> colGrField;
    private JTextField colGrColLien;
    private JBTable filtreTable;
    private JBTable recapTable;
    private DefaultTableModel filtreTableModel;
    private DefaultTableModel recapTableModel;
    private List<String> availableFiltreFields;
    private LinkedHashMap<String, ApjField> allInitialFieldsMap = new LinkedHashMap<>();
    private LinkedHashMap<String, ApjField> allFieldsMap = new LinkedHashMap<>();
    private LinkedHashMap<String, ApjField> allFieldsDataBaseMap = new LinkedHashMap<>();
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
    private String[] listeCrt;
    private String[] listeInt;
    private final ApjGenerationContext context;
    private final Project project;
    private enum ColonneFiltre {
        CHAMP(0),
        LIBELLE(1),
        TYPE(2),
        DETAILS(3);
        final int index;
        ColonneFiltre(int index) {
            this.index = index;
        }
    }
    private enum ColonneSom {
        COLONNE(0),
        LIBELLE(1);
        final int index;
        ColonneSom(int index) {
            this.index = index;
        }
    }

    public PageRechercheGroupeForm(ApjGenerationContext context, Project project) {
        this.context = context;
        this.project = project;
        initFiltreTable();
        initRecapTable();
        addActionListenerOnClassButton();
        addActionListenerOnTableButton();
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
            .customButtonAction(() -> askAI(false))
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
                tableModel.addRow(new Object[]{fieldName, StringUtils.majStart(fieldName),type,details});
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
            .removeAction(this::removeSelectedRow)
            .customButtonText("Générer les libellés via l'IA")
            .customButtonAction(() -> askAI(true))
            .build().init();
    }

    private void askAI(boolean isSomDefaut) {
        try {
            ProgressUtils.runWithProgress(project, "Traitement de la demande par l'IA…", indicator -> {
                DefaultTableModel model = filtreTableModel;
                JBTable table = filtreTable;
                String type = ConstantesApj.FILTRE;
                if (isSomDefaut) {
                    table = recapTable;
                    model = recapTableModel;
                    type = ConstantesApj.RECAP_GROUPE;
                }
                int[] selectedRows = table.getSelectedRows();
                ApjField[] fields = new ApjField[selectedRows.length];
                for (int i = 0; i < selectedRows.length; i++) {
                    String nom = String.valueOf(model.getValueAt(selectedRows[i], ColonneFiltre.CHAMP.index));
                    fields[i] = new ApjField();
                    if (isSomDefaut){
                        fields[i].setNom(nom + " (" + (selectedRows[i] + 1) + ")");
                    } else {
                        fields[i].setNom(nom);
                    }
                }
                String mapping = this.getMappingField().getText();
                LlmApiClient llmClient = new LlmApiClient();
                String[] libelles = llmClient.askForLabel(mapping, fields,type);
                for (int i = 0; i < libelles.length; i++) {
                    model.setValueAt(libelles[i], selectedRows[i], ColonneFiltre.LIBELLE.index);
                }
            });
        } catch (Exception e) {
            PopUtils.showError(mainPanel, "Erreur lors de la communication avec l'IA : " + e.getMessage());
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
            tableModel.addRow(new Object[]{fieldName,StringUtils.majStart(fieldName)});
        }
    }

    private void removeSelectedRow() {
        int selectedRow = recapTable.getSelectedRow();
        if (selectedRow < 0) return;

        String fieldName = recapTable.getValueAt(selectedRow, ColonneSom.COLONNE.index).toString();
        ApjField apjField = new ApjField();
        apjField.setNom(fieldName);
        availableRecapFieldsMap.put(fieldName, apjField);
        recapTableModel.removeRow(selectedRow);
    }

    public void addActionListenerOnClassButton() {
        chooseClassButton.setToolTipText("Cliquez pour sélectionner une classe Java du projet");
        chooseClassButton.addActionListener(e -> handleClassSelection());
    }

    public void handleClassSelection() {
        try {
            TreeClassChooser chooser = TreeClassChooserFactory.getInstance(project)
                    .createAllProjectScopeChooser("Sélectionner une classe");
            chooser.showDialog();
            PsiClass selectedClass = chooser.getSelected();
            if (selectedClass == null) return;
            String className = selectedClass.getQualifiedName();
            mappingField.setText(className);
            loadMapping(className);
        } catch (Exception e) {
            PopUtils.showError(mainPanel, "Erreur de chargement du mapping Java : " + e.getMessage());
        }
    }

    private void loadMapping(String className) throws Exception {
        ProgressUtils.runWithProgress(project, "Chargement du Mapping Java...", indicator -> {
            URLClassLoader loader = UtilClassLoader.buildLoader(context.getProjectJarDir(), context.getLibDir());

            ProgressUtils.updateProgress(indicator, "Classe Java en cours de chargement...", 0.3);
            Class<?> cls = loader.loadClass(className);
            List<Field> fields = UtilClassLoader.listFieldsStopOnSuperClassApj(cls);
            List<ApjField> apjFields = ApjField.javaFieldsToApjFields(fields);

            ProgressUtils.updateProgress(indicator, "Mise à jour des champs disponibles...", 0.85);
            ApplicationManager.getApplication().invokeAndWait(() -> {
                loadCombo(apjFields);
                loadFieldsMap(apjFields);
                removeDataPage();
            });
            ProgressUtils.updateProgress(indicator, "Terminé", 1.0);
        });
    }

    private void removeDataPage(){
        filtreTableModel.setRowCount(0);
        recapTableModel.setRowCount(0);
    }

    private void loadFieldsMap(List<ApjField> fields){
        allFieldsMap.clear();
        allInitialFieldsMap.clear();
        availableFilterFieldsMap.clear();
        availableBetweenFilterFieldsMap.clear();
        availableRecapFieldsMap.clear();
        availableTabFieldsMap.clear();
        availableListFilterFieldsMap.clear();
        availableListStringFilterFieldsMap.clear();
        availableOuiNonFilterFieldsMap.clear();

        for (ApjField field : fields) {
            allFieldsMap.put(field.getNom(), field);
            allInitialFieldsMap.put(field.getNom(), field);
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

    private void loadCombo(List<ApjField> fields) {
        List<String> fieldNames = fields.stream().map(ApjField::getNom).toList();
        colGrColField.removeAllItems();
        colGrField.removeAllItems();
        for (String f : fieldNames) {
            colGrColField.addItem(f);
            colGrField.addItem(f);
        }
    }

    public void addActionListenerOnTableButton(){
        getChooseTableButton().addActionListener(e -> showTableTree());
    }

    public void showTableTree() {
        try {
            String[] tables = context.getTables();
            String[] views = context.getVues();
            TableTreeChooser chooser = new TableTreeChooser(mainPanel, tables, views);
            String table = chooser.showDialog();
            if (table == null) return;
            nomTableField.setText(table);
            loadTableColumns(table);
        } catch (Exception e) {
            PopUtils.showError(mainPanel, "Erreur de chargement des colonnes : " + e.getMessage());
        }
    }

    private void loadTableColumns(String table) throws Exception {
        ProgressUtils.runWithProgress(project, "Chargement des colonnes de \""+table+"\"...", indicator -> {
            try (Connection conn = UtilDBDynamique.GetConn(context.getProjectJarDir(), context.getLibDir())) {
                List<ApjField> fields = Database.getTableColumns(conn, table);

                ProgressUtils.updateProgress(indicator, "Mise à jour du tableau...", 0.85);
                ApplicationManager.getApplication().invokeAndWait(() -> loadAllFieldsBase(fields));

                ProgressUtils.updateProgress(indicator, "Terminé", 1.0);
            }
        });
    }

    private void loadAllFieldsBase(List<ApjField> fields) {
        allFieldsDataBaseMap.clear();
        for (ApjField field : fields) {
            allFieldsDataBaseMap.put(field.getNom(), field);
        }
        List<ApjField> allFieldsReinit = new ArrayList<>(allInitialFieldsMap.values());
        loadFieldsMap(allFieldsReinit);
        removeNonCommun(allFieldsMap, allFieldsDataBaseMap);
        List<ApjField> newFields = new ArrayList<>(allFieldsMap.values());
        loadCombo(newFields);
        removeDataPage();
    }

    private void removeNonCommun(LinkedHashMap<String, ApjField> allFields,LinkedHashMap<String, ApjField> baseMap){
        Map<String, String> champsJava = new HashMap<>();
        for (String nom : allFields.keySet()) {
            champsJava.put(nom.toLowerCase(), nom);
        }
        Map<String, String> champsBase = new HashMap<>();
        for (String nom : baseMap.keySet()) {
            champsBase.put(nom.toLowerCase(), nom);
        }
        Set<String> communs = new HashSet<>(champsJava.keySet());
        communs.retainAll(champsBase.keySet());
        Set<String> toRemove = new HashSet<>();
        for (String nomLower : champsJava.keySet()) {
            if (!communs.contains(nomLower)) {
                toRemove.add(champsJava.get(nomLower));
            }
        }
        removeKeysFromAllMaps(toRemove);
    }

    private void removeKeysFromAllMaps(Set<String> keys) {
        List<Map<String, ApjField>> maps = List.of(
            allFieldsMap,
            availableRecapFieldsMap,
            availableTabFieldsMap,
            availableFilterFieldsMap,
            availableBetweenFilterFieldsMap,
            availableListFilterFieldsMap,
            availableListStringFilterFieldsMap,
            availableOuiNonFilterFieldsMap
        );
        for (Map<String, ApjField> map : maps) {
            keys.forEach(map::remove);
        }
    }

    public void fillDataTables(){
        this.setDataFiltre();
        this.setDataRecap();
    }

    private ApjField[] getDataTable(JTable table) {
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        int rowCount = model.getRowCount();
        ApjField[] fields = new ApjField[rowCount];
        for (int i = 0; i < rowCount; i++) {
            String nom = String.valueOf(model.getValueAt(i, ColonneSom.COLONNE.index));
            String libelle = String.valueOf(model.getValueAt(i, ColonneSom.LIBELLE.index));
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
            String type = String.valueOf(model.getValueAt(i, ColonneFiltre.TYPE.index));
            String nom = String.valueOf(model.getValueAt(i, ColonneFiltre.CHAMP.index));
            String libelle = String.valueOf(model.getValueAt(i, ColonneFiltre.LIBELLE.index));
            String details = String.valueOf(model.getValueAt(i, ColonneFiltre.DETAILS.index));
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

}

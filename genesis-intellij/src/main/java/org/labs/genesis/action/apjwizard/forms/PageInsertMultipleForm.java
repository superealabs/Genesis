package org.labs.genesis.action.apjwizard.forms;

import com.intellij.ide.util.TreeClassChooser;
import com.intellij.ide.util.TreeClassChooserFactory;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.application.ApplicationManager;
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
import org.labs.genesis.action.apjwizard.forms.tablehandler.InsertTableModel;
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
public class PageInsertMultipleForm {
    private JPanel mainPanel;
    private JTextField nomField;
    private JTextField mappingField;
    private JTextField nomTableField;
    private JTextField titreUpdateField;
    private JLabel nomLabel;
    private JPanel generalPanel;
    private JLabel nomTableLabel;
    private JLabel titreLabel;
    private JPanel propertiesPanel;
    private JTabbedPane tabPane;
    private JPanel filtrePanel;
    private JScrollPane scrollFiltre;
    private JButton chooseClassButton;
    private JPanel mappingPanel;
    private JButton chooseTableButton;
    private JTextField titreField;
    private JTextField colonneMereField;
    private JTextField nomTableFilleField;
    private JTextField mappingFilleField;
    private JButton chooseClassFilleButton;
    private JButton chooseTableFilleButton;
    private JScrollPane scrollFille;
    private JPanel fillePanel;
    private JBTable formTable;
    private JBTable formFilleTable;
    private DefaultTableModel formTableModel;
    private DefaultTableModel formFilleTableModel;
    private ApjField[] dataForm;
    private ApjField[] dataFormFille;
    private final ApjGenerationContext context;
    private final Project project;
    private LinkedHashMap<String, ApjField> allFieldsMap = new LinkedHashMap<>();
    private LinkedHashMap<String, ApjField> allInitialFieldsMap = new LinkedHashMap<>();
    private LinkedHashMap<String, ApjField> allFieldsDataBaseMap = new LinkedHashMap<>();
    private LinkedHashMap<String, ApjField> allFieldsFilleMap = new LinkedHashMap<>();
    private LinkedHashMap<String, ApjField> allInitialFieldsFilleMap = new LinkedHashMap<>();
    private LinkedHashMap<String, ApjField> allFieldsDataBaseFilleMap = new LinkedHashMap<>();
    private String primaryKey = "id";
    private String primaryKeyFille = "id";
    private String etat = "etat";
    private enum Colonne {
        VISIBLE(0),
        NOM(1),
        LIBELLE(2),
        AUTRE(3),
        TYPE(4),
        DETAILS(5);
        final int index;
        Colonne(int index) {
            this.index = index;
        }
    }

    public PageInsertMultipleForm(ApjGenerationContext context, Project project) {
        this.context = context;
        this.project = project;
        initFormulaireMereFille();
        addActionListenerOnClassButton();
        addActionListenerOnTableButton();
    }

    private void initFormulaireMereFille() {
        initFormTable(false);
        initFormTable(true);
    }

    private void addUpdateGroup(DefaultActionGroup group, String name,boolean isFille) {
        group.add(new AnAction(name) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                updateRows(name,isFille);
            }
        });
    }

    private void fixSizeColumn(JBTable table,int col,int size) {
        table.getColumnModel().getColumn(col).setPreferredWidth(size);
        table.getColumnModel().getColumn(col).setMinWidth(size);
        table.getColumnModel().getColumn(col).setMaxWidth(size);
    }

    private void initFormTable(boolean isFille) {
        JBTable tableForm;
        JScrollPane scrollPane;
        JPanel panel;
        Object[] columns = new Object[]{"Visible","Champ","Libellé","Autre","Type","Détails"};

        if (isFille) {
            formFilleTableModel = new InsertTableModel(columns);
            formFilleTable = new JBTable(formFilleTableModel);
            tableForm = formFilleTable;
            scrollPane = scrollFille;
            panel = fillePanel;
        } else {
            formTableModel = new InsertTableModel(columns);
            formTable = new JBTable(formTableModel);
            tableForm = formTable;
            scrollPane = scrollFiltre;
            panel = filtrePanel;
        }
        tableForm.getEmptyText().setText("Aucune ligne à afficher");
        tableForm.setDefaultRenderer(Object.class, new TableRenderer());
        tableForm.setDragEnabled(true);
        tableForm.setDropMode(DropMode.INSERT_ROWS);
        tableForm.setTransferHandler(new TableRowTransferHandler(tableForm));
        tableForm.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);

        fixSizeColumn(tableForm, Colonne.VISIBLE.index, 30);
        fixSizeColumn(tableForm, Colonne.NOM.index, 115);
        fixSizeColumn(tableForm, Colonne.LIBELLE.index, 125);
        fixSizeColumn(tableForm, Colonne.AUTRE.index, 70);
        fixSizeColumn(tableForm, Colonne.TYPE.index, 80);

        scrollPane.setViewportView(tableForm);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        tableForm.setBorder(BorderFactory.createEmptyBorder());

        DefaultActionGroup updateGroup = new DefaultActionGroup();
        addUpdateGroup(updateGroup,ConstantesApj.LISTE, isFille);
        addUpdateGroup(updateGroup,ConstantesApj.LISTE_STRING, isFille);
        addUpdateGroup(updateGroup,ConstantesApj.OUI_NON, isFille);
        addUpdateGroup(updateGroup,ConstantesApj.AUTO_COMPLETE, isFille);
        addUpdateGroup(updateGroup,ConstantesApj.SIMPLE, isFille);
        TableToolbarHelper.builder()
            .table(tableForm)
            .panel(panel)
            .updateActionGroup(updateGroup)
            .customButtonText("Générer les libellés via l'IA")
            .customButtonAction(()->askAI(isFille))
            .build().init();
    }

    private void askAI(boolean isFille) {
        try {
            ProgressUtils.runWithProgress(project, "Traitement de la demande par l'IA…", indicator -> {
                DefaultTableModel tableModel = formTableModel;
                JBTable tableForm = formTable;
                if (isFille){
                    tableModel = formFilleTableModel;
                    tableForm = formFilleTable;
                }
                int[] selectedRows = tableForm.getSelectedRows();
                ApjField[] fields = new ApjField[selectedRows.length];
                for (int i = 0; i < selectedRows.length; i++) {
                    String nom = String.valueOf(tableModel.getValueAt(selectedRows[i], Colonne.NOM.index));
                    fields[i] = new ApjField();
                    fields[i].setNom(nom);
                }
                String mapping = getMappingField().getText();
                LlmApiClient llmClient = new LlmApiClient();
                String[] libelles = llmClient.askForLabel(mapping, fields, ConstantesApj.STANDARD);

                for (int i = 0; i < libelles.length; i++) {
                    tableModel.setValueAt(libelles[i], selectedRows[i], Colonne.LIBELLE.index);
                }
            });
        } catch (Exception e) {
            PopUtils.showError(mainPanel, "Erreur lors de la communication avec l'IA : " + e.getMessage());
        }
    }

    private void updateRows(String type,boolean isFille) {
        LinkedHashMap<String, ApjField> allFields = allFieldsMap;
        DefaultTableModel tableModel = formTableModel;
        int selectedRow = formTable.getSelectedRow();
        if (isFille){
            allFields = allFieldsFilleMap;
            tableModel = formFilleTableModel;
            selectedRow = formFilleTable.getSelectedRow();
        }
        if (selectedRow < 0) return;

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
        } else if (type.equalsIgnoreCase(ConstantesApj.AUTO_COMPLETE)) {
            AutoCompleteDialog listeDialog = new AutoCompleteDialog(mainPanel, context, project,allFields);
            listeDialog.showDialog();
            String result = listeDialog.getDetails();
            if (result == null) return;
            details = result;
            withDetail = true;
        }

        tableModel.setValueAt(type, selectedRow, Colonne.TYPE.index);
        if (withDetail) {
            tableModel.setValueAt(details, selectedRow, Colonne.DETAILS.index);
        }
    }

    public void addActionListenerOnClassButton() {
        chooseClassButton.setToolTipText("Cliquez pour sélectionner une classe Java du projet");
        chooseClassButton.addActionListener(e -> handleClassSelection(mappingField, false));
        chooseClassFilleButton.setToolTipText("Cliquez pour sélectionner une classe Java du projet");
        chooseClassFilleButton.addActionListener(e -> handleClassSelection(mappingFilleField, true));
    }

    private void handleClassSelection(JTextField targetField, boolean isFille) {
        try {
            TreeClassChooser chooser = TreeClassChooserFactory.getInstance(project)
                .createAllProjectScopeChooser("Sélectionner une classe");
            chooser.showDialog();
            PsiClass selectedClass = chooser.getSelected();
            if (selectedClass == null) return;
            String className = selectedClass.getQualifiedName();
            targetField.setText(className);
            loadMapping(className, isFille);
        } catch (Exception e) {
            PopUtils.showError(mainPanel, "Erreur de chargement du mapping Java : " + e.getMessage());
        }
    }

    private void loadMapping(String className, boolean isFille) throws Exception {
        ProgressUtils.runWithProgress(project, "Chargement du Mapping Java...", indicator -> {
            URLClassLoader loader = UtilClassLoader.buildLoader(context.getProjectJarDir(), context.getLibDir());

            ProgressUtils.updateProgress(indicator, "Classe Java en cours de chargement...", 0.3);
            Class<?> cls = loader.loadClass(className);
            List<Field> fields = UtilClassLoader.listFieldsStopOnSuperClassApj(cls);
            List<ApjField> apjFields = ApjField.javaFieldsToApjFields(fields);

            ProgressUtils.updateProgress(indicator, "Mise à jour du tableau...", 0.85);
            ApplicationManager.getApplication().invokeAndWait(() -> {
                addRowTable(apjFields, isFille);
                loadFieldsMap(apjFields, isFille);
            });

            ProgressUtils.updateProgress(indicator, "Chargement terminé !", 1.0);
        });
    }

    private void addRowTable(List<ApjField> fields, boolean isFille) {
        DefaultTableModel tableModel = formTableModel;
        String pk = primaryKey;
        if (isFille) {
            tableModel = formFilleTableModel;
            pk = primaryKeyFille;
        }
        tableModel.setRowCount(0);
        for (ApjField field : fields) {
            String fieldName = field.getNom();
            if (fieldName.equalsIgnoreCase(pk) || fieldName.equalsIgnoreCase(etat)) {
                continue;
            }
            tableModel.addRow(new Object[]{Boolean.TRUE,fieldName, StringUtils.majStart(fieldName),null,ConstantesApj.SIMPLE,null});
        }
    }

    private void loadFieldsMap(List<ApjField> fields, boolean isFille) {
        Map<String, ApjField> targetMap;
        Map<String, ApjField> initialMap;
        if (isFille) {
            targetMap = allFieldsFilleMap;
            initialMap = allInitialFieldsFilleMap;
        } else {
            targetMap = allFieldsMap;
            initialMap = allInitialFieldsMap;
        }
        targetMap.clear();
        initialMap.clear();
        for (ApjField field : fields) {
            String key = field.getNom();
            targetMap.put(key, field);
            initialMap.put(key, field);
        }
    }

    public void addActionListenerOnTableButton(){
        getChooseTableButton().addActionListener(e -> showTableTree(nomTableField, false));
        getChooseTableFilleButton().addActionListener(e -> showTableTree(nomTableFilleField, true));
    }

    public void showTableTree(JTextField targetField, boolean isFille) {
        try {
            String[] tables = context.getTables();
            String[] views = context.getVues();
            TableTreeChooser chooser = new TableTreeChooser(mainPanel, tables, views);
            String table = chooser.showDialog();
            if (table == null) return;
            targetField.setText(table);
            loadTableColumns(table, isFille);
        } catch (Exception e) {
            PopUtils.showError(mainPanel, "Erreur de chargement des colonnes : " + e.getMessage());
        }
    }

    private void loadTableColumns(String table, boolean isFille) throws Exception {
        ProgressUtils.runWithProgress(project, "Chargement des colonnes de \"" + table + "\"...", indicator -> {
            ProgressUtils.updateProgress(indicator, "Connexion à la base de données...", 0.3);
            try (Connection conn = UtilDBDynamique.GetConn(context.getProjectJarDir(), context.getLibDir())) {
                List<ApjField> fields = Database.getTableColumns(conn, table);

                ProgressUtils.updateProgress(indicator, "Mise à jour du tableau...", 0.85);
                ApplicationManager.getApplication().invokeAndWait(() -> loadAllFieldsBase(fields, isFille));

                ProgressUtils.updateProgress(indicator, "Chargement terminé !", 1.0);
            }
        });
    }

    private void loadAllFieldsBase(List<ApjField> fields, boolean isFille) {
        LinkedHashMap<String, ApjField> baseMap;
        LinkedHashMap<String, ApjField> allFields;
        LinkedHashMap<String, ApjField> allInitialFields;
        if (isFille) {
            baseMap = allFieldsDataBaseFilleMap;
            allFields = allFieldsFilleMap;
            allInitialFields = allInitialFieldsFilleMap;
        } else {
            baseMap = allFieldsDataBaseMap;
            allFields = allFieldsMap;
            allInitialFields = allInitialFieldsMap;
        }
        baseMap.clear();
        for (ApjField field : fields) {
            baseMap.put(field.getNom(), field);
        }
        List<ApjField> allFieldsReinit = new ArrayList<>(allInitialFields.values());
        loadFieldsMap(allFieldsReinit, isFille);
        removeNonCommun(allFields, baseMap);
        List<ApjField> newRows = new ArrayList<>(allFields.values());
        addRowTable(newRows, isFille);
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
        for (String key : toRemove) {
            allFields.remove(key);
        }
    }

    public void fillDataTables(){
        this.dataForm = getDataTable(formTable);
        this.dataFormFille = getDataTable(formFilleTable);
    }

    private ApjField[] getDataTable(JTable table) {
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        int rowCount = model.getRowCount();
        ApjField[] fields = new ApjField[rowCount];
        for (int i = 0; i < rowCount; i++) {
            boolean visible = (Boolean) model.getValueAt(i, Colonne.VISIBLE.index);
            String nom = String.valueOf(model.getValueAt(i, Colonne.NOM.index));
            String libelle = String.valueOf(model.getValueAt(i, Colonne.LIBELLE.index));
            String autre = String.valueOf(model.getValueAt(i, Colonne.AUTRE.index));
            String type = String.valueOf(model.getValueAt(i, Colonne.TYPE.index));
            String details = String.valueOf(model.getValueAt(i, Colonne.DETAILS.index));

            ApjField f = new ApjField();
            f.setAutre(autre);
            f.setNom(nom);
            f.setLibelle(libelle);
            f.setType(type);
            f.setVisible(visible);
            f.setDetails(details);
            fields[i] = f;
        }
        return fields;
    }
}

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
import org.labs.genesis.apj.utilitaire.Database;
import org.labs.genesis.apj.utilitaire.UtilClassLoader;
import org.labs.genesis.apj.utilitaire.UtilDBDynamique;
import org.labs.genesis.config.langage.generator.project.LlmApiClient;
import org.labs.utils.StringUtils;
import org.labs.genesis.apj.utilitaire.ConstantesApj;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.lang.reflect.Field;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.util.*;
import java.util.List;

@Getter
@Setter
public class PageInsertForm {
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
    private JBTable formTable;
    private DefaultTableModel formTableModel;
    private ApjField[] dataForm;
    private final ApjGenerationContext context;
    private final Project project;
    private LinkedHashMap<String, ApjField> allFieldsMap = new LinkedHashMap<>();
    private LinkedHashMap<String, ApjField> allInitialFieldsMap = new LinkedHashMap<>();
    private LinkedHashMap<String, ApjField> allFieldsDataBaseMap = new LinkedHashMap<>();
    private String primaryKey = "id";
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

    public PageInsertForm(ApjGenerationContext context, Project project) {
        this.context = context;
        this.project = project;
        initFormTable();
        addActionListenerOnClassButton();
        addActionListenerOnTableButton();
    }

    private void addUpdateGroup(DefaultActionGroup group, String name) {
        group.add(new AnAction(name) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                updateRows(name);
            }
        });
    }

    private void fixSizeColumn(JBTable table,int col,int size) {
        table.getColumnModel().getColumn(col).setPreferredWidth(size);
        table.getColumnModel().getColumn(col).setMinWidth(size);
        table.getColumnModel().getColumn(col).setMaxWidth(size);
    }

    private void initFormTable() {
        formTableModel = new InsertTableModel(new Object[]{"Visible","Champ","Libellé","Autre","Type","Détails"});
        formTable = new JBTable(formTableModel);
        formTable.getEmptyText().setText("Aucune ligne à afficher");
        formTable.setDefaultRenderer(Object.class, new TableRenderer());
        formTable.setDragEnabled(true);
        formTable.setDropMode(DropMode.INSERT_ROWS);
        formTable.setTransferHandler(new TableRowTransferHandler(formTable));
        formTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        fixSizeColumn(formTable, Colonne.VISIBLE.index, 30);
        fixSizeColumn(formTable, Colonne.NOM.index, 115);
        fixSizeColumn(formTable, Colonne.LIBELLE.index, 125);
        fixSizeColumn(formTable, Colonne.AUTRE.index, 70);
        fixSizeColumn(formTable, Colonne.TYPE.index, 80);

        scrollFiltre.setViewportView(formTable);
        scrollFiltre.setBorder(BorderFactory.createEmptyBorder());
        formTable.setBorder(BorderFactory.createEmptyBorder());

        DefaultActionGroup updateGroup = new DefaultActionGroup();
        addUpdateGroup(updateGroup,ConstantesApj.LISTE);
        addUpdateGroup(updateGroup,ConstantesApj.LISTE_STRING);
        addUpdateGroup(updateGroup,ConstantesApj.OUI_NON);
        addUpdateGroup(updateGroup,ConstantesApj.AUTO_COMPLETE);
        addUpdateGroup(updateGroup,ConstantesApj.SIMPLE);
        TableToolbarHelper.builder()
            .table(formTable)
            .panel(filtrePanel)
            .updateActionGroup(updateGroup)
            .customButtonText("Générer les libellés via l'IA")
            .customButtonAction(this::askAI)
            .build().init();
    }

    private void askAI() {
        try {
            ProgressUtils.runWithProgress(project, "Traitement de la demande par l'IA…", indicator -> {
                int[] selectedRows = formTable.getSelectedRows();
                ApjField[] fields = new ApjField[selectedRows.length];
                for (int i = 0; i < selectedRows.length; i++) {
                    String nom = String.valueOf(formTableModel.getValueAt(selectedRows[i], Colonne.NOM.index));
                    fields[i] = new ApjField();
                    fields[i].setNom(nom);
                }
                String mapping = this.getMappingField().getText();
                LlmApiClient llmClient = new LlmApiClient();
                String[] libelles = llmClient.askForLabel(mapping, fields, ConstantesApj.STANDARD);
                for (int i = 0; i < libelles.length; i++) {
                    formTableModel.setValueAt(libelles[i], selectedRows[i], Colonne.LIBELLE.index);
                }
            });
        } catch (Exception e) {
            PopUtils.showError(mainPanel, "Erreur lors de la communication avec l'IA : " + e.getMessage());
        }
    }

    private void updateRows(String type) {
        int selectedRow = formTable.getSelectedRow();
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
            AutoCompleteDialog listeDialog = new AutoCompleteDialog(mainPanel, context, project,allFieldsMap);
            listeDialog.showDialog();
            String result = listeDialog.getDetails();
            if (result == null) return;
            details = result;
            withDetail = true;
        }

        formTableModel.setValueAt(type, selectedRow, Colonne.TYPE.index);
        if (withDetail) {
            formTableModel.setValueAt(details, selectedRow, Colonne.DETAILS.index);
        } else {
            formTableModel.setValueAt(null, selectedRow, Colonne.DETAILS.index);
        }
    }

    public void addActionListenerOnClassButton() {
        chooseClassButton.setToolTipText("Cliquez pour sélectionner une classe Java du projet");
        chooseClassButton.addActionListener(e -> handleClassSelection(mappingField));
    }

    public void handleClassSelection(JTextField targetField) {
        try {
            TreeClassChooser chooser = TreeClassChooserFactory.getInstance(project)
                    .createAllProjectScopeChooser("Sélectionner une classe");
            chooser.showDialog();
            PsiClass selectedClass = chooser.getSelected();
            if (selectedClass == null) return;
            String className = selectedClass.getQualifiedName();
            targetField.setText(className);
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

            ProgressUtils.updateProgress(indicator, "Mise à jour du tableau...", 0.85);
            ApplicationManager.getApplication().invokeAndWait(() -> {
                addRowTable(apjFields);
                loadFieldsMap(apjFields);
            });

            ProgressUtils.updateProgress(indicator, "Chargement terminé !", 1.0);
        });
    }

    private void addRowTable(List<ApjField> fields) {
        formTableModel.setRowCount(0);
        for (ApjField field : fields) {
            String fieldName = field.getNom();
            if (fieldName.equalsIgnoreCase(primaryKey) || fieldName.equalsIgnoreCase(etat)) {
                continue;
            }
            formTableModel.addRow(new Object[]{Boolean.TRUE,fieldName, StringUtils.majStart(fieldName),null,ConstantesApj.SIMPLE,null});
        }
    }

    private void loadFieldsMap(List<ApjField> fields) {
        allFieldsMap.clear();
        allInitialFieldsMap.clear();
        for (ApjField field : fields) {
            String key = field.getNom();
            allFieldsMap.put(key, field);
            allInitialFieldsMap.put(key, field);
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
        ProgressUtils.runWithProgress(project, "Chargement des colonnes de \"" + table + "\"...", indicator -> {
            ProgressUtils.updateProgress(indicator, "Connexion à la base de données...", 0.3);
            try (Connection conn = UtilDBDynamique.GetConn(context.getProjectJarDir(), context.getLibDir())) {
                List<ApjField> fields = Database.getTableColumns(conn, table);

                ProgressUtils.updateProgress(indicator, "Mise à jour du tableau...", 0.85);
                ApplicationManager.getApplication().invokeAndWait(() -> loadAllFieldsBase(fields));

                ProgressUtils.updateProgress(indicator, "Chargement terminé !", 1.0);
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
        List<ApjField> newRows = new ArrayList<>(allFieldsMap.values());
        addRowTable(newRows);
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

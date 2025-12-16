package org.labs.genesis.action.apjwizard.forms;

import com.intellij.ide.util.TreeClassChooser;
import com.intellij.ide.util.TreeClassChooserFactory;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.ui.table.JBTable;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.labs.genesis.action.apjwizard.forms.helper.ProgressUtils;
import org.labs.genesis.action.apjwizard.forms.helper.TableToolbarHelper;
import org.labs.genesis.action.apjwizard.forms.popup.PopUtils;
import org.labs.genesis.action.apjwizard.forms.popup.TableTreeChooser;
import org.labs.genesis.action.apjwizard.forms.renderer.TableRenderer;
import org.labs.genesis.action.apjwizard.forms.tablehandler.ConsulteTableModel;
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
public class PageConsulteForm {
    private JPanel mainPanel;
    private JTextField nomField;
    private JTextField mappingField;
    private JTextField nomTableField;
    private JTextField pageApresDelete;
    private JLabel nomLabel;
    private JPanel generalPanel;
    private JLabel nomTableLabel;
    private JLabel titreLabel;
    private JPanel propertiesPanel;
    private JScrollPane scrollProperties;
    private JTabbedPane tabPane;
    private JPanel filtrePanel;
    private JScrollPane scrollFiltre;
    private JButton chooseClassButton;
    private JPanel mappingPanel;
    private JButton chooseTableButton;
    private JTextField titreField;
    private JCheckBox withOngletCheckBox;
    private JTextField pageRetourField;
    private JTextField pageModifField;
    private JBTable formTable;
    private DefaultTableModel formTableModel;
    private ApjField[] dataForm;
    private LinkedHashMap<String, ApjField> allInitialFieldsMap = new LinkedHashMap<>();
    private LinkedHashMap<String, ApjField> allFieldsMap = new LinkedHashMap<>();
    private LinkedHashMap<String, ApjField> allFieldsDataBaseMap = new LinkedHashMap<>();
    private final ApjGenerationContext context;
    private final Project project;
    private enum Colonne {
        VISIBLE(0),
        CHAMP(1),
        LIBELLE(2),
        LIEN(3);
        final int index;
        Colonne(int index) {
            this.index = index;
        }
    }

    public PageConsulteForm(ApjGenerationContext context, Project project) {
        this.context = context;
        this.project = project;
        initFormTable();
        addActionListenerOnClassButton();
        addActionListenerOnTableButton();
    }

    private void fixSizeColumn(JBTable table,int row,int size) {
        table.getColumnModel().getColumn(row).setPreferredWidth(size);
        table.getColumnModel().getColumn(row).setMinWidth(size);
        table.getColumnModel().getColumn(row).setMaxWidth(size);
    }

    @SneakyThrows
    private void initFormTable() {
        formTableModel = new ConsulteTableModel(new Object[]{"Visible","Champ", "Libellé","Lien"});
        formTable = new JBTable(formTableModel);
        formTable.getEmptyText().setText("Aucune ligne à afficher");
        formTable.setDefaultRenderer(Object.class, new TableRenderer());
        formTable.setDragEnabled(true);
        formTable.setDropMode(DropMode.INSERT_ROWS);
        formTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        fixSizeColumn(formTable,Colonne.VISIBLE.index,30);
        fixSizeColumn(formTable,Colonne.CHAMP.index, 130);
        fixSizeColumn(formTable,Colonne.LIEN.index, 160);
        formTable.setTransferHandler(new TableRowTransferHandler(formTable));
        scrollFiltre.setViewportView(formTable);
        scrollFiltre.setBorder(BorderFactory.createEmptyBorder());
        formTable.setBorder(BorderFactory.createEmptyBorder());
        scrollFiltre.setViewportBorder(null);
        TableToolbarHelper.builder()
            .table(formTable)
            .panel(filtrePanel)
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
                    String nom = String.valueOf(formTableModel.getValueAt(selectedRows[i], Colonne.CHAMP.index));
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
            formTableModel.addRow(new Object[]{Boolean.TRUE,fieldName, StringUtils.majStart(fieldName)});
        }
    }

    private void loadFieldsMap(List<ApjField> fields) {
        allFieldsMap.clear();
        allInitialFieldsMap.clear();

        for (ApjField field : fields) {
            allFieldsMap.put(field.getNom(), field);
            allInitialFieldsMap.put(field.getNom(), field);
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
            String nom = String.valueOf(model.getValueAt(i, Colonne.CHAMP.index));
            String libelle = String.valueOf(model.getValueAt(i, Colonne.LIBELLE.index));
            boolean visible = (Boolean) model.getValueAt(i, Colonne.VISIBLE.index);
            String lien = String.valueOf(model.getValueAt(i, Colonne.LIEN.index));
            ApjField f = new ApjField();
            f.setNom(nom);
            f.setLibelle(libelle);
            f.setVisible(visible);
            f.setLien(lien);
            fields[i] = f;
        }
        return fields;
    }
}

package org.labs.genesis.action.apjwizard.forms;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.ui.table.JBTable;
import lombok.Getter;
import lombok.Setter;
import org.labs.genesis.action.apjwizard.forms.helper.ProgressUtils;
import org.labs.genesis.action.apjwizard.forms.helper.TableToolbarHelper;
import org.labs.genesis.action.apjwizard.forms.popup.PopUtils;
import org.labs.genesis.action.apjwizard.forms.popup.TableTreeChooser;
import org.labs.genesis.action.apjwizard.forms.renderer.TableRenderer;
import org.labs.genesis.action.apjwizard.forms.tablehandler.MappingTableModel;
import org.labs.genesis.action.apjwizard.forms.tablehandler.TableRowTransferHandler;
import org.labs.genesis.apj.ApjGenerationContext;
import org.labs.genesis.apj.component.ApjField;
import org.labs.genesis.apj.utilitaire.ConstantesApj;
import org.labs.genesis.apj.utilitaire.Database;
import org.labs.genesis.apj.utilitaire.UtilClassLoader;
import org.labs.genesis.apj.utilitaire.UtilDBDynamique;
import org.labs.genesis.config.langage.generator.project.LlmApiClient;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.lang.reflect.Field;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Setter
public class MappingMereFilleForm {
    private JPanel mainPanel;
    private JTextField nomField;
    private JTextField nomTableField;
    private JLabel nomLabel;
    private JPanel generalPanel;
    private JPanel propertiesPanel;
    private JTabbedPane tabPane;
    private JPanel filtrePanel;
    private JScrollPane scrollFiltre;
    private JLabel superClasse;
    private JTextField nomFilleField;
    private JTextField nomTableFilleField;
    private JButton chooseTableButton;
    private JButton chooseTableFilleButton;
    private JTextField liaison;
    private JScrollPane scrollFille;
    private JPanel fillePanel;
    private JBTable formTable;
    private JBTable formTableFille;
    private DefaultTableModel formTableModel;
    private DefaultTableModel formTableFilleModel;
    private ApjField[] dataForm;
    private ApjField[] dataFormFille;
    private final Project project;
    private final ApjGenerationContext context;
    private String primaryKey;
    private String primaryKeyFille;
    private enum Colonne {
        NOMBASE(0),
        TYPEBASE(1),
        NOM(2),
        TYPE(3);
        final int index;
        Colonne(int index) {
            this.index = index;
        }
    }

    public MappingMereFilleForm(ApjGenerationContext context, Project project) {
        this.project = project;
        this.context = context;
        initFormulaireMereFille();
        addActionListenerOnTableButton();
    }

    private void initFormulaireMereFille() {
        initFormTable(false);
        initFormTable(true);
    }

    private void initFormTable(boolean isFille)  {
        JBTable tableForm;
        JScrollPane scrollPane;
        JPanel panel;
        Object[] columns = new Object[]{"Nom colonne","Type", "Nom attribut Java","Type Java"};
        if (isFille) {
            formTableFilleModel = new MappingTableModel(columns);
            formTableFille = new JBTable(formTableFilleModel);
            tableForm = formTableFille;
            scrollPane = scrollFille;
            panel = fillePanel;
        } else {
            formTableModel = new MappingTableModel(columns);
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
        scrollPane.setViewportView(tableForm);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        tableForm.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setViewportBorder(null);
        TableToolbarHelper.builder()
            .table(tableForm)
            .panel(panel)
            .customButtonText("Générer les nom d'attribut via l'IA")
            .customButtonAction(()-> askAI(isFille))
            .build().init();
    }

    private void askAI(boolean isFille) {
        try {
            ProgressUtils.runWithProgress(project, "Traitement de la demande par l'IA…", indicator -> {
                JBTable formTable = this.formTable;
                DefaultTableModel formTableModel = this.formTableModel;
                if (isFille) {
                    formTable = this.formTableFille;
                    formTableModel = this.formTableFilleModel;
                }
                int[] selectedRows = formTable.getSelectedRows();
                ApjField[] fields = new ApjField[selectedRows.length];
                for (int i = 0; i < selectedRows.length; i++) {
                    String nom = String.valueOf(formTableModel.getValueAt(selectedRows[i], Colonne.NOMBASE.index));
                    fields[i] = new ApjField();
                    fields[i].setNom(nom.toLowerCase());
                }
                String mapping = "";
                LlmApiClient llmClient = new LlmApiClient();
                String[] libelles = llmClient.askForLabel(mapping, fields, ConstantesApj.ATTRIBUT);
                for (int i = 0; i < libelles.length; i++) {
                    formTableModel.setValueAt(libelles[i], selectedRows[i], Colonne.NOM.index);
                }
            });
        } catch (Exception e) {
            PopUtils.showError(mainPanel, "Erreur lors de la communication avec l'IA : " + e.getMessage());
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
                ApplicationManager.getApplication().invokeAndWait(() -> {
                    try {
                        loadAllFields(fields, isFille);
                    } catch (Exception e) {
                        PopUtils.showError(mainPanel, "Erreur lors de la mise à jour du tableau : " + e.getMessage());
                    }
                });

                ProgressUtils.updateProgress(indicator, "Chargement terminé !", 1.0);
            }
        });
    }

    private void loadAllFields(List<ApjField> fields,boolean isFille) throws Exception {
        String superClassName = ConstantesApj.CLASSMERE;
        DefaultTableModel model = formTableModel;
        if (isFille) {
            formTableFilleModel.setRowCount(0);
            superClassName = ConstantesApj.CLASSFILLE;
            model = formTableFilleModel;
        } else {
            formTableModel.setRowCount(0);
        }
        Set<String> superClassFields  = loadSuperclassFieldNames("bean."+superClassName);
        for (ApjField field : fields) {
            if (superClassFields.contains(field.getNom())) {
                continue;
            }

            if (field.isPrimaryKey()) {
                if (isFille) {
                    primaryKeyFille = field.getNom();
                } else {
                    primaryKey = field.getNom();
                }
            }

            model.addRow(new Object[]{
                field.getNomBase(),
                field.getTypeBase(),
                field.getNom(),
                field.getType()
            });
        }
    }

    private Set<String> loadSuperclassFieldNames(String superClassName) throws Exception {
        URLClassLoader loader = UtilClassLoader.buildLoader(context.getProjectJarDir(), context.getLibDir());
        Class<?> cls = loader.loadClass(superClassName);
        List<Field> superFields = UtilClassLoader.listFields(cls, "none");

        return superFields.stream()
            .map(Field::getName)
            .collect(Collectors.toSet());
    }

    public void fillDataTables(){
        this.dataForm = getDataTable(false);
        this.dataFormFille = getDataTable(true);
    }

    private ApjField[] getDataTable(boolean isFille) {
        String pk = primaryKey;
        JBTable table = formTable;
        if (isFille) {
            pk = primaryKeyFille;
            table = formTableFille;
        }
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        int rowCount = model.getRowCount();
        ApjField[] fields = new ApjField[rowCount];
        for (int i = 0; i < rowCount; i++) {
            String nomBase = String.valueOf(model.getValueAt(i, Colonne.NOMBASE.index));
            String typeBase = String.valueOf(model.getValueAt(i, Colonne.TYPEBASE.index));
            String nom = String.valueOf(model.getValueAt(i, Colonne.NOM.index));
            String type = String.valueOf(model.getValueAt(i, Colonne.TYPE.index));
            ApjField f = new ApjField();
            if (nomBase.equalsIgnoreCase(pk)) {
                this.setPrimaryKey(nom);
            }
            f.setNom(nom);
            f.setType(type);
            f.setTypeBase(typeBase);
            f.setNomBase(nomBase);
            fields[i] = f;
        }
        return fields;
    }
}

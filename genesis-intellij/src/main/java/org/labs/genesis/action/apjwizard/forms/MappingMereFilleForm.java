package org.labs.genesis.action.apjwizard.forms;

import com.intellij.openapi.project.Project;
import com.intellij.ui.table.JBTable;
import lombok.Getter;
import lombok.Setter;
import org.labs.genesis.action.apjwizard.forms.helper.TableToolbarHelper;
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
import java.util.Collections;
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
    private JScrollPane scrollProperties;
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

    public MappingMereFilleForm(ApjGenerationContext context, Project project) {
        this.project = project;
        this.context = context;
        initializeUI();
        initFormTable();
        initFormTableFille();
    }

    private void initializeUI() {
        if (scrollProperties != null) {
            scrollProperties.setBorder(BorderFactory.createEmptyBorder());
            scrollProperties.setViewportBorder(null);
        }
    }

    private void initFormTable() {
        formTableModel = new MappingTableModel(new Object[]{"Nom colonne","Type", "Nom attribut Java","Type Java"});
        formTable = new JBTable(formTableModel);
        formTable.getEmptyText().setText("Aucune ligne à afficher");
        formTable.setDefaultRenderer(Object.class, new TableRenderer());
        formTable.setDragEnabled(true);
        formTable.setDropMode(DropMode.INSERT_ROWS);
        formTable.setTransferHandler(new TableRowTransferHandler(formTable));
        scrollFiltre.setViewportView(formTable);
        scrollFiltre.setBorder(BorderFactory.createEmptyBorder());
        formTable.setBorder(BorderFactory.createEmptyBorder());
        scrollFiltre.setViewportBorder(null);
        TableToolbarHelper.builder()
            .table(formTable)
            .panel(filtrePanel)
            .customButtonText("Générer les nom d'attribut via l'IA")
            .customButtonAction(()->askAI(false))
            .build().init();
    }

    private void initFormTableFille() {
        formTableFilleModel = new MappingTableModel(new Object[]{"Nom colonne","Type", "Nom attribut Java","Type Java"});
        formTableFille = new JBTable(formTableFilleModel);
        formTableFille.getEmptyText().setText("Aucune ligne à afficher");
        formTableFille.setDefaultRenderer(Object.class, new TableRenderer());
        formTableFille.setDragEnabled(true);
        formTableFille.setDropMode(DropMode.INSERT_ROWS);
        formTableFille.setTransferHandler(new TableRowTransferHandler(formTableFille));
        scrollFiltre.setViewportView(formTableFille);
        scrollFiltre.setBorder(BorderFactory.createEmptyBorder());
        formTableFille.setBorder(BorderFactory.createEmptyBorder());
        scrollFiltre.setViewportBorder(null);
        TableToolbarHelper.builder()
            .table(formTableFille)
            .panel(fillePanel)
            .customButtonText("Générer les nom d'attribut via l'IA")
            .customButtonAction(()->askAI(true))
            .build().init();
    }

    private void askAI(boolean isFille) {
        JBTable formTable = this.formTable;
        DefaultTableModel formTableModel = this.formTableModel;
        if (isFille) {
            formTable = this.formTableFille;
            formTableModel = this.formTableFilleModel;
        }
        int[] selectedRows = formTable.getSelectedRows();
        ApjField[] fields = new ApjField[selectedRows.length];
        for (int i = 0; i < selectedRows.length; i++) {
            String nom = String.valueOf(formTableModel.getValueAt(selectedRows[i], 2));
            fields[i] = new ApjField();
            fields[i].setNom(nom);
        }
        String mapping = "";
        LlmApiClient llmClient = new LlmApiClient();
        String[] libelles = new String[selectedRows.length];
        try {
            libelles = llmClient.askForLabel(mapping, fields, ConstantesApj.ATTRIBUT);
        } catch (Exception ignored) {

        }
        for (int i = 0; i < libelles.length; i++) {
            formTableModel.setValueAt(libelles[i], selectedRows[i], 2);
        }
    }

    private void removeAllRows() {
        formTableModel.setRowCount(0);
    }

    private void removeAllFilleRows() {
        formTableFilleModel.setRowCount(0);
    }

    private Set<String> loadSuperclassFieldNames(String superClassName) throws Exception {
        URLClassLoader loader = UtilClassLoader.buildLoader(context.getProjectJarDir(), context.getLibDir());
        Class<?> cls = loader.loadClass(superClassName);
        List<Field> superFields = UtilClassLoader.listFields(cls, "none");

        return superFields.stream()
                .map(Field::getName)
                .collect(Collectors.toSet());
    }

    private void loadAllFields(List<ApjField> fields,boolean isFille) {
        String superClassName = ConstantesApj.CLASSMERE;
        DefaultTableModel model = formTableModel;
        if (isFille) {
            removeAllFilleRows();
            superClassName = ConstantesApj.CLASSFILLE;
            model = formTableFilleModel;
        } else {
            removeAllRows();
        }

        Set<String> superClassFields = Collections.emptySet();
        try {
            superClassFields = loadSuperclassFieldNames("bean."+superClassName);
        } catch (Exception ignored) {

        }
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

    public void showTableTree(String[] tables, String[] views) {
        TableTreeChooser chooser = new TableTreeChooser(mainPanel, tables, views);
        String table = chooser.showDialog();
        if (table != null) {
            nomTableField.setText(table);
            try (Connection conn = UtilDBDynamique.GetConn(context.getProjectJarDir(), context.getLibDir())) {
                List<ApjField> fields = Database.getTableColumns(conn, table);
                loadAllFields(fields,false);
            } catch (Exception ignored) {

            }
        }
    }
    public void showTableTreeFille(String[] tables, String[] views) {
        TableTreeChooser chooserFille = new TableTreeChooser(mainPanel, tables, views);
        String tableFille = chooserFille.showDialog();
        if (tableFille != null) {
            nomTableFilleField.setText(tableFille);
            try (Connection conn = UtilDBDynamique.GetConn(context.getProjectJarDir(), context.getLibDir())) {
                List<ApjField> fields = Database.getTableColumns(conn, tableFille);
                loadAllFields(fields,true);
            } catch (Exception ignored) {

            }
        }
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
            String nomBase = String.valueOf(model.getValueAt(i, 0));
            String typeBase = String.valueOf(model.getValueAt(i, 1));
            String nom = String.valueOf(model.getValueAt(i, 2));
            String type = String.valueOf(model.getValueAt(i, 3));
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

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
import org.labs.genesis.action.apjwizard.forms.listener.PlaceholderTextFieldHelper;
import org.labs.genesis.action.apjwizard.forms.popup.AutoCompleteDialog;
import org.labs.genesis.action.apjwizard.forms.popup.ListDetailsDialog;
import org.labs.genesis.action.apjwizard.forms.popup.ListeStringDialog;
import org.labs.genesis.action.apjwizard.forms.popup.TableTreeChooser;
import org.labs.genesis.action.apjwizard.forms.renderer.TableRenderer;
import org.labs.genesis.action.apjwizard.forms.tablehandler.InsertTableModel;
import org.labs.genesis.action.apjwizard.forms.tablehandler.TableRowTransferHandler;
import org.labs.genesis.apj.ApjGenerationContext;
import org.labs.genesis.apj.component.ApjField;
import org.labs.genesis.apj.utilitaire.UtilClassLoader;
import org.labs.genesis.config.langage.generator.project.LlmApiClient;
import org.labs.utils.StringUtils;
import org.labs.genesis.apj.utilitaire.ConstantesApj;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.lang.reflect.Field;
import java.net.URLClassLoader;
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
    private JScrollPane scrollProperties;
    private JTabbedPane tabPane;
    private JPanel filtrePanel;
    private JScrollPane scrollFiltre;
    private JButton chooseClassButton;
    private JPanel mappingPanel;
    private JButton chooseTableButton;
    private JTextField titreField;
    private JLabel fileNameLabel;
    private JBTable formTable;
    private DefaultTableModel formTableModel;
    private ApjField[] dataForm;
    private final ApjGenerationContext context;
    private final Project project;

    public PageInsertForm(ApjGenerationContext context, Project project) {
        this.context = context;
        this.project = project;
        initializeUI();
        initFormTable();
        new PlaceholderTextFieldHelper(nomField, fileNameLabel, ".jsp", "etudiant-saisie");
        new PlaceholderTextFieldHelper(titreField, null, null, "Saisie d'un étudiant");
        new PlaceholderTextFieldHelper(titreUpdateField, null, null, "Modification d'un étudiant");
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

    private void addUpdateGroup(DefaultActionGroup group, String name) {
        group.add(new AnAction(name) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                updateRows(name);
            }
        });
    }

    private void fixSizeColumn(JBTable table,int row,int size) {
        table.getColumnModel().getColumn(row).setPreferredWidth(size);
        table.getColumnModel().getColumn(row).setMinWidth(size);
        table.getColumnModel().getColumn(row).setMaxWidth(size);
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
        fixSizeColumn(formTable,0,30);
        fixSizeColumn(formTable,1,115);
        fixSizeColumn(formTable,2,125);
        fixSizeColumn(formTable,3,70);
        fixSizeColumn(formTable,4,80);

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
        int[] selectedRows = formTable.getSelectedRows();
        ApjField[] fields = new ApjField[selectedRows.length];
        for (int i = 0; i < selectedRows.length; i++) {
            String nom = String.valueOf(formTableModel.getValueAt(selectedRows[i], 1));
            fields[i] = new ApjField();
            fields[i].setNom(nom);
        }
        String mapping = this.getMappingField().getText();
        LlmApiClient llmClient = new LlmApiClient();
        String[] libelles = new String[selectedRows.length];
        try {
            libelles = llmClient.askForLabel(mapping, fields);
        } catch (Exception ignored) {

        }
        for (int i = 0; i < libelles.length; i++) {
            formTableModel.setValueAt(libelles[i], selectedRows[i], 2);
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
            AutoCompleteDialog listeDialog = new AutoCompleteDialog(mainPanel, context, project);
            listeDialog.showDialog();
            String result = listeDialog.getDetails();
            if (result == null) return;
            details = result;
            withDetail = true;
        }

        formTableModel.setValueAt(type, selectedRow, 4);
        if (withDetail) {
            formTableModel.setValueAt(details, selectedRow, 5);
        } else {
            formTableModel.setValueAt(null, selectedRow, 5);
        }
    }

    public void showClassChooser(Project project, ApjGenerationContext context) {
        chooseClassButton.setToolTipText("Cliquez pour sélectionner une classe Java du projet");
        chooseClassButton.addActionListener(e -> {
            TreeClassChooser chooser = TreeClassChooserFactory.getInstance(project)
                    .createAllProjectScopeChooser("Sélectionner une classe");
            chooser.showDialog();
            PsiClass selectedClass = chooser.getSelected();
            if (selectedClass != null) {
                mappingField.setText(selectedClass.getQualifiedName());
                try {
                    URLClassLoader loader = UtilClassLoader.buildLoader(context.getProjectJarDir(), context.getLibDir());
                    Class<?> cls = loader.loadClass(mappingField.getText());
                    List<Field> fields = UtilClassLoader.listFieldsStopClassMAPTable(cls);
                    List<ApjField> apjFields = ApjField.javaFieldsToApjFields(fields);
                    loadAllFields(apjFields);
                } catch (Exception ignored) {

                }
            }
        });
    }

    private void removeAllRows() {
        formTableModel.setRowCount(0);
    }

    private void loadAllFields(List<ApjField> fields) {
        removeAllRows();
        for (ApjField field : fields) {
            String fieldName = field.getNom();
            formTableModel.addRow(new Object[]{Boolean.TRUE,fieldName, StringUtils.majStart(fieldName),null,ConstantesApj.SIMPLE,null});
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
        this.dataForm = getDataTable(formTable);
    }

    private ApjField[] getDataTable(JTable table) {
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        int rowCount = model.getRowCount();
        ApjField[] fields = new ApjField[rowCount];
        for (int i = 0; i < rowCount; i++) {
            boolean visible = (Boolean) model.getValueAt(i, 0);
            String nom = String.valueOf(model.getValueAt(i, 1));
            String libelle = String.valueOf(model.getValueAt(i, 2));
            String autre = String.valueOf(model.getValueAt(i, 3));
            String type = String.valueOf(model.getValueAt(i, 4));
            String details = String.valueOf(model.getValueAt(i, 5));

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

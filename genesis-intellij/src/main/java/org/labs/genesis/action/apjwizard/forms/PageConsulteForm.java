package org.labs.genesis.action.apjwizard.forms;

import com.intellij.ide.util.TreeClassChooser;
import com.intellij.ide.util.TreeClassChooserFactory;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.ui.table.JBTable;
import lombok.Getter;
import lombok.Setter;
import org.labs.genesis.action.apjwizard.forms.helper.TableToolbarHelper;
import org.labs.genesis.action.apjwizard.forms.popup.TableTreeChooser;
import org.labs.genesis.action.apjwizard.forms.renderer.TableRenderer;
import org.labs.genesis.action.apjwizard.forms.tablehandler.ConsulteTableModel;
import org.labs.genesis.action.apjwizard.forms.tablehandler.TableRowTransferHandler;
import org.labs.genesis.apj.ApjGenerationContext;
import org.labs.genesis.apj.component.ApjField;
import org.labs.genesis.apj.utilitaire.UtilClassLoader;
import org.labs.genesis.config.langage.generator.project.LlmApiClient;
import org.labs.utils.StringUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.lang.reflect.Field;
import java.net.URLClassLoader;
import java.util.List;

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

    public PageConsulteForm() {
        initializeUI();
        initFormTable();
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

    private void fixSizeColumn(JBTable table,int row,int size) {
        table.getColumnModel().getColumn(row).setPreferredWidth(size);
        table.getColumnModel().getColumn(row).setMinWidth(size);
        table.getColumnModel().getColumn(row).setMaxWidth(size);
    }

    private void initFormTable() {
        formTableModel = new ConsulteTableModel(new Object[]{"Visible","Champ", "Libellé","Lien"});
        formTable = new JBTable(formTableModel);
        formTable.getEmptyText().setText("Aucune ligne à afficher");
        formTable.setDefaultRenderer(Object.class, new TableRenderer());
        formTable.setDragEnabled(true);
        formTable.setDropMode(DropMode.INSERT_ROWS);
        formTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        fixSizeColumn(formTable,0,30);
        fixSizeColumn(formTable,1,130);
        fixSizeColumn(formTable,2,160);
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
            formTableModel.addRow(new Object[]{Boolean.TRUE,fieldName, StringUtils.majStart(fieldName)});
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
            String nom = String.valueOf(model.getValueAt(i, 1));
            String libelle = String.valueOf(model.getValueAt(i, 2));
            boolean visible = (Boolean) model.getValueAt(i, 0);
            String lien = String.valueOf(model.getValueAt(i, 3));
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

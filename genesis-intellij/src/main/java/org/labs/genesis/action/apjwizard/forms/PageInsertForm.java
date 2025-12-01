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
import org.labs.genesis.action.apjwizard.forms.tablehandler.FormTableModel;
import org.labs.genesis.action.apjwizard.forms.tablehandler.TableRowTransferHandler;
import org.labs.genesis.apj.ApjGenerationContext;
import org.labs.genesis.apj.utilitaire.ApjField;
import org.labs.genesis.apj.utilitaire.UtilClassLoader;
import org.labs.utils.StringUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
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
    private JBTable formTable;
    private DefaultTableModel formTableModel;
    private ApjField[] dataForm;

    public PageInsertForm() {
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

    private void initFormTable() {
        formTableModel = new FormTableModel(new Object[]{"Champ", "Libellé","Visible"});
        formTable = new JBTable(formTableModel);
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
            .build().init();
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
            formTableModel.addRow(new Object[]{fieldName, StringUtils.majStart(fieldName),Boolean.TRUE});
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
            String nom = String.valueOf(model.getValueAt(i, 0));
            String libelle = String.valueOf(model.getValueAt(i, 1));
            boolean visible = (Boolean) model.getValueAt(i, 2);
            ApjField f = new ApjField();
            f.setNom(nom);
            f.setLibelle(libelle);
            f.setVisible(visible);
            fields[i] = f;
        }
        return fields;
    }
}

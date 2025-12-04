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
import org.labs.genesis.action.apjwizard.forms.tablehandler.InsertTableModel;
import org.labs.genesis.action.apjwizard.forms.tablehandler.TableRowTransferHandler;
import org.labs.genesis.apj.ApjGenerationContext;
import org.labs.genesis.apj.component.ApjField;
import org.labs.genesis.apj.utilitaire.UtilClassLoader;
import org.labs.utils.StringUtils;
import org.labs.genesis.apj.utilitaire.ConstantesApj;

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
    private final ApjGenerationContext context;
    private final Project project;

    public PageInsertForm(ApjGenerationContext context, Project project) {
        this.context = context;
        this.project = project;
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
        formTableModel = new InsertTableModel(new Object[]{"Visible","Champ","Libellé","Autre","Type","Détails"});
        formTable = new JBTable(formTableModel);
        formTable.setDefaultRenderer(Object.class, new TableRenderer());
        formTable.setDragEnabled(true);
        formTable.setDropMode(DropMode.INSERT_ROWS);
        formTable.setTransferHandler(new TableRowTransferHandler(formTable));
        scrollFiltre.setViewportView(formTable);
        scrollFiltre.setBorder(BorderFactory.createEmptyBorder());
        formTable.setBorder(BorderFactory.createEmptyBorder());
        scrollFiltre.setViewportBorder(null);
        DefaultActionGroup updateGroup = new DefaultActionGroup();
        updateGroup.add(new AnAction(ConstantesApj.LISTE) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                updateRows(ConstantesApj.LISTE);
            }
        });
        updateGroup.add(new AnAction(ConstantesApj.LISTE_STRING) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                updateRows(ConstantesApj.LISTE_STRING);
            }
        });
        updateGroup.add(new AnAction(ConstantesApj.OUI_NON) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                updateRows(ConstantesApj.OUI_NON);
            }
        });
        updateGroup.add(new AnAction(ConstantesApj.AUTO_COMPLETE) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                updateRows(ConstantesApj.AUTO_COMPLETE);
            }
        });
        updateGroup.add(new AnAction(ConstantesApj.AUTO_COMPLETE_INSERT) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                updateRows(ConstantesApj.AUTO_COMPLETE_INSERT);
            }
        });
        updateGroup.add(new AnAction(ConstantesApj.SIMPLE) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                updateRows(ConstantesApj.SIMPLE);
            }
        });
        TableToolbarHelper.builder()
            .table(formTable)
            .panel(filtrePanel)
            .updateActionGroup(updateGroup)
            .build().init();
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
        }

        formTableModel.setValueAt(type, selectedRow, 4);
        if (withDetail) {
            formTableModel.setValueAt(details, selectedRow, 5);
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

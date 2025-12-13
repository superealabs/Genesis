package org.labs.genesis.action.apjwizard.forms.popup;

import com.intellij.ide.util.TreeClassChooser;
import com.intellij.ide.util.TreeClassChooserFactory;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.ui.table.JBTable;
import lombok.Getter;
import lombok.Setter;
import org.labs.genesis.action.apjwizard.forms.helper.TableToolbarHelper;
import org.labs.genesis.action.apjwizard.forms.renderer.TableRenderer;
import org.labs.genesis.action.apjwizard.forms.tablehandler.ComboCellEditor;
import org.labs.genesis.action.apjwizard.forms.tablehandler.TableRowTransferHandler;
import org.labs.genesis.apj.ApjGenerationContext;
import org.labs.genesis.apj.component.ApjField;
import org.labs.genesis.apj.utilitaire.UtilClassLoader;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.lang.reflect.Field;
import java.net.URLClassLoader;
import java.util.LinkedHashMap;
import java.util.List;

@Getter
@Setter
public class AutoCompleteDialog extends JDialog{
    private JComboBox<String> affCombo;
    private JTextField mappingField;
    private JButton chooseClassButton;
    private JTextField nomTableField;
    private JButton chooseTableButton;
    private JPanel mainPanel;
    private JButton okButton;
    private JButton cancelButton;
    private JCheckBox inclurePageDInsertionCheckBox;
    private JTextField pageInsertionField;
    private JLabel pageLabel;
    private JLabel champAffLabel;
    private JScrollPane scroll;
    private JPanel tablePanel;
    private boolean validated;
    private final ApjGenerationContext context;
    private final Project project;
    private DefaultTableModel tableModel;
    private JBTable tableValues;
    private LinkedHashMap<String, ApjField> allFieldsMap = new LinkedHashMap<>();
    private String[] champRetourOptions = new String[0];
    private String[] mappingOptions = new String[0];


    public AutoCompleteDialog(JComponent parent, ApjGenerationContext context, Project project,LinkedHashMap<String, ApjField> allFieldsMap) {
        super(SwingUtilities.getWindowAncestor(parent));
        this.context = context;
        this.project = project;
        setModal(true);
        setTitle("AutoComplete");
        this.allFieldsMap = allFieldsMap;
        setContentPane(mainPanel);
        pack();
        setLocationRelativeTo(parent);

        initEvents();
        initHooks();
        initConfig();
        initTable();
    }

    public void initConfig() {
        pageInsertionField.setEnabled(false);
        champAffLabel.setEnabled(false);
        affCombo.setEnabled(false);
        pageLabel.setEnabled(false);
        inclurePageDInsertionCheckBox.addItemListener(e -> {
            boolean selected = inclurePageDInsertionCheckBox.isSelected();
            pageInsertionField.setEnabled(selected);
            champAffLabel.setEnabled(selected);
            affCombo.setEnabled(selected);
            pageLabel.setEnabled(selected);
        });
    }


    private void initHooks() {
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
                    Class<?> cls = loader.loadClass(selectedClass.getQualifiedName());
                    List<Field> fields = UtilClassLoader.listFieldsStopClassMAPTable(cls);
                    List<ApjField> apjFields = ApjField.javaFieldsToApjFields(fields);
                    List<String> names = apjFields.stream().map(ApjField::getNom).toList();
                    setFields(names);
                } catch (Exception ignored) {
                }
            }
        });

        chooseTableButton.addActionListener(e -> {
            TableTreeChooser chooser = new TableTreeChooser(mainPanel, context.getTables(), context.getVues());
            String table = chooser.showDialog();
            if (table != null) nomTableField.setText(table);
        });
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
        scroll.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        return table;
    }

    private void initTable() {
        tableModel = new DefaultTableModel(new Object[]{"Champ Retour","Champ Retour Mapping"},0);
        tableValues = initTable(tableValues, tableModel, scroll);
        tableValues.setModel(tableModel);
        tableValues.getEmptyText().setText("Aucune ligne à afficher");
        String[] mappingOptions = allFieldsMap.keySet().toArray(new String[0]);
        tableValues.getColumnModel().getColumn(1)
            .setCellEditor(new ComboCellEditor(mappingOptions));
        TableToolbarHelper.builder()
            .table(tableValues)
            .panel(tablePanel)
                .addAction((t) -> {
                    String defaultChamp = champRetourOptions.length > 0 ? champRetourOptions[0] : "";
                    String defaultMapping = mappingOptions.length > 0 ? mappingOptions[0] : "";
                    tableModel.addRow(new Object[]{defaultChamp, defaultMapping});
                })
            .removeAction(() -> {
                int selected = tableValues.getSelectedRow();
                if (selected >= 0) tableModel.removeRow(selected);
            })
            .build().init(1,1,1,1);
    }

    public void showDialog() {
        setVisible(true);
    }

    public String getDetails() {
        if (!validated) return null;
        if (tableValues.isEditing()) {
            tableValues.getCellEditor().stopCellEditing();
        }
        String mapping = mappingField.getText().trim();
        String table = nomTableField.getText().trim();
        StringBuilder result = new StringBuilder("{" + mapping + "," + table + "}");
        StringBuilder blocAff = new StringBuilder("{");
        StringBuilder blocVal = new StringBuilder("{");
        boolean first = true;
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String a = tableModel.getValueAt(i, 0) != null ? tableModel.getValueAt(i, 0).toString().trim() : "";
            String v = tableModel.getValueAt(i, 1) != null ? tableModel.getValueAt(i, 1).toString().trim() : "";
            if (a.isEmpty() && v.isEmpty()) continue;
            if (a.isEmpty()) a = v;
            if (v.isEmpty()) v = a;
            if (!first) {
                blocAff.append(",");
                blocVal.append(",");
            } else {
                first = false;
            }
            blocAff.append(a);
            blocVal.append(v);
        }
        blocAff.append("}");
        blocVal.append("}");
        result.append(blocAff).append(blocVal);
        if (inclurePageDInsertionCheckBox.isSelected()) {
            String pageInsert = pageInsertionField.getText().trim();
            String affComboVal = affCombo.getSelectedItem() != null ? affCombo.getSelectedItem().toString().trim() : "";
            result.append("{").append(pageInsert).append(",").append(affComboVal).append("}");
        }
        return result.toString();
    }

    private void initEvents() {
        okButton.addActionListener(e -> {
            if (mappingField.getText().trim().isEmpty()) return;
            if (nomTableField.getText().trim().isEmpty()) return;
            if (affCombo.getSelectedItem() == null) return;

            validated = true;
            dispose();
        });

        cancelButton.addActionListener(e -> {
            validated = false;
            dispose();
        });
    }

    public void setFields(List<String> fieldNames) {
        affCombo.removeAllItems();
        for (String f : fieldNames) {
            affCombo.addItem(f);
        }
        champRetourOptions = fieldNames.toArray(new String[0]);

        if (tableValues == null || tableValues.getColumnModel().getColumnCount() == 0) {
            return;
        }

        tableValues.getColumnModel().getColumn(0)
                .setCellEditor(new ComboCellEditor(champRetourOptions));
    }


}

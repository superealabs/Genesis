package org.labs.genesis.action.apjwizard.forms.popup;

import com.intellij.ide.util.TreeClassChooser;
import com.intellij.ide.util.TreeClassChooserFactory;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import lombok.Getter;
import lombok.Setter;
import org.labs.genesis.apj.ApjGenerationContext;
import org.labs.genesis.apj.component.ApjField;
import org.labs.genesis.apj.utilitaire.UtilClassLoader;

import javax.swing.*;
import java.lang.reflect.Field;
import java.net.URLClassLoader;
import java.util.List;

@Getter
@Setter
public class ListDetailsDialog extends JDialog{
    private JLabel nomTableLabel;
    private JComboBox<String> affCombo;
    private JComboBox<String> valCombo;
    private JTextField mappingField;
    private JButton chooseClassButton;
    private JTextField nomTableField;
    private JButton chooseTableButton;
    private JPanel mainPanel;
    private JButton okButton;
    private JButton cancelButton;
    private boolean validated;
    private final ApjGenerationContext context;
    private final Project project;

    public ListDetailsDialog(JComponent parent, ApjGenerationContext context, Project project) {
        super(SwingUtilities.getWindowAncestor(parent));
        this.context = context;
        this.project = project;
        setModal(true);
        setTitle("Liste");
        setContentPane(mainPanel);
        pack();
        setLocationRelativeTo(parent);

        initEvents();
        initHooks();
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

    public void showDialog() {
        setVisible(true);
    }

    public String getDetails() {
        if (!validated) return null;

        String mapping = mappingField.getText().trim();
        String table = nomTableField.getText().trim();
        String aff = String.valueOf(affCombo.getSelectedItem());
        String val = String.valueOf(valCombo.getSelectedItem());

        return "{" + mapping + "," + table + "," + aff + "," + val + "}";
    }


    private void initEvents() {
        okButton.addActionListener(e -> {
            if (mappingField.getText().trim().isEmpty()) return;
            if (nomTableField.getText().trim().isEmpty()) return;
            if (affCombo.getSelectedItem() == null) return;
            if (valCombo.getSelectedItem() == null) return;

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
        valCombo.removeAllItems();

        for (String f : fieldNames) {
            affCombo.addItem(f);
            valCombo.addItem(f);
        }
    }

}

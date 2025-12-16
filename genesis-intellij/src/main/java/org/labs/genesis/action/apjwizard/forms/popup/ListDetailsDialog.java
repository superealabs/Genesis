package org.labs.genesis.action.apjwizard.forms.popup;

import com.intellij.ide.util.TreeClassChooser;
import com.intellij.ide.util.TreeClassChooserFactory;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import lombok.Getter;
import lombok.Setter;
import org.labs.genesis.action.apjwizard.forms.helper.ProgressUtils;
import org.labs.genesis.apj.ApjGenerationContext;
import org.labs.genesis.apj.component.ApjField;
import org.labs.genesis.apj.utilitaire.Database;
import org.labs.genesis.apj.utilitaire.UtilClassLoader;
import org.labs.genesis.apj.utilitaire.UtilDBDynamique;

import javax.swing.*;
import java.lang.reflect.Field;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.util.*;

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
    private LinkedHashMap<String, ApjField> allInitialFieldsMap = new LinkedHashMap<>();
    private LinkedHashMap<String, ApjField> allFieldsMap = new LinkedHashMap<>();
    private LinkedHashMap<String, ApjField> allFieldsDataBaseMap = new LinkedHashMap<>();

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
        addActionListenerOnClassButton();
        addActionListenerOnTableButton();
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

            ProgressUtils.updateProgress(indicator, "Mise à jour des champs disponibles...", 0.85);
            ApplicationManager.getApplication().invokeAndWait(() -> {
                loadFieldsMap(apjFields);
                setFields();
            });
            ProgressUtils.updateProgress(indicator, "Terminé", 1.0);
        });
    }

    private void loadFieldsMap(List<ApjField> fields) {
        allFieldsMap.clear();
        allInitialFieldsMap.clear();
        for (ApjField field : fields) {
            allFieldsMap.put(field.getNom(), field);
            allInitialFieldsMap.put(field.getNom(), field);
        }
    }

    public void setFields() {
        affCombo.removeAllItems();
        valCombo.removeAllItems();
        for (Map.Entry<String, ApjField> entry : allFieldsMap.entrySet()) {
            String key = entry.getKey();
            affCombo.addItem(key);
            valCombo.addItem(key);
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
        ProgressUtils.runWithProgress(project, "Chargement des colonnes de \""+table+"\"...", indicator -> {
            try (Connection conn = UtilDBDynamique.GetConn(context.getProjectJarDir(), context.getLibDir())) {
                List<ApjField> fields = Database.getTableColumns(conn, table);

                ProgressUtils.updateProgress(indicator, "Mise à jour du tableau...", 0.85);
                ApplicationManager.getApplication().invokeAndWait(() -> loadAllFieldsBase(fields));

                ProgressUtils.updateProgress(indicator, "Terminé", 1.0);
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
        removeNonCommun();
        setFields();
    }

    private void removeNonCommun(){
        Map<String, String> champsJava = new HashMap<>();
        for (String nom : allFieldsMap.keySet()) {
            champsJava.put(nom.toLowerCase(), nom);
        }
        Map<String, String> champsBase = new HashMap<>();
        for (String nom : allFieldsDataBaseMap.keySet()) {
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
            allFieldsMap.remove(key);
        }
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
}

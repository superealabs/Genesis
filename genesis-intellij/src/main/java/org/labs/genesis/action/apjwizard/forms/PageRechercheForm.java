package org.labs.genesis.action.apjwizard.forms;

import com.intellij.icons.AllIcons;
import com.intellij.ui.JBColor;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.TreeSpeedSearch;
import com.intellij.ui.table.JBTable;
import lombok.Getter;
import lombok.Setter;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.*;
import com.intellij.ui.treeStructure.Tree;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.*;
import java.util.List;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.ide.util.TreeClassChooser;
import com.intellij.ide.util.TreeClassChooserFactory;

@Getter
@Setter
public class PageRechercheForm {
    private JPanel mainPanel;
    private JTextField nomField;
    private JTextField mappingField;
    private JTextField nomTableField;
    private JTextField titreField;
    private JLabel nomLabel;
    private JPanel generalPanel;
    private JLabel nomTableLabel;
    private JLabel titreLabel;
    private JPanel propertiesPanel;
    private JScrollPane scrollProperties;
    private JTabbedPane tabPane;
    private JPanel filtrePanel;
    private JPanel recapitulationPanel;
    private JPanel tableauPanel;
    private JScrollPane scrollFiltre;
    private JScrollPane scrollRecap;
    private JScrollPane scrollTableau;
    private JButton chooseClassButton;
    private JPanel mappingPanel;
    private JButton chooseTableButton;
    private JBTable filtreTable;
    private JBTable recapTable;
    private JBTable tableauTable;
    private DefaultTableModel filtreTableModel;
    private DefaultTableModel recapTableModel;
    private DefaultTableModel tableauTableModel;


    public void createDecorator(JPanel panel, JBTable table, DefaultTableModel tableModel){
        ToolbarDecorator decorator = ToolbarDecorator.createDecorator(table)
                .setAddAction(anActionButton -> { })
                .setRemoveAction(anActionButton -> {
                    int selectedRow = table.getSelectedRow();
                    if (selectedRow >= 0) {
                        tableModel.removeRow(selectedRow);
                    }
                });
        JPanel decoPanel = decorator.createPanel();
        decoPanel.setBorder(BorderFactory.createEmptyBorder());
        decoPanel.setBorder(BorderFactory.createMatteBorder(0, 1, 1, 1, JBColor.border()));
        panel.removeAll();
        panel.setLayout(new BorderLayout());
        panel.add(decoPanel, BorderLayout.CENTER);
    }

    public PageRechercheForm() {
        if (scrollProperties != null) {
            scrollProperties.setBorder(BorderFactory.createEmptyBorder());
            scrollProperties.setViewportBorder(null);
        }
        initTables();
        chooseClassButton.setBorder(UIManager.getBorder("TextField.border"));
        chooseClassButton.setContentAreaFilled(true);
        chooseClassButton.setFocusPainted(true);
        chooseClassButton.setBackground(mappingField.getBackground());
        nomTableField.setEditable(false);
    }

    private void initTables() {
        filtreTableModel = new DefaultTableModel(new Object[]{"Champ", "Libellé"}, 0);
        filtreTable = new JBTable(filtreTableModel);
        scrollFiltre.setViewportView(filtreTable);
        scrollFiltre.setBorder(BorderFactory.createEmptyBorder());
        scrollFiltre.setViewportBorder(null);
        createDecorator(filtrePanel, filtreTable, filtreTableModel);

        recapTableModel = new DefaultTableModel(new Object[]{"Colonne", "Libellé"}, 0);
        recapTable = new JBTable(recapTableModel);
        scrollRecap.setViewportView(recapTable);
        scrollRecap.setBorder(BorderFactory.createEmptyBorder());
        scrollRecap.setViewportBorder(null);
        createDecorator(recapitulationPanel, recapTable, recapTableModel);

        tableauTableModel = new DefaultTableModel(new Object[]{"Colonne", "Libellé"}, 0);
        tableauTable = new JBTable(tableauTableModel);
        scrollTableau.setViewportView(tableauTable);
        scrollTableau.setBorder(BorderFactory.createEmptyBorder());
        scrollTableau.setViewportBorder(null);
        createDecorator(tableauPanel, tableauTable, tableauTableModel);
    }

    public void showClassChooser(Project project) {
        chooseClassButton.addActionListener(e -> {
            TreeClassChooser chooser = TreeClassChooserFactory.getInstance(project)
                    .createAllProjectScopeChooser("Select Class");
            chooser.showDialog();
            PsiClass selectedClass = chooser.getSelected();
            if (selectedClass != null) {
                mappingField.setText(selectedClass.getQualifiedName());
            }
        });
    }

    public void showTableTree(List<String> tables, List<String> views) {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("root");
        DefaultMutableTreeNode tablesNode = new DefaultMutableTreeNode("Tables");
        tables.forEach(t -> tablesNode.add(new DefaultMutableTreeNode(t)));
        root.add(tablesNode);

        DefaultMutableTreeNode viewsNode = new DefaultMutableTreeNode("Views");
        views.forEach(v -> viewsNode.add(new DefaultMutableTreeNode(v)));
        root.add(viewsNode);

        Tree tree = new Tree(root);
        tree.setRootVisible(false);
        tree.setShowsRootHandles(true);
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

        tree.getSelectionModel().addTreeSelectionListener(e -> {
            TreePath path = e.getNewLeadSelectionPath();
            if (path != null) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                if (!node.isLeaf()) {
                    tree.clearSelection();
                }
            }
        });

        new TreeSpeedSearch(tree, path -> {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
            return node.getUserObject().toString();
        }, true);

        tree.setCellRenderer(new DefaultTreeCellRenderer() {
            @Override
            public Component getTreeCellRendererComponent(JTree tree, Object value,
                                                          boolean selected, boolean expanded,
                                                          boolean leaf, int row, boolean hasFocus) {
                super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);

                setBackgroundNonSelectionColor(JBColor.WHITE);
                setBorderSelectionColor(null);

                if (value instanceof DefaultMutableTreeNode node && node.isLeaf()) {
                    if (node.getParent() == tablesNode) setIcon(AllIcons.Nodes.DataTables);
                    else if (node.getParent() == viewsNode) setIcon(AllIcons.General.InspectionsEye);
                } else setIcon(null);

                return this;
            }
        });

        TableChooserDialog dialog = new TableChooserDialog(mainPanel, tree);
        if (dialog.showAndGet()) {
            TreePath path = dialog.getSelection();
            if (path != null) {
                DefaultMutableTreeNode node =
                        (DefaultMutableTreeNode) path.getLastPathComponent();
                nomTableField.setText(node.getUserObject().toString());
            }
        }

        tree.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    TreePath path = tree.getSelectionPath();
                    if (path != null) {
                        DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                        if (node.isLeaf()) {
                            dialog.triggerOK();
                        }
                    }
                }
            }
        });


    }



}

package org.labs.genesis.action.apjwizard.forms.popup;

import com.intellij.icons.AllIcons;
import com.intellij.ui.JBColor;
import com.intellij.ui.TreeSpeedSearch;
import com.intellij.ui.treeStructure.Tree;
import lombok.Getter;
import lombok.Setter;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

@Getter
@Setter
public class TableTreeChooser {

    private final JPanel parentPanel;
    private final String[] tables;
    private final String[] views;
    private String selectedTable;

    public TableTreeChooser(JPanel parentPanel, String[] tables, String[] views) {
        this.parentPanel = parentPanel;
        this.tables = tables;
        this.views = views;
    }


    public String showDialog() {
        if ((tables == null || tables.length == 0) && (views == null || views.length == 0)) {
            new EmptyDialog(parentPanel, "Aucune table ni vue disponible").showDialog();
            return null;
        }


        DefaultMutableTreeNode root = new DefaultMutableTreeNode("root");
        DefaultMutableTreeNode tablesNode = new DefaultMutableTreeNode("Tables");
        if (tables != null) {
            for (String t : tables) {
                tablesNode.add(new DefaultMutableTreeNode(t));
            }
        }
        root.add(tablesNode);

        DefaultMutableTreeNode viewsNode = new DefaultMutableTreeNode("Vues");
        if (views != null) {
            for (String v : views) {
                viewsNode.add(new DefaultMutableTreeNode(v));
            }
        }
        if (this.getViews().length > 0) {
            root.add(viewsNode);
        }

        Tree tree = new Tree(root);
        tree.setRootVisible(false);
        tree.setShowsRootHandles(true);
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

        tree.getSelectionModel().addTreeSelectionListener(e -> {
            TreePath path = e.getNewLeadSelectionPath();
            if (path != null) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                if (!node.isLeaf()) tree.clearSelection();
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

        TableChooserDialog dialog = new TableChooserDialog(parentPanel, tree);
        tree.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    TreePath path = tree.getSelectionPath();
                    if (path != null) {
                        DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                        if (node.isLeaf()) dialog.triggerOK();
                    }
                }
            }
        });

        if (dialog.showAndGet()) {
            TreePath path = dialog.getSelection();
            if (path != null) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                selectedTable = node.getUserObject().toString();
            }
        }
        return selectedTable;
    }
}

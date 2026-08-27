package org.labs.genesis.forms.ui.data.renderer;

import com.intellij.icons.AllIcons;
import org.labs.genesis.forms.theme.DashboardTheme;
import org.labs.genesis.forms.ui.data.DataPanelTree;
import org.labs.genesis.forms.ui.data.model.ColumnData;
import org.labs.genesis.forms.ui.data.model.TableData;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellRenderer;
import java.awt.*;

public class DataTreeCellRenderer extends JPanel implements TreeCellRenderer {
    private final JLabel iconLabel = new JLabel();
    private final JLabel textLabel = new JLabel();

    public DataTreeCellRenderer() {
        setLayout(new BorderLayout(6, 0));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
        iconLabel.setOpaque(false);
        textLabel.setOpaque(false);
        add(iconLabel, BorderLayout.WEST);
        add(textLabel, BorderLayout.CENTER);
    }

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded,
                                                  boolean leaf, int row, boolean hasFocus) {
        setOpaque(false);
        iconLabel.setIcon(null);
        textLabel.setText("");
        textLabel.setForeground(DashboardTheme.TEXT);

        DefaultMutableTreeNode node = (value instanceof DefaultMutableTreeNode) ? (DefaultMutableTreeNode) value : null;
        if (node == null) return this;

        Object userObject = node.getUserObject();
        if (userObject instanceof TableData table) {
            iconLabel.setIcon(AllIcons.Nodes.Folder);
            textLabel.setText(table.name);
            textLabel.setFont(DashboardTheme.boldFont(13));
        } else if (userObject instanceof ColumnData column) {
            textLabel.setText(column.name + "   " + column.type);
            textLabel.setFont(DashboardTheme.getFont(12));
            iconLabel.setIcon(column.isPrimaryKey ? AllIcons.Nodes.C_private : AllIcons.Nodes.C_plocal);
        } else {
            textLabel.setText(String.valueOf(userObject));
        }
        return this;
    }
}

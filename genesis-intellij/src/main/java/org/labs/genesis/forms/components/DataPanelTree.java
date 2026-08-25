package org.labs.genesis.forms.components;

import com.intellij.icons.AllIcons;
import com.intellij.ui.treeStructure.Tree;
import org.jspecify.annotations.NonNull;
import org.labs.genesis.forms.theme.DashboardTheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DataPanelTree extends JPanel {

    private final Tree tree;
    private JTextField searchField;
    private JButton resetButton;
    private JButton refreshButton;
    private final JScrollPane scrollPane;

    private List<TableData> tables;

    public DataPanelTree() {
        this(createDefaultTables());
    }

    public DataPanelTree(List<TableData> tables) {
        this.tables = tables != null ? tables : createDefaultTables();

        setLayout(new BorderLayout(0, 8));
        setOpaque(false);

        add(createSearchBar("Search tables or columns..."), BorderLayout.NORTH);
        tree = createTree();
        scrollPane = createScrollPane();
        add(scrollPane, BorderLayout.CENTER);
        populateTree();
    }

    private static List<TableData> createDefaultTables() {
        return List.of(
                new TableData("users", List.of(
                        new ColumnData("id", "INT", true, false),
                        new ColumnData("name", "VARCHAR", false, true),
                        new ColumnData("email", "VARCHAR", false, true),
                        new ColumnData("role_id", "BIGINT", false, false)
                )),
                new TableData("roles", List.of(
                        new ColumnData("id", "INT", true, false),
                        new ColumnData("name", "VARCHAR", false, true)
                )),
                new TableData("orders", List.of(
                        new ColumnData("id", "INT", true, false),
                        new ColumnData("user_id", "BIGINT", false, false),
                        new ColumnData("total", "DECIMAL", false, true),
                        new ColumnData("status", "VARCHAR", false, true)
                ))
        );
    }

    public void setTables(List<TableData> tables) {
        this.tables = tables != null ? tables : createDefaultTables();
        populateTree();
    }

    private JPanel createSearchBar(String message) {
        JPanel container = new JPanel(new BorderLayout(6, 0));
        container.setOpaque(false);

        JPanel fieldPanel = new JPanel(new BorderLayout());
        fieldPanel.setOpaque(true);
        fieldPanel.setBackground(DashboardTheme.SURFACE_2);
        fieldPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(DashboardTheme.BORDER_SUBTLE, 1),
                new EmptyBorder(0, 8, 0, 4)
        ));

        JLabel searchIcon = new JLabel(AllIcons.Actions.Find);
        searchIcon.setBorder(new EmptyBorder(0, 0, 0, 4));
        fieldPanel.add(searchIcon, BorderLayout.WEST);

        searchField = new JTextField();
        searchField.setOpaque(false);
        searchField.setBorder(null);
        searchField.setForeground(DashboardTheme.TEXT);
        searchField.setCaretColor(DashboardTheme.TEXT);
        searchField.setFont(searchField.getFont().deriveFont(12f));
        searchField.putClientProperty("JTextField.placeholderText", message);
        fieldPanel.add(searchField, BorderLayout.CENTER);

        resetButton = createIconButton(AllIcons.Actions.Cancel, "Clear search");
        resetButton.setVisible(false);
        fieldPanel.add(resetButton, BorderLayout.EAST);

        refreshButton = createIconButton(AllIcons.Actions.Refresh, "Refresh tables and columns");
        container.add(fieldPanel, BorderLayout.CENTER);
        container.add(refreshButton, BorderLayout.EAST);

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { applyFilter(); }
            @Override public void removeUpdate(DocumentEvent e) { applyFilter(); }
            @Override public void changedUpdate(DocumentEvent e) { applyFilter(); }
        });
        resetButton.addActionListener(e -> clearSearch());
        refreshButton.addActionListener(e -> refreshData());

        return container;
    }

    private JButton createIconButton(Icon icon, String tooltip) {
        JButton button = new JButton(icon);
        button.setToolTipText(tooltip);
        button.setFocusable(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setMargin(new Insets(0, 4, 0, 4));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(28, 28));
        return button;
    }

    private Tree createTree() {
        Tree result = createResult();
        result.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

        result.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() != 2) return;
                TreePath path = result.getPathForLocation(e.getX(), e.getY());
                if (path == null) return;
                Object last = path.getLastPathComponent();
                if (last instanceof DefaultMutableTreeNode node && node.getUserObject() instanceof TableData table) {
                    System.out.println("Double click table: " + table.name);
                }
            }
        });
        return result;
    }

    private static @NonNull Tree createResult() {
        Tree result = new Tree() {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(super.getPreferredSize().width, getRowHeight() * getRowCount() + 20);
            }
        };
        result.setOpaque(false);
        result.setBackground(new Color(0, 0, 0, 0));
        result.setForeground(DashboardTheme.TEXT);
        result.setRowHeight(28);
        result.setShowsRootHandles(true);
        result.setRootVisible(false);
        result.setCellRenderer(new DataTreeCellRenderer());
        return result;
    }

    private JScrollPane createScrollPane() {
        JScrollPane pane = new JScrollPane(tree);
        pane.setOpaque(false);
        pane.setBackground(new Color(0, 0, 0, 0));
        pane.getViewport().setOpaque(false);
        pane.getViewport().setBackground(new Color(0, 0, 0, 0));
        pane.setBorder(BorderFactory.createEmptyBorder());
        pane.getVerticalScrollBar().setUnitIncrement(16);
        return pane;
    }

    private void populateTree() {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Tables");
        for (TableData table : tables) {
            DefaultMutableTreeNode tableNode = new DefaultMutableTreeNode(table);
            for (ColumnData column : table.columns) {
                tableNode.add(new DefaultMutableTreeNode(column));
            }
            root.add(tableNode);
        }
        tree.setModel(new DefaultTreeModel(root));
        expandAllNodes();
    }

    private void applyFilter() {
        String query = searchField.getText().trim().toLowerCase(Locale.ROOT);
        resetButton.setVisible(!query.isEmpty());

        if (query.isEmpty()) {
            populateTree();
            return;
        }

        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Tables");
        for (TableData table : tables) {
            boolean tableMatches = table.name.toLowerCase(Locale.ROOT).contains(query);
            List<ColumnData> matchingColumns = new ArrayList<>();
            for (ColumnData col : table.columns) {
                if (col.name.toLowerCase(Locale.ROOT).contains(query) ||
                        col.type.toLowerCase(Locale.ROOT).contains(query)) {
                    matchingColumns.add(col);
                }
            }
            if (tableMatches || !matchingColumns.isEmpty()) {
                DefaultMutableTreeNode tableNode = new DefaultMutableTreeNode(table);
                List<ColumnData> columnsToDisplay = tableMatches ? table.columns : matchingColumns;
                for (ColumnData col : columnsToDisplay) {
                    tableNode.add(new DefaultMutableTreeNode(col));
                }
                root.add(tableNode);
            }
        }
        tree.setModel(new DefaultTreeModel(root));
        expandAllNodes();
    }

    private void clearSearch() {
        searchField.setText("");
        searchField.requestFocusInWindow();
        resetButton.setVisible(false);
    }

    private void refreshData() {
        String currentQuery = searchField.getText().trim();
        populateTree();
        if (!currentQuery.isEmpty()) applyFilter();
        refreshButton.requestFocusInWindow();
    }

    private void expandAllNodes() {
        for (int i = 0; i < tree.getRowCount(); i++) {
            tree.expandRow(i);
        }
    }

    // ---- Data models ----

    public static class TableData {
        public final String name;
        public final List<ColumnData> columns;
        public TableData(String name, List<ColumnData> columns) {
            this.name = name;
            this.columns = columns;
        }
        @Override public String toString() { return name; }
    }

    public static class ColumnData {
        public final String name;
        public final String type;
        public final boolean isPrimaryKey;
        public final boolean isNullable;
        public ColumnData(String name, String type, boolean isPrimaryKey, boolean isNullable) {
            this.name = name;
            this.type = type;
            this.isPrimaryKey = isPrimaryKey;
            this.isNullable = isNullable;
        }
        @Override public String toString() { return name + "  " + type; }
    }

    // ---- Renderer ----

    private static class DataTreeCellRenderer extends JPanel implements TreeCellRenderer {
        private final JLabel iconLabel = new JLabel();
        private final JLabel textLabel = new JLabel();

        DataTreeCellRenderer() {
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
}
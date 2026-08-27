package org.labs.genesis.forms.ui.data;

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
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DataPanelTree extends JPanel {

    // -------------------------------------------------------------------------
    // DATA FLAVOR
    // -------------------------------------------------------------------------

    public static final DataFlavor COLUMN_DATA_FLAVOR =
            new DataFlavor(org.labs.genesis.forms.components.DataPanelTree.ColumnData.class, "Database Column");

    // -------------------------------------------------------------------------
    // UI
    // -------------------------------------------------------------------------

    private final Tree tree;
    private JTextField searchField;
    private JButton resetButton;
    private JButton refreshButton;
    private final JScrollPane scrollPane;

    private List<org.labs.genesis.forms.components.DataPanelTree.TableData> tables;

    // -------------------------------------------------------------------------
    // DRAG & DROP
    // -------------------------------------------------------------------------

    private final DragSource dragSource;

    // -------------------------------------------------------------------------
    // CONSTRUCTORS
    // -------------------------------------------------------------------------

    public DataPanelTree() {
        this(createDefaultTables());
    }

    public DataPanelTree(List<org.labs.genesis.forms.components.DataPanelTree.TableData> tables) {
        this.tables = tables != null ? tables : createDefaultTables();

        setLayout(new BorderLayout(0, 8));
        setOpaque(false);

        add(createSearchBar("Search tables or columns..."), BorderLayout.NORTH);
        tree = createTree();
        scrollPane = createScrollPane();
        add(scrollPane, BorderLayout.CENTER);
        populateTree();

        dragSource = new DragSource();
        installDragAndDrop();
    }

    // -------------------------------------------------------------------------
    // DEFAULT DATA
    // -------------------------------------------------------------------------

    private static List<org.labs.genesis.forms.components.DataPanelTree.TableData> createDefaultTables() {
        return List.of(
                new org.labs.genesis.forms.components.DataPanelTree.TableData("users", List.of(
                        new org.labs.genesis.forms.components.DataPanelTree.ColumnData("id", "INT", true, false),
                        new org.labs.genesis.forms.components.DataPanelTree.ColumnData("name", "VARCHAR", false, true),
                        new org.labs.genesis.forms.components.DataPanelTree.ColumnData("email", "VARCHAR", false, true),
                        new org.labs.genesis.forms.components.DataPanelTree.ColumnData("role_id", "BIGINT", false, false)
                )),
                new org.labs.genesis.forms.components.DataPanelTree.TableData("roles", List.of(
                        new org.labs.genesis.forms.components.DataPanelTree.ColumnData("id", "INT", true, false),
                        new org.labs.genesis.forms.components.DataPanelTree.ColumnData("name", "VARCHAR", false, true)
                )),
                new org.labs.genesis.forms.components.DataPanelTree.TableData("orders", List.of(
                        new org.labs.genesis.forms.components.DataPanelTree.ColumnData("id", "INT", true, false),
                        new org.labs.genesis.forms.components.DataPanelTree.ColumnData("user_id", "BIGINT", false, false),
                        new org.labs.genesis.forms.components.DataPanelTree.ColumnData("total", "DECIMAL", false, true),
                        new org.labs.genesis.forms.components.DataPanelTree.ColumnData("status", "VARCHAR", false, true)
                ))
        );
    }

    // -------------------------------------------------------------------------
    // TABLES
    // -------------------------------------------------------------------------

    public void setTables(List<org.labs.genesis.forms.components.DataPanelTree.TableData> tables) {
        this.tables = tables != null ? tables : createDefaultTables();
        populateTree();
    }

    // -------------------------------------------------------------------------
    // DRAG & DROP - INSTALLATION
    // -------------------------------------------------------------------------

    private void installDragAndDrop() {
        DragGestureRecognizer recognizer = dragSource.createDefaultDragGestureRecognizer(
                tree,
                DnDConstants.ACTION_COPY,
                this::startColumnDrag
        );
        recognizer.setComponent(tree);
    }

    /**
     * Appelé lorsqu'un drag commence sur le JTree.
     */
    private void startColumnDrag(DragGestureEvent event) {
        TreePath path = tree.getPathForLocation(
                event.getDragOrigin().x,
                event.getDragOrigin().y
        );

        if (path == null) return;

        Object lastPathComponent = path.getLastPathComponent();

        if (!(lastPathComponent instanceof DefaultMutableTreeNode node)) return;

        Object userObject = node.getUserObject();

        if (!(userObject instanceof org.labs.genesis.forms.components.DataPanelTree.ColumnData columnData)) return;

        // Trouver le nom de la table parente
        String tableName = findParentTableName(path);

        // Créer le Transferable
        Transferable transferable = createColumnTransferable(columnData);

        // Créer l'image de drag avec "table.column"
        BufferedImage dragImage = createDragImage(columnData, tableName);

        // Offset pour centrer légèrement
        Point imageOffset = new Point(10, 10);

        try {
            Cursor handCursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);

            event.startDrag(
                    handCursor,
                    dragImage,
                    imageOffset,
                    transferable,
                    new DataPanelTree.ColumnDragSourceListener()
            );
        } catch (InvalidDnDOperationException ex) {
            Toolkit.getDefaultToolkit().beep();
        }
    }

    /**
     * Trouve le nom de la table parente d'une colonne.
     */
    private String findParentTableName(TreePath path) {
        if (path.getPathCount() >= 3) {
            Object parentNode = path.getPathComponent(path.getPathCount() - 2);
            if (parentNode instanceof DefaultMutableTreeNode parentTreeNode) {
                Object parentUserObject = parentTreeNode.getUserObject();
                if (parentUserObject instanceof org.labs.genesis.forms.components.DataPanelTree.TableData tableData) {
                    return tableData.name;
                }
            }
        }
        return "";
    }

    /**
     * Crée le Transferable utilisé par le dashboard.
     */
    private Transferable createColumnTransferable(org.labs.genesis.forms.components.DataPanelTree.ColumnData columnData) {
        return new Transferable() {
            private final DataFlavor[] flavors = {
                    COLUMN_DATA_FLAVOR,
                    DataFlavor.stringFlavor
            };

            @Override
            public DataFlavor[] getTransferDataFlavors() {
                return flavors.clone();
            }

            @Override
            public boolean isDataFlavorSupported(DataFlavor flavor) {
                return COLUMN_DATA_FLAVOR.equals(flavor) || DataFlavor.stringFlavor.equals(flavor);
            }

            @Override
            public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
                if (COLUMN_DATA_FLAVOR.equals(flavor)) {
                    return columnData;
                }
                if (DataFlavor.stringFlavor.equals(flavor)) {
                    return columnData.name;
                }
                throw new UnsupportedFlavorException(flavor);
            }
        };
    }

    // -------------------------------------------------------------------------
    // DRAG IMAGE - Badge élégant "table.column"
    // -------------------------------------------------------------------------

    /**
     * Crée un badge élégant qui suit la souris pendant le drag.
     * Affiche "table.column" avec une icône et un design moderne.
     */
    private BufferedImage createDragImage(org.labs.genesis.forms.components.DataPanelTree.ColumnData column, String tableName) {
        String displayText = tableName != null && !tableName.isEmpty()
                ? tableName + "." + column.name
                : column.name;

        Font font = DashboardTheme.boldFont(12);
        FontMetrics metrics = getFontMetrics(font);

        int textWidth = metrics.stringWidth(displayText);
        int textHeight = metrics.getHeight();

        Icon icon = column.isPrimaryKey ? AllIcons.Nodes.C_private : AllIcons.Nodes.C_plocal;

        int horizontalPadding = 14;
        int verticalPadding = 8;
        int iconGap = 8;

        int iconWidth = icon != null ? icon.getIconWidth() : 0;
        int iconHeight = icon != null ? icon.getIconHeight() : 0;

        int contentWidth = iconWidth + iconGap + textWidth;
        int width = contentWidth + horizontalPadding * 2;
        int height = Math.max(iconHeight, textHeight) + verticalPadding * 2;

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();

        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            // --- Ombre portée ---
            g.setColor(new Color(0, 0, 0, 40));
            g.fillRoundRect(2, 3, width - 3, height - 3, 12, 12);

            // --- Fond principal avec dégradé ---
            GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(45, 47, 54),
                    0, height, new Color(35, 37, 42)
            );
            g.setPaint(gradient);
            RoundRectangle2D shape = new RoundRectangle2D.Double(0, 0, width - 3, height - 3, 12, 12);
            g.fill(shape);

            // --- Bordure ---
            g.setColor(new Color(255, 255, 255, 60));
            g.setStroke(new BasicStroke(1.2f));
            g.draw(shape);

            // --- Icône ---
            if (icon != null) {
                int iconX = 10;
                int iconY = (height - icon.getIconHeight()) / 2;
                icon.paintIcon(null, g, iconX, iconY);
            }

            // --- Texte ---
            g.setFont(font);
            g.setColor(Color.WHITE);

            int textX = 10 + iconWidth + iconGap;
            int textY = (height - metrics.getHeight()) / 2 + metrics.getAscent();
            g.drawString(displayText, textX, textY);

            // --- Petit indicateur de type (INT, VARCHAR, etc.) ---
            Font typeFont = DashboardTheme.getFont(9).deriveFont(Font.PLAIN);
            g.setFont(typeFont);
            g.setColor(new Color(255, 255, 255, 140));
            FontMetrics typeMetrics = g.getFontMetrics(typeFont);
            int typeX = textX + textWidth + 6;
            int typeY = (height - typeMetrics.getHeight()) / 2 + typeMetrics.getAscent();
            g.drawString(column.type, typeX, typeY);

        } finally {
            g.dispose();
        }

        return image;
    }

    // -------------------------------------------------------------------------
    // DRAG SOURCE LISTENER
    // -------------------------------------------------------------------------

    private static class ColumnDragSourceListener extends DragSourceAdapter {
        @Override
        public void dragEnter(DragSourceDragEvent event) {
            // Rien à faire
        }

        @Override
        public void dragExit(DragSourceEvent event) {
            // Rien à faire
        }

        @Override
        public void dragOver(DragSourceDragEvent event) {
            // Rien à faire
        }

        @Override
        public void dropActionChanged(DragSourceDragEvent event) {
            // Rien à faire
        }

        @Override
        public void dragDropEnd(DragSourceDropEvent event) {
            // Le drag est terminé
        }
    }

    // -------------------------------------------------------------------------
    // SEARCH BAR
    // -------------------------------------------------------------------------

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
            @Override
            public void insertUpdate(DocumentEvent e) { applyFilter(); }
            @Override
            public void removeUpdate(DocumentEvent e) { applyFilter(); }
            @Override
            public void changedUpdate(DocumentEvent e) { applyFilter(); }
        });
        resetButton.addActionListener(e -> clearSearch());
        refreshButton.addActionListener(e -> refreshData());

        return container;
    }

    // -------------------------------------------------------------------------
    // ICON BUTTON
    // -------------------------------------------------------------------------

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

    // -------------------------------------------------------------------------
    // TREE
    // -------------------------------------------------------------------------

    private Tree createTree() {
        Tree result = createResult();
        result.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

        result.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() != 2) return;
                TreePath path = result.getPathForLocation(e.getX(), e.getY());
                if (path == null) return;
                Object last = path.getLastPathComponent();
                if (last instanceof DefaultMutableTreeNode node && node.getUserObject() instanceof org.labs.genesis.forms.components.DataPanelTree.TableData table) {
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
        result.setCellRenderer(new DataPanelTree.DataTreeCellRenderer());
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
        for (org.labs.genesis.forms.components.DataPanelTree.TableData table : tables) {
            DefaultMutableTreeNode tableNode = new DefaultMutableTreeNode(table);
            for (org.labs.genesis.forms.components.DataPanelTree.ColumnData column : table.columns) {
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
        for (org.labs.genesis.forms.components.DataPanelTree.TableData table : tables) {
            boolean tableMatches = table.name.toLowerCase(Locale.ROOT).contains(query);
            List<org.labs.genesis.forms.components.DataPanelTree.ColumnData> matchingColumns = new ArrayList<>();
            for (org.labs.genesis.forms.components.DataPanelTree.ColumnData col : table.columns) {
                if (col.name.toLowerCase(Locale.ROOT).contains(query) ||
                        col.type.toLowerCase(Locale.ROOT).contains(query)) {
                    matchingColumns.add(col);
                }
            }
            if (tableMatches || !matchingColumns.isEmpty()) {
                DefaultMutableTreeNode tableNode = new DefaultMutableTreeNode(table);
                List<org.labs.genesis.forms.components.DataPanelTree.ColumnData> columnsToDisplay = tableMatches ? table.columns : matchingColumns;
                for (org.labs.genesis.forms.components.DataPanelTree.ColumnData col : columnsToDisplay) {
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

    // =========================================================================
    // DATA MODELS
    // =========================================================================

    public static class TableData {
        public final String name;
        public final List<org.labs.genesis.forms.components.DataPanelTree.ColumnData> columns;
        public TableData(String name, List<org.labs.genesis.forms.components.DataPanelTree.ColumnData> columns) {
            this.name = name;
            this.columns = columns;
        }
        @Override
        public String toString() { return name; }
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
        @Override
        public String toString() { return name + "  " + type; }
    }

    // =========================================================================
    // TREE RENDERER
    // =========================================================================

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
            if (userObject instanceof org.labs.genesis.forms.components.DataPanelTree.TableData table) {
                iconLabel.setIcon(AllIcons.Nodes.Folder);
                textLabel.setText(table.name);
                textLabel.setFont(DashboardTheme.boldFont(13));
            } else if (userObject instanceof org.labs.genesis.forms.components.DataPanelTree.ColumnData column) {
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

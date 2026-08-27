package org.labs.genesis.forms.ui.data;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.ui.treeStructure.Tree;
import org.jspecify.annotations.NonNull;
import org.labs.genesis.config.ProjectGenerationContext;
import org.labs.genesis.connexion.Database;
import org.labs.genesis.connexion.model.ColumnMetadata;
import org.labs.genesis.connexion.model.TableMetadata;
import org.labs.genesis.forms.theme.DashboardTheme;
import org.labs.genesis.forms.ui.data.listener.ColumnDragSourceListener;
import org.labs.genesis.forms.ui.data.model.ColumnData;
import org.labs.genesis.forms.ui.data.model.TableData;
import org.labs.genesis.forms.ui.data.renderer.DataTreeCellRenderer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.sql.Connection;
import java.util.List;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * Main panel displaying database tables and columns in a tree structure.
 * Supports drag & drop, search filtering, and lazy/async column loading.
 */
public class DataPanelTree extends JPanel {

    // ============================================================
    // Constants
    // ============================================================

    public static final DataFlavor COLUMN_DATA_FLAVOR =
            new DataFlavor(ColumnData.class, "Database Column");

    private static final String ROOT_NAME = "Tables";
    private static final String LOADING_PLACEHOLDER = "Loading...";
    private static final String EMPTY_PLACEHOLDER = "";

    private static final int ROW_HEIGHT = 28;
    private static final int SEARCH_BAR_GAP = 8;
    private static final int SCROLL_UNIT_INCREMENT = 16;
    private static final int DRAG_IMAGE_OFFSET = 10;
    private static final int DRAG_IMAGE_H_PADDING = 14;
    private static final int DRAG_IMAGE_V_PADDING = 8;
    private static final int DRAG_IMAGE_ICON_GAP = 8;
    private static final int DRAG_IMAGE_CORNER_RADIUS = 12;
    private static final int BUTTON_SIZE = 28;

    // ============================================================
    // Fields
    // ============================================================

    private final Tree tree;
    private final JScrollPane scrollPane;
    private final ProjectGenerationContext context;
    private final DragSource dragSource;

    private final List<TableData> tables = new ArrayList<>();
    private final Set<String> loadingTableNames = ConcurrentHashMap.newKeySet();

    private JTextField searchField;
    private JButton resetButton;
    private JButton refreshButton;

    private final AtomicBoolean isRefreshing = new AtomicBoolean(false);

    // ============================================================
    // Constructors
    // ============================================================

    public DataPanelTree(ProjectGenerationContext context) {
        this.context = context;
        this.dragSource = new DragSource();

        setLayout(new BorderLayout(0, SEARCH_BAR_GAP));
        setOpaque(false);

        add(createSearchBar(), BorderLayout.NORTH);

        this.tree = createTree();
        this.scrollPane = createScrollPane();
        add(scrollPane, BorderLayout.CENTER);

        installDragAndDrop();
        installTreeLazyLoading();

        loadTablesAsync();
    }

    // ============================================================
    // Public API
    // ============================================================

    public void setTables(List<TableData> tables) {
        this.tables.clear();
        if (tables != null) {
            this.tables.addAll(tables);
        }

        SwingUtilities.invokeLater(() -> {
            populateTree();
            loadAllColumnsAsync();
        });
    }

    public List<TableData> getTables() {
        return new ArrayList<>(tables);
    }

    public void refreshData() {
        if (isRefreshing.getAndSet(true)) {
            return;
        }

        String currentQuery = searchField.getText().trim();
        refreshButton.setEnabled(false);
        setLoadingState(true);

        showLoadingMessage();

        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            try {
                List<TableData> loadedTables = fetchAllTablesAndViews();

                SwingUtilities.invokeLater(() -> {
                    tables.clear();
                    tables.addAll(loadedTables);

                    refreshButton.setEnabled(true);
                    isRefreshing.set(false);
                    setLoadingState(false);

                    populateTree();

                    if (!currentQuery.isEmpty()) {
                        applyFilter();
                    }

                    loadAllColumnsAsync();
                });

            } catch (Exception e) {
                handleError("Error refreshing data", e);

                SwingUtilities.invokeLater(() -> {
                    refreshButton.setEnabled(true);
                    isRefreshing.set(false);
                    setLoadingState(false);
                });
            }
        });
    }

    // ============================================================
    // Initial Loading
    // ============================================================

    private void loadTablesAsync() {
        setLoadingState(true);
        showLoadingMessage();

        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            try {
                List<TableData> loadedTables = fetchAllTablesAndViews();

                SwingUtilities.invokeLater(() -> {
                    tables.clear();
                    tables.addAll(loadedTables);

                    setLoadingState(false);
                    populateTree();
                    applyCurrentFilter();
                    loadAllColumnsAsync();
                });

            } catch (Exception e) {
                handleError("Error loading tables", e);

                SwingUtilities.invokeLater(() -> {
                    setLoadingState(false);
                    populateTree();
                });
            }
        });
    }

    private void showLoadingMessage() {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(ROOT_NAME);
        root.add(new DefaultMutableTreeNode("Loading tables..."));
        tree.setModel(new DefaultTreeModel(root));
        tree.revalidate();
        tree.repaint();
    }

    // ============================================================
    // Column Loading
    // ============================================================

    /**
     * Loads columns for all tables in a single batch operation.
     */
    private void loadAllColumnsAsync() {
        if (tables.isEmpty()) {
            return;
        }

        List<TableData> tablesToLoad = new ArrayList<>(tables);

        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            Database database = context.getDatabase();
            if (database == null) {
                return;
            }

            try (Connection connection = database.getConnection(context.getCredentials())) {

                Map<String, List<ColumnData>> columnsByTable = fetchAllColumns(database, connection);

                SwingUtilities.invokeLater(() -> {
                    for (TableData table : tablesToLoad) {
                        List<ColumnData> columns = columnsByTable.get(table.name);
                        table.setColumns(columns != null ? columns : new ArrayList<>());
                    }

                    applyCurrentFilter();
                    tree.revalidate();
                    tree.repaint();
                });

            } catch (Exception e) {
                handleError("Error loading columns", e);
            }
        });
    }

    private Map<String, List<ColumnData>> fetchAllColumns(Database database, Connection connection) throws Exception {
        Map<String, List<ColumnData>> result = new HashMap<>();

        List<TableMetadata> allMetadata = new ArrayList<>();
        allMetadata.addAll(database.getEntities(connection, context.getCredentials(),
                context.getLanguage(), context.getFramework()));
        allMetadata.addAll(database.getViews(connection, context.getCredentials(),
                context.getLanguage(), context.getFramework()));

        for (TableMetadata metadata : allMetadata) {
            String tableName = metadata.getTableName();
            if (tableName != null) {
                result.put(tableName, convertColumns(metadata.getColumns()));
            }
        }

        return result;
    }

    /**
     * Fallback lazy loading for individual tables.
     */
    private void loadColumnsAsync(TableData table, DefaultMutableTreeNode tableNode) {
        if (!loadingTableNames.add(table.name)) {
            return;
        }

        showLoadingPlaceholder(tableNode);

        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            try {
                List<ColumnData> columns = fetchColumnsForTable(table);

                SwingUtilities.invokeLater(() -> {
                    table.setColumns(columns);
                    updateTableNode(tableNode, columns);
                    loadingTableNames.remove(table.name);
                    applyCurrentFilter();
                });

            } catch (Exception e) {
                handleError("Error loading columns for " + table.name, e);

                SwingUtilities.invokeLater(() -> {
                    loadingTableNames.remove(table.name);
                });
            }
        });
    }

    private List<ColumnData> fetchColumnsForTable(TableData targetTable) {
        Database database = context.getDatabase();
        if (database == null) {
            return new ArrayList<>();
        }

        try (Connection connection = database.getConnection(context.getCredentials())) {
            List<TableMetadata> allMetadata = new ArrayList<>();
            allMetadata.addAll(database.getEntities(connection, context.getCredentials(),
                    context.getLanguage(), context.getFramework()));
            allMetadata.addAll(database.getViews(connection, context.getCredentials(),
                    context.getLanguage(), context.getFramework()));

            for (TableMetadata metadata : allMetadata) {
                if (targetTable.name.equals(metadata.getTableName())) {
                    return convertColumns(metadata.getColumns());
                }
            }

        } catch (Exception e) {
            handleError("Error fetching columns for " + targetTable.name, e);
        }

        return new ArrayList<>();
    }

    // ============================================================
    // Database Operations
    // ============================================================

    private List<TableData> fetchAllTablesAndViews() {
        Database database = context.getDatabase();
        if (database == null) {
            return new ArrayList<>();
        }

        try (Connection connection = database.getConnection(context.getCredentials())) {

            List<TableMetadata> tables = database.getEntities(connection, context.getCredentials(),
                    context.getLanguage(), context.getFramework());
            List<TableMetadata> views = database.getViews(connection, context.getCredentials(),
                    context.getLanguage(), context.getFramework());

            List<TableData> result = new ArrayList<>();

            for (TableMetadata table : tables) {
                result.add(new TableData(table.getTableName()));
            }

            for (TableMetadata view : views) {
                result.add(new TableData(view.getTableName()));
            }

            return result;

        } catch (Exception e) {
            handleError("Error fetching tables and views", e);
            return new ArrayList<>();
        }
    }

    private List<ColumnData> convertColumns(ColumnMetadata[] metadata) {
        if (metadata == null) {
            return new ArrayList<>();
        }

        List<ColumnData> columns = new ArrayList<>(metadata.length);

        for (ColumnMetadata col : metadata) {
            columns.add(new ColumnData(
                    col.getName(),
                    col.getColumnType() != null ? col.getColumnType().toUpperCase() : "UNKNOWN",
                    col.isPrimary(),
                    col.isNullable()
            ));
        }

        return columns;
    }

    // ============================================================
    // Tree Population
    // ============================================================

    private void populateTree() {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(ROOT_NAME);

        for (TableData table : tables) {
            DefaultMutableTreeNode tableNode = new DefaultMutableTreeNode(table);

            if (table.isColumnsLoaded()) {
                for (ColumnData column : table.getColumns()) {
                    tableNode.add(new DefaultMutableTreeNode(column));
                }
            } else {
                tableNode.add(new DefaultMutableTreeNode(EMPTY_PLACEHOLDER));
            }

            root.add(tableNode);
        }

        tree.setModel(new DefaultTreeModel(root));
        tree.revalidate();
        tree.repaint();
    }

    private void showLoadingPlaceholder(DefaultMutableTreeNode tableNode) {
        SwingUtilities.invokeLater(() -> {
            tableNode.removeAllChildren();
            tableNode.add(new DefaultMutableTreeNode(LOADING_PLACEHOLDER));

            TreeModel model = tree.getModel();
            if (model instanceof DefaultTreeModel treeModel) {
                treeModel.nodeStructureChanged(tableNode);
            }
        });
    }

    private void updateTableNode(DefaultMutableTreeNode tableNode, List<ColumnData> columns) {
        tableNode.removeAllChildren();

        for (ColumnData column : columns) {
            tableNode.add(new DefaultMutableTreeNode(column));
        }

        TreeModel model = tree.getModel();
        if (model instanceof DefaultTreeModel treeModel) {
            treeModel.nodeStructureChanged(tableNode);
        }
    }

    // ============================================================
    // Tree Configuration
    // ============================================================

    private Tree createTree() {
        Tree tree = new Tree() {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(
                        super.getPreferredSize().width,
                        getRowHeight() * getRowCount() + 20
                );
            }
        };

        tree.setOpaque(false);
        tree.setBackground(new Color(0, 0, 0, 0));
        tree.setForeground(DashboardTheme.TEXT);
        tree.setRowHeight(ROW_HEIGHT);
        tree.setShowsRootHandles(true);
        tree.setRootVisible(false);
        tree.setCellRenderer(new DataTreeCellRenderer());
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

        tree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    handleDoubleClick(e);
                }
            }
        });

        return tree;
    }

    private void handleDoubleClick(MouseEvent e) {
        TreePath path = tree.getPathForLocation(e.getX(), e.getY());
        if (path == null) {
            return;
        }

        Object last = path.getLastPathComponent();
        if (last instanceof DefaultMutableTreeNode node
                && node.getUserObject() instanceof TableData table) {
            // Double-click on table - could expand or show details
            System.out.println("Double click table: " + table.name);
        }
    }

    private JScrollPane createScrollPane() {
        JScrollPane pane = new JScrollPane(tree);
        pane.setOpaque(false);
        pane.setBackground(new Color(0, 0, 0, 0));
        pane.getViewport().setOpaque(false);
        pane.getViewport().setBackground(new Color(0, 0, 0, 0));
        pane.setBorder(BorderFactory.createEmptyBorder());
        pane.getVerticalScrollBar().setUnitIncrement(SCROLL_UNIT_INCREMENT);
        return pane;
    }

    // ============================================================
    // Tree Lazy Loading
    // ============================================================

    private void installTreeLazyLoading() {
        tree.addTreeWillExpandListener(new TreeWillExpandListener() {
            @Override
            public void treeWillExpand(TreeExpansionEvent event) {
                TreePath path = event.getPath();
                if (path == null) {
                    return;
                }

                Object component = path.getLastPathComponent();
                if (!(component instanceof DefaultMutableTreeNode node)) {
                    return;
                }

                Object userObject = node.getUserObject();
                if (!(userObject instanceof TableData table)) {
                    return;
                }

                if (!table.isColumnsLoaded()) {
                    loadColumnsAsync(table, node);
                }
            }

            @Override
            public void treeWillCollapse(TreeExpansionEvent event) {
                // Nothing to do
            }
        });
    }

    // ============================================================
    // Search Bar
    // ============================================================

    private JPanel createSearchBar() {
        JPanel container = new JPanel(new BorderLayout(6, 0));
        container.setOpaque(false);

        JPanel fieldPanel = createSearchFieldPanel();
        container.add(fieldPanel, BorderLayout.CENTER);
        container.add(createRefreshButton(), BorderLayout.EAST);

        return container;
    }

    private JPanel createSearchFieldPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(true);
        panel.setBackground(DashboardTheme.SURFACE_2);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(DashboardTheme.BORDER_SUBTLE, 1),
                new EmptyBorder(0, 8, 0, 4)
        ));

        panel.add(createSearchIcon(), BorderLayout.WEST);
        panel.add(createSearchField(), BorderLayout.CENTER);
        panel.add(createResetButton(), BorderLayout.EAST);

        return panel;
    }

    private JLabel createSearchIcon() {
        JLabel icon = new JLabel(AllIcons.Actions.Find);
        icon.setBorder(new EmptyBorder(0, 0, 0, 4));
        return icon;
    }

    private JTextField createSearchField() {
        searchField = new JTextField();
        searchField.setOpaque(false);
        searchField.setBorder(null);
        searchField.setForeground(DashboardTheme.TEXT);
        searchField.setCaretColor(DashboardTheme.TEXT);
        searchField.setFont(searchField.getFont().deriveFont(12f));
        searchField.putClientProperty("JTextField.placeholderText", "Search tables or columns...");

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                applyFilter();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                applyFilter();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                applyFilter();
            }
        });

        return searchField;
    }

    private JButton createResetButton() {
        resetButton = createIconButton(AllIcons.Actions.Cancel, "Clear search");
        resetButton.setVisible(false);
        resetButton.addActionListener(e -> clearSearch());
        return resetButton;
    }

    private JButton createRefreshButton() {
        refreshButton = createIconButton(AllIcons.Actions.Refresh, "Refresh tables and columns");
        refreshButton.addActionListener(e -> refreshData());
        return refreshButton;
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
        button.setPreferredSize(new Dimension(BUTTON_SIZE, BUTTON_SIZE));
        return button;
    }

    // ============================================================
    // Filter / Search
    // ============================================================

    private void applyFilter() {
        if (searchField == null) {
            return;
        }

        String query = searchField.getText().trim().toLowerCase(Locale.ROOT);
        resetButton.setVisible(!query.isEmpty());

        if (query.isEmpty()) {
            populateTree();
            return;
        }

        DefaultMutableTreeNode root = new DefaultMutableTreeNode(ROOT_NAME);

        for (TableData table : tables) {
            boolean tableMatches = table.name.toLowerCase(Locale.ROOT).contains(query);
            List<ColumnData> matchingColumns = findMatchingColumns(table, query);

            if (tableMatches) {
                addTableWithAllColumns(root, table);
            } else if (!matchingColumns.isEmpty()) {
                addTableWithFilteredColumns(root, table, matchingColumns);
            }
        }

        tree.setModel(new DefaultTreeModel(root));
        expandAllNodes();
        tree.revalidate();
        tree.repaint();
    }

    private List<ColumnData> findMatchingColumns(TableData table, String query) {
        if (!table.isColumnsLoaded()) {
            return new ArrayList<>();
        }

        return table.getColumns().stream()
                .filter(col -> matchesQuery(col, query))
                .collect(Collectors.toList());
    }

    private boolean matchesQuery(ColumnData column, String query) {
        String name = column.name != null ? column.name.toLowerCase(Locale.ROOT) : "";
        String type = column.type != null ? column.type.toLowerCase(Locale.ROOT) : "";
        return name.contains(query) || type.contains(query);
    }

    private void addTableWithAllColumns(DefaultMutableTreeNode root, TableData table) {
        DefaultMutableTreeNode tableNode = new DefaultMutableTreeNode(table);

        if (table.isColumnsLoaded()) {
            for (ColumnData column : table.getColumns()) {
                tableNode.add(new DefaultMutableTreeNode(column));
            }
        } else {
            tableNode.add(new DefaultMutableTreeNode(EMPTY_PLACEHOLDER));
        }

        root.add(tableNode);
    }

    private void addTableWithFilteredColumns(DefaultMutableTreeNode root, TableData table,
                                             List<ColumnData> columns) {
        DefaultMutableTreeNode tableNode = new DefaultMutableTreeNode(table);

        for (ColumnData column : columns) {
            tableNode.add(new DefaultMutableTreeNode(column));
        }

        root.add(tableNode);
    }

    private void applyCurrentFilter() {
        if (searchField == null) {
            return;
        }

        String query = searchField.getText().trim();
        if (query.isEmpty()) {
            populateTree();
        } else {
            applyFilter();
        }
    }

    private void clearSearch() {
        searchField.setText("");
        searchField.requestFocusInWindow();
        resetButton.setVisible(false);
        populateTree();
    }

    private void expandAllNodes() {
        for (int i = 0; i < tree.getRowCount(); i++) {
            tree.expandRow(i);
        }
    }

    // ============================================================
    // Drag & Drop
    // ============================================================

    private void installDragAndDrop() {
        DragGestureRecognizer recognizer = dragSource.createDefaultDragGestureRecognizer(
                tree,
                DnDConstants.ACTION_COPY,
                this::startColumnDrag
        );
        recognizer.setComponent(tree);
    }

    private void startColumnDrag(DragGestureEvent event) {
        TreePath path = tree.getPathForLocation(
                event.getDragOrigin().x,
                event.getDragOrigin().y
        );

        if (path == null) {
            return;
        }

        Object lastPathComponent = path.getLastPathComponent();
        if (!(lastPathComponent instanceof DefaultMutableTreeNode node)) {
            return;
        }

        Object userObject = node.getUserObject();
        if (!(userObject instanceof ColumnData columnData)) {
            return;
        }

        String tableName = findParentTableName(path);
        String fullColumnName = tableName != null && !tableName.isEmpty()
                ? tableName + "." + columnData.name
                : columnData.name;

        Transferable transferable = createColumnTransferable(columnData, fullColumnName);
        BufferedImage dragImage = createDragImage(columnData, tableName);
        Point imageOffset = new Point(DRAG_IMAGE_OFFSET, DRAG_IMAGE_OFFSET);

        try {
            event.startDrag(
                    Cursor.getPredefinedCursor(Cursor.HAND_CURSOR),
                    dragImage,
                    imageOffset,
                    transferable,
                    new ColumnDragSourceListener()
            );
        } catch (InvalidDnDOperationException ex) {
            Toolkit.getDefaultToolkit().beep();
        }
    }

    private String findParentTableName(TreePath path) {
        if (path.getPathCount() < 3) {
            return "";
        }

        Object parentNode = path.getPathComponent(path.getPathCount() - 2);
        if (parentNode instanceof DefaultMutableTreeNode parentTreeNode) {
            Object parentUserObject = parentTreeNode.getUserObject();
            if (parentUserObject instanceof TableData tableData) {
                return tableData.name;
            }
        }

        return "";
    }

    private Transferable createColumnTransferable(ColumnData columnData, String fullColumnName) {
        return new Transferable() {
            private final DataFlavor[] flavors = {COLUMN_DATA_FLAVOR, DataFlavor.stringFlavor};

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
                    return new ColumnData(
                            fullColumnName,
                            columnData.type,
                            columnData.isPrimaryKey,
                            columnData.isNullable
                    );
                }

                if (DataFlavor.stringFlavor.equals(flavor)) {
                    return fullColumnName;
                }

                throw new UnsupportedFlavorException(flavor);
            }
        };
    }

    // ============================================================
    // Drag Image Creation
    // ============================================================

    private BufferedImage createDragImage(ColumnData column, String tableName) {
        String displayText = tableName != null && !tableName.isEmpty()
                ? tableName + "." + column.name
                : column.name;

        Font font = DashboardTheme.boldFont(12);
        FontMetrics metrics = getFontMetrics(font);

        int textWidth = metrics.stringWidth(displayText);
        int textHeight = metrics.getHeight();

        Icon icon = column.isPrimaryKey ? AllIcons.Nodes.C_private : AllIcons.Nodes.C_plocal;
        int iconWidth = icon != null ? icon.getIconWidth() : 0;
        int iconHeight = icon != null ? icon.getIconHeight() : 0;

        int contentWidth = iconWidth + DRAG_IMAGE_ICON_GAP + textWidth;
        int width = contentWidth + DRAG_IMAGE_H_PADDING * 2;
        int height = Math.max(iconHeight, textHeight) + DRAG_IMAGE_V_PADDING * 2;

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();

        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            drawDragBackground(g, width, height);
            drawDragIcon(g, icon, height);
            drawDragText(g, displayText, column.type, iconWidth, textWidth, textHeight, font);

        } finally {
            g.dispose();
        }

        return image;
    }

    private void drawDragBackground(Graphics2D g, int width, int height) {
        // Shadow
        g.setColor(new Color(0, 0, 0, 40));
        g.fillRoundRect(2, 3, width - 3, height - 3,
                DRAG_IMAGE_CORNER_RADIUS, DRAG_IMAGE_CORNER_RADIUS);

        // Gradient background
        GradientPaint gradient = new GradientPaint(
                0, 0, new Color(45, 47, 54),
                0, height, new Color(35, 37, 42)
        );
        g.setPaint(gradient);

        RoundRectangle2D shape = new RoundRectangle2D.Double(
                0, 0, width - 3, height - 3,
                DRAG_IMAGE_CORNER_RADIUS, DRAG_IMAGE_CORNER_RADIUS
        );
        g.fill(shape);

        // Border
        g.setColor(new Color(255, 255, 255, 60));
        g.setStroke(new BasicStroke(1.2f));
        g.draw(shape);
    }

    private void drawDragIcon(Graphics2D g, Icon icon, int height) {
        if (icon != null) {
            icon.paintIcon(null, g, DRAG_IMAGE_H_PADDING - 4,
                    (height - icon.getIconHeight()) / 2);
        }
    }

    private void drawDragText(Graphics2D g, String text, String type,
                              int iconWidth, int textWidth, int textHeight, Font font) {
        int textX = DRAG_IMAGE_H_PADDING - 4 + iconWidth + DRAG_IMAGE_ICON_GAP;
        int textY = (textHeight - getFontMetrics(font).getHeight()) / 2
                + getFontMetrics(font).getAscent();

        g.setFont(font);
        g.setColor(Color.WHITE);
        g.drawString(text, textX, textY);

        // Type
        Font typeFont = DashboardTheme.getFont(9).deriveFont(Font.PLAIN);
        g.setFont(typeFont);
        g.setColor(new Color(255, 255, 255, 140));

        FontMetrics typeMetrics = g.getFontMetrics(typeFont);
        int typeX = textX + textWidth + 6;
        int typeY = (textHeight - typeMetrics.getHeight()) / 2 + typeMetrics.getAscent();

        g.drawString(type, typeX, typeY);
    }

    // ============================================================
    // Utility Methods
    // ============================================================

    private void setLoadingState(boolean loading) {
        if (refreshButton != null) {
            refreshButton.setEnabled(!loading);
        }
        tree.setEnabled(!loading);
    }

    private void handleError(String message, Exception e) {
        System.err.println(message + ": " + e.getMessage());
        e.printStackTrace();
    }
}
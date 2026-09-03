package org.labs.genesis.forms.ui.visualization;

import com.intellij.openapi.application.ApplicationManager;
import lombok.Getter;
import lombok.Setter;
import org.labs.genesis.config.ProjectGenerationContext;
import org.labs.genesis.forms.renderer.VisualizationRenderer;
import org.labs.genesis.forms.renderer.VisualizationRendererFactory;
import org.labs.genesis.forms.renderer.provider.ChartData;
import org.labs.genesis.forms.renderer.provider.DataProvider;
import org.labs.genesis.forms.renderer.provider.TableData;
import org.labs.genesis.forms.renderer.table.TableRenderer;
import org.labs.genesis.forms.theme.DashboardTheme;
import org.labs.genesis.forms.ui.visualization.model.VisualizationConfig;
import org.labs.genesis.forms.ui.visualization.model.VisualizationItem;
import org.labs.genesis.forms.ui.visualization.model.VisualizationParameter;
import org.labs.genesis.forms.utils.CursorUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.List;

@Getter
@Setter
public class DashboardVisualComponent extends JPanel {

    public static final int DEFAULT_GRID_WIDTH = 4;
    public static final int DEFAULT_GRID_HEIGHT = 4;

    private static final int RADIUS = 10;
    private static final int HANDLE_SIZE = 10;
    private static final int HANDLE_ZONE = 14;

    private boolean selected;
    private ResizeDirection activeResizeDirection = ResizeDirection.NONE;
    private boolean resizing;
    private boolean dragging;

    private final VisualizationItem visualizationItem;
    private final VisualizationRenderer renderer;
    private final JComponent visualComponent;
    private final VisualizationConfig config;
    private final ProjectGenerationContext context;
    private final DataProvider dataProvider;

    private int gridX;
    private int gridY;
    private int gridWidth;
    private int gridHeight;

    private JLabel titleLabel;
    private JComponent errorComponent;
    private boolean showError = false;
    private String missingParameters = "";

    /**
     * Permet d'éviter qu'une ancienne requête asynchrone
     * écrase les données d'une requête plus récente.
     */
    private int dataLoadVersion = 0;

    public DashboardVisualComponent(
            VisualizationItem visualizationItem,
            int gridWidth,
            int gridHeight
    ) {
        this(
                visualizationItem,
                gridWidth,
                gridHeight,
                null
        );
    }

    public DashboardVisualComponent(
            VisualizationItem visualizationItem,
            int gridWidth,
            int gridHeight,
            ProjectGenerationContext context
    ) {
        this.visualizationItem = visualizationItem;
        this.gridWidth = Math.max(1, gridWidth);
        this.gridHeight = Math.max(1, gridHeight);
        this.config = new VisualizationConfig();
        this.context = context;
        this.dataProvider = new DataProvider();

        this.renderer =
                VisualizationRendererFactory.create(
                        visualizationItem.rendererClass
                );

        this.visualComponent =
                renderer.createComponent(config);

        configureComponent();
        updateDisplayState();
    }

    // =========================================================================
    // CONFIG
    // =========================================================================

    public Object getConfigObject(String key) {
        return config.getValue(key);
    }

    public String getConfigValue(String key) {
        return config.getString(key);
    }

    public String getConfigValue(
            String key,
            String defaultValue
    ) {
        return config.getString(key, defaultValue);
    }

    public void updateConfig(
            String key,
            Object value
    ) {
        config.setValue(key, value);

        if ("title".equals(key)) {

            String titleValue =
                    value != null
                            ? value.toString()
                            : "";

            titleLabel.setText(
                    titleValue.trim().isEmpty()
                            ? visualizationItem.name
                            : titleValue
            );

            revalidate();
            repaint();
        }

        renderer.updateConfig(config);

        refreshDataIfNeeded();

        updateDisplayState();
    }

    public void setTitle(String title) {

        titleLabel.setText(
                title == null || title.trim().isEmpty()
                        ? visualizationItem.name
                        : title
        );

        config.setValue(
                "title",
                title
        );

        revalidate();
        repaint();
    }

    public String getTitle() {
        return titleLabel.getText();
    }

    // =========================================================================
    // COMPONENT CONFIGURATION
    // =========================================================================

    private void configureComponent() {

        setLayout(new BorderLayout());
        setOpaque(false);

        setCursor(
                Cursor.getPredefinedCursor(
                        Cursor.HAND_CURSOR
                )
        );

        titleLabel =
                new JLabel(
                        visualizationItem.name
                );

        titleLabel.setForeground(
                DashboardTheme.TEXT_DARK
        );

        titleLabel.setFont(
                DashboardTheme.boldFont(11)
        );

        titleLabel.setBorder(
                new EmptyBorder(
                        6,
                        8,
                        4,
                        8
                )
        );

        add(
                titleLabel,
                BorderLayout.NORTH
        );

        JPanel contentContainer =
                new JPanel(
                        new BorderLayout()
                );

        contentContainer.setOpaque(false);

        contentContainer.setMinimumSize(
                new Dimension(0, 0)
        );

        contentContainer.setPreferredSize(
                new Dimension(0, 0)
        );

        contentContainer.setName(
                "contentContainer"
        );

        updateContentContainer(
                contentContainer
        );

        add(
                contentContainer,
                BorderLayout.CENTER
        );
    }

    private void updateContentContainer(
            JPanel contentContainer
    ) {

        contentContainer.removeAll();

        if (showError) {

            if (errorComponent == null) {

                errorComponent =
                        new ErrorVisualizationComponent(
                                missingParameters
                        );
            }

            errorComponent.setMinimumSize(
                    new Dimension(0, 0)
            );

            errorComponent.setPreferredSize(
                    new Dimension(0, 0)
            );

            errorComponent.setMaximumSize(
                    new Dimension(
                            Integer.MAX_VALUE,
                            Integer.MAX_VALUE
                    )
            );

            contentContainer.add(
                    errorComponent,
                    BorderLayout.CENTER
            );

        } else {

            visualComponent.setMinimumSize(
                    new Dimension(0, 0)
            );

            visualComponent.setPreferredSize(
                    new Dimension(0, 0)
            );

            visualComponent.setMaximumSize(
                    new Dimension(
                            Integer.MAX_VALUE,
                            Integer.MAX_VALUE
                    )
            );

            contentContainer.add(
                    visualComponent,
                    BorderLayout.CENTER
            );
        }

        contentContainer.revalidate();
        contentContainer.repaint();
    }

    // =========================================================================
    // DATA LOADING
    // =========================================================================

    private void refreshDataIfNeeded() {

        if (context == null) {
            return;
        }

        if (context.getConnection() == null) {
            return;
        }

        if (!isProperlyConfigured()) {
            return;
        }

        String tableName =
                getTableName();

        if (tableName == null
                || tableName.isBlank()) {
            return;
        }

        final int version =
                ++dataLoadVersion;

        clearVisualizationData();

        renderer.updateConfig(config);

        visualComponent.revalidate();
        visualComponent.repaint();

        ApplicationManager
                .getApplication()
                .executeOnPooledThread(() -> {

                    try {

                        /*
                         * =====================================================
                         * TABLE
                         * =====================================================
                         */

                        if (isTableVisualization()) {

                            TableData data =
                                    dataProvider.loadTable(
                                            context.getConnection(),
                                            tableName,
                                            config
                                    );

                            SwingUtilities.invokeLater(() -> {

                                if (version != dataLoadVersion) {
                                    return;
                                }

                                setTableData(data);

                                renderer.updateConfig(config);

                                visualComponent.revalidate();
                                visualComponent.repaint();
                            });

                            return;
                        }

                        /*
                         * =====================================================
                         * CHART
                         * =====================================================
                         */

                        ChartData data =
                                dataProvider.loadChart(
                                        context.getConnection(),
                                        tableName,
                                        config,
                                        visualizationItem
                                );

                        SwingUtilities.invokeLater(() -> {

                            if (version != dataLoadVersion) {
                                return;
                            }

                            setChartData(data);

                            renderer.updateConfig(config);

                            visualComponent.revalidate();
                            visualComponent.repaint();
                        });

                    } catch (Exception e) {

                        SwingUtilities.invokeLater(() -> {

                            if (version != dataLoadVersion) {
                                return;
                            }

                            setVisualizationError(
                                    getErrorMessage(e)
                            );

                            renderer.updateConfig(config);

                            visualComponent.revalidate();
                            visualComponent.repaint();
                        });
                    }
                });
    }

    // =========================================================================
    // CHART DATA
    // =========================================================================

    private void setChartData(
            ChartData data
    ) {

        config.setValue(
                ChartData.CONFIG_KEY,
                data
        );

        config.setValue(
                ChartData.ERROR_KEY,
                null
        );

        config.setValue(
                ChartData.LOADING_KEY,
                false
        );
    }

    // =========================================================================
    // TABLE DATA
    // =========================================================================

    private void setTableData(
            TableData data
    ) {

        config.setValue(
                TableData.CONFIG_KEY,
                data
        );

        config.setValue(
                TableData.ERROR_KEY,
                null
        );

        config.setValue(
                TableData.LOADING_KEY,
                false
        );
    }

    // =========================================================================
    // CLEAR DATA
    // =========================================================================

    private void clearVisualizationData() {

        if (isTableVisualization()) {

            config.setValue(
                    TableData.LOADING_KEY,
                    true
            );

            config.setValue(
                    TableData.ERROR_KEY,
                    null
            );

            config.setValue(
                    TableData.CONFIG_KEY,
                    null
            );

        } else {

            config.setValue(
                    ChartData.LOADING_KEY,
                    true
            );

            config.setValue(
                    ChartData.ERROR_KEY,
                    null
            );

            config.setValue(
                    ChartData.CONFIG_KEY,
                    null
            );
        }
    }

    // =========================================================================
    // ERROR
    // =========================================================================

    private void setVisualizationError(
            String message
    ) {

        if (isTableVisualization()) {

            config.setValue(
                    TableData.CONFIG_KEY,
                    null
            );

            config.setValue(
                    TableData.ERROR_KEY,
                    message
            );

            config.setValue(
                    TableData.LOADING_KEY,
                    false
            );

        } else {

            config.setValue(
                    ChartData.CONFIG_KEY,
                    null
            );

            config.setValue(
                    ChartData.ERROR_KEY,
                    message
            );

            config.setValue(
                    ChartData.LOADING_KEY,
                    false
            );
        }
    }

    private String getErrorMessage(
            Exception e
    ) {

        if (e == null) {
            return "Unknown error";
        }

        String message =
                e.getMessage();

        if (message == null
                || message.isBlank()) {

            return e.getClass()
                    .getSimpleName();
        }

        return message;
    }

    // =========================================================================
    // TABLE DETECTION
    // =========================================================================

    private boolean isTableVisualization() {

        return TableRenderer.class.equals(
                visualizationItem.rendererClass
        );
    }

    // =========================================================================
    // TABLE NAME
    // =========================================================================

    private String getTableName() {

        // =====================================================================
        // 1. Explicit table
        // =====================================================================

        Object table =
                config.getValue("table");

        if (table != null
                && !table.toString().trim().isEmpty()) {

            return table.toString().trim();
        }

        // =====================================================================
        // 2. TABLE COLUMNS
        // =====================================================================

        Object columns =
                config.getValue("columns");

        if (columns instanceof List<?> list) {

            for (Object value : list) {

                if (value == null) {
                    continue;
                }

                String stringValue =
                        value.toString().trim();

                if (stringValue.isEmpty()) {
                    continue;
                }

                String tableName =
                        DataProvider.extractTableNameStatic(
                                stringValue
                        );

                if (tableName != null
                        && !tableName.isBlank()) {

                    return tableName;
                }
            }
        }

        // =====================================================================
        // 3. NORMAL PARAMETERS
        // =====================================================================

        for (VisualizationParameter param :
                visualizationItem.parameters) {

            if (!param.hasQueryRole()) {
                continue;
            }

            Object value =
                    config.getValue(
                            param.getKey()
                    );

            if (value == null) {
                continue;
            }

            String stringValue =
                    value.toString().trim();

            if (stringValue.isEmpty()) {
                continue;
            }

            String tableName =
                    DataProvider.extractTableNameStatic(
                            stringValue
                    );

            if (tableName != null
                    && !tableName.isBlank()) {

                return tableName;
            }
        }

        // =====================================================================
        // 4. DATA SOURCE
        // =====================================================================

        Object dataSource =
                config.getValue(
                        "dataSource"
                );

        if (dataSource != null
                && !dataSource.toString().trim().isEmpty()) {

            return dataSource.toString().trim();
        }

        return null;
    }

    // =========================================================================
    // DISPLAY STATE
    // =========================================================================

    private void updateDisplayState() {

        boolean shouldShowError =
                !visualizationItem.hasAllRequiredParameters(
                        config
                );

        if (shouldShowError == showError) {
            return;
        }

        showError =
                shouldShowError;

        if (showError) {

            StringBuilder missing =
                    new StringBuilder();

            for (VisualizationParameter param :
                    visualizationItem.parameters) {

                if (!param.isRequired()) {
                    continue;
                }

                Object value =
                        config.getValue(
                                param.getKey()
                        );

                boolean isEmpty =
                        value == null
                                || (
                                value instanceof String
                                        && ((String) value)
                                        .trim()
                                        .isEmpty()
                        )
                                || (
                                value instanceof List
                                        && ((List<?>) value)
                                        .isEmpty()
                        );

                if (isEmpty) {

                    if (missing.length() > 0) {
                        missing.append(", ");
                    }

                    missing.append(
                            param.getLabel()
                    );
                }
            }

            missingParameters =
                    missing.toString();

            if (errorComponent != null) {

                Container parent =
                        errorComponent.getParent();

                if (parent != null) {
                    parent.remove(errorComponent);
                }
            }

            errorComponent =
                    new ErrorVisualizationComponent(
                            missingParameters
                    );
        }

        for (Component component :
                getComponents()) {

            if (
                    "contentContainer".equals(
                            component.getName()
                    )
                            && component instanceof JPanel panel
            ) {

                updateContentContainer(panel);
                break;
            }
        }

        revalidate();
        repaint();
    }

    public boolean isProperlyConfigured() {

        return visualizationItem.hasAllRequiredParameters(
                config
        );
    }

    // =========================================================================
    // DRAG / RESIZE
    // =========================================================================

    public void setResizing(boolean resizing) {

        this.resizing = resizing;

        if (!resizing) {
            restoreCursor();
        }
    }

    public void setDragging(boolean dragging) {

        this.dragging = dragging;

        if (!dragging) {
            restoreCursor();
        }
    }

    private void restoreCursor() {

        Point mousePos =
                getMousePosition();

        if (mousePos != null) {

            handleMouseMoved(mousePos);

        } else {

            setCursor(
                    Cursor.getPredefinedCursor(
                            Cursor.HAND_CURSOR
                    )
            );
        }
    }

    public void handleMouseMoved(
            Point point
    ) {

        if (resizing || dragging) {
            return;
        }

        if (activeResizeDirection
                != ResizeDirection.NONE) {

            setCursor(
                    CursorUtils.getCursorForDirection(
                            activeResizeDirection
                    )
            );

            return;
        }

        if (!selected) {

            setCursor(
                    Cursor.getPredefinedCursor(
                            Cursor.HAND_CURSOR
                    )
            );

            return;
        }

        ResizeDirection direction =
                getResizeDirection(point);

        if (direction != ResizeDirection.NONE) {

            setCursor(
                    CursorUtils.getCursorForDirection(
                            direction
                    )
            );

        } else if (isInHeaderArea(point)) {

            setCursor(
                    Cursor.getPredefinedCursor(
                            Cursor.MOVE_CURSOR
                    )
            );

        } else {

            setCursor(
                    Cursor.getDefaultCursor()
            );
        }
    }

    public void setActiveResizeDirection(
            ResizeDirection direction
    ) {

        this.activeResizeDirection =
                direction;

        if (resizing || dragging) {
            return;
        }

        if (direction == ResizeDirection.NONE) {

            Point mousePos =
                    getMousePosition();

            if (mousePos != null) {

                handleMouseMoved(mousePos);

            } else {

                setCursor(
                        Cursor.getPredefinedCursor(
                                Cursor.HAND_CURSOR
                        )
                );
            }

        } else {

            setCursor(
                    CursorUtils.getCursorForDirection(
                            direction
                    )
            );
        }
    }

    public ResizeDirection getResizeDirection(
            Point point
    ) {

        if (!selected) {
            return ResizeDirection.NONE;
        }

        int w = getWidth();
        int h = getHeight();

        int zone =
                HANDLE_ZONE;

        boolean left =
                point.x <= zone;

        boolean right =
                point.x >= w - zone;

        boolean top =
                point.y <= zone;

        boolean bottom =
                point.y >= h - zone;

        if (top && left)
            return ResizeDirection.NORTH_WEST;

        if (top && right)
            return ResizeDirection.NORTH_EAST;

        if (bottom && left)
            return ResizeDirection.SOUTH_WEST;

        if (bottom && right)
            return ResizeDirection.SOUTH_EAST;

        if (top)
            return ResizeDirection.NORTH;

        if (bottom)
            return ResizeDirection.SOUTH;

        if (left)
            return ResizeDirection.WEST;

        if (right)
            return ResizeDirection.EAST;

        return ResizeDirection.NONE;
    }

    public boolean isInHeaderArea(
            Point p
    ) {

        return titleLabel != null
                && titleLabel.getBounds()
                .contains(p);
    }

    // =========================================================================
    // PAINT
    // =========================================================================

    @Override
    protected void paintComponent(
            Graphics g
    ) {

        Graphics2D g2 =
                (Graphics2D) g.create();

        try {

            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            int w = getWidth();
            int h = getHeight();

            if (w <= 0 || h <= 0) {
                return;
            }

            RoundRectangle2D shape =
                    new RoundRectangle2D.Double(
                            0.5,
                            0.5,
                            w - 1,
                            h - 1,
                            RADIUS,
                            RADIUS
                    );

            g2.setColor(
                    DashboardTheme.ACCENT_LIGHT
            );

            g2.fill(shape);

            g2.setColor(
                    DashboardTheme.CANVAS_BORDER
            );

            g2.draw(shape);

            if (selected) {

                g2.setColor(
                        DashboardTheme.ACCENT
                );

                g2.setStroke(
                        new BasicStroke(2f)
                );

                g2.draw(shape);

                paintResizeHandles(g2);
            }

        } finally {

            g2.dispose();
        }
    }

    private void paintResizeHandles(
            Graphics2D g2
    ) {

        int w = getWidth();
        int h = getHeight();

        int half =
                HANDLE_SIZE / 2;

        Point[] points = {

                new Point(0, 0),
                new Point(w / 2, 0),
                new Point(w, 0),

                new Point(w, h / 2),

                new Point(w, h),
                new Point(w / 2, h),
                new Point(0, h),

                new Point(0, h / 2)
        };

        g2.setColor(
                DashboardTheme.ACCENT
        );

        for (Point point : points) {

            g2.fillRect(
                    point.x - half,
                    point.y - half,
                    HANDLE_SIZE,
                    HANDLE_SIZE
            );
        }
    }

    // =========================================================================
    // GRID
    // =========================================================================

    public void setGridX(int gridX) {
        this.gridX =
                Math.max(0, gridX);
    }

    public void setGridY(int gridY) {
        this.gridY =
                Math.max(0, gridY);
    }

    public void setGridWidth(int gridWidth) {
        this.gridWidth =
                Math.max(1, gridWidth);
    }

    public void setGridHeight(int gridHeight) {
        this.gridHeight =
                Math.max(1, gridHeight);
    }

    public void setGridSize(
            int width,
            int height
    ) {

        this.gridWidth =
                Math.max(1, width);

        this.gridHeight =
                Math.max(1, height);
    }

    public void setSelected(
            boolean selected
    ) {

        this.selected =
                selected;

        repaint();
    }

    public enum ResizeDirection {

        NONE,

        NORTH,
        SOUTH,
        WEST,
        EAST,

        NORTH_WEST,
        NORTH_EAST,
        SOUTH_WEST,
        SOUTH_EAST
    }
}
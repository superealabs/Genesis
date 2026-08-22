package org.labs.genesis.forms.components;

import org.labs.genesis.forms.theme.DashboardTheme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

public class GridCanvas extends JPanel {

    private static final int GRID_COLUMNS = 12;
    private static final int RADIUS = 12;
    private static final int BORDER_WIDTH = 1;

    private final java.util.List<DashboardVisualComponent> visualComponents =
            new java.util.ArrayList<>();

    private DashboardVisualComponent selectedVisual;

    // ============================================================
    // REDIMENSIONNEMENT
    // ============================================================

    private DashboardVisualComponent resizingComponent;

    private DashboardVisualComponent.ResizeDirection resizeDirection =
            DashboardVisualComponent.ResizeDirection.NONE;

    private int resizeStartGridX;
    private int resizeStartGridY;
    private int resizeStartWidth;
    private int resizeStartHeight;

    /**
     * Toujours dans les coordonnées de GridCanvas.
     */
    private Point resizeStartMouse;

    /**
     * Taille de cellule figée pendant le resize.
     */
    private int resizeCellSize;

    // ============================================================
    // DÉPLACEMENT
    // ============================================================

    private DashboardVisualComponent draggingComponent;

    /**
     * Toujours dans les coordonnées de GridCanvas.
     */
    private Point dragStartMouse;

    private int dragStartGridX;
    private int dragStartGridY;

    /**
     * Taille de cellule figée pendant le drag.
     */
    private int dragCellSize;

    // ============================================================
    // ÉTAT
    // ============================================================

    private boolean isDraggingOrResizing = false;

    public GridCanvas() {
        setOpaque(false);
        setBorder(null);
        setMinimumSize(new Dimension(0, 0));
        setLayout(null);

        // ========================================================
        // ÉVÉNEMENTS DU CANVAS
        // ========================================================

        addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                Component component = getComponentAt(e.getPoint());

                if (component == GridCanvas.this) {
                    selectVisual(null);
                    return;
                }

                DashboardVisualComponent visual = findVisualParent(component);

                if (visual != null) {
                    handleMousePressed(visual, e);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                stopResize();
                stopDrag();
            }
        });

        addMouseMotionListener(new MouseAdapter() {

            @Override
            public void mouseDragged(MouseEvent e) {

                if (resizingComponent != null) {
                    resizeComponent(e.getPoint());

                } else if (draggingComponent != null) {
                    dragComponent(e.getPoint());
                }
            }
        });
    }

    // ============================================================
    // GESTION DES ÉVÉNEMENTS
    // ============================================================

    private void handleMousePressed(
            DashboardVisualComponent visual,
            MouseEvent e) {

        if (isDraggingOrResizing) {
            return;
        }

        selectVisual(visual);

        /*
         * IMPORTANT :
         *
         * e.getPoint() appartient au composant qui a reçu
         * l'événement.
         *
         * On convertit donc explicitement vers :
         *
         * 1. visual       -> pour savoir si on est sur un handle
         * 2. GridCanvas   -> pour mémoriser la position de départ
         */

        Point visualPoint = SwingUtilities.convertPoint(
                e.getComponent(),
                e.getPoint(),
                visual
        );

        Point canvasPoint = SwingUtilities.convertPoint(
                e.getComponent(),
                e.getPoint(),
                GridCanvas.this
        );

        DashboardVisualComponent.ResizeDirection direction =
                visual.getResizeDirection(visualPoint);

        if (direction != DashboardVisualComponent.ResizeDirection.NONE) {

            startResize(
                    visual,
                    direction,
                    canvasPoint
            );

        } else {

            startDrag(
                    visual,
                    canvasPoint
            );
        }
    }

    /**
     * Retourne le DashboardVisualComponent parent d'un composant.
     */
    private DashboardVisualComponent findVisualParent(Component component) {

        Component current = component;

        while (current != null) {

            if (current instanceof DashboardVisualComponent visual) {
                return visual;
            }

            current = current.getParent();
        }

        return null;
    }

    // ============================================================
    // PROPAGATION DES ÉVÉNEMENTS
    // ============================================================

    private void installMouseForwarding(Component component) {

        MouseAdapter adapter = new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {

                DashboardVisualComponent visual =
                        findVisualParent(component);

                if (visual != null) {
                    handleMousePressed(visual, e);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                stopResize();
                stopDrag();
            }

            @Override
            public void mouseDragged(MouseEvent e) {

                if (resizingComponent != null) {

                    Point point = SwingUtilities.convertPoint(
                            component,
                            e.getPoint(),
                            GridCanvas.this
                    );

                    resizeComponent(point);

                } else if (draggingComponent != null) {

                    Point point = SwingUtilities.convertPoint(
                            component,
                            e.getPoint(),
                            GridCanvas.this
                    );

                    dragComponent(point);
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {

                if (isDraggingOrResizing) {
                    return;
                }

                DashboardVisualComponent visual =
                        findVisualParent(e.getComponent());

                if (visual != null) {

                    Point point = SwingUtilities.convertPoint(
                            e.getComponent(),
                            e.getPoint(),
                            visual
                    );

                    visual.handleMouseMoved(point);
                }
            }
        };

        component.addMouseListener(adapter);
        component.addMouseMotionListener(adapter);

        if (component instanceof Container container) {

            for (Component child : container.getComponents()) {
                installMouseForwarding(child);
            }
        }
    }

    // ============================================================
    // REDIMENSIONNEMENT
    // ============================================================

    private void startResize(
            DashboardVisualComponent component,
            DashboardVisualComponent.ResizeDirection direction,
            Point mousePoint) {

        isDraggingOrResizing = true;

        resizingComponent = component;
        resizeDirection = direction;

        /*
         * mousePoint est déjà dans GridCanvas.
         */
        resizeStartMouse = new Point(mousePoint);

        resizeStartGridX = component.getGridX();
        resizeStartGridY = component.getGridY();

        resizeStartWidth = component.getGridWidth();
        resizeStartHeight = component.getGridHeight();

        /*
         * On fige la taille de cellule pendant tout le resize.
         */
        resizeCellSize = getCellSize();

        component.setResizing(true);
        component.setActiveResizeDirection(direction);

        setCursor(getResizeCursorForDirection(direction));
    }

    private Cursor getResizeCursorForDirection(
            DashboardVisualComponent.ResizeDirection dir) {

        int cursorType;

        switch (dir) {

            case NORTH:
            case SOUTH:
                cursorType = Cursor.N_RESIZE_CURSOR;
                break;

            case EAST:
            case WEST:
                cursorType = Cursor.E_RESIZE_CURSOR;
                break;

            case NORTH_EAST:
            case SOUTH_WEST:
                cursorType = Cursor.NE_RESIZE_CURSOR;
                break;

            case NORTH_WEST:
            case SOUTH_EAST:
                cursorType = Cursor.NW_RESIZE_CURSOR;
                break;

            default:
                cursorType = Cursor.DEFAULT_CURSOR;
                break;
        }

        return Cursor.getPredefinedCursor(cursorType);
    }

    private void resizeComponent(Point mousePoint) {

        if (resizingComponent == null ||
                resizeStartMouse == null) {
            return;
        }

        int cellSize = resizeCellSize;

        if (cellSize <= 0) {
            return;
        }

        /*
         * Les deux points sont maintenant dans le même système
         * de coordonnées : GridCanvas.
         */
        int deltaX =
                mousePoint.x - resizeStartMouse.x;

        int deltaY =
                mousePoint.y - resizeStartMouse.y;

        int gridDeltaX =
                Math.round(deltaX / (float) cellSize);

        int gridDeltaY =
                Math.round(deltaY / (float) cellSize);

        int newX = resizeStartGridX;
        int newY = resizeStartGridY;

        int newWidth = resizeStartWidth;
        int newHeight = resizeStartHeight;

        switch (resizeDirection) {

            case EAST:

                newWidth =
                        resizeStartWidth + gridDeltaX;

                break;

            case WEST:

                newX =
                        resizeStartGridX + gridDeltaX;

                newWidth =
                        resizeStartWidth - gridDeltaX;

                break;

            case SOUTH:

                newHeight =
                        resizeStartHeight + gridDeltaY;

                break;

            case NORTH:

                newY =
                        resizeStartGridY + gridDeltaY;

                newHeight =
                        resizeStartHeight - gridDeltaY;

                break;

            case NORTH_EAST:

                newY =
                        resizeStartGridY + gridDeltaY;

                newHeight =
                        resizeStartHeight - gridDeltaY;

                newWidth =
                        resizeStartWidth + gridDeltaX;

                break;

            case NORTH_WEST:

                newX =
                        resizeStartGridX + gridDeltaX;

                newWidth =
                        resizeStartWidth - gridDeltaX;

                newY =
                        resizeStartGridY + gridDeltaY;

                newHeight =
                        resizeStartHeight - gridDeltaY;

                break;

            case SOUTH_EAST:

                newWidth =
                        resizeStartWidth + gridDeltaX;

                newHeight =
                        resizeStartHeight + gridDeltaY;

                break;

            case SOUTH_WEST:

                newX =
                        resizeStartGridX + gridDeltaX;

                newWidth =
                        resizeStartWidth - gridDeltaX;

                newHeight =
                        resizeStartHeight + gridDeltaY;

                break;

            default:
                return;
        }

        // ========================================================
        // CONTRAINTES
        // ========================================================

        if (newWidth < 1) {

            newWidth = 1;

            if (resizeDirection ==
                    DashboardVisualComponent.ResizeDirection.WEST
                    || resizeDirection ==
                    DashboardVisualComponent.ResizeDirection.NORTH_WEST
                    || resizeDirection ==
                    DashboardVisualComponent.ResizeDirection.SOUTH_WEST) {

                newX =
                        resizeStartGridX +
                                resizeStartWidth -
                                1;
            }
        }

        if (newHeight < 1) {

            newHeight = 1;

            if (resizeDirection ==
                    DashboardVisualComponent.ResizeDirection.NORTH
                    || resizeDirection ==
                    DashboardVisualComponent.ResizeDirection.NORTH_WEST
                    || resizeDirection ==
                    DashboardVisualComponent.ResizeDirection.NORTH_EAST) {

                newY =
                        resizeStartGridY +
                                resizeStartHeight -
                                1;
            }
        }

        // Limite gauche
        if (newX < 0) {

            newWidth += newX;
            newX = 0;
        }

        // Limite haute
        if (newY < 0) {

            newHeight += newY;
            newY = 0;
        }

        // Limite droite
        if (newX + newWidth > GRID_COLUMNS) {

            newWidth =
                    GRID_COLUMNS - newX;
        }

        newWidth = Math.max(1, newWidth);
        newHeight = Math.max(1, newHeight);

        // ========================================================
        // APPLICATION
        // ========================================================

        resizingComponent.setGridX(newX);
        resizingComponent.setGridY(newY);

        resizingComponent.setGridSize(
                newWidth,
                newHeight
        );

        updateCanvasSize();

        revalidate();
        repaint();
    }

    private void stopResize() {

        if (resizingComponent != null) {

            resizingComponent.setResizing(false);

            resizingComponent.setActiveResizeDirection(
                    DashboardVisualComponent.ResizeDirection.NONE
            );
        }

        resizingComponent = null;

        resizeDirection =
                DashboardVisualComponent.ResizeDirection.NONE;

        resizeStartMouse = null;

        resizeCellSize = 0;

        isDraggingOrResizing = false;
    }

    // ============================================================
    // DÉPLACEMENT
    // ============================================================

    private void startDrag(
            DashboardVisualComponent component,
            Point mousePoint) {

        isDraggingOrResizing = true;

        draggingComponent = component;

        /*
         * mousePoint est TOUJOURS dans GridCanvas.
         *
         * C'est la correction principale du bug de saut.
         */
        dragStartMouse = new Point(mousePoint);

        dragStartGridX = component.getGridX();
        dragStartGridY = component.getGridY();

        /*
         * Figer la taille de cellule pendant le drag.
         */
        dragCellSize = getCellSize();

        component.setDragging(true);

        setCursor(
                Cursor.getPredefinedCursor(
                        Cursor.MOVE_CURSOR
                )
        );
    }

    private void dragComponent(Point mousePoint) {

        if (draggingComponent == null ||
                dragStartMouse == null) {
            return;
        }

        int cellSize = dragCellSize;

        if (cellSize <= 0) {
            return;
        }

        /*
         * Les deux points sont dans GridCanvas.
         */
        int deltaX =
                mousePoint.x - dragStartMouse.x;

        int deltaY =
                mousePoint.y - dragStartMouse.y;

        int gridDeltaX =
                Math.round(deltaX / (float) cellSize);

        int gridDeltaY =
                Math.round(deltaY / (float) cellSize);

        int newX =
                dragStartGridX + gridDeltaX;

        int newY =
                dragStartGridY + gridDeltaY;

        int compWidth =
                draggingComponent.getGridWidth();

        int compHeight =
                draggingComponent.getGridHeight();

        // ========================================================
        // LIMITES
        // ========================================================

        newX = Math.max(
                0,
                Math.min(
                        GRID_COLUMNS - compWidth,
                        newX
                )
        );

        newY = Math.max(
                0,
                newY
        );

        // ========================================================
        // COLLISIONS
        // ========================================================

        if (isAreaAvailableExcluding(
                newX,
                newY,
                compWidth,
                compHeight,
                draggingComponent
        )) {

            draggingComponent.setGridX(newX);
            draggingComponent.setGridY(newY);

            updateCanvasSize();

            revalidate();
            repaint();
        }
    }

    private void stopDrag() {

        if (draggingComponent != null) {

            draggingComponent.setDragging(false);

            /*
             * Restaurer le curseur correspondant à la position
             * actuelle de la souris.
             */
            Point mousePos =
                    draggingComponent.getMousePosition();

            if (mousePos != null) {
                draggingComponent.handleMouseMoved(mousePos);
            }
        }

        draggingComponent = null;

        dragStartMouse = null;

        dragCellSize = 0;

        isDraggingOrResizing = false;
    }

    // ============================================================
    // POSITIONNEMENT
    // ============================================================

    private int getCellSize() {

        int availableWidth =
                getWidth() - 2 * BORDER_WIDTH;

        return Math.max(
                1,
                availableWidth / GRID_COLUMNS
        );
    }

    private boolean isAreaAvailableExcluding(
            int x,
            int y,
            int width,
            int height,
            DashboardVisualComponent exclude) {

        for (DashboardVisualComponent existing :
                visualComponents) {

            if (existing == exclude) {
                continue;
            }

            if (rectanglesIntersect(
                    x,
                    y,
                    width,
                    height,
                    existing.getGridX(),
                    existing.getGridY(),
                    existing.getGridWidth(),
                    existing.getGridHeight()
            )) {
                return false;
            }
        }

        return true;
    }

    private boolean rectanglesIntersect(
            int x1,
            int y1,
            int w1,
            int h1,
            int x2,
            int y2,
            int w2,
            int h2) {

        return x1 < x2 + w2
                && x1 + w1 > x2
                && y1 < y2 + h2
                && y1 + h1 > y2;
    }

    // ============================================================
    // SÉLECTION
    // ============================================================

    public void selectVisual(
            DashboardVisualComponent component) {

        if (selectedVisual != null) {
            selectedVisual.setSelected(false);
        }

        selectedVisual = component;

        if (selectedVisual != null) {

            selectedVisual.setSelected(true);

            Point mousePos =
                    selectedVisual.getMousePosition();

            if (mousePos != null) {
                selectedVisual.handleMouseMoved(mousePos);
            }
        }

        repaint();
    }

    // ============================================================
    // AJOUT
    // ============================================================

    public void addVisualComponent(String title) {

        DashboardVisualComponent component =
                new DashboardVisualComponent(title);

        addVisualComponent(component);
    }

    public void addVisualComponent(
            DashboardVisualComponent component) {

        Point position =
                findAvailablePosition(
                        component.getGridWidth(),
                        component.getGridHeight()
                );

        component.setGridX(position.x);
        component.setGridY(position.y);

        visualComponents.add(component);

        add(component);

        installMouseForwarding(component);

        selectVisual(component);

        updateCanvasSize();
        updateVisualComponents();

        revalidate();
        repaint();

        SwingUtilities.invokeLater(() ->
                scrollRectToVisible(
                        component.getBounds()
                )
        );
    }

    private Point findAvailablePosition(
            int componentWidth,
            int componentHeight) {

        for (int y = 0; ; y++) {

            for (
                    int x = 0;
                    x <= GRID_COLUMNS - componentWidth;
                    x++
            ) {

                if (isAreaAvailable(
                        x,
                        y,
                        componentWidth,
                        componentHeight
                )) {

                    return new Point(x, y);
                }
            }
        }
    }

    private boolean isAreaAvailable(
            int x,
            int y,
            int width,
            int height) {

        for (DashboardVisualComponent existing :
                visualComponents) {

            if (rectanglesIntersect(
                    x,
                    y,
                    width,
                    height,
                    existing.getGridX(),
                    existing.getGridY(),
                    existing.getGridWidth(),
                    existing.getGridHeight()
            )) {
                return false;
            }
        }

        return true;
    }

    // ============================================================
    // CANVAS
    // ============================================================

    private void updateCanvasSize() {

        int width =
                getParent() != null
                        ? getParent().getWidth()
                        : getWidth();

        if (width <= 0) {
            return;
        }

        int cellSize =
                Math.max(
                        1,
                        width / GRID_COLUMNS
                );

        int requiredRows = 8;

        for (DashboardVisualComponent component :
                visualComponents) {

            int bottom =
                    component.getGridY()
                            + component.getGridHeight();

            requiredRows =
                    Math.max(
                            requiredRows,
                            bottom
                    );
        }

        int requiredHeight =
                requiredRows * cellSize;

        setPreferredSize(
                new Dimension(
                        width,
                        requiredHeight
                )
        );
    }

    private void updateVisualComponents() {

        int width = getWidth();

        if (width <= 0) {
            return;
        }

        int availableWidth =
                width - 2 * BORDER_WIDTH;

        int cellSize =
                Math.max(
                        1,
                        availableWidth / GRID_COLUMNS
                );

        for (DashboardVisualComponent component :
                visualComponents) {

            int x =
                    BORDER_WIDTH
                            + component.getGridX()
                            * cellSize;

            int y =
                    BORDER_WIDTH
                            + component.getGridY()
                            * cellSize;

            int compWidth =
                    component.getGridWidth()
                            * cellSize;

            int compHeight =
                    component.getGridHeight()
                            * cellSize;

            component.setBounds(
                    x,
                    y,
                    compWidth,
                    compHeight
            );
        }
    }

    @Override
    public void doLayout() {
        updateVisualComponents();
    }

    // ============================================================
    // PAINT
    // ============================================================

    @Override
    protected void paintComponent(Graphics g) {

        super.paintComponent(g);

        int width = getWidth();
        int height = getHeight();

        if (width <= 0 || height <= 0) {
            return;
        }

        Graphics2D graphics =
                (Graphics2D) g.create();

        try {

            graphics.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            graphics.setRenderingHint(
                    RenderingHints.KEY_STROKE_CONTROL,
                    RenderingHints.VALUE_STROKE_PURE
            );

            double canvasX = 0.5;
            double canvasY = 0.5;

            double canvasWidth =
                    width - 1.0;

            double canvasHeight =
                    height - 1.0;

            RoundRectangle2D canvasShape =
                    new RoundRectangle2D.Double(
                            canvasX,
                            canvasY,
                            canvasWidth,
                            canvasHeight,
                            RADIUS,
                            RADIUS
                    );

            graphics.setColor(
                    DashboardTheme.CANVAS_BG
            );

            graphics.fill(canvasShape);

            Shape oldClip =
                    graphics.getClip();

            graphics.clip(canvasShape);

            int availableWidth =
                    width - 2 * BORDER_WIDTH;

            int availableHeight =
                    height - 2 * BORDER_WIDTH;

            int cellSize =
                    Math.max(
                            1,
                            availableWidth / GRID_COLUMNS
                    );

            int gridRows =
                    Math.max(
                            1,
                            (int) Math.ceil(
                                    availableHeight
                                            / (double) cellSize
                            )
                    );

            int gridWidth =
                    GRID_COLUMNS * cellSize;

            int gridHeight =
                    gridRows * cellSize;

            int offsetX =
                    Math.max(
                            0,
                            (width - gridWidth) / 2
                    );

            int offsetY = 0;

            graphics.setColor(
                    DashboardTheme.GRID_COLOR
            );

            for (
                    int col = 0;
                    col <= GRID_COLUMNS;
                    col++
            ) {

                int x =
                        offsetX
                                + col * cellSize;

                graphics.drawLine(
                        x,
                        offsetY,
                        x,
                        Math.min(
                                height,
                                offsetY + gridHeight
                        )
                );
            }

            for (
                    int row = 0;
                    row <= gridRows;
                    row++
            ) {

                int y =
                        offsetY
                                + row * cellSize;

                graphics.drawLine(
                        offsetX,
                        y,
                        Math.min(
                                width,
                                offsetX + gridWidth
                        ),
                        y
                );
            }

            graphics.setClip(oldClip);

            graphics.setColor(
                    DashboardTheme.CANVAS_BORDER
            );

            graphics.setStroke(
                    new BasicStroke(
                            BORDER_WIDTH
                    )
            );

            graphics.draw(canvasShape);

            graphics.setColor(
                    DashboardTheme.TEXT_SECONDARY
            );

            graphics.setFont(
                    getFont().deriveFont(
                            Font.BOLD,
                            16f
                    )
            );

            String title = "CANVAS";

            FontMetrics metrics =
                    graphics.getFontMetrics();

            int titleX =
                    (width
                            - metrics.stringWidth(title))
                            / 2;

            int titleY =
                    (height
                            - metrics.getHeight())
                            / 2;

            graphics.drawString(
                    title,
                    titleX,
                    titleY
            );

        } finally {

            graphics.dispose();
        }
    }

    @Override
    public Dimension getPreferredSize() {

        int width =
                DashboardTheme.MAX_WIDTH;

        if (getParent() != null) {

            width =
                    Math.max(
                            DashboardTheme.MAX_WIDTH,
                            getParent().getWidth()
                    );
        }

        return new Dimension(
                width,
                super.getPreferredSize().height
        );
    }
}
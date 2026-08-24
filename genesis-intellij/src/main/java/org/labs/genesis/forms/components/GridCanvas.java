package org.labs.genesis.forms.components;

import org.labs.genesis.forms.theme.DashboardTheme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

public class GridCanvas extends JPanel {

    private static final int GRID_COLUMNS = 30;
    private static final int RADIUS = 12;
    private static final int BORDER_WIDTH = 1;

    private final List<DashboardVisualComponent> visualComponents = new ArrayList<>();
    private DashboardVisualComponent selectedVisual;

    // Resize state
    private DashboardVisualComponent resizingComponent;
    private DashboardVisualComponent.ResizeDirection resizeDirection = DashboardVisualComponent.ResizeDirection.NONE;
    private int resizeStartGridX, resizeStartGridY, resizeStartWidth, resizeStartHeight;
    private Point resizeStartMouse;
    private int resizeCellSize;

    // Drag state
    private DashboardVisualComponent draggingComponent;
    private Point dragStartMouse;
    private int dragStartGridX, dragStartGridY;
    private int dragCellSize;

    private boolean isDraggingOrResizing = false;

    public GridCanvas() {
        setOpaque(false);
        setBorder(null);
        setMinimumSize(new Dimension(0, 0));
        setLayout(null);

        MouseAdapter canvasAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Component comp = getComponentAt(e.getPoint());
                if (comp == GridCanvas.this) {
                    selectVisual(null);
                    return;
                }
                DashboardVisualComponent visual = findVisualParent(comp);
                if (visual != null) handleMousePressed(visual, e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                stopResize();
                stopDrag();
            }
        };
        addMouseListener(canvasAdapter);
        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (resizingComponent != null) resizeComponent(e.getPoint());
                else if (draggingComponent != null) dragComponent(e.getPoint());
            }
        });
    }

    private void handleMousePressed(DashboardVisualComponent visual, MouseEvent e) {
        if (isDraggingOrResizing) return;
        selectVisual(visual);

        Point visualPoint = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), visual);
        Point canvasPoint = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), GridCanvas.this);

        DashboardVisualComponent.ResizeDirection dir = visual.getResizeDirection(visualPoint);
        if (dir != DashboardVisualComponent.ResizeDirection.NONE) {
            startResize(visual, dir, canvasPoint);
        } else if (visual.isInHeaderArea(visualPoint)) {
            // Seul un clic sur l'en-tête déclenche le déplacement
            startDrag(visual, canvasPoint);
        }
        // Sinon, on ne fait que sélectionner
    }

    private DashboardVisualComponent findVisualParent(Component comp) {
        Component current = comp;
        while (current != null) {
            if (current instanceof DashboardVisualComponent visual) return visual;
            current = current.getParent();
        }
        return null;
    }

    // ---- Mouse forwarding to children ----

    private void installMouseForwarding(Component component) {
        MouseAdapter adapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                DashboardVisualComponent visual = findVisualParent(component);
                if (visual != null) handleMousePressed(visual, e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                stopResize();
                stopDrag();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                Point p = SwingUtilities.convertPoint(component, e.getPoint(), GridCanvas.this);
                if (resizingComponent != null) resizeComponent(p);
                else if (draggingComponent != null) dragComponent(p);
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                if (isDraggingOrResizing) return;
                DashboardVisualComponent visual = findVisualParent(e.getComponent());
                if (visual != null) {
                    Point p = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), visual);
                    visual.handleMouseMoved(p);
                }
            }
        };
        component.addMouseListener(adapter);
        component.addMouseMotionListener(adapter);
        if (component instanceof Container cont) {
            for (Component child : cont.getComponents()) {
                installMouseForwarding(child);
            }
        }
    }

    // ---- Resize ----

    private void startResize(DashboardVisualComponent comp,
                             DashboardVisualComponent.ResizeDirection dir,
                             Point mousePoint) {
        isDraggingOrResizing = true;
        resizingComponent = comp;
        resizeDirection = dir;
        resizeStartMouse = new Point(mousePoint);
        resizeStartGridX = comp.getGridX();
        resizeStartGridY = comp.getGridY();
        resizeStartWidth = comp.getGridWidth();
        resizeStartHeight = comp.getGridHeight();
        resizeCellSize = getCellSize();
        comp.setResizing(true);
        comp.setActiveResizeDirection(dir);
        setCursor(getResizeCursorForDirection(dir));
    }

    private Cursor getResizeCursorForDirection(DashboardVisualComponent.ResizeDirection dir) {
        int type = switch (dir) {
            case NORTH, SOUTH -> Cursor.N_RESIZE_CURSOR;
            case EAST, WEST -> Cursor.E_RESIZE_CURSOR;
            case NORTH_EAST, SOUTH_WEST -> Cursor.NE_RESIZE_CURSOR;
            case NORTH_WEST, SOUTH_EAST -> Cursor.NW_RESIZE_CURSOR;
            default -> Cursor.DEFAULT_CURSOR;
        };
        return Cursor.getPredefinedCursor(type);
    }

    private void resizeComponent(Point mousePoint) {
        if (resizingComponent == null || resizeStartMouse == null) return;
        int cellSize = resizeCellSize;
        if (cellSize <= 0) return;

        int deltaX = mousePoint.x - resizeStartMouse.x;
        int deltaY = mousePoint.y - resizeStartMouse.y;
        int gridDeltaX = Math.round(deltaX / (float) cellSize);
        int gridDeltaY = Math.round(deltaY / (float) cellSize);

        int newX = resizeStartGridX, newY = resizeStartGridY;
        int newWidth = resizeStartWidth, newHeight = resizeStartHeight;

        switch (resizeDirection) {
            case EAST -> newWidth = resizeStartWidth + gridDeltaX;
            case WEST -> { newX = resizeStartGridX + gridDeltaX; newWidth = resizeStartWidth - gridDeltaX; }
            case SOUTH -> newHeight = resizeStartHeight + gridDeltaY;
            case NORTH -> { newY = resizeStartGridY + gridDeltaY; newHeight = resizeStartHeight - gridDeltaY; }
            case NORTH_EAST -> { newY = resizeStartGridY + gridDeltaY; newHeight = resizeStartHeight - gridDeltaY; newWidth = resizeStartWidth + gridDeltaX; }
            case NORTH_WEST -> { newX = resizeStartGridX + gridDeltaX; newWidth = resizeStartWidth - gridDeltaX; newY = resizeStartGridY + gridDeltaY; newHeight = resizeStartHeight - gridDeltaY; }
            case SOUTH_EAST -> { newWidth = resizeStartWidth + gridDeltaX; newHeight = resizeStartHeight + gridDeltaY; }
            case SOUTH_WEST -> { newX = resizeStartGridX + gridDeltaX; newWidth = resizeStartWidth - gridDeltaX; newHeight = resizeStartHeight + gridDeltaY; }
            default -> { return; }
        }

        // Contraintes
        if (newWidth < 1) {
            newWidth = 1;
            if (resizeDirection == DashboardVisualComponent.ResizeDirection.WEST ||
                    resizeDirection == DashboardVisualComponent.ResizeDirection.NORTH_WEST ||
                    resizeDirection == DashboardVisualComponent.ResizeDirection.SOUTH_WEST) {
                newX = resizeStartGridX + resizeStartWidth - 1;
            }
        }
        if (newHeight < 1) {
            newHeight = 1;
            if (resizeDirection == DashboardVisualComponent.ResizeDirection.NORTH ||
                    resizeDirection == DashboardVisualComponent.ResizeDirection.NORTH_WEST ||
                    resizeDirection == DashboardVisualComponent.ResizeDirection.NORTH_EAST) {
                newY = resizeStartGridY + resizeStartHeight - 1;
            }
        }
        if (newX < 0) { newWidth += newX; newX = 0; }
        if (newY < 0) { newHeight += newY; newY = 0; }
        if (newX + newWidth > GRID_COLUMNS) newWidth = GRID_COLUMNS - newX;
        newWidth = Math.max(1, newWidth);
        newHeight = Math.max(1, newHeight);

        resizingComponent.setGridX(newX);
        resizingComponent.setGridY(newY);
        resizingComponent.setGridSize(newWidth, newHeight);
        updateCanvasSize();
        revalidate();
        repaint();
    }

    private void stopResize() {
        if (resizingComponent != null) {
            resizingComponent.setResizing(false);
            resizingComponent.setActiveResizeDirection(DashboardVisualComponent.ResizeDirection.NONE);
        }
        resizingComponent = null;
        resizeDirection = DashboardVisualComponent.ResizeDirection.NONE;
        resizeStartMouse = null;
        resizeCellSize = 0;
        isDraggingOrResizing = false;
    }

    // ---- Drag ----

    private void startDrag(DashboardVisualComponent comp, Point mousePoint) {
        isDraggingOrResizing = true;
        draggingComponent = comp;
        dragStartMouse = new Point(mousePoint);
        dragStartGridX = comp.getGridX();
        dragStartGridY = comp.getGridY();
        dragCellSize = getCellSize();
        comp.setDragging(true);
        setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
    }

    private void dragComponent(Point mousePoint) {
        if (draggingComponent == null || dragStartMouse == null) return;
        int cellSize = dragCellSize;
        if (cellSize <= 0) return;

        int deltaX = mousePoint.x - dragStartMouse.x;
        int deltaY = mousePoint.y - dragStartMouse.y;
        int gridDeltaX = Math.round(deltaX / (float) cellSize);
        int gridDeltaY = Math.round(deltaY / (float) cellSize);

        int newX = Math.max(0, Math.min(GRID_COLUMNS - draggingComponent.getGridWidth(),
                dragStartGridX + gridDeltaX));
        int newY = Math.max(0, dragStartGridY + gridDeltaY);

        if (isAreaAvailableExcluding(newX, newY,
                draggingComponent.getGridWidth(),
                draggingComponent.getGridHeight(),
                draggingComponent)) {
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
            Point mousePos = draggingComponent.getMousePosition();
            if (mousePos != null) draggingComponent.handleMouseMoved(mousePos);
        }
        draggingComponent = null;
        dragStartMouse = null;
        dragCellSize = 0;
        isDraggingOrResizing = false;
    }

    // ---- Utilities ----

    private int getCellSize() {
        int available = getWidth() - 2 * BORDER_WIDTH;
        return Math.max(1, available / GRID_COLUMNS);
    }

    private boolean isAreaAvailableExcluding(int x, int y, int w, int h,
                                             DashboardVisualComponent exclude) {
        for (DashboardVisualComponent existing : visualComponents) {
            if (existing == exclude) continue;
            if (rectanglesIntersect(x, y, w, h,
                    existing.getGridX(), existing.getGridY(),
                    existing.getGridWidth(), existing.getGridHeight())) {
                return false;
            }
        }
        return true;
    }

    private boolean rectanglesIntersect(int x1, int y1, int w1, int h1,
                                        int x2, int y2, int w2, int h2) {
        return x1 < x2 + w2 && x1 + w1 > x2 && y1 < y2 + h2 && y1 + h1 > y2;
    }

    public void selectVisual(DashboardVisualComponent comp) {
        if (selectedVisual != null) selectedVisual.setSelected(false);
        selectedVisual = comp;
        if (selectedVisual != null) {
            selectedVisual.setSelected(true);
            Point mousePos = selectedVisual.getMousePosition();
            if (mousePos != null) selectedVisual.handleMouseMoved(mousePos);
        }
        repaint();
    }

    public void addVisualComponent(DashboardVisualComponent component) {
        Point pos = findAvailablePosition(component.getGridWidth(), component.getGridHeight());
        component.setGridX(pos.x);
        component.setGridY(pos.y);

        visualComponents.add(component);
        add(component);
        installMouseForwarding(component);
        selectVisual(component);

        updateCanvasSize();
        updateVisualComponents();
        revalidate();
        repaint();

        SwingUtilities.invokeLater(() -> scrollRectToVisible(component.getBounds()));
    }

    private Point findAvailablePosition(int compW, int compH) {
        for (int y = 0; ; y++) {
            for (int x = 0; x <= GRID_COLUMNS - compW; x++) {
                if (isAreaAvailable(x, y, compW, compH)) return new Point(x, y);
            }
        }
    }

    private boolean isAreaAvailable(int x, int y, int w, int h) {
        for (DashboardVisualComponent existing : visualComponents) {
            if (rectanglesIntersect(x, y, w, h,
                    existing.getGridX(), existing.getGridY(),
                    existing.getGridWidth(), existing.getGridHeight())) {
                return false;
            }
        }
        return true;
    }

    // ---- Layout ----

    private void updateCanvasSize() {
        int width = getParent() != null ? getParent().getWidth() : getWidth();
        if (width <= 0) return;
        int cellSize = Math.max(1, width / GRID_COLUMNS);
        int requiredRows = 8;
        for (DashboardVisualComponent comp : visualComponents) {
            requiredRows = Math.max(requiredRows, comp.getGridY() + comp.getGridHeight());
        }
        setPreferredSize(new Dimension(width, requiredRows * cellSize));
    }

    private void updateVisualComponents() {
        int width = getWidth();
        if (width <= 0) return;
        int cellSize = Math.max(1, (width - 2 * BORDER_WIDTH) / GRID_COLUMNS);
        for (DashboardVisualComponent comp : visualComponents) {
            int x = BORDER_WIDTH + comp.getGridX() * cellSize;
            int y = BORDER_WIDTH + comp.getGridY() * cellSize;
            comp.setBounds(x, y, comp.getGridWidth() * cellSize, comp.getGridHeight() * cellSize);
        }
    }

    @Override
    public void doLayout() {
        updateVisualComponents();
    }

    // ---- Painting ----

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int w = getWidth(), h = getHeight();
        if (w <= 0 || h <= 0) return;

        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

            RoundRectangle2D shape = new RoundRectangle2D.Double(0.5, 0.5, w - 1, h - 1, RADIUS, RADIUS);
            g2.setColor(DashboardTheme.CANVAS_BG);
            g2.fill(shape);

            Shape oldClip = g2.getClip();
            g2.clip(shape);

            int cellSize = Math.max(1, (w - 2 * BORDER_WIDTH) / GRID_COLUMNS);
            int gridRows = Math.max(1, (int) Math.ceil((h - 2 * BORDER_WIDTH) / (double) cellSize));
            int gridWidth = GRID_COLUMNS * cellSize;
            int gridHeight = gridRows * cellSize;
            int offsetX = Math.max(0, (w - gridWidth) / 2);
            int offsetY = 0;

            g2.setColor(DashboardTheme.GRID_COLOR);
            for (int col = 0; col <= GRID_COLUMNS; col++) {
                int x = offsetX + col * cellSize;
                g2.drawLine(x, offsetY, x, Math.min(h, offsetY + gridHeight));
            }
            for (int row = 0; row <= gridRows; row++) {
                int y = offsetY + row * cellSize;
                g2.drawLine(offsetX, y, Math.min(w, offsetX + gridWidth), y);
            }

            g2.setClip(oldClip);
            g2.setColor(DashboardTheme.CANVAS_BORDER);
            g2.setStroke(new BasicStroke(BORDER_WIDTH));
            g2.draw(shape);

            g2.setColor(DashboardTheme.TEXT_SECONDARY);
            g2.setFont(getFont().deriveFont(Font.BOLD, 16f));
            String title = "CANVAS";
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(title, (w - fm.stringWidth(title)) / 2, (h - fm.getHeight()) / 2 + fm.getAscent());

        } finally {
            g2.dispose();
        }
    }

    @Override
    public Dimension getPreferredSize() {
        int width = DashboardTheme.MAX_WIDTH;
        if (getParent() != null) width = Math.max(DashboardTheme.MAX_WIDTH, getParent().getWidth());
        return new Dimension(width, super.getPreferredSize().height);
    }
}
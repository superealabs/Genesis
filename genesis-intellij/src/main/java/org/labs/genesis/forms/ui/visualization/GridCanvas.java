package org.labs.genesis.forms.ui.visualization;

import lombok.Getter;
import lombok.Setter;
import org.labs.genesis.forms.theme.DashboardTheme;
import org.labs.genesis.forms.utils.CursorUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Setter
@Getter
public class GridCanvas extends JPanel {

    private static final int GRID_COLUMNS = 24;
    private static final int RADIUS = 12;

    private final List<DashboardVisualComponent> visualComponents = new ArrayList<>();
    private DashboardVisualComponent selectedVisual;

    private static final double PAGE_ASPECT_RATIO = 16.0 / 9.0;
    private static final int PAGE_MARGIN = 20;
    private static final int PAGE_PADDING = 12;
    private static final int MAX_PAGE_WIDTH = 1600;

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

    private Consumer<DashboardVisualComponent> onSelectionChanged;

    public GridCanvas() {
        setOpaque(false);
        setBorder(null);
        setMinimumSize(new Dimension(0, 0));
        setLayout(null);

        MouseAdapter canvasAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                DashboardVisualComponent visual = findVisualAt(e.getPoint());
                if (visual != null) {
                    handleMousePressed(visual, e);
                } else {
                    selectVisual(null);
                }
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

    /**
     * Trouve le composant visual à une position donnée en évitant getComponentAt.
     */
    private DashboardVisualComponent findVisualAt(Point p) {
        for (int i = visualComponents.size() - 1; i >= 0; i--) {
            DashboardVisualComponent comp = visualComponents.get(i);
            if (comp.getBounds().contains(p)) {
                return comp;
            }
        }
        return null;
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
            startDrag(visual, canvasPoint);
        }
    }

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

    private DashboardVisualComponent findVisualParent(Component comp) {
        Component current = comp;
        while (current != null) {
            if (current instanceof DashboardVisualComponent visual) return visual;
            current = current.getParent();
        }
        return null;
    }

    private void startResize(DashboardVisualComponent comp,
                             DashboardVisualComponent.ResizeDirection dir,
                             Point mousePoint) {
        resizeCellSize = getCellSize();
        isDraggingOrResizing = true;
        resizingComponent = comp;
        resizeDirection = dir;
        resizeStartMouse = new Point(mousePoint);
        resizeStartGridX = comp.getGridX();
        resizeStartGridY = comp.getGridY();
        resizeStartWidth = comp.getGridWidth();
        resizeStartHeight = comp.getGridHeight();
        comp.setResizing(true);
        comp.setActiveResizeDirection(dir);
        setCursor(CursorUtils.getCursorForDirection(dir));
    }

    public void removeVisualComponent(DashboardVisualComponent comp) {
        if (comp == null) return;
        visualComponents.remove(comp);
        remove(comp);
        if (selectedVisual == comp) {
            selectedVisual = null;
            if (onSelectionChanged != null) {
                onSelectionChanged.accept(null);
            }
        }
        updateCanvasSize();
        revalidate();
        repaint();
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
        newX = Math.min(GRID_COLUMNS - 1, newX);
        newWidth = Math.max(1, Math.min(newWidth, GRID_COLUMNS - newX));
        int maxRows = getVisibleRows();
        newY = Math.min(maxRows - 1, newY);
        newHeight = Math.max(1, Math.min(newHeight, maxRows - newY));

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
        updateCanvasSize();
        revalidate();
        repaint();
        setCursor(Cursor.getDefaultCursor());
    }

    private void startDrag(DashboardVisualComponent comp, Point mousePoint) {
        dragCellSize = getCellSize();
        isDraggingOrResizing = true;
        draggingComponent = comp;
        dragStartMouse = new Point(mousePoint);
        dragStartGridX = comp.getGridX();
        dragStartGridY = comp.getGridY();
        comp.setDragging(true);
        setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
        setComponentZOrder(comp, 0);
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
        int newY = Math.max(0, Math.min(getVisibleRows() - draggingComponent.getGridHeight(),
                dragStartGridY + gridDeltaY));

        draggingComponent.setGridX(newX);
        draggingComponent.setGridY(newY);
        updateCanvasSize();
        revalidate();
        repaint();
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
        updateCanvasSize();
        revalidate();
        repaint();
        setCursor(Cursor.getDefaultCursor());
    }

    private int getCellSize() {
        if (isDraggingOrResizing) {
            return Math.max(1, resizingComponent != null ? resizeCellSize : dragCellSize);
        }
        Rectangle page = getPageBounds();
        return Math.max(1, Math.min((page.width - 2 * PAGE_PADDING) / GRID_COLUMNS,
                (page.height - 2 * PAGE_PADDING) / getRequiredRows()));
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
        if (onSelectionChanged != null) {
            onSelectionChanged.accept(comp);
        }
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

    private int getRequiredRows() {
        int rows = 8;
        for (DashboardVisualComponent comp : visualComponents) {
            rows = Math.max(rows, comp.getGridY() + comp.getGridHeight());
        }
        return rows;
    }

    private Rectangle getPageBounds() {
        int width = Math.max(1, Math.min(MAX_PAGE_WIDTH, getWidth() - 2 * PAGE_MARGIN));
        int height = Math.max(1, (int) Math.round(width / PAGE_ASPECT_RATIO));
        return new Rectangle((getWidth() - width) / 2,
                Math.max(PAGE_MARGIN, (getHeight() - height) / 2), width, height);
    }

    private int getVisibleRows() {
        return Math.max(1, (getPageBounds().height - 2 * PAGE_PADDING) / getCellSize());
    }

    private Point getGridOrigin() {
        Rectangle page = getPageBounds();
        return new Point(page.x + (page.width - GRID_COLUMNS * getCellSize()) / 2,
                page.y + PAGE_PADDING);
    }

    private void updateCanvasSize() {
        updateVisualComponents();
    }

    private void updateVisualComponents() {
        if (getWidth() <= 0) return;
        int cellSize = getCellSize();
        Point origin = getGridOrigin();
        for (DashboardVisualComponent comp : visualComponents) {
            comp.setBounds(origin.x + comp.getGridX() * cellSize,
                    origin.y + comp.getGridY() * cellSize,
                    Math.max(1, comp.getGridWidth() * cellSize - 4),
                    Math.max(1, comp.getGridHeight() * cellSize - 4));
        }
    }

    @Override
    public void doLayout() {
        updateVisualComponents();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Rectangle page = getPageBounds();
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            RoundRectangle2D shape = new RoundRectangle2D.Double(page.x, page.y,
                    page.width, page.height, RADIUS, RADIUS);
            g2.setColor(DashboardTheme.CANVAS_BG);
            g2.fill(shape);
            g2.setColor(DashboardTheme.CANVAS_BORDER);
            g2.draw(shape);
            g2.clip(shape);
            int cell = getCellSize();
            Point origin = getGridOrigin();
            int rows = getVisibleRows();
            g2.setColor(DashboardTheme.GRID_COLOR);
            for (int col = 0; col <= GRID_COLUMNS; col++) {
                int x = origin.x + col * cell;
                g2.drawLine(x, origin.y, x, origin.y + rows * cell);
            }
            for (int row = 0; row <= rows; row++) {
                int y = origin.y + row * cell;
                g2.drawLine(origin.x, y, origin.x + GRID_COLUMNS * cell, y);
            }
        } finally {
            g2.dispose();
        }
    }

    @Override
    public Dimension getPreferredSize() {
        int width = getParent() != null ? getParent().getWidth() : 960;
        int pageWidth = Math.max(1, Math.min(MAX_PAGE_WIDTH, width - 2 * PAGE_MARGIN));
        return new Dimension(Math.max(1, width),
                (int) Math.round(pageWidth / PAGE_ASPECT_RATIO) + 2 * PAGE_MARGIN);
    }
}

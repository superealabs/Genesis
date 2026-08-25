package org.labs.genesis.forms.components;

import lombok.Getter;
import lombok.Setter;
import org.labs.genesis.forms.theme.DashboardTheme;
import org.labs.genesis.forms.visuals.VisualizationRenderer;
import org.labs.genesis.forms.visuals.VisualizationRendererFactory;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

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

    private final VisualizationPanel.VisualizationItem visualizationItem;

    private final VisualizationRenderer renderer;
    private final JComponent visualComponent;

    private int gridX;
    private int gridY;
    private int gridWidth;
    private int gridHeight;

    // === NOUVEAU : référence au label du titre pour détection de la zone d'en-tête ===
    private JLabel titleLabel;

    public DashboardVisualComponent(VisualizationPanel.VisualizationItem visualizationItem) {
        this(visualizationItem, DEFAULT_GRID_WIDTH, DEFAULT_GRID_HEIGHT);
    }

    public DashboardVisualComponent(VisualizationPanel.VisualizationItem visualizationItem,
                                    int gridWidth,
                                    int gridHeight) {
        this.visualizationItem = visualizationItem;
        this.gridWidth = Math.max(1, gridWidth);
        this.gridHeight = Math.max(1, gridHeight);

        this.renderer = VisualizationRendererFactory.create(visualizationItem.rendererClass);
        this.visualComponent = renderer.createComponent();

        configureComponent();
    }

    private void configureComponent() {
        setLayout(new BorderLayout());
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Création du label de titre
        titleLabel = new JLabel(visualizationItem.name);
        titleLabel.setForeground(DashboardTheme.TEXT_DARK);
        titleLabel.setFont(DashboardTheme.boldFont(11));
        titleLabel.setBorder(new EmptyBorder(6, 8, 4, 8));

        add(titleLabel, BorderLayout.NORTH);

        JPanel contentContainer = new JPanel(new BorderLayout());
        contentContainer.setOpaque(false);
        contentContainer.setMinimumSize(new Dimension(0, 0));
        contentContainer.setPreferredSize(new Dimension(0, 0));

        visualComponent.setMinimumSize(new Dimension(0, 0));
        visualComponent.setPreferredSize(new Dimension(0, 0));
        visualComponent.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        contentContainer.add(visualComponent, BorderLayout.CENTER);
        add(contentContainer, BorderLayout.CENTER);
    }

    // =========================================================
    // CURSOR MANAGEMENT
    // =========================================================

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
        Point mousePos = getMousePosition();
        if (mousePos != null) {
            handleMouseMoved(mousePos);
        } else {
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
    }

    /**
     * Met à jour le curseur en fonction de la position de la souris.
     * - Sur les bords : curseur de redimensionnement.
     * - Sur l'en-tête (et sélectionné) : curseur de déplacement.
     * - Sinon : curseur par défaut.
     */
    public void handleMouseMoved(Point point) {
        if (resizing || dragging) {
            return;
        }

        if (activeResizeDirection != ResizeDirection.NONE) {
            setResizeCursor(activeResizeDirection);
            return;
        }

        if (!selected) {
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            return;
        }

        ResizeDirection direction = getResizeDirection(point);
        if (direction != ResizeDirection.NONE) {
            setResizeCursor(direction);
        } else if (isInHeaderArea(point)) {
            // Sur l'en-tête → curseur de déplacement
            setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
        } else {
            // Sur le contenu → curseur par défaut (ou main selon votre choix)
            setCursor(Cursor.getDefaultCursor());
        }
    }

    public void setActiveResizeDirection(ResizeDirection direction) {
        this.activeResizeDirection = direction;
        if (resizing || dragging) {
            return;
        }
        if (direction == ResizeDirection.NONE) {
            Point mousePos = getMousePosition();
            if (mousePos != null) {
                handleMouseMoved(mousePos);
            } else {
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }
        } else {
            setResizeCursor(direction);
        }
    }

    private void setResizeCursor(ResizeDirection direction) {
        int cursorType = switch (direction) {
            case NORTH, SOUTH -> Cursor.N_RESIZE_CURSOR;
            case EAST, WEST -> Cursor.E_RESIZE_CURSOR;
            case NORTH_EAST, SOUTH_WEST -> Cursor.NE_RESIZE_CURSOR;
            case NORTH_WEST, SOUTH_EAST -> Cursor.NW_RESIZE_CURSOR;
            default -> Cursor.DEFAULT_CURSOR;
        };
        setCursor(Cursor.getPredefinedCursor(cursorType));
    }

    // =========================================================
    // RESIZE DETECTION
    // =========================================================

    public ResizeDirection getResizeDirection(Point point) {
        if (!selected) return ResizeDirection.NONE;

        int w = getWidth();
        int h = getHeight();
        int zone = HANDLE_ZONE;

        boolean left = point.x <= zone;
        boolean right = point.x >= w - zone;
        boolean top = point.y <= zone;
        boolean bottom = point.y >= h - zone;

        if (top && left) return ResizeDirection.NORTH_WEST;
        if (top && right) return ResizeDirection.NORTH_EAST;
        if (bottom && left) return ResizeDirection.SOUTH_WEST;
        if (bottom && right) return ResizeDirection.SOUTH_EAST;
        if (top) return ResizeDirection.NORTH;
        if (bottom) return ResizeDirection.SOUTH;
        if (left) return ResizeDirection.WEST;
        if (right) return ResizeDirection.EAST;

        return ResizeDirection.NONE;
    }

    public void setTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            titleLabel.setText(visualizationItem.name);  // retour au nom par défaut
        } else {
            titleLabel.setText(title);
        }
        revalidate();
        repaint();
    }

    public String getTitle() {
        return titleLabel.getText();
    }

    // =========================================================
    // HEADER AREA DETECTION (NOUVEAU)
    // =========================================================

    /**
     * Teste si le point (en coordonnées locales du composant) se trouve
     * dans la zone de l'en-tête (le label du titre).
     */
    public boolean isInHeaderArea(Point p) {
        if (titleLabel == null) return false;
        return titleLabel.getBounds().contains(p);
    }

    // =========================================================
    // PAINTING
    // =========================================================

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            if (w <= 0 || h <= 0) return;

            RoundRectangle2D shape = new RoundRectangle2D.Double(0.5, 0.5, w - 1, h - 1, RADIUS, RADIUS);
            g2.setColor(DashboardTheme.ACCENT_LIGHT);
            g2.fill(shape);
            g2.setColor(DashboardTheme.CANVAS_BORDER);
            g2.draw(shape);

            if (selected) {
                g2.setColor(DashboardTheme.ACCENT);
                g2.setStroke(new BasicStroke(2f));
                g2.draw(shape);
                paintResizeHandles(g2);
            }
        } finally {
            g2.dispose();
        }
    }

    private void paintResizeHandles(Graphics2D g2) {
        int w = getWidth();
        int h = getHeight();
        int half = HANDLE_SIZE / 2;
        Point[] points = {
                new Point(0, 0), new Point(w / 2, 0), new Point(w, 0),
                new Point(w, h / 2),
                new Point(w, h),
                new Point(w / 2, h),
                new Point(0, h),
                new Point(0, h / 2)
        };
        g2.setColor(DashboardTheme.ACCENT);
        for (Point point : points) {
            g2.fillRect(point.x - half, point.y - half, HANDLE_SIZE, HANDLE_SIZE);
        }
    }

    // =========================================================
    // GRID
    // =========================================================

    public void setGridX(int gridX) {
        this.gridX = Math.max(0, gridX);
    }

    public void setGridY(int gridY) {
        this.gridY = Math.max(0, gridY);
    }

    public void setGridWidth(int gridWidth) {
        this.gridWidth = Math.max(1, gridWidth);
    }

    public void setGridHeight(int gridHeight) {
        this.gridHeight = Math.max(1, gridHeight);
    }

    public void setGridSize(int width, int height) {
        this.gridWidth = Math.max(1, width);
        this.gridHeight = Math.max(1, height);
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
        repaint();
    }

    public enum ResizeDirection {
        NONE,
        NORTH, SOUTH,
        WEST, EAST,
        NORTH_WEST, NORTH_EAST,
        SOUTH_WEST, SOUTH_EAST
    }
}
package org.labs.genesis.forms.components;

import lombok.Getter;
import lombok.Setter;
import org.labs.genesis.forms.theme.DashboardTheme;

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

    private boolean selected = false;

    private ResizeDirection activeResizeDirection =
            ResizeDirection.NONE;

    private boolean resizing = false;
    private boolean dragging = false;

    private final String title;

    private int gridX;
    private int gridY;

    private int gridWidth;
    private int gridHeight;

    public DashboardVisualComponent(String title) {
        this(
                title,
                DEFAULT_GRID_WIDTH,
                DEFAULT_GRID_HEIGHT
        );
    }

    public DashboardVisualComponent(
            String title,
            int gridWidth,
            int gridHeight) {

        this.title = title;

        this.gridWidth =
                Math.max(1, gridWidth);

        this.gridHeight =
                Math.max(1, gridHeight);

        configureComponent();
    }

    // ============================================================
    // CONFIGURATION
    // ============================================================

    private void configureComponent() {

        setLayout(new BorderLayout());

        setOpaque(false);

        /*
         * Curseur par défaut du composant.
         *
         * Le curseur de resize sera ensuite appliqué
         * lorsque la souris entre dans une zone de handle.
         */
        setCursor(
                Cursor.getPredefinedCursor(
                        Cursor.HAND_CURSOR
                )
        );

        JLabel titleLabel =
                new JLabel(title);

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

        JPanel content =
                new JPanel(new BorderLayout());

        content.setOpaque(false);

        JLabel placeholder =
                new JLabel("Visual");

        placeholder.setForeground(
                DashboardTheme.TEXT_SECONDARY
        );

        placeholder.setHorizontalAlignment(
                SwingConstants.CENTER
        );

        content.add(
                placeholder,
                BorderLayout.CENTER
        );

        add(
                content,
                BorderLayout.CENTER
        );
    }

    // ============================================================
    // CURSEUR
    // ============================================================

    public void setResizing(boolean resizing) {

        this.resizing = resizing;

        if (resizing) {

            /*
             * Le GridCanvas contrôle le curseur pendant
             * l'opération de resize.
             */
            return;

        }

        restoreCursor();
    }

    public void setDragging(boolean dragging) {

        this.dragging = dragging;

        if (dragging) {

            /*
             * Le GridCanvas contrôle le curseur pendant
             * le drag.
             */
            return;
        }

        restoreCursor();
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

    public void handleMouseMoved(Point point) {

        /*
         * Pendant un drag ou un resize, on ne touche surtout
         * pas au curseur.
         */
        if (resizing || dragging) {
            return;
        }

        if (activeResizeDirection !=
                ResizeDirection.NONE) {

            setResizeCursor(
                    activeResizeDirection
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

        ResizeDirection dir =
                getResizeDirection(point);

        if (dir != ResizeDirection.NONE) {

            setResizeCursor(dir);

        } else {

            setCursor(
                    Cursor.getPredefinedCursor(
                            Cursor.MOVE_CURSOR
                    )
            );
        }
    }

    public void setActiveResizeDirection(
            ResizeDirection direction) {

        this.activeResizeDirection =
                direction;

        if (resizing || dragging) {
            return;
        }

        if (direction ==
                ResizeDirection.NONE) {

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

            setResizeCursor(direction);
        }
    }

    private void setResizeCursor(
            ResizeDirection dir) {

        int cursorType;

        switch (dir) {

            case NORTH:
            case SOUTH:

                cursorType =
                        Cursor.N_RESIZE_CURSOR;

                break;

            case EAST:
            case WEST:

                cursorType =
                        Cursor.E_RESIZE_CURSOR;

                break;

            case NORTH_EAST:
            case SOUTH_WEST:

                cursorType =
                        Cursor.NE_RESIZE_CURSOR;

                break;

            case NORTH_WEST:
            case SOUTH_EAST:

                cursorType =
                        Cursor.NW_RESIZE_CURSOR;

                break;

            default:

                cursorType =
                        Cursor.DEFAULT_CURSOR;

                break;
        }

        setCursor(
                Cursor.getPredefinedCursor(
                        cursorType
                )
        );
    }

    // ============================================================
    // RESIZE DIRECTION
    // ============================================================

    public ResizeDirection getResizeDirection(
            Point point) {

        if (!selected) {
            return ResizeDirection.NONE;
        }

        int width = getWidth();
        int height = getHeight();

        int zone = HANDLE_ZONE;

        boolean left =
                point.x <= zone;

        boolean right =
                point.x >= width - zone;

        boolean top =
                point.y <= zone;

        boolean bottom =
                point.y >= height - zone;

        if (top && left) {
            return ResizeDirection.NORTH_WEST;
        }

        if (top && right) {
            return ResizeDirection.NORTH_EAST;
        }

        if (bottom && left) {
            return ResizeDirection.SOUTH_WEST;
        }

        if (bottom && right) {
            return ResizeDirection.SOUTH_EAST;
        }

        if (top) {
            return ResizeDirection.NORTH;
        }

        if (bottom) {
            return ResizeDirection.SOUTH;
        }

        if (left) {
            return ResizeDirection.WEST;
        }

        if (right) {
            return ResizeDirection.EAST;
        }

        return ResizeDirection.NONE;
    }

    // ============================================================
    // DESSIN
    // ============================================================

    @Override
    protected void paintComponent(Graphics g) {

        Graphics2D g2 =
                (Graphics2D) g.create();

        try {

            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            int width = getWidth();
            int height = getHeight();

            if (width <= 0 || height <= 0) {
                return;
            }

            RoundRectangle2D shape =
                    new RoundRectangle2D.Double(
                            0.5,
                            0.5,
                            width - 1,
                            height - 1,
                            RADIUS,
                            RADIUS
                    );

            // Fond
            g2.setColor(
                    DashboardTheme.ACCENT_LIGHT
            );

            g2.fill(shape);

            // Bordure
            g2.setColor(
                    DashboardTheme.CANVAS_BORDER
            );

            g2.draw(shape);

            // Sélection
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

    private void paintResizeHandles(Graphics2D g2) {

        int width = getWidth();
        int height = getHeight();

        int half =
                HANDLE_SIZE / 2;

        Point[] points = {

                new Point(0, 0),

                new Point(
                        width / 2,
                        0
                ),

                new Point(
                        width,
                        0
                ),

                new Point(
                        width,
                        height / 2
                ),

                new Point(
                        width,
                        height
                ),

                new Point(
                        width / 2,
                        height
                ),

                new Point(
                        0,
                        height
                ),

                new Point(
                        0,
                        height / 2
                )
        };

        g2.setColor(
                DashboardTheme.ACCENT
        );

        for (Point point : points) {

            int x =
                    point.x - half;

            int y =
                    point.y - half;

            g2.fillRect(
                    x,
                    y,
                    HANDLE_SIZE,
                    HANDLE_SIZE
            );
        }
    }

    // ============================================================
    // GRID SETTERS
    // ============================================================

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
            int height) {

        this.gridWidth =
                Math.max(1, width);

        this.gridHeight =
                Math.max(1, height);
    }

    public void setSelected(boolean selected) {

        this.selected = selected;

        repaint();
    }

    // ============================================================
    // RESIZE DIRECTIONS
    // ============================================================

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
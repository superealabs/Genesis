package org.labs.genesis.forms.components;

import org.labs.genesis.forms.theme.DashboardTheme;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class GridCanvas extends JPanel {

    private static final int GRID_COLUMNS = 12;
    private static final int RADIUS = 12;
    private static final int BORDER_WIDTH = 1;
    private static final Color CANVAS_BACKGROUND = new Color(245, 246, 248);
    private static final Color CANVAS_BORDER = new Color(210, 213, 218);
    private static final Color GRID_COLOR = new Color(225, 228, 232);
    private static final Color TEXT_SECONDARY = new Color(155, 159, 168);

    public GridCanvas() {
        setOpaque(false);
        setBorder(null);
        setMinimumSize(new Dimension(0, 0));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        int width = getWidth();
        int height = getHeight();
        if (width <= 0 || height <= 0) return;

        Graphics2D graphics = (Graphics2D) g.create();
        try {
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
                    RenderingHints.VALUE_STROKE_PURE);

            // Fond arrondi
            double canvasX = 0.5;
            double canvasY = 0.5;
            double canvasWidth = width - 1.0;
            double canvasHeight = height - 1.0;
            RoundRectangle2D canvasShape = new RoundRectangle2D.Double(
                    canvasX, canvasY, canvasWidth, canvasHeight, RADIUS, RADIUS
            );

            graphics.setColor(CANVAS_BACKGROUND);
            graphics.fill(canvasShape);

            // Clipping pour la grille
            Shape oldClip = graphics.getClip();
            graphics.clip(canvasShape);

            int availableWidth = width - 2 * BORDER_WIDTH;
            int availableHeight = height - 2 * BORDER_WIDTH;
            int cellSize = Math.max(1, availableWidth / GRID_COLUMNS);
            int gridRows = Math.max(1, (int) Math.ceil(availableHeight / (double) cellSize));
            int gridWidth = GRID_COLUMNS * cellSize;
            int gridHeight = gridRows * cellSize;

            int offsetX = Math.max(0, (width - gridWidth) / 2);
            int offsetY = 0;

            graphics.setColor(GRID_COLOR);

            // Lignes verticales
            for (int col = 0; col <= GRID_COLUMNS; col++) {
                int x = offsetX + col * cellSize;
                graphics.drawLine(x, offsetY,
                        x, Math.min(height, offsetY + gridHeight));
            }

            // Lignes horizontales
            for (int row = 0; row <= gridRows; row++) {
                int y = offsetY + row * cellSize;
                graphics.drawLine(offsetX, y,
                        Math.min(width, offsetX + gridWidth), y);
            }

            graphics.setClip(oldClip);

            // Bordure
            graphics.setColor(CANVAS_BORDER);
            graphics.setStroke(new BasicStroke(BORDER_WIDTH));
            graphics.draw(canvasShape);

            // Texte "CANVAS"
            graphics.setColor(TEXT_SECONDARY);
            graphics.setFont(getFont().deriveFont(Font.BOLD, 16f));
            String title = "CANVAS";
            FontMetrics metrics = graphics.getFontMetrics();
            int titleX = (width - metrics.stringWidth(title)) / 2;
            int titleY = (height - metrics.getHeight()) / 2;
            graphics.drawString(title, titleX, titleY);

        } finally {
            graphics.dispose();
        }
    }

    @Override
    public Dimension getPreferredSize() {
        int width = DashboardTheme.MAX_WIDTH;

        if (getParent() != null) {
            width = Math.max(
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
package org.labs.genesis.forms.visuals;

import org.labs.genesis.forms.theme.DashboardTheme;

import java.awt.*;
import java.awt.geom.Ellipse2D;

public class ScatterPlotRenderer extends AbstractChartRenderer {

    // =========================== DATA ===========================
    private static final double[][] POINTS = {
            {5, 18}, {8, 25}, {10, 32}, {12, 28}, {15, 45},
            {18, 50}, {20, 58}, {22, 54}, {25, 68}, {28, 72}
    };

    private static final String DEFAULT_AXIS_X_LABEL = "";
    private static final String COMPACT_AXIS_X_LABEL = "";
    private static final String DEFAULT_AXIS_Y_LABEL = "";
    private static final String COMPACT_AXIS_Y_LABEL = "";

    private static final int GRID_ALPHA = 90;
    private static final int POINT_ALPHA = 220;
    private static final int POINT_BORDER_ALPHA = 120;

    private static final int TINY_POINT_SIZE = 6;
    private static final int COMPACT_POINT_SIZE = 8;
    private static final int MEDIUM_POINT_SIZE = 10;
    private static final int NORMAL_POINT_SIZE = 12;

    // =========================== PAINT ===========================
    @Override
    protected void paintChart(Graphics2D g2, int width, int height) {
        if (width <= 0 || height <= 0) return;

        Graphics2D g = (Graphics2D) g2.create();
        try {
            setupRendering(g);
            boolean tiny = width < 180 || height < 130;
            boolean compact = !tiny && (width < 280 || height < 190);
            boolean medium = !tiny && !compact && (width < 420 || height < 280);
            drawScatterPlot(g, width, height, tiny, compact, medium);
        } finally {
            g.dispose();
        }
    }

    private void setupRendering(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    }

    // =========================== HELPERS ===========================
    private Padding getPadding(boolean tiny, boolean compact, boolean medium) {
        if (tiny) return new Padding(4, 2);
        if (compact) return new Padding(8, 5);
        if (medium) return new Padding(12, 8);
        return new Padding(18, 12);
    }

    private FontSizes getFontSizes(boolean tiny, boolean compact, boolean medium) {
        if (tiny) return new FontSizes(7f, 7f, 0f);
        if (compact) return new FontSizes(8f, 8f, 9f);
        if (medium) return new FontSizes(9f, 9f, 10f);
        return new FontSizes(10f, 10f, 11f);
    }

    private int getPointSize(boolean tiny, boolean compact, boolean medium) {
        if (tiny) return TINY_POINT_SIZE;
        if (compact) return COMPACT_POINT_SIZE;
        if (medium) return MEDIUM_POINT_SIZE;
        return NORMAL_POINT_SIZE;
    }

    private int getBottomLabelSpace(boolean tiny, boolean compact) {
        if (tiny) return 6;
        if (compact) return 26;
        return 38;
    }

    private int getLeftLabelSpace(boolean tiny, boolean compact, boolean medium) {
        if (tiny) return 0;
        if (compact) return 14;
        return medium ? 16 : 22;
    }

    // =========================== MAIN CHART ===========================
    private void drawScatterPlot(Graphics2D g, int width, int height,
                                 boolean tiny, boolean compact, boolean medium) {
        // Récupérer les légendes depuis la configuration
        String legendX = getConfigString("legendX", compact ? COMPACT_AXIS_X_LABEL : DEFAULT_AXIS_X_LABEL);
        String legendY = getConfigString("legendY", compact ? COMPACT_AXIS_Y_LABEL : DEFAULT_AXIS_Y_LABEL);

        // Vérifier si les légendes doivent être affichées
        boolean showXLegend = !tiny && legendX != null && !legendX.trim().isEmpty();
        boolean showYLegend = !tiny && legendY != null && !legendY.trim().isEmpty();

        Padding pad = getPadding(tiny, compact, medium);
        FontSizes fonts = getFontSizes(tiny, compact, medium);

        Font xFont = g.getFont().deriveFont(Font.PLAIN, fonts.xSize);
        Font yFont = g.getFont().deriveFont(Font.PLAIN, fonts.ySize);
        Font axisFont = g.getFont().deriveFont(Font.PLAIN, fonts.axisSize);

        double xUpper = calculateXUpperBound(tiny, compact);
        double yUpper = calculateYUpperBound(tiny, compact);
        double xTickUnit = calculateXTickUnit(tiny, compact);
        double yTickUnit = calculateYTickUnit(tiny, compact);

        FontMetrics yMetrics = g.getFontMetrics(yFont);
        int yAxisWidth = tiny ? 3 : computeYAxisWidth(yMetrics, yUpper, yTickUnit);

        FontMetrics xMetrics = g.getFontMetrics(xFont);
        int xAxisHeight = tiny ? 6 : xMetrics.getHeight() + 8;

        // Ajuster les espaces selon la présence des légendes
        int bottomLabelSpace = getBottomLabelSpace(tiny, compact);
        if (showXLegend) {
            bottomLabelSpace += (compact ? 14 : 20);
        }

        int leftLabelSpace = getLeftLabelSpace(tiny, compact, medium);
        if (showYLegend) {
            leftLabelSpace += (compact ? 10 : 16);
        }

        int plotX = pad.horizontal + yAxisWidth + leftLabelSpace;
        int plotY = pad.vertical;
        int plotWidth = width - plotX - pad.horizontal;
        int plotHeight = height - plotY - xAxisHeight - bottomLabelSpace - pad.vertical;

        if (plotWidth <= 20 || plotHeight <= 20) {
            drawEmptyMessage(g, width, height, "");
            return;
        }

        if (!tiny) {
            drawHorizontalGrid(g, plotX, plotY, plotWidth, plotHeight, yUpper, yTickUnit);
            if (!compact) {
                drawVerticalGrid(g, plotX, plotY, plotWidth, plotHeight, xUpper, xTickUnit);
            }
        }

        drawAxes(g, plotX, plotY, plotWidth, plotHeight);

        if (!tiny) {
            drawYAxisTicks(g, plotX, plotY, plotHeight, yUpper, yTickUnit, yFont);
            drawXAxisTicks(g, plotX, plotY, plotWidth, plotHeight, xUpper, xTickUnit, xFont);

            // Dessiner les légendes si présentes
            if (showYLegend) {
                drawYAxisLabel(g, legendY, pad.horizontal, plotY, plotHeight, axisFont);
            }
            if (showXLegend) {
                int labelY = plotY + plotHeight + xAxisHeight + (compact ? 12 : 18);
                drawXAxisLabel(g, legendX, plotX, labelY, plotWidth, axisFont);
            }
        }

        drawPoints(g, plotX, plotY, plotWidth, plotHeight, xUpper, yUpper,
                tiny, compact, medium);
    }

    // =========================== COMPUTATIONS ===========================
    private int computeYAxisWidth(FontMetrics metrics, double upper, double tickUnit) {
        int maxW = 0;
        for (double v = 0; v <= upper + 0.001; v += tickUnit) {
            String text = formatValue(v);
            maxW = Math.max(maxW, metrics.stringWidth(text));
        }
        return Math.max(26, maxW + 10);
    }

    private double calculateXUpperBound(boolean tiny, boolean compact) {
        double max = 0;
        for (double[] p : POINTS) max = Math.max(max, p[0]);
        if (tiny) return Math.ceil(max / 10.0) * 10;
        if (compact) return Math.ceil(max / 10.0) * 10 + 5;
        return Math.ceil((max + 2) / 5.0) * 5;
    }

    private double calculateYUpperBound(boolean tiny, boolean compact) {
        double max = 0;
        for (double[] p : POINTS) max = Math.max(max, p[1]);
        if (tiny) return Math.ceil(max / 20.0) * 20;
        if (compact) return Math.ceil(max / 20.0) * 20;
        return Math.ceil((max + 5) / 10.0) * 10;
    }

    private double calculateXTickUnit(boolean tiny, boolean compact) {
        return (tiny || compact) ? 10 : 5;
    }

    private double calculateYTickUnit(boolean tiny, boolean compact) {
        return (tiny || compact) ? 20 : 10;
    }

    // =========================== GRID & AXES ===========================
    private void drawHorizontalGrid(Graphics2D g, int x, int y, int width, int height,
                                    double upper, double tickUnit) {
        Stroke old = g.getStroke();
        g.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1f, new float[]{3f, 3f}, 0f));
        g.setColor(withAlpha(DashboardTheme.BORDER, GRID_ALPHA));
        for (double v = 0; v <= upper + 0.001; v += tickUnit) {
            double ratio = v / upper;
            int lineY = y + height - (int) Math.round(height * ratio);
            g.drawLine(x, lineY, x + width, lineY);
        }
        g.setStroke(old);
    }

    private void drawVerticalGrid(Graphics2D g, int x, int y, int width, int height,
                                  double upper, double tickUnit) {
        Stroke old = g.getStroke();
        g.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1f, new float[]{3f, 3f}, 0f));
        g.setColor(withAlpha(DashboardTheme.BORDER, GRID_ALPHA));
        for (double v = 0; v <= upper + 0.001; v += tickUnit) {
            double ratio = v / upper;
            int lineX = x + (int) Math.round(width * ratio);
            g.drawLine(lineX, y, lineX, y + height);
        }
        g.setStroke(old);
    }

    private void drawAxes(Graphics2D g, int x, int y, int width, int height) {
        g.setColor(DashboardTheme.BORDER);
        Stroke old = g.getStroke();
        g.setStroke(new BasicStroke(1f));
        g.drawLine(x, y, x, y + height);
        g.drawLine(x, y + height, x + width, y + height);
        g.setStroke(old);
    }

    // =========================== TICKS ===========================
    private void drawYAxisTicks(Graphics2D g, int plotX, int plotY, int plotHeight,
                                double upper, double tickUnit, Font font) {
        g.setFont(font);
        FontMetrics fm = g.getFontMetrics();
        for (double v = 0; v <= upper + 0.001; v += tickUnit) {
            double ratio = v / upper;
            int y = plotY + plotHeight - (int) Math.round(plotHeight * ratio);
            g.setColor(DashboardTheme.BORDER);
            g.drawLine(plotX - 4, y, plotX, y);
            String text = formatValue(v);
            int textX = plotX - 7 - fm.stringWidth(text);
            int textY = y - fm.getHeight() / 2 + fm.getAscent();
            g.setColor(DashboardTheme.TEXT_MUTED);
            g.drawString(text, textX, textY);
        }
    }

    private void drawXAxisTicks(Graphics2D g, int plotX, int plotY, int plotWidth, int plotHeight,
                                double upper, double tickUnit, Font font) {
        g.setFont(font);
        FontMetrics fm = g.getFontMetrics();
        int axisY = plotY + plotHeight;
        for (double v = 0; v <= upper + 0.001; v += tickUnit) {
            double ratio = v / upper;
            int x = plotX + (int) Math.round(plotWidth * ratio);
            g.setColor(DashboardTheme.BORDER);
            g.drawLine(x, axisY, x, axisY + 4);
            String text = formatValue(v);
            int textX = x - fm.stringWidth(text) / 2;
            int textY = axisY + fm.getAscent() + 6;
            g.setColor(DashboardTheme.TEXT_MUTED);
            g.drawString(text, textX, textY);
        }
    }

    // =========================== LABELS ===========================
    private void drawXAxisLabel(Graphics2D g, String text, int x, int y, int width, Font font) {
        g.setFont(font);
        FontMetrics fm = g.getFontMetrics();
        int textX = x + (width - fm.stringWidth(text)) / 2;
        g.setColor(DashboardTheme.TEXT_MUTED);
        g.drawString(text, textX, y);
    }

    private void drawYAxisLabel(Graphics2D g, String text, int x, int y, int height, Font font) {
        Graphics2D rotated = (Graphics2D) g.create();
        try {
            rotated.setFont(font);
            FontMetrics fm = rotated.getFontMetrics();
            int centerY = y + height / 2;
            rotated.rotate(-Math.PI / 2);
            rotated.setColor(DashboardTheme.TEXT_MUTED);
            int textX = -centerY - fm.stringWidth(text) / 2;
            int textY = x + fm.getAscent();
            rotated.drawString(text, textX, textY);
        } finally {
            rotated.dispose();
        }
    }

    // =========================== POINTS ===========================
    private void drawPoints(Graphics2D g, int plotX, int plotY, int plotWidth, int plotHeight,
                            double xUpper, double yUpper,
                            boolean tiny, boolean compact, boolean medium) {
        int size = getPointSize(tiny, compact, medium);
        Color pointColor = withAlpha(DashboardTheme.ACCENT, POINT_ALPHA);
        Color borderColor = withAlpha(DashboardTheme.ACCENT, POINT_BORDER_ALPHA);

        for (double[] p : POINTS) {
            double xRatio = p[0] / xUpper;
            double yRatio = p[1] / yUpper;
            double cx = plotX + plotWidth * xRatio;
            double cy = plotY + plotHeight - plotHeight * yRatio;
            double px = cx - size / 2.0;
            double py = cy - size / 2.0;

            Ellipse2D ellipse = new Ellipse2D.Double(px, py, size, size);
            g.setColor(pointColor);
            g.fill(ellipse);

            if (!tiny) {
                Stroke old = g.getStroke();
                g.setStroke(new BasicStroke(1f));
                g.setColor(borderColor);
                g.draw(ellipse);
                g.setStroke(old);
            }
        }
    }

    // =========================== UTILITIES ===========================
    private Color withAlpha(Color base, int alpha) {
        return new Color(base.getRed(), base.getGreen(), base.getBlue(), alpha);
    }

    private String formatValue(double value) {
        return Math.floor(value) == value ? String.valueOf((int) value) : String.format("%.1f", value);
    }

    protected void drawEmptyMessage(Graphics2D g, int width, int height, String msg) {
        g.setColor(DashboardTheme.TEXT_MUTED);
        g.setFont(g.getFont().deriveFont(Font.PLAIN, 14f));
        FontMetrics fm = g.getFontMetrics();
        String text = "Données insuffisantes";
        int x = (width - fm.stringWidth(text)) / 2;
        int y = (height - fm.getHeight()) / 2 + fm.getAscent();
        g.drawString(text, x, y);
    }

    // =========================== INNER CLASSES ===========================
    private static class Padding {
        final int horizontal, vertical;
        Padding(int h, int v) { horizontal = h; vertical = v; }
    }

    private static class FontSizes {
        final float xSize, ySize, axisSize;
        FontSizes(float x, float y, float a) { xSize = x; ySize = y; axisSize = a; }
    }
}
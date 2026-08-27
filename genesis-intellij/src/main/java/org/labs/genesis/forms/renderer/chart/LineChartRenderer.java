package org.labs.genesis.forms.renderer.chart;

import org.labs.genesis.forms.renderer.AbstractChartRenderer;
import org.labs.genesis.forms.theme.DashboardTheme;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;

public class LineChartRenderer extends AbstractChartRenderer {

    // =========================== DATA ===========================
    private static final String[] MONTHS = {"Jan", "Fév", "Mar", "Avr", "Mai", "Juin"};
    private static final double[] VALUES = {32, 45, 38, 52, 60, 58};

    private static final int GRID_ALPHA = 90;
    private static final int AREA_TOP_ALPHA = 70;

    // =========================== PAINT ===========================
    @Override
    protected void paintChart(Graphics2D g2, int width, int height) {
        if (width <= 0 || height <= 0) return;

        Graphics2D g = (Graphics2D) g2.create();
        try {
            setupRendering(g);
            boolean tiny = width < 180 || height < 120;
            boolean compact = !tiny && (width < 280 || height < 180);
            boolean medium = !tiny && !compact && (width < 430 || height < 270);
            drawLineChart(g, width, height, tiny, compact, medium);
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

    // =========================== MAIN CHART ===========================
    private void drawLineChart(Graphics2D g, int width, int height,
                               boolean tiny, boolean compact, boolean medium) {
        // Récupérer les légendes depuis la configuration
        String legendX = getConfigString("legendX", "");
        String legendY = getConfigString("legendY", "");

        // Vérifier si les légendes doivent être affichées
        boolean showXLegend = legendX != null && !legendX.trim().isEmpty();
        boolean showYLegend = legendY != null && !legendY.trim().isEmpty();

        Padding pad = getPadding(tiny, compact, medium);
        FontSizes fonts = getFontSizes(tiny, compact, medium);

        Font xFont = g.getFont().deriveFont(Font.PLAIN, fonts.xSize);
        Font yFont = g.getFont().deriveFont(Font.PLAIN, fonts.ySize);
        Font axisLabelFont = g.getFont().deriveFont(Font.PLAIN, fonts.axisLabelSize);

        double upperBound = calculateUpperBound(tiny);
        double tickUnit = calculateTickUnit(tiny, compact);

        FontMetrics yMetrics = g.getFontMetrics(yFont);
        int yAxisWidth = tiny ? 4 : computeYAxisWidth(yMetrics, upperBound, tickUnit);

        FontMetrics xMetrics = g.getFontMetrics(xFont);
        int xAxisHeight = tiny ? 5 : xMetrics.getHeight() + 8;

        // Ajuster les espaces selon la présence des légendes
        int bottomLabelSpace = 0;
        if (showXLegend && fonts.axisLabelSize > 0) {
            bottomLabelSpace = medium ? 20 : 24;
        }

        int leftLabelSpace = 0;
        if (showYLegend && fonts.axisLabelSize > 0) {
            leftLabelSpace = medium ? 16 : 22;
        }

        int plotX = pad.horizontal + yAxisWidth + leftLabelSpace;
        int plotY = pad.vertical;
        int plotWidth = width - plotX - pad.horizontal;
        int plotHeight = height - plotY - xAxisHeight - bottomLabelSpace - pad.vertical;

        if (plotWidth <= 10 || plotHeight <= 10) return;

        // Y axis label (seulement si présent et non vide)
        if (showYLegend && fonts.axisLabelSize > 0) {
            drawYAxisLabel(g, legendY, pad.horizontal, plotY, plotHeight, axisLabelFont);
        }

        // Grid
        if (!tiny) {
            drawHorizontalGrid(g, plotX, plotY, plotWidth, plotHeight, upperBound, tickUnit);
        }

        // Axes
        drawAxes(g, plotX, plotY, plotWidth, plotHeight);

        // Y ticks
        if (!tiny) {
            drawYAxisTicks(g, plotX, plotY, plotHeight, upperBound, tickUnit, yFont);
        }

        // X labels
        if (!tiny) {
            drawXLabels(g, plotX, plotY, plotWidth, plotHeight, xFont);
        }

        // Area & Line
        drawArea(g, plotX, plotY, plotWidth, plotHeight, upperBound);
        drawLine(g, plotX, plotY, plotWidth, plotHeight, upperBound, tiny, compact, medium);

        // X axis label (seulement si présent et non vide)
        if (showXLegend && fonts.axisLabelSize > 0) {
            int labelY = plotY + plotHeight + xAxisHeight + (medium ? 8 : 10);
            drawXAxisLabel(g, legendX, plotX, labelY, plotWidth, axisLabelFont);
        }
    }

    // =========================== HELPERS ===========================
    private Padding getPadding(boolean tiny, boolean compact, boolean medium) {
        if (tiny) return new Padding(3, 2);
        if (compact) return new Padding(7, 5);
        if (medium) return new Padding(11, 8);
        return new Padding(16, 12);
    }

    private FontSizes getFontSizes(boolean tiny, boolean compact, boolean medium) {
        if (tiny) return new FontSizes(7f, 7f, 0f);
        if (compact) return new FontSizes(8f, 8f, 0f);
        if (medium) return new FontSizes(9f, 9f, 10f);
        return new FontSizes(11f, 10f, 11f);
    }

    private float getLineWidth(boolean tiny, boolean compact, boolean medium) {
        if (tiny) return 1.2f;
        if (compact) return 1.6f;
        if (medium) return 2f;
        return 2.2f;
    }

    private int getDataPointSize(boolean tiny, boolean compact, boolean medium) {
        if (tiny) return 0; // no points
        if (compact) return 6;
        if (medium) return 8;
        return 10;
    }

    private int computeYAxisWidth(FontMetrics metrics, double upperBound, double tickUnit) {
        int maxWidth = 0;
        for (double v = 0; v <= upperBound + 0.001; v += tickUnit) {
            String text = formatValue(v);
            maxWidth = Math.max(maxWidth, metrics.stringWidth(text));
        }
        return maxWidth + 10;
    }

    // =========================== DRAWING ===========================
    private void drawHorizontalGrid(Graphics2D g, int x, int y, int width, int height,
                                    double upperBound, double tickUnit) {
        Stroke old = g.getStroke();
        g.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1f, new float[]{3f, 3f}, 0f));
        g.setColor(withAlpha(DashboardTheme.BORDER, GRID_ALPHA));

        for (double v = 0; v <= upperBound + 0.001; v += tickUnit) {
            double ratio = v / upperBound;
            int gridY = y + height - (int) Math.round(height * ratio);
            g.drawLine(x, gridY, x + width, gridY);
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

    private void drawYAxisTicks(Graphics2D g, int plotX, int plotY, int plotHeight,
                                double upperBound, double tickUnit, Font font) {
        g.setFont(font);
        FontMetrics fm = g.getFontMetrics();
        for (double v = 0; v <= upperBound + 0.001; v += tickUnit) {
            double ratio = v / upperBound;
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

    private void drawXLabels(Graphics2D g, int plotX, int plotY, int plotWidth, int plotHeight, Font font) {
        g.setFont(font);
        FontMetrics fm = g.getFontMetrics();
        int count = MONTHS.length;
        if (count == 0) return;

        for (int i = 0; i < count; i++) {
            double ratio = (count == 1) ? 0.5 : i / (double) (count - 1);
            int x = plotX + (int) Math.round(plotWidth * ratio);
            String text = MONTHS[i];
            int textX = x - fm.stringWidth(text) / 2;
            int textY = plotY + plotHeight + fm.getAscent() + 6;
            g.setColor(DashboardTheme.TEXT_MUTED);
            g.drawString(text, textX, textY);
        }
    }

    private void drawArea(Graphics2D g, int plotX, int plotY, int plotWidth, int plotHeight, double upperBound) {
        if (VALUES.length == 0) return;

        Path2D area = new Path2D.Double();
        double baselineY = plotY + plotHeight;

        for (int i = 0; i < VALUES.length; i++) {
            double xRatio = (VALUES.length == 1) ? 0.5 : i / (double) (VALUES.length - 1);
            double yRatio = VALUES[i] / upperBound;
            double x = plotX + plotWidth * xRatio;
            double y = plotY + plotHeight - plotHeight * yRatio;

            if (i == 0) {
                area.moveTo(x, baselineY);
                area.lineTo(x, y);
            } else {
                area.lineTo(x, y);
            }
        }
        double lastX = plotX + plotWidth;
        area.lineTo(lastX, baselineY);
        area.closePath();

        g.setPaint(new GradientPaint(0, plotY,
                withAlpha(DashboardTheme.ACCENT, AREA_TOP_ALPHA),
                0, (float) baselineY,
                withAlpha(DashboardTheme.ACCENT, 0)));
        g.fill(area);
        g.setPaint(null);
    }

    private void drawLine(Graphics2D g, int plotX, int plotY, int plotWidth, int plotHeight,
                          double upperBound, boolean tiny, boolean compact, boolean medium) {
        if (VALUES.length == 0) return;

        Path2D path = new Path2D.Double();
        for (int i = 0; i < VALUES.length; i++) {
            double xRatio = (VALUES.length == 1) ? 0.5 : i / (double) (VALUES.length - 1);
            double yRatio = VALUES[i] / upperBound;
            double x = plotX + plotWidth * xRatio;
            double y = plotY + plotHeight - plotHeight * yRatio;

            if (i == 0) path.moveTo(x, y);
            else path.lineTo(x, y);
        }

        float lineWidth = getLineWidth(tiny, compact, medium);
        Stroke old = g.getStroke();

        // discrete shadow
        if (!tiny) {
            Path2D shadow = (Path2D) path.clone();
            java.awt.geom.AffineTransform t = java.awt.geom.AffineTransform.getTranslateInstance(0, 1.2);
            shadow.transform(t);
            g.setStroke(new BasicStroke(lineWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.setColor(withAlpha(Color.BLACK, 25));
            g.draw(shadow);
        }

        g.setStroke(new BasicStroke(lineWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(DashboardTheme.ACCENT);
        g.draw(path);
        g.setStroke(old);

        // Data points
        if (!tiny) {
            drawDataPoints(g, plotX, plotY, plotWidth, plotHeight, upperBound, compact, medium);
        }
    }

    private void drawDataPoints(Graphics2D g, int plotX, int plotY, int plotWidth, int plotHeight,
                                double upperBound, boolean compact, boolean medium) {
        int size = getDataPointSize(false, compact, medium);
        if (size == 0) return;
        int innerSize = Math.max(2, size - 4);
        int lastIndex = VALUES.length - 1;

        for (int i = 0; i < VALUES.length; i++) {
            double xRatio = (VALUES.length == 1) ? 0.5 : i / (double) (VALUES.length - 1);
            double yRatio = VALUES[i] / upperBound;
            double centerX = plotX + plotWidth * xRatio;
            double centerY = plotY + plotHeight - plotHeight * yRatio;

            boolean isLast = i == lastIndex;
            int pointSize = isLast ? size + 4 : size;
            double x = centerX - pointSize / 2.0;
            double y = centerY - pointSize / 2.0;

            g.setColor(Color.WHITE);
            g.fill(new Ellipse2D.Double(x, y, pointSize, pointSize));

            g.setColor(DashboardTheme.ACCENT);
            g.setStroke(new BasicStroke(isLast ? 2.2f : 1.6f));
            g.draw(new Ellipse2D.Double(x, y, pointSize, pointSize));

            double innerX = centerX - innerSize / 2.0;
            double innerY = centerY - innerSize / 2.0;
            g.fill(new Ellipse2D.Double(innerX, innerY, innerSize, innerSize));
        }
    }

    private void drawXAxisLabel(Graphics2D g, String text, int x, int y, int width, Font font) {
        g.setFont(font);
        FontMetrics fm = g.getFontMetrics();
        int textX = x + (width - fm.stringWidth(text)) / 2;
        g.setColor(DashboardTheme.TEXT_DARK);
        g.drawString(text, textX, y);
    }

    private void drawYAxisLabel(Graphics2D g, String text, int x, int y, int height, Font font) {
        Graphics2D rotated = (Graphics2D) g.create();
        try {
            rotated.setFont(font);
            FontMetrics fm = rotated.getFontMetrics();
            int centerY = y + height / 2;
            rotated.rotate(-Math.PI / 2);
            rotated.setColor(DashboardTheme.TEXT_DARK);
            int textX = -centerY - fm.stringWidth(text) / 2;
            int textY = x + fm.getAscent();
            rotated.drawString(text, textX, textY);
        } finally {
            rotated.dispose();
        }
    }

    // =========================== UTILITY ===========================
    private Color withAlpha(Color base, int alpha) {
        return new Color(base.getRed(), base.getGreen(), base.getBlue(), alpha);
    }

    private double calculateUpperBound(boolean tiny) {
        double max = 0;
        for (double v : VALUES) max = Math.max(max, v);
        return tiny ? Math.ceil(max / 20.0) * 20 : Math.ceil((max + 10) / 20.0) * 20;
    }

    private double calculateTickUnit(boolean tiny, boolean compact) {
        return (tiny || compact) ? 20 : 10;
    }

    private String formatValue(double value) {
        return Math.floor(value) == value ? String.valueOf((int) value) : String.format("%.1f", value);
    }

    // =========================== INNER CLASSES ===========================
    private static class Padding {
        final int horizontal, vertical;
        Padding(int h, int v) { horizontal = h; vertical = v; }
    }

    private static class FontSizes {
        final float xSize, ySize, axisLabelSize;
        FontSizes(float x, float y, float a) { xSize = x; ySize = y; axisLabelSize = a; }
    }
}
package org.labs.genesis.forms.visuals;

import org.labs.genesis.forms.theme.DashboardTheme;

import java.awt.*;
import java.awt.geom.Rectangle2D;

public class VerticalBarChartRenderer extends AbstractChartRenderer {

    private static final String[] LABELS = {"Prod A", "Prod B", "Prod C", "Prod D"};
    private static final double[] VALUES = {420, 380, 310, 260};

    private static final Color BAR_COLOR = DashboardTheme.ACCENT;
    private static final Color GRID_COLOR = new Color(203, 213, 225);
    private static final Color AXIS_COLOR = new Color(100, 116, 139);
    private static final Color LABEL_COLOR = new Color(71, 85, 105);
    private static final Color MUTED_COLOR = DashboardTheme.TEXT_MUTED;

    // =========================== PAINT ===========================
    @Override
    protected void paintChart(Graphics2D g2, int width, int height) {
        if (width <= 0 || height <= 0) return;

        Graphics2D g = (Graphics2D) g2.create();
        try {
            setupRendering(g);
            boolean tiny = width < 180 || height < 120;
            boolean compact = width < 280 || height < 180;
            boolean medium = width < 430 || height < 270;
            drawChart(g, width, height, tiny, compact, medium);
        } finally {
            g.dispose();
        }
    }

    private void setupRendering(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    }

    // =========================== HELPERS ===========================
    private Padding getPadding(boolean tiny, boolean compact, boolean medium) {
        if (tiny) return new Padding(2, 2, 2, 2);
        if (compact) return new Padding(5, 7, 5, 7);
        if (medium) return new Padding(9, 11, 9, 11);
        return new Padding(14, 16, 14, 16);
    }

    private FontSizes getFontSizes(boolean compact, boolean medium) {
        if (compact) return new FontSizes(8f, 8f, 8f);
        if (medium) return new FontSizes(9f, 9f, 9f);
        return new FontSizes(11f, 10f, 11f);
    }

    // =========================== MAIN CHART ===========================
    private void drawChart(Graphics2D g, int width, int height,
                           boolean tiny, boolean compact, boolean medium) {
        // Récupérer les légendes depuis la configuration
        String legendX = getConfigString("legendX", "");
        String legendY = getConfigString("legendY", "");

        // Vérifier si les légendes doivent être affichées
        boolean showXLegend = !tiny && !compact && legendX != null && !legendX.trim().isEmpty();
        boolean showYLegend = !tiny && !compact && legendY != null && !legendY.trim().isEmpty();

        Padding pad = getPadding(tiny, compact, medium);
        FontSizes fonts = getFontSizes(compact, medium);

        int yAxisWidth = calculateYAxisWidth(g, tiny, compact, medium);
        int xAxisHeight = calculateXAxisHeight(tiny, compact, medium);

        // Ajuster les espaces selon la présence des légendes
        if (showXLegend) {
            xAxisHeight += 20;
        }
        if (showYLegend) {
            yAxisWidth += 16;
        }

        int chartX = pad.left + yAxisWidth;
        int chartY = pad.top;
        int chartWidth = width - pad.left - pad.right - yAxisWidth;
        int chartHeight = height - pad.top - pad.bottom - xAxisHeight;

        if (chartWidth <= 1 || chartHeight <= 1) return;

        double upperBound = calculateUpperBound(tiny, compact);
        double tickUnit = 100;

        drawGrid(g, chartX, chartY, chartWidth, chartHeight, upperBound, tickUnit, tiny, compact);
        drawAxes(g, chartX, chartY, chartWidth, chartHeight, tiny);
        drawBars(g, chartX, chartY, chartWidth, chartHeight, upperBound, width, tiny, compact, medium);

        drawXAxisLabels(g, chartX, chartY + chartHeight, chartWidth, xAxisHeight, tiny, compact, medium, fonts.xLabel);
        drawYAxisLabels(g, chartX, chartY, chartWidth, chartHeight, upperBound, tickUnit, tiny, compact, medium, fonts.yLabel);

        if (!tiny) {
            drawAxisTitles(g, chartX, chartY, chartWidth, chartHeight, yAxisWidth, xAxisHeight,
                    compact, medium, fonts.title, legendX, legendY, showXLegend, showYLegend);
        }
    }

    // =========================== GRID ===========================
    private void drawGrid(Graphics2D g, int x, int y, int width, int height,
                          double upperBound, double tickUnit, boolean tiny, boolean compact) {
        if (tiny) return;

        g.setColor(GRID_COLOR);
        g.setStroke(new BasicStroke(compact ? 0.7f : 0.8f,
                BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, new float[]{4f, 4f}, 0f));

        for (double v = 0; v <= upperBound; v += tickUnit) {
            double ratio = v / upperBound;
            int lineY = y + height - (int) Math.round(ratio * height);
            if (lineY == y + height) continue;
            g.drawLine(x, lineY, x + width, lineY);
        }
    }

    // =========================== AXES ===========================
    private void drawAxes(Graphics2D g, int x, int y, int width, int height, boolean tiny) {
        if (tiny) return;
        g.setColor(AXIS_COLOR);
        g.setStroke(new BasicStroke(1f));
        g.drawLine(x, y + height, x + width, y + height);
        g.drawLine(x, y, x, y + height);
    }

    // =========================== BARS ===========================
    private void drawBars(Graphics2D g, int chartX, int chartY, int chartWidth, int chartHeight,
                          double upperBound, double width, boolean tiny, boolean compact, boolean medium) {
        int count = LABELS.length;
        if (count == 0) return;

        double categoryGap;
        if (tiny) categoryGap = 4;
        else if (compact) categoryGap = 8;
        else if (medium) categoryGap = 14;
        else categoryGap = Math.min(26, Math.max(14, width * 0.025));

        double available = chartWidth - categoryGap * (count + 1);
        if (available <= 0) return;

        double barWidth = Math.min(available / count, tiny ? Double.MAX_VALUE : 90);
        double spacing = (chartWidth - barWidth * count) / (count + 1);

        for (int i = 0; i < count; i++) {
            double value = VALUES[i];
            double ratio = Math.max(0, Math.min(1, value / upperBound));
            int barHeight = (int) Math.round(ratio * chartHeight);
            if (barHeight <= 0) continue;

            double barX = chartX + spacing + i * (barWidth + spacing);
            double barY = chartY + chartHeight - barHeight;
            g.setColor(BAR_COLOR);
            g.fill(new Rectangle2D.Double(barX, barY, barWidth, barHeight));
        }
    }

    // =========================== LABELS ===========================
    private void drawXAxisLabels(Graphics2D g, int chartX, int axisY, int chartWidth, int axisHeight,
                                 boolean tiny, boolean compact, boolean medium, float fontSize) {
        if (tiny) return;

        Font font = g.getFont().deriveFont(Font.PLAIN, fontSize);
        g.setFont(font);
        FontMetrics fm = g.getFontMetrics();
        int count = LABELS.length;
        double catWidth = chartWidth / (double) count;

        for (int i = 0; i < count; i++) {
            String label = LABELS[i];
            int tw = fm.stringWidth(label);
            int cx = chartX + (int) Math.round(catWidth * i + catWidth / 2);
            int tx = Math.max(chartX, Math.min(cx - tw / 2, chartX + chartWidth - tw));
            int baseline = axisY + fm.getAscent() + Math.max(5, axisHeight / 5);
            g.setColor(LABEL_COLOR);
            g.drawString(label, tx, baseline);
        }
    }

    private void drawYAxisLabels(Graphics2D g, int chartX, int chartY, int chartWidth, int chartHeight,
                                 double upperBound, double tickUnit, boolean tiny, boolean compact,
                                 boolean medium, float fontSize) {
        if (tiny) return;

        Font font = g.getFont().deriveFont(Font.PLAIN, fontSize);
        g.setFont(font);
        FontMetrics fm = g.getFontMetrics();

        for (double v = 0; v <= upperBound; v += tickUnit) {
            double ratio = v / upperBound;
            int y = chartY + chartHeight - (int) Math.round(ratio * chartHeight);
            String label = formatNumber(v);
            int tx = chartX - fm.stringWidth(label) - 7;
            int baseline = y + (fm.getAscent() - fm.getDescent()) / 2;
            g.setColor(MUTED_COLOR);
            g.drawString(label, tx, baseline);
        }
    }

    // =========================== TITLES ===========================
    private void drawAxisTitles(Graphics2D g, int chartX, int chartY, int chartWidth, int chartHeight,
                                int yAxisWidth, int xAxisHeight, boolean compact, boolean medium,
                                float fontSize, String legendX, String legendY,
                                boolean showXLegend, boolean showYLegend) {
        if (compact) return; // Cache les titres en mode compact

        Font font = g.getFont().deriveFont(Font.PLAIN, fontSize);
        g.setFont(font);
        FontMetrics fm = g.getFontMetrics();
        g.setColor(LABEL_COLOR);

        // X title (seulement si présent et non vide)
        if (showXLegend) {
            int xTw = fm.stringWidth(legendX);
            int xTx = chartX + (chartWidth - xTw) / 2;
            int xTy = chartY + chartHeight + xAxisHeight - 4;
            g.drawString(legendX, xTx, xTy);
        }

        // Y title (rotated, seulement si présent et non vide)
        if (showYLegend) {
            Graphics2D rotated = (Graphics2D) g.create();
            try {
                rotated.setFont(font);
                rotated.setColor(LABEL_COLOR);
                rotated.rotate(-Math.PI / 2);
                int tw = fm.stringWidth(legendY);
                int tx = -(chartY + chartHeight / 2 + tw / 2);
                int ty = Math.max(12, yAxisWidth / 2);
                rotated.drawString(legendY, tx, ty);
            } finally {
                rotated.dispose();
            }
        }
    }

    // =========================== DIMENSIONS ===========================
    private int calculateYAxisWidth(Graphics2D g, boolean tiny, boolean compact, boolean medium) {
        if (tiny) return 0;
        float fontSize = compact ? 8f : (medium ? 9f : 10f);
        Font font = g.getFont().deriveFont(Font.PLAIN, fontSize);
        FontMetrics fm = g.getFontMetrics(font);
        return fm.stringWidth("500") + 12;
    }

    private int calculateXAxisHeight(boolean tiny, boolean compact, boolean medium) {
        if (tiny) return 0;
        if (compact) return 24;
        if (medium) return 34;
        return 48;
    }

    // =========================== UTILITY ===========================
    private double calculateUpperBound(boolean tiny, boolean compact) {
        double max = 0;
        for (double v : VALUES) max = Math.max(max, v);
        if (tiny) return Math.ceil(max / 100.0) * 100;
        if (compact) return Math.ceil((max + 30) / 100.0) * 100;
        return Math.ceil((max + 50) / 100.0) * 100;
    }

    private String formatNumber(double value) {
        return value == (long) value ? Long.toString((long) value) : String.format(java.util.Locale.US, "%.1f", value);
    }

    // =========================== INNER CLASSES ===========================
    private static class Padding {
        final int top, right, bottom, left;
        Padding(int t, int r, int b, int l) { top = t; right = r; bottom = b; left = l; }
    }

    private static class FontSizes {
        final float xLabel, yLabel, title;
        FontSizes(float x, float y, float t) { xLabel = x; yLabel = y; title = t; }
    }
}
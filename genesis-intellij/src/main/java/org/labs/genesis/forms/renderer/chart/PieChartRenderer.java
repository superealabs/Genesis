package org.labs.genesis.forms.renderer.chart;

import org.labs.genesis.forms.renderer.AbstractChartRenderer;
import org.labs.genesis.forms.theme.DashboardTheme;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.util.Locale;

public class PieChartRenderer extends AbstractChartRenderer {

    // =========================== DATA ===========================
    private static final String[] LABELS = {"Produit A", "Produit B", "Produit C", "Produit D"};
    private static final double[] VALUES = {420, 380, 310, 260};
    private static final Color[] COLORS = {
            new Color(99, 102, 241),
            new Color(34, 197, 94),
            new Color(245, 158, 11),
            new Color(239, 68, 68)
    };

    private enum LegendLayout { BOTTOM, RIGHT, HIDDEN }

    // =========================== PAINT ===========================
    @Override
    protected void paintChart(Graphics2D g2, int width, int height) {
        if (width <= 0 || height <= 0) return;

        Graphics2D g = (Graphics2D) g2.create();
        try {
            setupRendering(g);
            boolean tiny = width < 180 || height < 130;
            boolean compact = !tiny && (width < 280 || height < 190);
            boolean medium = !tiny && !compact && (width < 430 || height < 280);
            LegendLayout layout = calculateLegendLayout(width, height, tiny, compact, medium);
            drawPieChart(g, width, height, tiny, compact, medium, layout);
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

    // =========================== LAYOUT ===========================
    private LegendLayout calculateLegendLayout(int width, int height,
                                               boolean tiny, boolean compact, boolean medium) {
        if (tiny) return LegendLayout.HIDDEN;
        if (compact) return LegendLayout.BOTTOM;
        if (medium) {
            if (width > height * 1.25) return LegendLayout.RIGHT;
            return LegendLayout.BOTTOM;
        }
        return (width > height * 1.35) ? LegendLayout.RIGHT : LegendLayout.BOTTOM;
    }

    private Padding getPadding(boolean tiny, boolean compact, boolean medium) {
        if (tiny) return new Padding(2, 2);
        if (compact) return new Padding(5, 5);
        if (medium) return new Padding(10, 10);
        return new Padding(16, 16);
    }

    private FontSizes getFontSizes(boolean compact, boolean medium) {
        if (compact) return new FontSizes(8f, 9f);
        if (medium) return new FontSizes(9f, 10f);
        return new FontSizes(11f, 11f);
    }

    private int getLegendHeight(int height, boolean compact, boolean medium) {
        int base = compact ? 36 : (medium ? 52 : 62);
        int max = compact ? 52 : (medium ? 72 : 90);
        return Math.max(base, Math.min(max, height / 4));
    }

    private int getLegendWidth(int width) {
        return Math.max(110, Math.min(190, (int)(width * 0.32)));
    }

    // =========================== MAIN DRAWING ===========================
    private void drawPieChart(Graphics2D g, int width, int height,
                              boolean tiny, boolean compact, boolean medium,
                              LegendLayout layout) {
        Padding pad = getPadding(tiny, compact, medium);

        Rectangle chartArea;
        switch (layout) {
            case RIGHT:
                int legendW = getLegendWidth(width);
                chartArea = new Rectangle(pad.h, pad.v,
                        Math.max(1, width - legendW - pad.h * 2),
                        Math.max(1, height - pad.v * 2));
                break;
            case BOTTOM:
                int legendH = getLegendHeight(height, compact, medium);
                chartArea = new Rectangle(pad.h, pad.v,
                        Math.max(1, width - pad.h * 2),
                        Math.max(1, height - legendH - pad.v * 2));
                break;
            default: // HIDDEN
                chartArea = new Rectangle(pad.h, pad.v,
                        Math.max(1, width - pad.h * 2),
                        Math.max(1, height - pad.v * 2));
                break;
        }

        drawPie(g, chartArea, tiny, compact, medium);

        if (layout == LegendLayout.RIGHT) {
            int legendX = chartArea.x + chartArea.width + pad.h / 2;
            int legendW = width - legendX - pad.h;
            drawRightLegend(g, legendX, pad.v, legendW, height - pad.v * 2, compact, medium);
        } else if (layout == LegendLayout.BOTTOM) {
            int legendH = getLegendHeight(height, compact, medium);
            drawBottomLegend(g, pad.h, height - legendH - pad.v,
                    width - pad.h * 2, legendH, compact, medium);
        }
    }

    // =========================== PIE ===========================
    private void drawPie(Graphics2D g, Rectangle area, boolean tiny, boolean compact, boolean medium) {
        int size = Math.min(area.width, area.height);
        int internalPad = tiny ? 2 : (compact ? 4 : (medium ? 7 : 10));
        size = Math.max(1, size - internalPad * 2);

        if (size <= 5) return;

        int pieX = area.x + (area.width - size) / 2;
        int pieY = area.y + (area.height - size) / 2;

        double total = calculateTotal();
        if (total <= 0) {
            drawEmptyMessage(g, area.width, area.height, "Aucune donnée");
            return;
        }

        int startAngle = 90;
        for (int i = 0; i < VALUES.length; i++) {
            int angle = (int) Math.round(VALUES[i] / total * 360);
            g.setColor(COLORS[i % COLORS.length]);
            g.fillArc(pieX, pieY, size, size, startAngle, -angle);
            startAngle -= angle;
        }

        if (!tiny) {
            drawPieLabels(g, pieX, pieY, size, compact, medium, total);
        }
    }

    // =========================== LABELS ===========================
    private void drawPieLabels(Graphics2D g, int pieX, int pieY, int size,
                               boolean compact, boolean medium, double total) {
        FontSizes fs = getFontSizes(compact, medium);
        Font font = g.getFont().deriveFont(Font.PLAIN, fs.labelSize);
        g.setFont(font);
        FontMetrics fm = g.getFontMetrics();

        double centerX = pieX + size / 2.0;
        double centerY = pieY + size / 2.0;
        double radius = size / 2.0;
        double labelRadius = compact ? radius * 0.58 : radius * 0.64;

        double startAngle = 90;
        for (int i = 0; i < VALUES.length; i++) {
            double angle = VALUES[i] / total * 360.0;
            double midAngle = startAngle - angle / 2.0;
            double rad = Math.toRadians(midAngle);
            double textX = centerX + Math.cos(rad) * labelRadius;
            double textY = centerY - Math.sin(rad) * labelRadius;

            String label = formatPercent(VALUES[i], total);
            int tw = fm.stringWidth(label);
            int tx = (int) Math.round(textX - tw / 2.0);
            int ty = (int) Math.round(textY + fm.getAscent() / 2.0);

            g.setColor(Color.WHITE);
            g.drawString(label, tx, ty);
            startAngle -= angle;
        }
    }

    // =========================== LEGEND ===========================
    private void drawBottomLegend(Graphics2D g, int x, int y, int width, int height,
                                  boolean compact, boolean medium) {
        int columns = width >= 500 ? 4 : (width >= 320 ? 2 : 1);
        int rows = (int) Math.ceil(LABELS.length / (double) columns);
        int rowHeight = Math.max(18, height / Math.max(1, rows));
        int colWidth = width / columns;

        for (int i = 0; i < LABELS.length; i++) {
            int col = i % columns;
            int row = i / columns;
            int itemX = x + col * colWidth;
            int itemY = y + row * rowHeight;
            drawLegendItem(g, itemX, itemY, colWidth, rowHeight, i, compact, medium);
        }
    }

    private void drawRightLegend(Graphics2D g, int x, int y, int width, int height,
                                 boolean compact, boolean medium) {
        if (width < 50) return;
        int rowHeight = Math.max(24, Math.min(42, height / LABELS.length));
        int totalH = rowHeight * LABELS.length;
        int startY = y + Math.max(0, (height - totalH) / 2);

        for (int i = 0; i < LABELS.length; i++) {
            drawLegendItem(g, x, startY + i * rowHeight, width, rowHeight, i, compact, medium);
        }
    }

    private void drawLegendItem(Graphics2D g, int x, int y, int width, int height,
                                int index, boolean compact, boolean medium) {
        int indicatorSize = compact ? 7 : (medium ? 8 : 10);
        int centerY = y + height / 2;
        int indX = x + 2;
        int indY = centerY - indicatorSize / 2;

        g.setColor(COLORS[index % COLORS.length]);
        g.fill(new Ellipse2D.Double(indX, indY, indicatorSize, indicatorSize));

        float fontSize = compact ? 9f : (medium ? 10f : 11f);
        Font font = g.getFont().deriveFont(Font.PLAIN, fontSize);
        g.setFont(font);
        FontMetrics fm = g.getFontMetrics();

        int textX = indX + indicatorSize + 6;
        int baseline = centerY - fm.getHeight() / 2 + fm.getAscent();
        int avail = Math.max(0, width - (textX - x));
        String text = fitText(LABELS[index], fm, avail);

        g.setColor(DashboardTheme.TEXT_DARK);
        g.drawString(text, textX, baseline);
    }

    // =========================== UTILITY ===========================
    private double calculateTotal() {
        double total = 0;
        for (double v : VALUES) total += v;
        return total;
    }

    private String formatPercent(double value, double total) {
        return String.format(Locale.US, "%.0f%%", value * 100.0 / total);
    }

    private String fitText(String text, FontMetrics fm, int maxWidth) {
        if (fm.stringWidth(text) <= maxWidth) return text;
        String ellipsis = "...";
        int avail = maxWidth - fm.stringWidth(ellipsis);
        if (avail <= 0) return "";
        StringBuilder sb = new StringBuilder();
        for (char c : text.toCharArray()) {
            if (fm.stringWidth(sb.toString() + c) > avail) break;
            sb.append(c);
        }
        return sb + ellipsis;
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
        final int h, v;
        Padding(int h, int v) { this.h = h; this.v = v; }
    }

    private static class FontSizes {
        final float labelSize, legendSize;
        FontSizes(float label, float legend) { labelSize = label; legendSize = legend; }
    }
}
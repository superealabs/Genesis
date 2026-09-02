package org.labs.genesis.forms.renderer.chart;

import org.labs.genesis.forms.renderer.AbstractChartRenderer;
import org.labs.genesis.forms.theme.DashboardTheme;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.util.List;
import java.util.Locale;

public class PieChartRenderer extends AbstractChartRenderer {

    private static final Color[] COLORS = {
            new Color(99, 102, 241),   // Indigo
            new Color(34, 197, 94),    // Green
            new Color(245, 158, 11),   // Amber
            new Color(239, 68, 68),    // Red
            new Color(168, 85, 247),   // Purple
            new Color(236, 72, 153),   // Pink
            new Color(20, 184, 166),   // Teal
            new Color(251, 146, 60),   // Orange
            new Color(96, 165, 250),   // Blue
            new Color(52, 211, 153),   // Emerald
            new Color(251, 191, 36),   // Yellow
            new Color(244, 63, 94)     // Rose
    };

    private enum LegendLayout {
        BOTTOM,
        RIGHT,
        HIDDEN
    }

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

            ChartData data = getChartData();

            if (isChartDataLoading()) {
                drawEmptyMessage(g, width, height, "Chargement...");
                return;
            }

            String error = getChartDataError();
            if (error != null && !error.isBlank()) {
                drawEmptyMessage(g, width, height, error);
                return;
            }

            if (data == null || !data.hasSeries()) {
                drawEmptyMessage(g, width, height, "Aucune donnée");
                return;
            }

            double[] values = data.values();
            List<String> labels = data.labels();

            if (values == null || values.length == 0) {
                drawEmptyMessage(g, width, height, "Aucune donnée");
                return;
            }

            if (!hasValidPositiveValues(values)) {
                drawEmptyMessage(g, width, height, "Données invalides");
                return;
            }

            LegendLayout layout = calculateLegendLayout(width, height, tiny, compact, medium);
            drawPieChart(g, width, height, tiny, compact, medium, layout, labels, values);

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
    private LegendLayout calculateLegendLayout(int width, int height, boolean tiny, boolean compact, boolean medium) {
        if (tiny) return LegendLayout.HIDDEN;
        if (compact) return LegendLayout.BOTTOM;
        if (medium) {
            return (width > height * 1.25) ? LegendLayout.RIGHT : LegendLayout.BOTTOM;
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
        return Math.max(110, Math.min(190, (int) (width * 0.32)));
    }

    // =========================== MAIN DRAWING ===========================
    private void drawPieChart(Graphics2D g, int width, int height,
                              boolean tiny, boolean compact, boolean medium,
                              LegendLayout layout, List<String> labels, double[] values) {
        Padding pad = getPadding(tiny, compact, medium);
        Rectangle chartArea = calculateChartArea(width, height, pad, layout, compact, medium);

        drawPie(g, values, chartArea, tiny, compact, medium);

        if (layout == LegendLayout.RIGHT) {
            int legendX = width - getLegendWidth(width) - pad.h;
            int legendW = getLegendWidth(width);
            drawRightLegend(g, labels, values, legendX, pad.v, legendW, height - pad.v * 2, compact, medium);
        } else if (layout == LegendLayout.BOTTOM) {
            int legendH = getLegendHeight(height, compact, medium);
            drawBottomLegend(g, labels, values, pad.h, height - legendH - pad.v, width - pad.h * 2, legendH, compact, medium);
        }
    }

    private Rectangle calculateChartArea(int width, int height, Padding pad, LegendLayout layout,
                                         boolean compact, boolean medium) {
        switch (layout) {
            case RIGHT:
                int legendW = getLegendWidth(width);
                return new Rectangle(pad.h, pad.v,
                        Math.max(1, width - legendW - pad.h * 2),
                        Math.max(1, height - pad.v * 2));
            case BOTTOM:
                int legendH = getLegendHeight(height, compact, medium);
                return new Rectangle(pad.h, pad.v,
                        Math.max(1, width - pad.h * 2),
                        Math.max(1, height - legendH - pad.v * 2));
            default:
                return new Rectangle(pad.h, pad.v,
                        Math.max(1, width - pad.h * 2),
                        Math.max(1, height - pad.v * 2));
        }
    }

    // =========================== PIE ===========================
    private void drawPie(Graphics2D g, double[] values, Rectangle area,
                         boolean tiny, boolean compact, boolean medium) {
        int size = Math.min(area.width, area.height);
        int internalPadding = tiny ? 2 : (compact ? 4 : (medium ? 7 : 10));
        size = Math.max(1, size - internalPadding * 2);

        if (size <= 5) return;

        int pieX = area.x + (area.width - size) / 2;
        int pieY = area.y + (area.height - size) / 2;

        double total = calculateTotal(values);
        if (total <= 0) {
            drawEmptyMessage(g, area.width, area.height, "Aucune donnée");
            return;
        }

        int segmentCount = countPositiveValues(values);
        int[] angles = computeSliceAngles(values, total);
        int[] colors = generateColors(segmentCount);

        drawDropShadow(g, pieX, pieY, size);

        int startAngle = 90;
        int drawnSegment = 0;

        for (int i = 0; i < values.length; i++) {
            if (values[i] <= 0 || !Double.isFinite(values[i])) continue;

            int angle = angles[i];
            if (angle <= 0) continue;

            Color baseColor = getColorForIndex(drawnSegment, colors);
            Paint gradient = createSliceGradient(baseColor, pieX, pieY, size);

            g.setPaint(gradient);
            g.fillArc(pieX, pieY, size, size, startAngle, -angle);

            drawSliceHighlight(g, pieX, pieY, size, startAngle, angle);

            startAngle -= angle;
            drawnSegment++;
        }

        drawOuterRing(g, pieX, pieY, size);

        if (!tiny) {
            drawPieLabels(g, values, pieX, pieY, size, compact, medium, total);
        }
    }

    // =========================== DATA ===========================
    private boolean hasValidPositiveValues(double[] values) {
        for (double value : values) {
            if (Double.isFinite(value) && value > 0) return true;
        }
        return false;
    }

    private int countPositiveValues(double[] values) {
        int count = 0;
        for (double value : values) {
            if (Double.isFinite(value) && value > 0) count++;
        }
        return count;
    }

    private double calculateTotal(double[] values) {
        double total = 0;
        for (double value : values) {
            if (Double.isFinite(value) && value > 0) total += value;
        }
        return total;
    }

    private int[] computeSliceAngles(double[] values, double total) {
        int[] angles = new int[values.length];

        for (int i = 0; i < values.length; i++) {
            if (!Double.isFinite(values[i]) || values[i] <= 0) {
                angles[i] = 0;
                continue;
            }
            angles[i] = (int) Math.round(values[i] / total * 360);
            if (angles[i] < 1) angles[i] = 1;
        }

        int sum = 0;
        for (int angle : angles) sum += angle;

        if (sum != 360) {
            int largestIndex = -1;
            int largestAngle = -1;
            for (int i = 0; i < angles.length; i++) {
                if (angles[i] > largestAngle) {
                    largestAngle = angles[i];
                    largestIndex = i;
                }
            }
            if (largestIndex >= 0) {
                angles[largestIndex] += 360 - sum;
                if (angles[largestIndex] < 0) angles[largestIndex] = 0;
            }
        }

        return angles;
    }

    // =========================== COLORS ===========================
    private int[] generateColors(int count) {
        int[] colors = new int[count];
        for (int i = 0; i < count; i++) {
            if (i < COLORS.length) {
                colors[i] = COLORS[i].getRGB();
                continue;
            }
            float hue = (float) i / count;
            float saturation = 0.70f + (i % 3) * 0.08f;
            float brightness = 0.68f + (i % 2) * 0.12f;
            colors[i] = Color.HSBtoRGB(hue, Math.min(1f, saturation), Math.min(1f, brightness));
        }
        return colors;
    }

    private Color getColorForIndex(int index, int[] colors) {
        if (index >= 0 && index < colors.length) {
            return new Color(colors[index], true);
        }
        return COLORS[index % COLORS.length];
    }

    // =========================== GRADIENT ===========================
    private Paint createSliceGradient(Color base, int x, int y, int size) {
        Color highlight = tint(base, 0.32f);
        Color light = tint(base, 0.12f);
        Color dark = shade(base, 0.18f);

        return new LinearGradientPaint(
                new Point2D.Double(x + size * 0.18, y + size * 0.05),
                new Point2D.Double(x + size * 0.88, y + size * 0.92),
                new float[]{0f, 0.22f, 0.58f, 1f},
                new Color[]{highlight, light, base, dark}
        );
    }

    private Color tint(Color color, float factor) {
        int r = (int) Math.min(255, color.getRed() + (255 - color.getRed()) * factor);
        int g = (int) Math.min(255, color.getGreen() + (255 - color.getGreen()) * factor);
        int b = (int) Math.min(255, color.getBlue() + (255 - color.getBlue()) * factor);
        return new Color(r, g, b, color.getAlpha());
    }

    private Color shade(Color color, float factor) {
        int r = (int) Math.max(0, color.getRed() * (1 - factor));
        int g = (int) Math.max(0, color.getGreen() * (1 - factor));
        int b = (int) Math.max(0, color.getBlue() * (1 - factor));
        return new Color(r, g, b, color.getAlpha());
    }

    // =========================== SHADOW ===========================
    private void drawDropShadow(Graphics2D g, int x, int y, int size) {
        g.setColor(new Color(15, 23, 42, 12));
        g.fillOval(x + 1, y + Math.max(5, size / 28), size, size);
        g.setColor(new Color(15, 23, 42, 20));
        g.fillOval(x + 1, y + Math.max(3, size / 40), size, size);
        g.setColor(new Color(15, 23, 42, 28));
        g.fillOval(x, y + 2, size, size);
    }

    // =========================== HIGHLIGHT ===========================
    private void drawSliceHighlight(Graphics2D g, int x, int y, int size, int startAngle, int angle) {
        if (size < 80 || angle < 12) return;

        Stroke oldStroke = g.getStroke();
        try {
            float stroke = Math.max(1f, size * 0.012f);
            g.setStroke(new BasicStroke(stroke, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.setColor(new Color(255, 255, 255, 38));
            int highlightAngle = Math.max(1, (int) (angle * 0.62));
            g.drawArc(x + 1, y + 1, size - 2, size - 2, startAngle - 3, -highlightAngle);
        } finally {
            g.setStroke(oldStroke);
        }
    }

    // =========================== OUTER RING ===========================
    private void drawOuterRing(Graphics2D g, int x, int y, int size) {
        Stroke oldStroke = g.getStroke();
        try {
            g.setStroke(new BasicStroke(Math.max(0.8f, size * 0.008f)));
            g.setColor(new Color(15, 23, 42, 28));
            g.drawOval(x, y, size - 1, size - 1);
        } finally {
            g.setStroke(oldStroke);
        }
    }

    // =========================== PIE LABELS ===========================
    private void drawPieLabels(Graphics2D g, double[] values, int pieX, int pieY, int size,
                               boolean compact, boolean medium, double total) {
        FontSizes fs = getFontSizes(compact, medium);
        Font font = g.getFont().deriveFont(Font.BOLD, fs.labelSize);
        g.setFont(font);
        FontMetrics fm = g.getFontMetrics();

        double centerX = pieX + size / 2.0;
        double centerY = pieY + size / 2.0;
        double radius = size / 2.0;
        double labelRadius = compact ? radius * 0.56 : (medium ? radius * 0.62 : radius * 0.66);

        double startAngle = 90;

        for (int i = 0; i < values.length; i++) {
            if (!Double.isFinite(values[i]) || values[i] <= 0) continue;

            double angle = values[i] / total * 360.0;

            if (!shouldDrawPieLabel(angle, size, compact, medium)) {
                startAngle -= angle;
                continue;
            }

            double midAngle = startAngle - angle / 2.0;
            double rad = Math.toRadians(midAngle);
            double textX = centerX + Math.cos(rad) * labelRadius;
            double textY = centerY - Math.sin(rad) * labelRadius;

            String label = formatPercent(values[i], total);
            int textWidth = fm.stringWidth(label);

            int tx = (int) Math.round(textX - textWidth / 2.0);
            int ty = (int) Math.round(textY + (fm.getAscent() - fm.getDescent()) / 2.0);

            g.setColor(new Color(0, 0, 0, 120));
            g.drawString(label, tx + 1, ty + 1);
            g.setColor(Color.WHITE);
            g.drawString(label, tx, ty);

            startAngle -= angle;
        }
    }

    private boolean shouldDrawPieLabel(double angle, int size, boolean compact, boolean medium) {
        if (compact) return angle >= 22;
        if (medium) return angle >= 16;
        return angle >= 10;
    }

    // =========================== LEGEND ===========================
    private void drawBottomLegend(Graphics2D g, List<String> labels, double[] values,
                                  int x, int y, int width, int height,
                                  boolean compact, boolean medium) {
        if (height < 20 || width < 50) return;

        int validCount = countPositiveValues(values);
        if (validCount == 0) return;

        int columns = calculateLegendColumns(width, validCount);
        int rows = (int) Math.ceil(validCount / (double) columns);
        int rowHeight = Math.max(18, height / Math.max(1, rows));
        int colWidth = width / columns;

        int validIndex = 0;
        for (int i = 0; i < labels.size(); i++) {
            if (i >= values.length || values[i] <= 0 || !Double.isFinite(values[i])) continue;

            int col = validIndex % columns;
            int row = validIndex / columns;
            int itemX = x + col * colWidth;
            int itemY = y + row * rowHeight;

            drawLegendItem(g, labels.get(i), itemX, itemY, colWidth, rowHeight, validIndex, compact, medium);
            validIndex++;
        }
    }

    private void drawRightLegend(Graphics2D g, List<String> labels, double[] values,
                                 int x, int y, int width, int height,
                                 boolean compact, boolean medium) {
        if (width < 50 || height < 30) return;

        int validCount = countPositiveValues(values);
        if (validCount == 0) return;

        int rowHeight = Math.max(24, Math.min(42, height / validCount));
        int totalHeight = rowHeight * validCount;
        int startY = y + Math.max(0, (height - totalHeight) / 2);

        int validIndex = 0;
        for (int i = 0; i < labels.size(); i++) {
            if (i >= values.length || values[i] <= 0 || !Double.isFinite(values[i])) continue;

            drawLegendItem(g, labels.get(i), x, startY + validIndex * rowHeight,
                    width, rowHeight, validIndex, compact, medium);
            validIndex++;
        }
    }

    private int calculateLegendColumns(int width, int itemCount) {
        if (width >= 500) return Math.min(4, itemCount);
        if (width >= 320) return Math.min(2, itemCount);
        return 1;
    }

    private void drawLegendItem(Graphics2D g, String label, int x, int y, int width, int height,
                                int index, boolean compact, boolean medium) {
        if (label == null) label = "";

        int indicatorSize = compact ? 7 : (medium ? 8 : 10);
        int centerY = y + height / 2;
        int indicatorX = x + 2;
        int indicatorY = centerY - indicatorSize / 2;

        Color baseColor = getColorForIndex(index, generateColors(Math.max(index + 1, COLORS.length)));
        Paint gradient = createSliceGradient(baseColor, indicatorX, indicatorY, indicatorSize);

        g.setPaint(gradient);
        g.fill(new Ellipse2D.Double(indicatorX, indicatorY, indicatorSize, indicatorSize));

        g.setColor(new Color(255, 255, 255, 70));
        g.fillOval(indicatorX + 2, indicatorY + 1,
                Math.max(2, indicatorSize / 3), Math.max(2, indicatorSize / 4));

        g.setColor(new Color(15, 23, 42, 25));
        g.setStroke(new BasicStroke(0.8f));
        g.draw(new Ellipse2D.Double(indicatorX, indicatorY, indicatorSize, indicatorSize));

        float fontSize = compact ? 9f : (medium ? 10f : 11f);
        Font font = g.getFont().deriveFont(Font.PLAIN, fontSize);
        g.setFont(font);
        FontMetrics fm = g.getFontMetrics();

        int textX = indicatorX + indicatorSize + 6;
        int baseline = centerY - (fm.getAscent() + fm.getDescent()) / 2 + fm.getAscent();
        int availableWidth = Math.max(0, width - (textX - x) - 4);
        String text = fitText(label, fm, availableWidth);

        g.setColor(DashboardTheme.TEXT_DARK);
        g.drawString(text, textX, baseline);
    }

    // =========================== TEXT ===========================
    private String fitText(String text, FontMetrics metrics, int maxWidth) {
        if (text == null || text.isEmpty()) return "";
        if (metrics.stringWidth(text) <= maxWidth) return text;

        String ellipsis = "...";
        int ellipsisWidth = metrics.stringWidth(ellipsis);
        if (ellipsisWidth >= maxWidth) return "";

        int available = maxWidth - ellipsisWidth;
        StringBuilder result = new StringBuilder();

        for (char c : text.toCharArray()) {
            if (metrics.stringWidth(result.toString() + c) > available) break;
            result.append(c);
        }

        return result + ellipsis;
    }

    private String formatPercent(double value, double total) {
        if (total <= 0) return "0%";
        double percent = value * 100.0 / total;
        if (percent < 0.1 && percent > 0) {
            return String.format(Locale.US, "%.1f%%", percent);
        }
        return String.format(Locale.US, "%.0f%%", percent);
    }

    // =========================== EMPTY ===========================
    protected void drawEmptyMessage(Graphics2D g, int width, int height, String msg) {
        g.setColor(DashboardTheme.TEXT_MUTED);
        g.setFont(g.getFont().deriveFont(Font.PLAIN, 14f));
        FontMetrics fm = g.getFontMetrics();
        String text = msg == null || msg.isBlank() ? "Données insuffisantes" : msg;
        int x = (width - fm.stringWidth(text)) / 2;
        int y = (height - fm.getHeight()) / 2 + fm.getAscent();
        g.drawString(text, x, y);
    }

    // =========================== INNER CLASSES ===========================
    private static class Padding {
        final int h;
        final int v;
        Padding(int h, int v) {
            this.h = h;
            this.v = v;
        }
    }

    private static class FontSizes {
        final float labelSize;
        final float legendSize;
        FontSizes(float label, float legend) {
            this.labelSize = label;
            this.legendSize = legend;
        }
    }
}
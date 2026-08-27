package org.labs.genesis.forms.renderer.chart;

import org.labs.genesis.forms.renderer.AbstractChartRenderer;
import org.labs.genesis.forms.theme.DashboardTheme;

import java.awt.*;

public class HorizontalBarChartRenderer extends AbstractChartRenderer {

    // ============================== DONNÉES ==============================
    private static final String[] CATEGORIES = {"Nord", "Sud", "Est", "Ouest"};
    private static final int[] VALUES = {140, 110, 95, 70};

    // ============================== CONFIGURATION ==============================
    private static final String DEFAULT_AXIS_LABEL = "";
    private static final String COMPACT_AXIS_LABEL = "";
    private static final int GRID_ALPHA = 90;
    private static final float SECONDARY_SATURATION_CUT = 0.22f;
    private static final float SECONDARY_BRIGHTNESS_BOOST = 0.18f;

    // ============================== PAINT ==============================
    @Override
    protected void paintChart(Graphics2D g2, int width, int height) {
        if (width <= 0 || height <= 0) return;

        Graphics2D g = (Graphics2D) g2.create();
        try {
            setupRendering(g);
            boolean tiny = width < 180 || height < 130;
            boolean compact = !tiny && (width < 280 || height < 190);
            boolean medium = !tiny && !compact && (width < 420 || height < 280);
            drawHorizontalBarChart(g, width, height, tiny, compact, medium);
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

    // ============================== MAIN CHART ==============================
    private void drawHorizontalBarChart(Graphics2D g, int width, int height,
                                        boolean tiny, boolean compact, boolean medium) {
        // Récupérer les légendes depuis la configuration
        String legendX = getConfigString("legendX", compact ? COMPACT_AXIS_LABEL : DEFAULT_AXIS_LABEL);
        String legendY = getConfigString("legendY", "");

        // Vérifier si les légendes doivent être affichées
        boolean showXLegend = !tiny && legendX != null && !legendX.trim().isEmpty();
        boolean showYLegend = !tiny && legendY != null && !legendY.trim().isEmpty();

        Padding padding = getPadding(tiny, compact, medium);
        FontSizes fonts = getFontSizes(tiny, compact, medium);

        Font categoryFont = g.getFont().deriveFont(Font.PLAIN, fonts.categorySize);
        Font numberFont = g.getFont().deriveFont(Font.PLAIN, fonts.numberSize);
        Font axisFont = g.getFont().deriveFont(Font.PLAIN, fonts.axisSize);

        // Zone des catégories (plus large si légende Y présente)
        FontMetrics categoryMetrics = g.getFontMetrics(categoryFont);
        int maxCategoryWidth = 0;
        if (!tiny) {
            for (String cat : CATEGORIES) {
                maxCategoryWidth = Math.max(maxCategoryWidth, categoryMetrics.stringWidth(cat));
            }
        }
        int categoryAreaWidth = tiny ? Math.max(26, width / 6) : Math.max(42, maxCategoryWidth + 12);

        // Espace additionnel pour la légende Y
        if (showYLegend) {
            FontMetrics axisMetrics = g.getFontMetrics(axisFont);
            categoryAreaWidth += axisMetrics.getHeight() + 8;
        }

        // Espace sous l'axe (plus grand si légende X présente)
        int bottomAxisSpace = tiny ? 6 : (compact ? 26 : 38);
        if (showXLegend) {
            bottomAxisSpace += (compact ? 14 : 20);
        }

        // Zone de dessin
        int chartX = padding.horizontal + categoryAreaWidth;
        int chartY = padding.vertical;
        int chartWidth = width - chartX - padding.horizontal;
        int chartHeight = height - chartY - bottomAxisSpace - padding.vertical;

        if (chartWidth <= 20 || chartHeight <= 20) {
            drawEmptyMessage(g, width, height, "");
            return;
        }

        int maxValue = calculateMaxValue();
        int upperBound = calculateUpperBound(maxValue, tiny, compact);
        int tickUnit = calculateTickUnit(tiny, compact);

        if (!tiny) {
            drawVerticalGrid(g, chartX, chartY, chartWidth, chartHeight, upperBound, tickUnit, numberFont);
        }

        drawBars(g, chartX, chartY, chartWidth, chartHeight, upperBound, categoryFont,
                tiny, compact, medium, padding.horizontal);

        drawXAxis(g, chartX, chartY, chartWidth, chartHeight, upperBound, tickUnit, numberFont, tiny);

        // Dessiner la légende X si présente
        if (showXLegend) {
            int labelY = chartY + chartHeight + (compact ? 30 : 40);
            drawAxisLabel(g, legendX, chartX, labelY, chartWidth, axisFont);
        }

        // Dessiner la légende Y si présente
        if (showYLegend) {
            drawYAxisLabel(g, legendY, padding.horizontal, chartY, chartHeight, axisFont);
        }
    }

    // ============================== BARS ==============================
    private void drawBars(Graphics2D g, int chartX, int chartY, int chartWidth, int chartHeight,
                          int upperBound, Font categoryFont, boolean tiny, boolean compact,
                          boolean medium, int horizontalPadding) {
        int count = VALUES.length;
        if (count == 0) return;

        int categoryGap = getCategoryGap(tiny, compact, medium);
        int totalGap = categoryGap * Math.max(0, count - 1);
        int availableHeight = chartHeight - totalGap;
        int barHeight = Math.max(4, availableHeight / count);
        int maxBarHeight = tiny ? 22 : (compact ? 32 : (medium ? 44 : 56));
        barHeight = Math.min(barHeight, maxBarHeight);

        int usedHeight = barHeight * count + totalGap;
        int startY = chartY + Math.max(0, (chartHeight - usedHeight) / 2);

        FontMetrics categoryMetrics = g.getFontMetrics(categoryFont);
        int leaderIndex = indexOfMax();

        for (int i = 0; i < count; i++) {
            int y = startY + i * (barHeight + categoryGap);

            // Catégorie
            if (!tiny) {
                String category = CATEGORIES[i];
                int textWidth = categoryMetrics.stringWidth(category);
                int textX = chartX - horizontalPadding - textWidth;
                int textY = y + (barHeight - categoryMetrics.getHeight()) / 2 + categoryMetrics.getAscent();
                g.setFont(i == leaderIndex ? categoryFont.deriveFont(Font.BOLD) : categoryFont);
                g.setColor(DashboardTheme.TEXT_DARK);
                g.drawString(category, textX, textY);
            }

            // Barre
            double ratio = VALUES[i] / (double) upperBound;
            int barWidth = (int) Math.round(chartWidth * ratio);
            if (VALUES[i] > 0) barWidth = Math.max(2, barWidth);

            if (barWidth > 0) {
                Color baseColor = i == leaderIndex
                        ? DashboardTheme.ACCENT
                        : tint(DashboardTheme.ACCENT, SECONDARY_SATURATION_CUT, SECONDARY_BRIGHTNESS_BOOST);
                Color lightColor = tint(baseColor, 0.05f, 0.10f);

                g.setPaint(new GradientPaint(chartX, y, lightColor, chartX + barWidth, y, baseColor));
                g.fillRect(chartX, y, barWidth, barHeight);
                g.setPaint(null);
            }
        }
    }

    // ============================== GRID & AXES ==============================
    private void drawVerticalGrid(Graphics2D g, int x, int y, int width, int height,
                                  int upperBound, int tickUnit, Font font) {
        Stroke oldStroke = g.getStroke();
        g.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1f, new float[]{3f, 3f}, 0f));
        g.setColor(withAlpha(DashboardTheme.BORDER, GRID_ALPHA));

        for (int value = 0; value <= upperBound; value += tickUnit) {
            double ratio = value / (double) upperBound;
            int gridX = x + (int) Math.round(width * ratio);
            g.drawLine(gridX, y, gridX, y + height);
        }
        g.setStroke(oldStroke);
    }

    private void drawXAxis(Graphics2D g, int x, int y, int width, int height,
                           int upperBound, int tickUnit, Font font, boolean tiny) {
        int axisY = y + height;
        g.setColor(DashboardTheme.BORDER);
        g.drawLine(x, axisY, x + width, axisY);

        if (tiny) return;

        g.setFont(font);
        FontMetrics metrics = g.getFontMetrics();
        for (int value = 0; value <= upperBound; value += tickUnit) {
            double ratio = value / (double) upperBound;
            int tickX = x + (int) Math.round(width * ratio);

            g.setColor(DashboardTheme.BORDER);
            g.drawLine(tickX, axisY, tickX, axisY + 4);

            String text = String.valueOf(value);
            int textX = tickX - metrics.stringWidth(text) / 2;
            int textY = axisY + metrics.getAscent() + 6;
            g.setColor(DashboardTheme.TEXT_MUTED);
            g.drawString(text, textX, textY);
        }
    }

    private void drawAxisLabel(Graphics2D g, String text, int x, int y, int width, Font font) {
        g.setFont(font);
        FontMetrics metrics = g.getFontMetrics();
        int textX = x + (width - metrics.stringWidth(text)) / 2;
        g.setColor(DashboardTheme.TEXT_MUTED);
        g.drawString(text, textX, y);
    }

    /**
     * Dessine la légende de l'axe Y (rotée à 90 degrés).
     */
    private void drawYAxisLabel(Graphics2D g, String text, int x, int y, int height, Font font) {
        Graphics2D rotated = (Graphics2D) g.create();
        try {
            rotated.setFont(font);
            rotated.setColor(DashboardTheme.TEXT_MUTED);
            FontMetrics fm = rotated.getFontMetrics();
            int centerY = y + height / 2;
            rotated.rotate(-Math.PI / 2);
            int textX = -centerY - fm.stringWidth(text) / 2;
            int textY = x + fm.getAscent();
            rotated.drawString(text, textX, textY);
        } finally {
            rotated.dispose();
        }
    }

    // ============================== UTILITIES ==============================
    private Color tint(Color base, float saturationCut, float brightnessBoost) {
        float[] hsb = Color.RGBtoHSB(base.getRed(), base.getGreen(), base.getBlue(), null);
        float saturation = Math.max(0f, hsb[1] - saturationCut);
        float brightness = Math.min(1f, hsb[2] + brightnessBoost);
        return Color.getHSBColor(hsb[0], saturation, brightness);
    }

    private Color withAlpha(Color base, int alpha) {
        return new Color(base.getRed(), base.getGreen(), base.getBlue(), alpha);
    }

    private int indexOfMax() {
        int leader = 0;
        for (int i = 1; i < VALUES.length; i++) {
            if (VALUES[i] > VALUES[leader]) leader = i;
        }
        return leader;
    }

    private int calculateMaxValue() {
        int max = 0;
        for (int v : VALUES) max = Math.max(max, v);
        return max;
    }

    private int calculateUpperBound(int maxValue, boolean tiny, boolean compact) {
        int base = (int) Math.ceil(maxValue / 20.0) * 20;
        return tiny ? base : base + 20;
    }

    private int calculateTickUnit(boolean tiny, boolean compact) {
        return (tiny || compact) ? 50 : 20;
    }

    // ============================== HELPER DATA CLASSES ==============================
    private Padding getPadding(boolean tiny, boolean compact, boolean medium) {
        if (tiny) return new Padding(4, 2);
        if (compact) return new Padding(8, 5);
        if (medium) return new Padding(12, 8);
        return new Padding(18, 12);
    }

    private FontSizes getFontSizes(boolean tiny, boolean compact, boolean medium) {
        if (tiny) return new FontSizes(8f, 7f, 0f);
        if (compact) return new FontSizes(9f, 8f, 9f);
        if (medium) return new FontSizes(10f, 9f, 10f);
        return new FontSizes(12f, 10f, 11f);
    }

    private int getCategoryGap(boolean tiny, boolean compact, boolean medium) {
        if (tiny) return 6;
        if (compact) return 9;
        if (medium) return 13;
        return 20;
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

    // ============================== INNER CLASSES ==============================
    private static class Padding {
        final int horizontal, vertical;
        Padding(int h, int v) { horizontal = h; vertical = v; }
    }

    private static class FontSizes {
        final float categorySize, numberSize, axisSize;
        FontSizes(float c, float n, float a) { categorySize = c; numberSize = n; axisSize = a; }
    }
}
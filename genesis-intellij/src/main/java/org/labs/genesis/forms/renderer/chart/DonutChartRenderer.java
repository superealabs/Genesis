package org.labs.genesis.forms.renderer.chart;

import org.labs.genesis.forms.renderer.AbstractChartRenderer;
import org.labs.genesis.forms.theme.DashboardTheme;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.util.Locale;

/**
 * Rendu d'un donut chart avec légende adaptable (droite ou bas).
 * Les données (labels, valeurs, couleurs) sont fixes pour l'exemple.
 */
public class DonutChartRenderer extends AbstractChartRenderer {

    // Données du graphique
    private static final String[] LABELS = { "Desktop", "Mobile", "Tablette" };
    private static final int[] VALUES = { 45, 35, 20 };
    private static final int TOTAL = 100;

    // Couleurs de base (le renderer génère automatiquement les dégradés)
    private static final Color[] BASE_COLORS = {
            DashboardTheme.ACCENT,
            new Color(34, 197, 94),
            new Color(245, 158, 11)
    };

    // Couleur des séparations (volontairement différente du thème)
    private static final Color GAP_COLOR = new Color(255, 255, 255, 225);

    // Disposition de la légende
    private enum LegendLayout { BOTTOM_ROW, RIGHT_COLUMN, HIDDEN }

    // ------------------------------------------------------------
    // Point d'entrée du rendu
    // ------------------------------------------------------------

    @Override
    protected void paintChart(Graphics2D g2, int width, int height) {
        if (width <= 0 || height <= 0) return;

        Graphics2D g = (Graphics2D) g2.create();
        try {
            applyRenderingHints(g);
            LegendLayout layout = chooseLegendLayout(width, height);
            drawChart(g, width, height, layout);
        } finally {
            g.dispose();
        }
    }

    // ------------------------------------------------------------
    // Configuration du rendu
    // ------------------------------------------------------------

    private void applyRenderingHints(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
    }

    // ------------------------------------------------------------
    // Choix de la disposition de la légende (OPTIMISÉ)
    // ------------------------------------------------------------

    private LegendLayout chooseLegendLayout(int w, int h) {
        // Seuil minimal pour afficher ne serait-ce qu'une légende
        if (w < 160 || h < 140) return LegendLayout.HIDDEN;

        // Si le graphique est significativement plus large que haut → légende à droite
        // Sinon, en bas (plus naturel sur les écrans carrés ou portrait)
        return (w > h * 1.4) ? LegendLayout.RIGHT_COLUMN : LegendLayout.BOTTOM_ROW;
    }

    // ------------------------------------------------------------
    // Dessin global du graphique + légende
    // ------------------------------------------------------------

    private void drawChart(Graphics2D g, int w, int h, LegendLayout layout) {
        int padding = computePadding(w, h);
        Rectangle chartArea = computeChartArea(w, h, padding, layout);

        drawDonut(g, chartArea);

        if (layout == LegendLayout.RIGHT_COLUMN) {
            int legendWidth = computeLegendWidth(w);
            int legendX = chartArea.x + chartArea.width + padding / 2;
            drawRightLegend(g, legendX, padding, legendWidth, h - padding * 2);
        } else if (layout == LegendLayout.BOTTOM_ROW) {
            int legendHeight = computeLegendHeight(h);
            int legendY = h - legendHeight;
            drawBottomLegend(g, padding, legendY, w - padding * 2, legendHeight);
        }
    }

    // ------------------------------------------------------------
    // Calcul des zones
    // ------------------------------------------------------------

    private int computePadding(int w, int h) {
        int minDim = Math.min(w, h);
        return Math.max(6, Math.min(18, minDim / 14));
    }

    private Rectangle computeChartArea(int w, int h, int padding, LegendLayout layout) {
        int left = padding, top = padding;
        int right = w - padding, bottom = h - padding;

        switch (layout) {
            case RIGHT_COLUMN:
                right -= computeLegendWidth(w);
                break;
            case BOTTOM_ROW:
                bottom -= computeLegendHeight(h);
                break;
            default: // HIDDEN
                break;
        }
        return new Rectangle(left, top, Math.max(1, right - left), Math.max(1, bottom - top));
    }

    private int computeLegendHeight(int h) {
        if (h < 170) return 28;
        if (h < 240) return 48;
        return 70;
    }

    private int computeLegendWidth(int w) {
        return Math.max(115, Math.min(190, (int) (w * 0.34)));
    }

    // ------------------------------------------------------------
    // Dessin du donut (cœur du graphique)
    // ------------------------------------------------------------

    private void drawDonut(Graphics2D g, Rectangle area) {
        int size = Math.min(area.width, area.height);
        int internalPadding = Math.max(5, size / 18);
        size -= internalPadding * 2;
        if (size <= 10) return;

        int x = area.x + (area.width - size) / 2;
        int y = area.y + (area.height - size) / 2;

        // Ombres
        drawDropShadow(g, x, y, size);

        // Calcul des angles une fois
        int[] angles = computeSliceAngles();

        // Tranchées
        int startAngle = 90;
        for (int i = 0; i < VALUES.length; i++) {
            int angle = angles[i];
            if (angle <= 0) continue;

            Paint gradient = createSliceGradient(BASE_COLORS[i], x, y, size);
            g.setPaint(gradient);
            g.fillArc(x, y, size, size, startAngle, -angle);

            drawSliceHighlight(g, x, y, size, startAngle, angle);

            startAngle -= angle;
        }

        // Séparations (gaps)
        drawSliceGaps(g, x, y, size, angles);

        // Bordure externe
        drawOuterRing(g, x, y, size);

        // Trou central
        double holeRatio = (size < 100) ? 0.58 : (size < 180 ? 0.55 : 0.52);
        int holeSize = (int) (size * holeRatio);
        int holeX = x + (size - holeSize) / 2;
        int holeY = y + (size - holeSize) / 2;
        drawCenterHole(g, holeX, holeY, holeSize);

        // Texte central
        drawCenterText(g, x, y, size);
    }

    private int[] computeSliceAngles() {
        int[] angles = new int[VALUES.length];
        for (int i = 0; i < VALUES.length; i++) {
            angles[i] = (int) Math.round(VALUES[i] * 360.0 / TOTAL);
        }
        return angles;
    }

    // ------------------------------------------------------------
    // Ombres portées
    // ------------------------------------------------------------

    private void drawDropShadow(Graphics2D g, int x, int y, int size) {
        g.setColor(new Color(15, 23, 42, 12));
        g.fillOval(x + 1, y + Math.max(5, size / 28), size, size);
        g.setColor(new Color(15, 23, 42, 20));
        g.fillOval(x + 1, y + Math.max(3, size / 40), size, size);
        g.setColor(new Color(15, 23, 42, 28));
        g.fillOval(x, y + 2, size, size);
    }

    // ------------------------------------------------------------
    // Dégradé pour une tranche
    // ------------------------------------------------------------

    private Paint createSliceGradient(Color base, int x, int y, int size) {
        Color highlight = tint(base, 0.32f);
        Color light    = tint(base, 0.12f);
        Color dark     = shade(base, 0.18f);

        return new LinearGradientPaint(
                new Point2D.Double(x + size * 0.18, y + size * 0.05),
                new Point2D.Double(x + size * 0.88, y + size * 0.92),
                new float[] { 0f, 0.22f, 0.58f, 1f },
                new Color[] { highlight, light, base, dark }
        );
    }

    // ------------------------------------------------------------
    // Surbrillance en haut de la tranche
    // ------------------------------------------------------------

    private void drawSliceHighlight(Graphics2D g, int x, int y, int size, int startAngle, int angle) {
        if (size < 80 || angle < 12) return;

        Stroke old = g.getStroke();
        try {
            float stroke = Math.max(1f, size * 0.012f);
            g.setStroke(new BasicStroke(stroke, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.setColor(new Color(255, 255, 255, 38));
            int highlightAngle = Math.max(1, (int) (angle * 0.62));
            g.drawArc(x + 1, y + 1, size - 2, size - 2, startAngle - 3, -highlightAngle);
        } finally {
            g.setStroke(old);
        }
    }

    // ------------------------------------------------------------
    // Séparations (gaps)
    // ------------------------------------------------------------

    private void drawSliceGaps(Graphics2D g, int x, int y, int size, int[] angles) {
        Stroke old = g.getStroke();
        try {
            float gapWidth = Math.max(1.5f, Math.min(4.5f, size * 0.018f));
            g.setStroke(new BasicStroke(gapWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.setColor(GAP_COLOR);

            int startAngle = 90;
            for (int angle : angles) {
                if (angle > 0) {
                    drawGapLine(g, x, y, size, startAngle);
                    startAngle -= angle;
                }
            }
        } finally {
            g.setStroke(old);
        }
    }

    private void drawGapLine(Graphics2D g, int x, int y, int size, double angleDeg) {
        double rad = Math.toRadians(angleDeg);
        double cx = x + size / 2.0;
        double cy = y + size / 2.0;
        double radius = size / 2.0;
        double innerRadius = radius * 0.52;

        double x1 = cx + Math.cos(rad) * radius;
        double y1 = cy - Math.sin(rad) * radius;
        double x2 = cx + Math.cos(rad) * innerRadius;
        double y2 = cy - Math.sin(rad) * innerRadius;

        g.drawLine((int) Math.round(x1), (int) Math.round(y1),
                (int) Math.round(x2), (int) Math.round(y2));
    }

    // ------------------------------------------------------------
    // Bordure externe
    // ------------------------------------------------------------

    private void drawOuterRing(Graphics2D g, int x, int y, int size) {
        Stroke old = g.getStroke();
        try {
            g.setStroke(new BasicStroke(Math.max(0.8f, size * 0.008f)));
            g.setColor(new Color(15, 23, 42, 28));
            g.drawOval(x, y, size - 1, size - 1);
        } finally {
            g.setStroke(old);
        }
    }

    // ------------------------------------------------------------
    // Trou central
    // ------------------------------------------------------------

    private void drawCenterHole(Graphics2D g, int x, int y, int size) {
        g.setColor(new Color(15, 23, 42, 22));
        g.fillOval(x + 1, y + 2, size, size);

        Paint gradient = new LinearGradientPaint(
                x, y, x + size, y + size,
                new float[] { 0f, 0.5f, 1f },
                new Color[] {
                        tint(DashboardTheme.ACCENT_LIGHT, 0.025f),
                        DashboardTheme.ACCENT_LIGHT,
                        shade(DashboardTheme.ACCENT_LIGHT, 0.025f)
                }
        );
        g.setPaint(gradient);
        g.fillOval(x, y, size, size);

        g.setColor(new Color(15, 23, 42, 18));
        g.setStroke(new BasicStroke(Math.max(1f, size * 0.008f)));
        g.drawOval(x, y, size - 1, size - 1);
    }

    // ------------------------------------------------------------
    // Texte central
    // ------------------------------------------------------------

    private void drawCenterText(Graphics2D g, int x, int y, int size) {
        if (size < 80) return;

        float mainSize = Math.max(13f, Math.min(28f, size * 0.16f));
        float subSize  = Math.max(8f, Math.min(12f, size * 0.065f));

        Font mainFont = g.getFont().deriveFont(Font.BOLD, mainSize);
        Font subFont  = g.getFont().deriveFont(Font.PLAIN, subSize);

        String mainText = "100%";
        String subText = "Total";

        FontMetrics mainMetrics = g.getFontMetrics(mainFont);
        FontMetrics subMetrics  = g.getFontMetrics(subFont);

        int cx = x + size / 2;
        int cy = y + size / 2;

        g.setFont(mainFont);
        int mainX = cx - mainMetrics.stringWidth(mainText) / 2;
        int mainY = cy - mainMetrics.getHeight() / 2 + mainMetrics.getAscent();

        g.setColor(new Color(255, 255, 255, 90));
        g.drawString(mainText, mainX, mainY + 1);
        g.setColor(DashboardTheme.TEXT_DARK);
        g.drawString(mainText, mainX, mainY);

        g.setFont(subFont);
        int subX = cx - subMetrics.stringWidth(subText) / 2;
        int subY = mainY + Math.max(12, (int) (subSize * 1.25));
        g.setColor(DashboardTheme.TEXT_MUTED);
        g.drawString(subText, subX, subY);
    }

    // ============================================================
    // LÉGENDES (NOUVELLE VERSION OPTIMISÉE)
    // ============================================================

    // ------------------------------------------------------------
    // Légende en bas (avec répartition dynamique des colonnes)
    // ------------------------------------------------------------

    private void drawBottomLegend(Graphics2D g, int x, int y, int width, int height) {
        if (height < 20 || width < 50) return;

        // Calcul de la taille de police en fonction de la hauteur dispo
        float fontSize = Math.max(9f, Math.min(13f, height * 0.36f));
        Font font = g.getFont().deriveFont(Font.PLAIN, fontSize);
        FontMetrics fm = g.getFontMetrics(font);

        // On estime la largeur nécessaire pour chaque élément (indicateur + label + pourcentage + marges)
        int indicatorSize = Math.max(9, Math.min(14, height / 3));
        int gapBetweenItems = 24;
        int maxItemWidth = 0;
        for (int i = 0; i < LABELS.length; i++) {
            String text = LABELS[i] + " " + VALUES[i] + "%";
            int w = fm.stringWidth(text) + indicatorSize + gapBetweenItems;
            maxItemWidth = Math.max(maxItemWidth, w);
        }

        // Calcul du nombre de colonnes possible
        int columns = Math.max(1, Math.min(LABELS.length, width / maxItemWidth));
        int rows = (int) Math.ceil(LABELS.length / (double) columns);

        // Ajustement de l'espacement horizontal pour centrer le bloc
        int totalItemsWidth = 0;
        for (int i = 0; i < columns && i < LABELS.length; i++) {
            // On prend la largeur max sur la colonne (simplifié)
        }
        // On utilise une largeur de colonne uniforme répartie sur toute la largeur
        int colWidth = width / columns;
        int rowHeight = Math.max(22, height / Math.max(1, rows));

        // Dessin de chaque élément
        for (int i = 0; i < LABELS.length; i++) {
            int col = i % columns;
            int row = i / columns;
            int itemX = x + col * colWidth;
            int itemY = y + row * rowHeight;
            // En bas : on centre le texte dans la colonne (pas d'alignement à droite)
            drawLegendItem(g, itemX, itemY, colWidth, rowHeight, i, false);
        }
    }

    // ------------------------------------------------------------
    // Légende à droite (avec pourcentage aligné à droite)
    // ------------------------------------------------------------

    private void drawRightLegend(Graphics2D g, int x, int y, int width, int height) {
        if (width < 90 || height < 30) return;

        int rowHeight = Math.max(30, Math.min(50, height / LABELS.length));
        int totalHeight = rowHeight * LABELS.length;
        int startY = y + Math.max(0, (height - totalHeight) / 2);

        for (int i = 0; i < LABELS.length; i++) {
            // En colonne de droite : le pourcentage est aligné à droite
            drawLegendItem(g, x, startY + i * rowHeight, width, rowHeight, i, true);
        }
    }

    // ------------------------------------------------------------
    // Élément de légende unifié (avec option d'alignement à droite)
    // ------------------------------------------------------------

    private void drawLegendItem(Graphics2D g, int x, int y, int width, int height, int index, boolean alignPercentRight) {
        int indicatorSize = Math.max(9, Math.min(13, height / 3));
        int cy = y + height / 2;

        // --- 1. Cercle indicateur (avec le même dégradé que la tranche) ---
        int indicatorX = x + 2;
        int indicatorY = cy - indicatorSize / 2;
        Paint gradient = createSliceGradient(BASE_COLORS[index], indicatorX, indicatorY, indicatorSize);
        g.setPaint(gradient);
        g.fill(new Ellipse2D.Double(indicatorX, indicatorY, indicatorSize, indicatorSize));

        // Mini-highlight
        g.setColor(new Color(255, 255, 255, 70));
        g.fillOval(indicatorX + 2, indicatorY + 1,
                Math.max(2, indicatorSize / 3), Math.max(2, indicatorSize / 4));

        // Bordure
        g.setColor(new Color(15, 23, 42, 25));
        g.setStroke(new BasicStroke(0.8f));
        g.draw(new Ellipse2D.Double(indicatorX, indicatorY, indicatorSize, indicatorSize));

        // --- 2. Textes ---
        float fontSize = Math.max(9f, Math.min(12f, height * 0.34f));
        Font labelFont = g.getFont().deriveFont(Font.PLAIN, fontSize);
        Font percentFont = g.getFont().deriveFont(Font.BOLD, fontSize);

        String label = LABELS[index];
        String percent = String.format(Locale.US, "%d%%", VALUES[index]);

        FontMetrics labelMetrics = g.getFontMetrics(labelFont);
        FontMetrics percentMetrics = g.getFontMetrics(percentFont);

        int textX = indicatorX + indicatorSize + 6;
        int baseline = cy - (labelMetrics.getAscent() + labelMetrics.getDescent()) / 2 + labelMetrics.getAscent();

        // Cas particulier : alignement à droite (pour la colonne de droite)
        if (alignPercentRight) {
            // On calcule la position du pourcentage à droite
            int percentX = x + width - percentMetrics.stringWidth(percent) - 4;
            int labelMaxWidth = percentX - textX - 4;

            // On vérifie si le label tient dans l'espace restant
            if (labelMetrics.stringWidth(label) > labelMaxWidth) {
                // Si le label est trop long, on le réduit avec "..."
                String fittedLabel = fitText(label, labelMetrics, Math.max(10, labelMaxWidth));
                g.setFont(labelFont);
                g.setColor(DashboardTheme.TEXT_DARK);
                g.drawString(fittedLabel, textX, baseline);
            } else {
                g.setFont(labelFont);
                g.setColor(DashboardTheme.TEXT_DARK);
                g.drawString(label, textX, baseline);
            }

            // Pourcentage aligné à droite
            g.setFont(percentFont);
            g.setColor(DashboardTheme.TEXT_MUTED);
            g.drawString(percent, percentX, baseline);
            return;
        }

        // --- Cas standard (légende du bas) : label + pourcentage collés ---
        int gap = 6;
        int percentX = textX + labelMetrics.stringWidth(label) + gap;

        // Si les deux ne tiennent pas sur une ligne, on les fusionne en une seule chaîne
        if (percentX + percentMetrics.stringWidth(percent) > x + width - 2) {
            String compact = label + " " + percent;
            g.setFont(labelFont);
            g.setColor(DashboardTheme.TEXT_DARK);
            int available = Math.max(0, width - (textX - x) - 2);
            String fitted = fitText(compact, g.getFontMetrics(), available);
            g.drawString(fitted, textX, baseline);
            return;
        }

        // Label
        g.setFont(labelFont);
        g.setColor(DashboardTheme.TEXT_DARK);
        g.drawString(label, textX, baseline);

        // Pourcentage
        g.setFont(percentFont);
        g.setColor(DashboardTheme.TEXT_MUTED);
        g.drawString(percent, percentX, baseline);
    }

    // ------------------------------------------------------------
    // Ajustement du texte avec ellipsis
    // ------------------------------------------------------------

    private String fitText(String text, FontMetrics metrics, int maxWidth) {
        if (metrics.stringWidth(text) <= maxWidth) return text;

        String ellipsis = "...";
        int available = maxWidth - metrics.stringWidth(ellipsis);
        if (available <= 0) return "";

        StringBuilder sb = new StringBuilder();
        for (char c : text.toCharArray()) {
            if (metrics.stringWidth(sb.toString() + c) > available) break;
            sb.append(c);
        }
        return sb + ellipsis;
    }

    // ------------------------------------------------------------
    // Utilitaires de couleurs
    // ------------------------------------------------------------

    private Color tint(Color c, float factor) {
        int r = (int) Math.min(255, c.getRed()   + (255 - c.getRed())   * factor);
        int g = (int) Math.min(255, c.getGreen() + (255 - c.getGreen()) * factor);
        int b = (int) Math.min(255, c.getBlue()  + (255 - c.getBlue())  * factor);
        return new Color(r, g, b, c.getAlpha());
    }

    private Color shade(Color c, float factor) {
        int r = (int) Math.max(0, c.getRed()   * (1 - factor));
        int g = (int) Math.max(0, c.getGreen() * (1 - factor));
        int b = (int) Math.max(0, c.getBlue()  * (1 - factor));
        return new Color(r, g, b, c.getAlpha());
    }
}
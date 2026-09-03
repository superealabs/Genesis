package org.labs.genesis.forms.renderer.metric;

import org.labs.genesis.forms.renderer.AbstractChartRenderer;
import org.labs.genesis.forms.renderer.provider.ChartData;
import org.labs.genesis.forms.theme.DashboardTheme;
import org.labs.genesis.forms.ui.visualization.model.VisualizationConfig;

import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.geom.Path2D;

/**
 * Renderer responsive pour une jauge semi-circulaire.
 *
 * Les données sont fournies par ChartData.
 *
 * La première valeur de ChartData est utilisée comme valeur
 * de la jauge et est supposée être comprise entre 0 et 100.
 */
public class GaugeRenderer extends AbstractChartRenderer {

    private static final Color COLOR_RED =
            Color.decode("#ef4444");

    private static final Color COLOR_ORANGE =
            Color.decode("#f59e0b");

    private static final Color COLOR_LIGHT_GREEN =
            Color.decode("#84cc16");

    private static final Color COLOR_GREEN =
            Color.decode("#22c55e");

    private static final Color COLOR_BACKGROUND =
            Color.decode("#e2e8f0");

    private static final Color COLOR_NEEDLE =
            Color.decode("#0f172a");

    private static final Color COLOR_PIVOT_OUTER =
            Color.decode("#0f172a");

    private static final Color COLOR_PIVOT_INNER =
            Color.WHITE;

    private static final Color COLOR_SUBTEXT =
            Color.decode("#94a3b8");

    private double value = 0.0;

    @Override
    protected void paintChart(
            Graphics2D g2,
            int width,
            int height
    ) {

        if (width <= 20 || height <= 20) {
            return;
        }

        g2.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );

        g2.setRenderingHint(
                RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY
        );

        // -----------------------------------------------------------------
        // VALUE
        // -----------------------------------------------------------------

        double currentValue = Math.max(
                0.0,
                Math.min(100.0, value)
        );

        // -----------------------------------------------------------------
        // Modes responsifs
        // -----------------------------------------------------------------

        boolean tiny =
                width < 180 || height < 120;

        boolean compact =
                width < 280 || height < 180;

        boolean medium =
                width < 430 || height < 270;

        // -----------------------------------------------------------------
        // Padding horizontal et zone réservée au texte
        // -----------------------------------------------------------------

        double hPad =
                tiny ? 7 :
                        compact ? 12 :
                                medium ? 18 :
                                        24;

        double textArea =
                tiny ? 28 :
                        compact ? 40 :
                                medium ? 52 :
                                        64;

        // -----------------------------------------------------------------
        // Rayon maximum
        // -----------------------------------------------------------------

        double radiusFromWidth =
                (width - hPad * 2) / 2.0;

        double radiusFromHeight =
                height - textArea - hPad;

        double radius =
                Math.min(
                        radiusFromWidth,
                        radiusFromHeight
                ) - (tiny ? 2 : 5);

        if (radius <= 8) {
            return;
        }

        // -----------------------------------------------------------------
        // Centre
        // -----------------------------------------------------------------

        double centerX =
                width / 2.0;

        double centerY =
                hPad + radius;

        double arcX =
                centerX - radius;

        double arcY =
                centerY - radius;

        double arcSize =
                radius * 2;

        // -----------------------------------------------------------------
        // Épaisseur du trait
        // -----------------------------------------------------------------

        float strokeWidth =
                (float) Math.min(
                        Math.max(
                                tiny ? 6f :
                                        compact ? 8f :
                                                medium ? 9f :
                                                        11f,

                                (float) (
                                        radius *
                                                (
                                                        tiny ? 0.15 :
                                                                compact ? 0.17 :
                                                                        medium ? 0.18 :
                                                                                0.19
                                                )
                                )
                        ),
                        radius * 0.22
                );

        g2.setStroke(
                new BasicStroke(
                        strokeWidth,
                        BasicStroke.CAP_ROUND,
                        BasicStroke.JOIN_ROUND
                )
        );

        // -----------------------------------------------------------------
        // Arc de fond
        // -----------------------------------------------------------------

        Arc2D.Double backgroundArc =
                new Arc2D.Double(
                        arcX,
                        arcY,
                        arcSize,
                        arcSize,
                        180,
                        -180,
                        Arc2D.OPEN
                );

        g2.setColor(COLOR_BACKGROUND);
        g2.draw(backgroundArc);

        // -----------------------------------------------------------------
        // Couleur de progression
        // -----------------------------------------------------------------

        Color progressColor;

        if (currentValue < 40) {
            progressColor = COLOR_RED;
        }
        else if (currentValue < 60) {
            progressColor = COLOR_ORANGE;
        }
        else if (currentValue < 80) {
            progressColor = COLOR_LIGHT_GREEN;
        }
        else {
            progressColor = COLOR_GREEN;
        }

        // -----------------------------------------------------------------
        // Arc de progression
        // -----------------------------------------------------------------

        double progressAngle =
                -(180.0 * currentValue / 100.0);

        Arc2D.Double progressArc =
                new Arc2D.Double(
                        arcX,
                        arcY,
                        arcSize,
                        arcSize,
                        180,
                        progressAngle,
                        Arc2D.OPEN
                );

        g2.setColor(progressColor);
        g2.draw(progressArc);

        // -----------------------------------------------------------------
        // Longueur de l'aiguille
        // -----------------------------------------------------------------

        double needleLength =
                Math.min(
                        radius *
                                (
                                        tiny ? 0.55 :
                                                compact ? 0.60 :
                                                        medium ? 0.64 :
                                                                0.67
                                ),
                        radius - strokeWidth - 4
                );

        double angleRad =
                Math.toRadians(
                        180.0 -
                                (currentValue / 100.0) * 180.0
                );

        double needleEndX =
                centerX +
                        Math.cos(angleRad) *
                                needleLength;

        double needleEndY =
                centerY -
                        Math.sin(angleRad) *
                                needleLength;

        // -----------------------------------------------------------------
        // Largeur de l'aiguille
        // -----------------------------------------------------------------

        double needleWidth =
                Math.max(
                        tiny ? 2 :
                                compact ? 2.5 :
                                        3,

                        radius *
                                (
                                        tiny ? 0.028 :
                                                compact ? 0.032 :
                                                        0.038
                                )
                );

        double perpX =
                -Math.sin(angleRad) *
                        needleWidth;

        double perpY =
                -Math.cos(angleRad) *
                        needleWidth;

        // -----------------------------------------------------------------
        // Dessin de l'aiguille
        // -----------------------------------------------------------------

        Path2D.Double needle =
                new Path2D.Double();

        needle.moveTo(
                centerX + perpX,
                centerY + perpY
        );

        needle.lineTo(
                needleEndX,
                needleEndY
        );

        needle.lineTo(
                centerX - perpX,
                centerY - perpY
        );

        needle.closePath();

        g2.setColor(COLOR_NEEDLE);
        g2.fill(needle);

        // -----------------------------------------------------------------
        // Pivot
        // -----------------------------------------------------------------

        int pivotOuter =
                (int) Math.min(
                        Math.max(
                                tiny ? 5 :
                                        compact ? 6 :
                                                7,

                                radius *
                                        (
                                                tiny ? 0.065 :
                                                        compact ? 0.075 :
                                                                0.08
                                        )
                        ),
                        radius * 0.10
                );

        int pivotInner =
                Math.max(
                        2,
                        pivotOuter / 2
                );

        g2.setColor(COLOR_PIVOT_OUTER);

        g2.fillOval(
                (int) centerX - pivotOuter,
                (int) centerY - pivotOuter,
                pivotOuter * 2,
                pivotOuter * 2
        );

        g2.setColor(COLOR_PIVOT_INNER);

        g2.fillOval(
                (int) centerX - pivotInner,
                (int) centerY - pivotInner,
                pivotInner * 2,
                pivotInner * 2
        );

        // -----------------------------------------------------------------
        // Texte
        // -----------------------------------------------------------------

        drawResponsiveText(
                g2,
                width,
                height,
                centerX,
                centerY,
                radius,
                strokeWidth,
                tiny,
                compact,
                medium,
                currentValue
        );
    }

    private void drawResponsiveText(
            Graphics2D g2,
            int width,
            int height,
            double cx,
            double cy,
            double radius,
            float strokeWidth,
            boolean tiny,
            boolean compact,
            boolean medium,
            double currentValue
    ) {

        double textZoneTop =
                cy + radius * 0.10;

        double availableHeight =
                (height - 3) - textZoneTop;

        if (availableHeight < 16) {
            return;
        }

        // -----------------------------------------------------------------
        // Taille de la valeur
        // -----------------------------------------------------------------

        float valueSize =
                (float) Math.min(
                        Math.max(
                                tiny ? 10 :
                                        compact ? 13 :
                                                16,

                                Math.min(
                                        radius * 0.28,
                                        availableHeight * 0.58
                                )
                        ),
                        30
                );

        String valueText =
                String.format(
                        "%.0f%%",
                        currentValue
                );

        Font valueFont =
                g2.getFont().deriveFont(
                        Font.BOLD,
                        valueSize
                );

        g2.setFont(valueFont);

        FontMetrics vm =
                g2.getFontMetrics();

        int vw =
                vm.stringWidth(valueText);

        int vh =
                vm.getHeight();

        double safeTop =
                cy + radius * 0.18;

        double availableForValue =
                (height - 3) - safeTop;

        double baseline =
                (availableForValue >= vh)
                        ? safeTop +
                        (availableForValue - vh) / 2.0 +
                        vm.getAscent()
                        : safeTop + vm.getAscent();

        baseline =
                Math.min(
                        baseline,
                        height - 2
                );

        baseline =
                Math.max(
                        baseline,
                        vm.getAscent() + 2
                );

        g2.setColor(
                DashboardTheme.TEXT_DARK
        );

        g2.drawString(
                valueText,
                (int) cx - vw / 2,
                (int) baseline
        );

        // -----------------------------------------------------------------
        // Sous-texte
        // -----------------------------------------------------------------

        if (tiny) {
            return;
        }

        String subText =
                "Objectif atteint";

        float subSize =
                compact ? 8f :
                        medium ? 9f :
                                10f;

        Font subFont =
                g2.getFont().deriveFont(
                        Font.BOLD,
                        subSize
                );

        g2.setFont(subFont);

        FontMetrics sm =
                g2.getFontMetrics();

        int sw =
                sm.stringWidth(subText);

        int subBaseline =
                (int) (
                        baseline +
                                vm.getDescent() +
                                4 +
                                sm.getAscent()
                );

        if (subBaseline + sm.getDescent() > height - 2) {
            return;
        }

        g2.setColor(COLOR_SUBTEXT);

        g2.drawString(
                subText,
                (int) cx - sw / 2,
                subBaseline
        );
    }

    @Override
    public void updateConfig(VisualizationConfig config) {
        this.config = config;

        Object dataObject = config.getValue(ChartData.CONFIG_KEY);

        if (dataObject instanceof ChartData) {
            ChartData data = (ChartData) dataObject;

            onChartDataChanged(data);
        }
    }

    // =========================================================================
    // CONFIGURATION / DATA
    // =========================================================================

    protected void onChartDataChanged(ChartData data) {

        if (data == null ||
                data.values() == null ||
                data.values().length == 0) {

            value = 0.0;
            return;
        }

        value = data.values()[0];

        // Une gauge est normalement bornée entre 0 et 100.
        value = Math.max(
                0.0,
                Math.min(100.0, value)
        );
    }
}
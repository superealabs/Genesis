package org.labs.genesis.forms.renderer;

import org.labs.genesis.forms.ui.visualization.model.VisualizationConfig;
import org.labs.genesis.forms.theme.DashboardTheme;
import org.labs.genesis.forms.renderer.provider.ChartData;

import javax.swing.*;
import java.awt.*;
import java.text.NumberFormat;

public abstract class AbstractChartRenderer implements VisualizationRenderer {

    protected VisualizationConfig config;

    @Override
    public JComponent createComponent() {
        return createComponent(null);
    }

    @Override
    public JComponent createComponent(VisualizationConfig config) {
        this.config = config;

        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                try {
                    g2.setRenderingHint(
                            RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON
                    );
                    paintChart(g2, getWidth(), getHeight());
                } finally {
                    g2.dispose();
                }
            }
        };

        panel.setOpaque(false);
        return panel;
    }

    @Override
    public void updateConfig(VisualizationConfig config) {
        this.config = config;
    }

    protected abstract void paintChart(Graphics2D g2, int width, int height);

    /**
     * Récupère la valeur d'un paramètre de configuration.
     */
    protected String getConfigString(String key, String defaultValue) {
        if (config != null) {
            return config.getString(key, defaultValue);
        }
        return defaultValue;
    }

    /**
     * Vérifie si un paramètre de configuration est présent et non vide.
     */
    protected boolean hasConfigValue(String key) {
        return config != null && config.isNotEmpty(key);
    }

    protected ChartData getChartData() {
        if (config == null) {
            return null;
        }
        Object value = config.getValue(ChartData.CONFIG_KEY);
        return value instanceof ChartData chartData ? chartData : null;
    }

    protected boolean isChartDataLoading() {
        return config != null && Boolean.TRUE.equals(config.getValue(ChartData.LOADING_KEY));
    }

    protected String getChartDataError() {
        if (config == null) {
            return null;
        }
        Object value = config.getValue(ChartData.ERROR_KEY);
        return value != null ? value.toString() : null;
    }

    protected void drawEmptyMessage(Graphics2D g2, int width, int height, String message) {
        g2.setColor(DashboardTheme.TEXT_SECONDARY);
        FontMetrics fm = g2.getFontMetrics();
        int x = (width - fm.stringWidth(message)) / 2;
        int y = height / 2;
        g2.drawString(message, x, y);
    }

    protected String formatConfiguredValue(double value) {
        String prefix = getConfigString("valuePrefix", "");
        String suffix = getConfigString("valueSuffix", "");
        int decimals = getConfigInteger("decimals", 0);
        double displayValue = value;
        String unit = "";

        double absoluteValue = Math.abs(value);

        if (absoluteValue >= 1_000_000_000) {
            displayValue = value / 1_000_000_000.0;
            unit = "B";
        } else if (absoluteValue >= 1_000_000) {
            displayValue = value / 1_000_000.0;
            unit = "M";
        } else if (absoluteValue >= 1_000) {
            displayValue = value / 1_000.0;
            unit = "K";
        }

        NumberFormat formatter = NumberFormat.getNumberInstance();

        int effectiveDecimals = displayValue == Math.rint(displayValue) ? 0 : decimals;

        formatter.setMinimumFractionDigits(effectiveDecimals);
        formatter.setMaximumFractionDigits(effectiveDecimals);

        return prefix + formatter.format(displayValue) + unit + suffix;
    }

    protected int getConfigInteger(String key, int defaultValue) {
        if (config == null) {
            return defaultValue;
        }
        Object value = config.getValue(key);

        if (value instanceof Number number) {
            return number.intValue();
        }

        if (value != null) {
            try {
                return Integer.parseInt(value.toString());
            } catch (NumberFormatException ignored) {
                // valeur par défaut
            }
        }

        return defaultValue;
    }
}

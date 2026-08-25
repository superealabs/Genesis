package org.labs.genesis.forms.visuals;

import org.labs.genesis.forms.data.VisualizationConfig;
import org.labs.genesis.forms.theme.DashboardTheme;

import javax.swing.*;
import java.awt.*;

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

    protected void drawEmptyMessage(Graphics2D g2, int width, int height, String message) {
        g2.setColor(DashboardTheme.TEXT_SECONDARY);
        FontMetrics fm = g2.getFontMetrics();
        int x = (width - fm.stringWidth(message)) / 2;
        int y = height / 2;
        g2.drawString(message, x, y);
    }
}
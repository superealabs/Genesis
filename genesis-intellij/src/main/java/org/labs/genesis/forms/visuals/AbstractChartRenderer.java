package org.labs.genesis.forms.visuals;

import org.labs.genesis.forms.theme.DashboardTheme;

import javax.swing.*;
import java.awt.*;

public abstract class AbstractChartRenderer implements VisualizationRenderer {

    @Override
    public JComponent createComponent() {

        JPanel panel = new JPanel() {

            @Override
            protected void paintComponent(Graphics g) {

                super.paintComponent(g);

                Graphics2D g2 =
                        (Graphics2D) g.create();

                try {

                    g2.setRenderingHint(
                            RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON
                    );

                    paintChart(
                            g2,
                            getWidth(),
                            getHeight()
                    );

                } finally {

                    g2.dispose();
                }
            }
        };

        panel.setOpaque(false);

        return panel;
    }

    protected abstract void paintChart(
            Graphics2D g2,
            int width,
            int height
    );

    protected void drawEmptyMessage(
            Graphics2D g2,
            int width,
            int height,
            String message
    ) {

        g2.setColor(
                DashboardTheme.TEXT_SECONDARY
        );

        FontMetrics fm =
                g2.getFontMetrics();

        int x =
                (width - fm.stringWidth(message)) / 2;

        int y =
                height / 2;

        g2.drawString(
                message,
                x,
                y
        );
    }
}
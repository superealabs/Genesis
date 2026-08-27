package org.labs.genesis.forms.ui.visualization;

import org.labs.genesis.forms.theme.DashboardTheme;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class ErrorVisualizationComponent extends JPanel {

    private static final int ARC = 12;

    private final String missingParameters;
    private final JLabel titleLabel;
    private final JLabel detailsLabel;

    private int currentMode = -1;

    private static final int MODE_TINY = 0;
    private static final int MODE_SMALL = 1;
    private static final int MODE_MEDIUM = 2;
    private static final int MODE_LARGE = 3;

    public ErrorVisualizationComponent(String missingParameters) {
        this.missingParameters = missingParameters;

        setOpaque(false);
        setLayout(new GridBagLayout());

        titleLabel = new JLabel("Missing configuration");
        titleLabel.setForeground(DashboardTheme.TEXT_SECONDARY);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

        detailsLabel = new JLabel();
        detailsLabel.setForeground(DashboardTheme.TEXT_SECONDARY);
        detailsLabel.setHorizontalAlignment(SwingConstants.CENTER);

        updateResponsiveLayout();
    }

    @Override
    public void doLayout() {
        updateResponsiveLayout();
        super.doLayout();
    }

    private void updateResponsiveLayout() {
        int width = getWidth();
        int height = getHeight();

        if (width <= 0 || height <= 0) {
            return;
        }

        int newMode;

        if (width < 70 || height < 45) {
            newMode = MODE_TINY;
        } else if (width < 160 || height < 90) {
            newMode = MODE_SMALL;
        } else if (width < 280 || height < 170) {
            newMode = MODE_MEDIUM;
        } else {
            newMode = MODE_LARGE;
        }

        if (newMode == currentMode) {
            return;
        }

        currentMode = newMode;
        removeAll();

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.CENTER;

        switch (currentMode) {
            case MODE_TINY:
                // Aucun composant Swing.
                // L'icône sera dessinée directement dans paintComponent().
                break;

            case MODE_SMALL:
                titleLabel.setText("⚠");
                titleLabel.setFont(DashboardTheme.boldFont(18));
                titleLabel.setForeground(new Color(230, 90, 90));

                gbc.gridy = 0;
                add(titleLabel, gbc);
                break;

            case MODE_MEDIUM:
                titleLabel.setText("⚠  Missing configuration");
                titleLabel.setFont(DashboardTheme.boldFont(11));
                titleLabel.setForeground(DashboardTheme.TEXT_SECONDARY);

                gbc.gridy = 0;
                add(titleLabel, gbc);
                break;

            case MODE_LARGE:
                JLabel iconLabel = new JLabel("⚠");
                iconLabel.setFont(DashboardTheme.boldFont(36));
                iconLabel.setForeground(DashboardTheme.ERROR);
                iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

                titleLabel.setText("Missing required configuration");
                titleLabel.setFont(DashboardTheme.boldFont(13));
                titleLabel.setForeground(DashboardTheme.TEXT_SECONDARY);

                detailsLabel.setFont(DashboardTheme.regularFont(11));
                detailsLabel.setText(createDetailsHtml(Math.max(100, width - 50)));

                JPanel content = new JPanel();
                content.setOpaque(false);
                content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

                iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                detailsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

                content.add(iconLabel);
                content.add(Box.createVerticalStrut(8));
                content.add(titleLabel);
                content.add(Box.createVerticalStrut(4));
                content.add(detailsLabel);

                gbc.gridy = 0;
                add(content, gbc);
                break;
        }

        revalidate();
        repaint();
    }

    private String createDetailsHtml(int width) {
        if (missingParameters == null || missingParameters.trim().isEmpty()) {
            return "<html><div style='width:" + width + "px;text-align:center;'>"
                    + "Configure the required parameters"
                    + "</div></html>";
        }

        return "<html><div style='width:" + width + "px;text-align:center;'>"
                + "Configure: <b>" + escapeHtml(missingParameters) + "</b>"
                + "</div></html>";
    }

    private String escapeHtml(String text) {
        return text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();

        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();

            if (width <= 2 || height <= 2) {
                return;
            }

            // Si le composant est extrêmement petit, on ne dessine même pas l'icône
            if (currentMode == MODE_TINY) {
                paintTinyState(g2, width, height);
                return;
            }

            int margin = (currentMode == MODE_SMALL) ? 3 : 6;
            int x = margin;
            int y = margin;
            int w = width - margin * 2;
            int h = height - margin * 2;

            if (w <= 2 || h <= 2) {
                return;
            }

            Shape shape = new RoundRectangle2D.Double(x, y, w - 1, h - 1, ARC, ARC);

            // Background
            g2.setColor(new Color(255, 100, 100, 18));
            g2.fill(shape);

            // Dashed border
            g2.setColor(new Color(230, 90, 90, 110));
            float dash = (currentMode == MODE_SMALL) ? 3f : 5f;
            g2.setStroke(new BasicStroke(
                    1f,
                    BasicStroke.CAP_ROUND,
                    BasicStroke.JOIN_ROUND,
                    0,
                    new float[]{dash, dash},
                    0
            ));
            g2.draw(shape);

        } finally {
            g2.dispose();
        }
    }

    /**
     * État pour les composants extrêmement petits.
     * On dessine uniquement un petit indicateur.
     * Aucun texte, aucune icône Unicode.
     */
    private void paintTinyState(Graphics2D g2, int width, int height) {
        if (width < 8 || height < 8) {
            return;
        }

        int size = Math.min(8, Math.min(width / 2, height / 2));
        int x = (width - size) / 2;
        int y = (height - size) / 2;

        g2.setColor(new Color(230, 90, 90, 150));
        g2.fillOval(x, y, size, size);
    }
}
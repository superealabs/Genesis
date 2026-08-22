package org.labs.genesis.forms.components;

import javax.swing.border.AbstractBorder;
import java.awt.*;

public class RoundedBorder extends AbstractBorder {

    private final Color color;
    private final int thickness;
    private final int radius;

    public RoundedBorder(
            Color color,
            int thickness,
            int radius
    ) {
        this.color = color;
        this.thickness = thickness;
        this.radius = radius;
    }

    @Override
    public void paintBorder(
            Component c,
            Graphics g,
            int x,
            int y,
            int width,
            int height
    ) {

        Graphics2D g2 = (Graphics2D) g.create();

        g2.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );

        g2.setColor(color);

        g2.setStroke(
                new BasicStroke(thickness)
        );

        int offset = thickness / 2;

        g2.drawRoundRect(
                x + offset,
                y + offset,
                width - thickness,
                height - thickness,
                radius,
                radius
        );

        g2.dispose();
    }

    @Override
    public Insets getBorderInsets(
            Component c
    ) {
        return new Insets(
                thickness + 5,
                thickness + 5,
                thickness + 5,
                thickness + 5
        );
    }
}

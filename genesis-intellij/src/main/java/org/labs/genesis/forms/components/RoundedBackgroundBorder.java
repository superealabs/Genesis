package org.labs.genesis.forms.components;

import javax.swing.border.AbstractBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class RoundedBackgroundBorder extends AbstractBorder {

    private final Color backgroundColor;
    private final Color borderColor;
    private final int radius;
    private final int thickness;
    private final boolean active;

    public RoundedBackgroundBorder(Color backgroundColor,
                                   Color borderColor,
                                   int radius,
                                   int thickness,
                                   boolean active) {
        this.backgroundColor = backgroundColor;
        this.borderColor = borderColor;
        this.radius = radius;
        this.thickness = thickness;
        this.active = active;
    }

    public boolean isActive() {
        return active;
    }

    @Override
    public Insets getBorderInsets(Component component) {
        return new Insets(thickness, thickness, thickness, thickness);
    }

    @Override
    public Insets getBorderInsets(Component component, Insets insets) {
        insets.top = thickness;
        insets.left = thickness;
        insets.bottom = thickness;
        insets.right = thickness;
        return insets;
    }

    @Override
    public void paintBorder(Component component,
                            Graphics graphics,
                            int x,
                            int y,
                            int width,
                            int height) {
        Graphics2D g = (Graphics2D) graphics.create();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            float inset = thickness / 2.0f;
            float w = width - thickness;
            float h = height - thickness;

            RoundRectangle2D rounded = new RoundRectangle2D.Double(
                    x + inset,
                    y + inset,
                    w - inset,
                    h - inset,
                    radius,
                    radius
            );

            g.setColor(backgroundColor);
            g.fill(rounded);

            g.setColor(borderColor);
            g.setStroke(new BasicStroke(thickness));
            g.draw(rounded);

        } finally {
            g.dispose();
        }
    }
}
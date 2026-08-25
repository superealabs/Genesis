package org.labs.genesis.forms.components;

import lombok.Getter;

import javax.swing.border.AbstractBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Bordure arrondie avec option de fond.
 */
@Getter
public class RoundedBorder extends AbstractBorder {

    private final Color backgroundColor;
    private final Color borderColor;
    private final int radius;
    private final int thickness;
    private final boolean fillBackground;

    public RoundedBorder(Color borderColor, int thickness, int radius) {
        this(null, borderColor, radius, thickness, false);
    }

    public RoundedBorder(Color backgroundColor, Color borderColor, int radius, int thickness, boolean fillBackground) {
        this.backgroundColor = backgroundColor;
        this.borderColor = borderColor;
        this.radius = radius;
        this.thickness = thickness;
        this.fillBackground = fillBackground;
    }

    @Override
    public Insets getBorderInsets(Component c) {
        return new Insets(thickness + 5, thickness + 5, thickness + 5, thickness + 5);
    }

    @Override
    public Insets getBorderInsets(Component c, Insets insets) {
        insets.top = insets.left = insets.bottom = insets.right = thickness + 5;
        return insets;
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            float inset = thickness / 2.0f;
            float w = width - thickness;
            float h = height - thickness;
            RoundRectangle2D rect = new RoundRectangle2D.Double(x + inset, y + inset, w - inset, h - inset, radius, radius);

            if (fillBackground && backgroundColor != null) {
                g2.setColor(backgroundColor);
                g2.fill(rect);
            }

            if (borderColor != null && thickness > 0) {
                g2.setColor(borderColor);
                g2.setStroke(new BasicStroke(thickness));
                g2.draw(rect);
            }
        } finally {
            g2.dispose();
        }
    }
}
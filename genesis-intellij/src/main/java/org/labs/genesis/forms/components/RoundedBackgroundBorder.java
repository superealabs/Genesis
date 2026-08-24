package org.labs.genesis.forms.components;

import lombok.Getter;

import javax.swing.border.AbstractBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

@Getter
public class RoundedBackgroundBorder extends AbstractBorder {

    private final Color backgroundColor;
    private final Color borderColor;
    private final int radius;
    private final int thickness;
    private final boolean active;

    public RoundedBackgroundBorder(Color backgroundColor, Color borderColor, int radius, int thickness, boolean active) {
        this.backgroundColor = backgroundColor;
        this.borderColor = borderColor;
        this.radius = radius;
        this.thickness = thickness;
        this.active = active;
    }

    @Override
    public Insets getBorderInsets(Component c) {
        return new Insets(thickness, thickness, thickness, thickness);
    }

    @Override
    public Insets getBorderInsets(Component c, Insets insets) {
        insets.top = insets.left = insets.bottom = insets.right = thickness;
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
            g2.setColor(backgroundColor);
            g2.fill(rect);
            g2.setColor(borderColor);
            g2.setStroke(new BasicStroke(thickness));
            g2.draw(rect);
        } finally {
            g2.dispose();
        }
    }
}
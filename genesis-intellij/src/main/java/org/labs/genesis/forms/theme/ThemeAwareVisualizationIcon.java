package org.labs.genesis.forms.theme;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public final class ThemeAwareVisualizationIcon implements Icon {
    private final BufferedImage source;
    private BufferedImage themed;
    private int foregroundRgb;

    public ThemeAwareVisualizationIcon(Image image, int width, int height) {
        source = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = source.createGraphics();
        try {
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            graphics.drawImage(image, 0, 0, width, height, null);
        } finally {
            graphics.dispose();
        }
    }

    @Override
    public void paintIcon(Component component, Graphics graphics, int x, int y) {
        int foreground = DashboardTheme.TEXT.getRGB() & 0xFFFFFF;
        if (themed == null || foreground != foregroundRgb) {
            themed = new BufferedImage(getIconWidth(), getIconHeight(), BufferedImage.TYPE_INT_ARGB);
            for (int row = 0; row < getIconHeight(); row++) {
                for (int col = 0; col < getIconWidth(); col++) {
                    int pixel = source.getRGB(col, row);
                    int red = (pixel >>> 16) & 255;
                    int green = (pixel >>> 8) & 255;
                    int blue = pixel & 255;
                    int min = Math.min(red, Math.min(green, blue));
                    int max = Math.max(red, Math.max(green, blue));
                    if (min >= 220 && max - min <= 20) {
                        pixel = (pixel & 0xFF000000) | foreground;
                    }
                    themed.setRGB(col, row, pixel);
                }
            }
            foregroundRgb = foreground;
        }
        graphics.drawImage(themed, x, y, null);
    }

    @Override
    public int getIconWidth() { return source.getWidth(); }

    @Override
    public int getIconHeight() { return source.getHeight(); }
}

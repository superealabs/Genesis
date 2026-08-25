package org.labs.genesis.forms.components;

import javax.swing.*;
import java.awt.*;

public class RotatableLabel extends JComponent {

    public enum Rotation { NONE, CLOCKWISE, COUNTER_CLOCKWISE }

    private String text = "";
    private Rotation rotation = Rotation.NONE;
    private Dimension cachedSize;
    private String cachedText;
    private Rotation cachedRotation;

    public RotatableLabel() {
        setOpaque(false);
        setFont(UIManager.getFont("Label.font").deriveFont(Font.BOLD, 11f));
        setForeground(UIManager.getColor("Label.foreground"));
    }

    public RotatableLabel(String text) {
        this();
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text == null ? "" : text;
        clearCache();
        revalidate();
        repaint();
    }

    public Rotation getRotation() {
        return rotation;
    }

    public void setRotation(Rotation rotation) {
        this.rotation = rotation == null ? Rotation.NONE : rotation;
        clearCache();
        revalidate();
        repaint();
    }

    public boolean isRotated() {
        return rotation != Rotation.NONE;
    }

    public void setRotated(boolean rotated) {
        setRotation(rotated ? Rotation.COUNTER_CLOCKWISE : Rotation.NONE);
    }

    private void clearCache() {
        cachedSize = null;
        cachedText = null;
        cachedRotation = null;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setFont(getFont());
            g2.setColor(getForeground());
            FontMetrics fm = g2.getFontMetrics();
            int tw = fm.stringWidth(text);
            int th = fm.getAscent();

            if (rotation == Rotation.NONE) {
                g2.drawString(text, 0, th);
                return;
            }

            g2.translate(getWidth() / 2.0, getHeight() / 2.0);
            double angle = rotation == Rotation.CLOCKWISE ? Math.PI / 2 : -Math.PI / 2;
            g2.rotate(angle);
            g2.drawString(text, -tw / 2, th / 2);
        } finally {
            g2.dispose();
        }
    }

    @Override
    public Dimension getPreferredSize() {
        // Mise en cache de la taille préférée
        if (cachedSize != null && text.equals(cachedText) && rotation == cachedRotation) {
            return cachedSize;
        }

        FontMetrics fm = getFontMetrics(getFont());
        int w = fm.stringWidth(text);
        int h = fm.getHeight();
        cachedSize = rotation == Rotation.NONE ? new Dimension(w, h) : new Dimension(h, w);
        cachedText = text;
        cachedRotation = rotation;
        return cachedSize;
    }
}
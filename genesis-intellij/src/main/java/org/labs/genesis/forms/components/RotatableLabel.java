package org.labs.genesis.forms.components;

import javax.swing.*;
import java.awt.*;

public class RotatableLabel extends JComponent {

    public enum Rotation {
        NONE,
        CLOCKWISE,
        COUNTER_CLOCKWISE
    }

    private String text = "";
    private Rotation rotation = Rotation.NONE;

    public RotatableLabel() {

        setOpaque(false);

        setFont(
                UIManager.getFont("Label.font")
                        .deriveFont(Font.BOLD, 11f)
        );

        setForeground(
                UIManager.getColor("Label.foreground")
        );
    }

    public RotatableLabel(String text) {
        this();

        this.text = text;
    }

    // =========================================================
    // TEXT
    // =========================================================

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text =
                text == null
                        ? ""
                        : text;

        revalidate();
        repaint();
    }

    // =========================================================
    // ROTATION
    // =========================================================

    public Rotation getRotation() {
        return rotation;
    }

    public void setRotation(Rotation rotation) {

        this.rotation =
                rotation == null
                        ? Rotation.NONE
                        : rotation;

        revalidate();
        repaint();
    }

    public boolean isRotated() {
        return rotation != Rotation.NONE;
    }

    /*
     * Compatibilité avec ton code actuel.
     */
    public void setRotated(boolean rotated) {

        setRotation(
                rotated
                        ? Rotation.COUNTER_CLOCKWISE
                        : Rotation.NONE
        );
    }

    // =========================================================
    // PAINT
    // =========================================================

    @Override
    protected void paintComponent(
            Graphics graphics
    ) {

        Graphics2D g =
                (Graphics2D) graphics.create();

        try {

            g.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            g.setRenderingHint(
                    RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON
            );

            g.setFont(
                    getFont()
            );

            g.setColor(
                    getForeground()
            );

            FontMetrics metrics =
                    g.getFontMetrics();

            int textWidth =
                    metrics.stringWidth(text);

            int textHeight =
                    metrics.getAscent();

            // -------------------------------------------------
            // NORMAL
            // -------------------------------------------------

            if (rotation == Rotation.NONE) {

                int x = 0;

                int y =
                        textHeight;

                g.drawString(
                        text,
                        x,
                        y
                );

                return;
            }

            // -------------------------------------------------
            // ROTATED
            // -------------------------------------------------

            g.translate(
                    getWidth() / 2.0,
                    getHeight() / 2.0
            );

            if (rotation ==
                    Rotation.CLOCKWISE) {

                g.rotate(
                        Math.PI / 2
                );

            } else {

                g.rotate(
                        -Math.PI / 2
                );
            }

            g.drawString(
                    text,
                    -textWidth / 2,
                    textHeight / 2
            );

        } finally {

            g.dispose();
        }
    }

    // =========================================================
    // SIZE
    // =========================================================

    @Override
    public Dimension getPreferredSize() {

        FontMetrics metrics =
                getFontMetrics(
                        getFont()
                );

        int width =
                metrics.stringWidth(text);

        int height =
                metrics.getHeight();

        if (rotation == Rotation.NONE) {

            return new Dimension(
                    width,
                    height
            );
        }

        return new Dimension(
                height,
                width
        );
    }
}
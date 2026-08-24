package org.labs.genesis.forms.theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

public final class DashboardTheme {

    // Couleurs principales
    public static final Color BACKGROUND = new Color(43, 43, 43);
    public static final Color SURFACE = new Color(30, 31, 34);
    public static final Color SURFACE_2 = new Color(37, 38, 42);
    public static final Color SURFACE_ACTIVE = new Color(58, 61, 68);
    public static final Color BORDER = new Color(70, 72, 78);
    public static final Color TEXT = new Color(220, 223, 228);
    public static final Color TEXT_DARK = new Color(48, 47, 47);
    public static final Color TEXT_DARK_SECONDARY = new Color(90, 90, 96);
    public static final Color TEXT_SECONDARY = new Color(150, 154, 162);
    public static final Color TEXT_DISABLED = new Color(100, 103, 110);
    public static final Color SURFACE_HOVER = new Color(43, 45, 50);
    public static final Color TAB_HOVER = new Color(48, 50, 56);
    public static final Color BORDER_SUBTLE = new Color(54, 56, 62);
    // Couleurs Canvas (pour les zones de dessin)
    public static final Color CANVAS_BG = new Color(245, 246, 248);
    public static final Color CANVAS_BORDER = new Color(171, 171, 175);
    public static final Color GRID_COLOR = new Color(225, 228, 232);
    public static final Color TEXT_MUTED = new Color(160, 160, 160);

    // Couleurs d'accentuation
    public static final Color ACCENT = new Color(66, 133, 244);
    public static final Color ACCENT_HOVER = new Color(52, 115, 220);
    public static final Color ACCENT_LIGHT = new Color(232, 240, 254);
    public static final Color SUCCESS = new Color(52, 168, 83);
    public static final Color WARNING = new Color(251, 188, 5);
    public static final Color ERROR = new Color(234, 67, 53);

    // Dimensions
    public static final int EXPANDED_WIDTH = 210;
    public static final int EXPANDED_WIDTH_EXTEND = 250;
    public static final int COLLAPSED_WIDTH = 48;
    public static final int MIN_WIDTH = 160;
    public static final int MAX_WIDTH = 300;
    public static final int SIDEBAR_RADIUS = 12;
    public static final int TOP_BAR_HEIGHT = 32;
    public static final int COLLAPSE_BTN_SIZE = 24;
    public static final int PADDING = 16;
    public static final int COMPONENT_SPACING = 12;
    public static final int COLLAPSE_BUTTON_SIZE = 24;
    public static final int TAB_RADIUS = 8;

    // Polices
    public static Font getFont(float size) {
        return new Font("Segoe UI", Font.PLAIN, (int) size);
    }

    public static Font boldFont(float size) {
        return new Font("Segoe UI", Font.BOLD, (int) size);
    }

    public static Font lightFont(float size) {
        return new Font("Segoe UI Light", Font.PLAIN, (int) size);
    }

    // Méthodes utilitaires pour appliquer le thème à un composant
    public static void styleTitle(JLabel label) {
        label.setForeground(TEXT);
        label.setFont(boldFont(18));
        label.setBorder(new EmptyBorder(5, 0, 5, 0));
    }

    public static Font regularFont(int size) {
        return new Font(Font.SANS_SERIF, Font.PLAIN, size);
    }
}
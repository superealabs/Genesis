package org.labs.genesis.forms.theme;

import com.intellij.ui.JBColor;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public final class DashboardTheme {

    // Resolve colors through the active IntelliJ theme, including after a theme switch.
    public static final Color BACKGROUND = JBColor.namedColor("Panel.background", 0xF7F8FA, 0x2B2D30);
    public static final Color SURFACE = JBColor.namedColor("ToolWindow.background", 0xFFFFFF, 0x1E1F22);
    public static final Color SURFACE_2 = JBColor.namedColor("Popup.background", 0xF2F3F5, 0x25262A);
    public static final Color SURFACE_ACTIVE = JBColor.namedColor("List.selectionBackground", 0xDCEBFF, 0x3A3D44);
    public static final Color BORDER = JBColor.namedColor("Component.borderColor", 0xCDD0D6, 0x46484E);
    public static final Color TEXT = JBColor.namedColor("Label.foreground", 0x303030, 0xDCDFE4);
    // Legacy names retained for renderers; preview text follows the active theme too.
    public static final Color TEXT_DARK = TEXT;
    public static final Color TEXT_DARK_SECONDARY = JBColor.namedColor("Label.infoForeground", 0x5A5A60, 0xADB0B8);
    public static final Color TEXT_SECONDARY = TEXT_DARK_SECONDARY;
    public static final Color TEXT_DISABLED = JBColor.namedColor("Label.disabledForeground", 0x8C8F94, 0x64676E);
    public static final Color SURFACE_HOVER = JBColor.namedColor("List.hoverBackground", 0xE9EDF3, 0x2B2D32);
    public static final Color TAB_HOVER = SURFACE_HOVER;
    public static final Color BORDER_SUBTLE = JBColor.namedColor("Separator.foreground", 0xDEE1E6, 0x36383E);

    public static final Color CANVAS_BG = JBColor.namedColor("EditorPane.background", 0xFFFFFF, 0x1E1F22);
    public static final Color CANVAS_BORDER = BORDER;
    public static final Color GRID_COLOR = JBColor.namedColor("Component.borderColor", 0xE1E4E8, 0x36383E);
    public static final Color TEXT_MUTED = TEXT_DISABLED;

    public static final Color ACCENT = JBColor.namedColor("Component.focusColor", 0x4285F4, 0x6B9BFA);
    public static final Color ACCENT_HOVER = JBColor.namedColor("Link.activeForeground", 0x3473DC, 0x8AB4FF);
    public static final Color ACCENT_LIGHT = JBColor.namedColor("TableHeader.background", 0xE8F0FE, 0x2D3442);
    public static final Color SUCCESS = JBColor.namedColor("Label.successForeground", 0x248A3D, 0x73BD79);
    public static final Color WARNING = JBColor.namedColor("Label.warningForeground", 0xA66B00, 0xEBC158);
    public static final Color ERROR = JBColor.namedColor("Label.errorForeground", 0xC93C3C, 0xF08080);

    // Dimensions
    public static final int EXPANDED_WIDTH = 280;
    public static final int EXPANDED_WIDTH_EXTEND = 260;
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

    public static Font regularFont(int size) {
        return new Font(Font.SANS_SERIF, Font.PLAIN, size);
    }

    // Méthodes utilitaires pour appliquer le thème à un composant
    public static void styleTitle(JLabel label) {
        label.setForeground(TEXT);
        label.setFont(boldFont(18));
        label.setBorder(new EmptyBorder(5, 0, 5, 0));
    }

    // Nouvelle méthode utilitaire pour les couleurs avec alpha
    public static Color withAlpha(Color color, int alpha) {
        return JBColor.lazy(() -> new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha));
    }
}
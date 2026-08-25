package org.labs.genesis.forms.components;

import java.awt.*;

/**
 * Utilitaire centralisé pour la gestion des curseurs de redimensionnement.
 */
public final class CursorUtils {

    private CursorUtils() {
        // Classe utilitaire
    }

    /**
     * Retourne le type de curseur correspondant à une direction de redimensionnement.
     */
    public static int getCursorTypeForDirection(DashboardVisualComponent.ResizeDirection direction) {
        return switch (direction) {
            case NORTH, SOUTH -> Cursor.N_RESIZE_CURSOR;
            case EAST, WEST -> Cursor.E_RESIZE_CURSOR;
            case NORTH_EAST, SOUTH_WEST -> Cursor.NE_RESIZE_CURSOR;
            case NORTH_WEST, SOUTH_EAST -> Cursor.NW_RESIZE_CURSOR;
            default -> Cursor.DEFAULT_CURSOR;
        };
    }

    /**
     * Retourne le curseur correspondant à une direction de redimensionnement.
     */
    public static Cursor getCursorForDirection(DashboardVisualComponent.ResizeDirection direction) {
        return Cursor.getPredefinedCursor(getCursorTypeForDirection(direction));
    }
}
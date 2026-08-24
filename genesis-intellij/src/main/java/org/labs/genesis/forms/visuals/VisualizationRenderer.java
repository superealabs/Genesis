package org.labs.genesis.forms.visuals;

import javax.swing.*;

/**
 * Contrat commun pour tous les visuels du Dashboard Builder.
 *
 * Chaque renderer retourne un composant Swing responsive.
 * Le composant doit pouvoir être redimensionné librement.
 */
public interface VisualizationRenderer {

    /**
     * Crée le composant principal du visuel.
     */
    JComponent createComponent();

    /**
     * Nom optionnel utilisé comme fallback.
     */
    default String getDisplayName() {
        return getClass().getSimpleName();
    }
}
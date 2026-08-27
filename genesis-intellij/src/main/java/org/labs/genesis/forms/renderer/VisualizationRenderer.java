package org.labs.genesis.forms.renderer;

import org.labs.genesis.forms.ui.visualization.model.VisualizationConfig;

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
     * Crée le composant principal du visuel avec une configuration.
     */
    default JComponent createComponent(VisualizationConfig config) {
        return createComponent();
    }

    /**
     * Met à jour la configuration du renderer.
     */
    default void updateConfig(VisualizationConfig config) {
        // Par défaut, ne fait rien
    }

    /**
     * Nom optionnel utilisé comme fallback.
     */
    default String getDisplayName() {
        return getClass().getSimpleName();
    }
}
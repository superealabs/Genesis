package org.labs.genesis.forms.ui.visualization.configuration.listener;

import org.labs.genesis.forms.ui.visualization.model.VisualizationItem;

/**
 * Interface pour écouter les sélections de visualisation.
 */
@FunctionalInterface
public interface VisualizationSelectionListener {
    void onVisualizationSelected(VisualizationItem item);
}
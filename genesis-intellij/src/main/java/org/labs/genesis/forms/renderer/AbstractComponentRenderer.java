package org.labs.genesis.forms.renderer;

import org.labs.genesis.forms.ui.visualization.model.VisualizationConfig;

import javax.swing.*;
import java.awt.*;

public abstract class AbstractComponentRenderer
        implements VisualizationRenderer {

    protected VisualizationConfig config;

    @Override
    public JComponent createComponent() {
        return createComponent(null);
    }

    @Override
    public JComponent createComponent(VisualizationConfig config) {
        this.config = config;

        JComponent component =
                createSwingComponent();

        component.setMinimumSize(
                new Dimension(0, 0)
        );

        return component;
    }

    protected abstract JComponent createSwingComponent();
}
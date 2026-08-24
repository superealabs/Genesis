package org.labs.genesis.forms.visuals;

import javax.swing.*;
import java.awt.*;

public abstract class AbstractComponentRenderer
        implements VisualizationRenderer {

    @Override
    public JComponent createComponent() {

        JComponent component =
                createSwingComponent();

        component.setMinimumSize(
                new Dimension(0, 0)
        );

        return component;
    }

    protected abstract JComponent createSwingComponent();
}
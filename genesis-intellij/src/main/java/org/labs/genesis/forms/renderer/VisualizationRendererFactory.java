package org.labs.genesis.forms.renderer;

import org.labs.genesis.forms.ui.visualization.model.VisualizationConfig;

public final class VisualizationRendererFactory {

    private VisualizationRendererFactory() {
    }

    public static VisualizationRenderer create(
            Class<? extends VisualizationRenderer> rendererClass
    ) {
        return create(rendererClass, null);
    }

    public static VisualizationRenderer create(
            Class<? extends VisualizationRenderer> rendererClass,
            VisualizationConfig config
    ) {
        try {
            VisualizationRenderer renderer = rendererClass
                    .getDeclaredConstructor()
                    .newInstance();

            if (config != null) {
                renderer.updateConfig(config);
            }

            return renderer;
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Unable to create visualization renderer: "
                            + rendererClass.getName(),
                    e
            );
        }
    }
}
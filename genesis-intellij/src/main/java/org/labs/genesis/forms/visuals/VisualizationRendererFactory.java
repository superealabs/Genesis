package org.labs.genesis.forms.visuals;

public final class VisualizationRendererFactory {

    private VisualizationRendererFactory() {
    }

    public static VisualizationRenderer create(
            Class<? extends VisualizationRenderer> rendererClass
    ) {

        try {
            return rendererClass
                    .getDeclaredConstructor()
                    .newInstance();

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Unable to create visualization renderer: "
                            + rendererClass.getName(),
                    e
            );
        }
    }
}
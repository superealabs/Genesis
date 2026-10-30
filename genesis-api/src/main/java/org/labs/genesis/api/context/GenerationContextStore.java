package org.labs.genesis.api.context;

import lombok.Setter;
import org.labs.genesis.config.ProjectGenerationContext;
import org.springframework.stereotype.Component;

@Component
public class GenerationContextStore {

    private volatile ProjectGenerationContext context = new ProjectGenerationContext();

    @Setter
    private GenerationMode generationMode;

    public ProjectGenerationContext getContext() {
        return context;
    }

    public GenerationMode getGenerationMode() {
        return generationMode;
    }

    public synchronized ProjectGenerationContext reset() {
        ProjectGenerationContext previous = context;
        context = new ProjectGenerationContext();
        generationMode = null;
        return previous;
    }
}
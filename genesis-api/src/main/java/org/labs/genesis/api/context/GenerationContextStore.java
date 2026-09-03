package org.labs.genesis.api.context;

import lombok.Setter;
import org.labs.genesis.config.ProjectGenerationContext;
import org.springframework.stereotype.Component;

@Component
public class GenerationContextStore {

    private final ProjectGenerationContext context = new ProjectGenerationContext();
    @Setter
    private GenerationMode generationMode;

    public ProjectGenerationContext getContext() {
        return context;
    }

    public GenerationMode getGenerationMode() {
        return generationMode;
    }

}
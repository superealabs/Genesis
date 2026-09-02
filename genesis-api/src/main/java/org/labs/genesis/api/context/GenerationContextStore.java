package org.labs.genesis.api.context;

import org.labs.genesis.config.ProjectGenerationContext;
import org.springframework.stereotype.Component;

@Component
public class GenerationContextStore {

    private final ProjectGenerationContext context = new ProjectGenerationContext();

    public ProjectGenerationContext getContext() {
        return context;
    }
}
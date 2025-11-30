package org.labs.genesis.context;

import lombok.Getter;
import lombok.Setter;
import org.labs.genesis.config.ProjectGenerationContext;
import org.labs.genesis.enums.GenerationProcessChoice;

@Getter
@Setter
public class GenerationContextManager {
    public ProjectGenerationContext context;
    public GenerationProcessChoice generationMode;

    public GenerationContextManager(ProjectGenerationContext context) {
        setContext(context);
    }

    public void setGenerateNewProjectMode(){
        setGenerationMode(GenerationProcessChoice.GENERATE_NEW_PROJECT);
    }

    public void setRuleToCodeProcess(){
        setGenerationMode(GenerationProcessChoice.RULE_TO_CODE_GENERATION);
    }

    public void setSyncProjectProcess(){
        setGenerationMode(GenerationProcessChoice.SYNCHRONISATION);
    }
}

package org.labs.genesis.config.langage.generator.indicator;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IntellijGenerationProcess {
    private boolean generateProjectProcess=true;
    private boolean synchGenerationProcess=false;
    private boolean runToCodeGenerationProcess=false;
}

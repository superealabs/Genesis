package org.labs.genesis.config.langage.generator.sync;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EvaluationParameters {
    private boolean evaluateViews = true;
    private boolean evaluateTables = true;
    private boolean evaluateAdd = true;
    private boolean evaluateRemove = true;
    private boolean evaluateModify = true;
}

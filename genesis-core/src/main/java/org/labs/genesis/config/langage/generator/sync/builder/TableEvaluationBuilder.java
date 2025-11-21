package org.labs.genesis.config.langage.generator.sync.builder;

import org.labs.genesis.config.langage.generator.sync.EvaluationParameters;
import org.labs.genesis.config.langage.generator.sync.evaluators.AddedTableEvaluation;
import org.labs.genesis.config.langage.generator.sync.evaluators.RemovedTableEvaluation;

public class TableEvaluationBuilder {
    public static AddedTableEvaluation buildAddedTableEvaluation(EvaluationParameters evaluationParameters) {
        if (!evaluationParameters.isEvaluateAdd()){
            return  null;
        }
        return  new AddedTableEvaluation();
    }

    public static RemovedTableEvaluation buildRemovedTableEvaluation(EvaluationParameters evaluationParameters) {
        if (!evaluationParameters.isEvaluateRemove()){
            return  null;
        }
        return  new RemovedTableEvaluation();
    }
}

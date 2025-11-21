package org.labs.genesis.config.langage.generator.sync.builder;

import org.labs.genesis.config.langage.generator.sync.EvaluationParameters;
import org.labs.genesis.config.langage.generator.sync.evaluators.DatabaseEvaluatorsManager;
import org.labs.genesis.config.langage.generator.sync.evaluators.IDatabaseEvaluator;

public class DatabaseEvaluatorManagerBuilder {
    public static DatabaseEvaluatorsManager build(EvaluationParameters evaluationParameters) {
        IDatabaseEvaluator[] evaluators = new IDatabaseEvaluator[]{
                TableEvaluationBuilder.buildAddedTableEvaluation(evaluationParameters),
                TableEvaluationBuilder.buildRemovedTableEvaluation(evaluationParameters),
        };
        return new DatabaseEvaluatorsManager(evaluators);
    }
}

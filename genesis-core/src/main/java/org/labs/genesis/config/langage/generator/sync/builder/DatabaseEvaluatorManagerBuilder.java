package org.labs.genesis.config.langage.generator.sync.builder;

import org.labs.genesis.config.langage.generator.sync.evaluators.DatabaseEvaluatorsManager;
import org.labs.genesis.config.langage.generator.sync.evaluators.IDatabaseEvaluator;

public class DatabaseEvaluatorManagerBuilder {
    public static DatabaseEvaluatorsManager build() {
        IDatabaseEvaluator[] evaluators = new IDatabaseEvaluator[]{
                TableEvaluationBuilder.buildAddedTableEvaluation(),
                TableEvaluationBuilder.buildRemovedTableEvaluation(),
                TableEvaluationBuilder.buildColumnModificationEvaluation()
        };
        return new DatabaseEvaluatorsManager(evaluators);
    }
}

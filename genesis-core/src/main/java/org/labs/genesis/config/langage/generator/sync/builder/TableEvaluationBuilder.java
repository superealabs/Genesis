package org.labs.genesis.config.langage.generator.sync.builder;

import org.labs.genesis.config.langage.generator.sync.evaluators.AddedTableEvaluation;
import org.labs.genesis.config.langage.generator.sync.evaluators.ColumnModificationEvaluation;
import org.labs.genesis.config.langage.generator.sync.evaluators.RemovedTableEvaluation;

public class TableEvaluationBuilder {
    public static AddedTableEvaluation buildAddedTableEvaluation() {
        return  new AddedTableEvaluation();
    }

    public static RemovedTableEvaluation buildRemovedTableEvaluation() {
        return  new RemovedTableEvaluation();
    }

    public static ColumnModificationEvaluation buildColumnModificationEvaluation() {
        return  new ColumnModificationEvaluation();
    }
}

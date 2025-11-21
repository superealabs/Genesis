package org.labs.genesis.config.langage.generator.sync.evaluators;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.labs.genesis.config.langage.generator.sync.report.DatabaseReportManager;
import org.labs.genesis.connexion.model.TableMetadata;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class DatabaseEvaluatorsManager {
    private IDatabaseEvaluator[] evaluators;
    public void evaluate(List<TableMetadata> initialTables, List<TableMetadata> targetTables, DatabaseReportManager report) {
        for (IDatabaseEvaluator evaluator : evaluators) {
            if (evaluator == null) {
                continue;
            }
            evaluator.evaluate(initialTables, targetTables, report);
        }
    }
}

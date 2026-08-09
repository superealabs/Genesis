package org.labs.genesis.config.langage.generator.sync.evaluators;

import org.labs.genesis.config.langage.generator.sync.report.DatabaseReportManager;
import org.labs.genesis.connexion.model.TableMetadata;

import java.util.List;

public interface IDatabaseEvaluator {
    public void evaluate(List<TableMetadata> initialTables, List<TableMetadata> targetTables, DatabaseReportManager report);
}

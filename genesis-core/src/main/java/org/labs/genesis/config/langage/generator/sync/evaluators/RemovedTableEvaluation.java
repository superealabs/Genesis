package org.labs.genesis.config.langage.generator.sync.evaluators;

import org.labs.genesis.config.langage.generator.sync.report.TableChangeReport;
import org.labs.genesis.config.langage.generator.sync.report.DatabaseReportManager;
import org.labs.genesis.connexion.model.TableMetadata;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class RemovedTableEvaluation implements IDatabaseEvaluator {
    @Override
    public void evaluate(List<TableMetadata> initialTables, List<TableMetadata> targetTables, DatabaseReportManager report) {
        Set<String> initialTableNames = targetTables.stream()
                .map(TableMetadata::getTableName)
                .collect(Collectors.toSet());
        List<TableMetadata> removedTables = initialTables.stream()
                .filter(table -> !initialTableNames.contains(table.getTableName()))
                .toList();
        for ( TableMetadata tableMetadata :removedTables){
            TableChangeReport tableReport = report.getTableReport(tableMetadata.getTableName());
            tableReport.onRemoveTable(tableMetadata);
            report.addTableReport(tableReport);
        }
    }
}

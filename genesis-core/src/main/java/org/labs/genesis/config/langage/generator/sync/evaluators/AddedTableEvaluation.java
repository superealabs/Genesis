package org.labs.genesis.config.langage.generator.sync.evaluators;

import org.labs.genesis.config.langage.generator.sync.report.TableChangeReport;
import org.labs.genesis.config.langage.generator.sync.report.DatabaseReportManager;
import org.labs.genesis.connexion.model.TableMetadata;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class AddedTableEvaluation implements IDatabaseEvaluator {
    @Override
    public void evaluate(List<TableMetadata> initialTables, List<TableMetadata> targetTables, DatabaseReportManager report) {
        Set<String> initialTableNames = initialTables.stream()
                .map(TableMetadata::getTableName)
                .collect(Collectors.toSet());
        List<TableMetadata> toAddTables = targetTables.stream()
                .filter(table -> !initialTableNames.contains(table.getTableName()))
                .toList();
        if (!toAddTables.isEmpty()) {
            for (TableMetadata tableMetadata : toAddTables){
                TableChangeReport tableReport = report.getTableReport(tableMetadata.getTableName());
                tableReport.onAddTable(tableMetadata);
                report.addTableReport(tableReport);
            }
        }
    }
}

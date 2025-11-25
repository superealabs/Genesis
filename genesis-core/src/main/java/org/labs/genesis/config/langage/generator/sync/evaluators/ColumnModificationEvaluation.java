package org.labs.genesis.config.langage.generator.sync.evaluators;

import org.labs.genesis.config.langage.generator.sync.report.DatabaseReportManager;
import org.labs.genesis.config.langage.generator.sync.report.TableChangeReport;
import org.labs.genesis.connexion.model.ColumnMetadata;
import org.labs.genesis.connexion.model.TableMetadata;

import java.util.*;
import java.util.stream.Collectors;

public class ColumnModificationEvaluation implements IDatabaseEvaluator {
    @Override
    public void evaluate(List<TableMetadata> initialTables, List<TableMetadata> targetTables, DatabaseReportManager report) {
        for (TableMetadata initialTable : initialTables) {
            for (TableMetadata targetTable : targetTables) {
                if (initialTable.getTableName().equalsIgnoreCase(targetTable.getTableName())) {
                    evaluateTablesColumnsCount(initialTable, targetTable, report);
                }
            }
        }
    }

    public void evaluateTablesColumnsCount(TableMetadata initialTable, TableMetadata targetTable, DatabaseReportManager report) {
        boolean hasChanges = false;

        if (initialTable.getColumns().length != targetTable.getColumns().length) {
            hasChanges = true;
        }
        Map<String, ColumnMetadata> targetColumnsMap = Arrays.stream(targetTable.getColumns())
                .collect(Collectors.toMap(
                        col -> col.getName().toLowerCase(),
                        col -> col
                ));

        for (ColumnMetadata initialCol : initialTable.getColumns()) {
            String colName = initialCol.getName().toLowerCase();
            ColumnMetadata targetCol = targetColumnsMap.get(colName);

            if (targetCol == null) {
                hasChanges = true;
                break;
            } else if (!initialCol.equals(targetCol)) {
                hasChanges = true;
                break;
            }
        }

        if (!hasChanges) {
            Map<String, ColumnMetadata> initialColumnsMap = Arrays.stream(initialTable.getColumns())
                    .collect(Collectors.toMap(
                            col -> col.getName().toLowerCase(),
                            col -> col
                    ));

            for (ColumnMetadata targetCol : targetTable.getColumns()) {
                if (!initialColumnsMap.containsKey(targetCol.getName().toLowerCase())) {
                    hasChanges = true;
                    break;
                }
            }
        }

        if (hasChanges) {
            TableChangeReport tableChangeReport = report.getTableReport(initialTable.getTableName());
            tableChangeReport.onUpdateTable(initialTable, targetTable);
            report.addTableReport(tableChangeReport);
        }
    }
}

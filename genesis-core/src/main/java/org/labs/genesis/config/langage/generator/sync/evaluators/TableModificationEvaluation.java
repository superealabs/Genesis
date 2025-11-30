package org.labs.genesis.config.langage.generator.sync.evaluators;

import org.labs.genesis.config.langage.generator.sync.report.DatabaseReportManager;
import org.labs.genesis.config.langage.generator.sync.report.TableChangeReport;
import org.labs.genesis.connexion.model.ChildTableMetadata;
import org.labs.genesis.connexion.model.ColumnMetadata;
import org.labs.genesis.connexion.model.ParentTableMetadata;
import org.labs.genesis.connexion.model.TableMetadata;

import java.util.*;
import java.util.stream.Collectors;

public class TableModificationEvaluation implements IDatabaseEvaluator {
    @Override
    public void evaluate(List<TableMetadata> initialTables, List<TableMetadata> targetTables, DatabaseReportManager report) {
        for (TableMetadata initialTable : initialTables) {
            for (TableMetadata targetTable : targetTables) {
                if (initialTable.getTableName().equalsIgnoreCase(targetTable.getTableName())) {
                    evaluateTableColumns(initialTable, targetTable, report);
                    evaluateTableRelations(initialTable, targetTable, report);
                }
            }
        }
    }

    public void evaluateTableColumns(TableMetadata initialTable, TableMetadata targetTable, DatabaseReportManager report) {
        boolean hasChanges = false;
        List<String> description = new ArrayList<>();
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
                description.add("Column '" + initialCol.getName() + "' was removed.");
                break;
            } else if (!initialCol.equals(targetCol)) {
                hasChanges = true;
                description.add("Column '" + initialCol.getName() + "' was modified.");
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
                    description.add("Column '" + targetCol.getName() + "' was added.");
                    break;
                }
            }
        }

        if (hasChanges) {
            TableChangeReport tableChangeReport = report.getTableReport(initialTable.getTableName());
            tableChangeReport.onUpdateTable(initialTable, targetTable, description);
            onModelUpdateColumn(tableChangeReport);
            report.addTableReport(tableChangeReport);
        }
    }

    private void onModelUpdateColumn(TableChangeReport tableChangeReport) {
        tableChangeReport.updateWebApiChangeReport(true, true, false, false);
        tableChangeReport.updateFrontendChangeReport(true,true, false);
    }

    private void onRelationUpdate(TableChangeReport tableChangeReport){
        tableChangeReport.updateWebApiChangeReport(true, true, true, true);
        tableChangeReport.updateFrontendChangeReport(true, true, true);
    }

    public void evaluateParentTableRelations(TableMetadata initialTable, TableMetadata targetTable, DatabaseReportManager report){
        boolean hasChanges = false;
        List<String> description = new ArrayList<>();
        Map<String, ChildTableMetadata> initialRelationsMap = Arrays.stream(initialTable.getChildTables().toArray(new ChildTableMetadata[0]))
                .collect(Collectors.toMap(
                        rel -> rel.getTable().getTableName().toLowerCase(),  // relationKey = sourceCol + "->" + targetTable
                        rel -> rel
                ));

        Map<String, ChildTableMetadata> targetRelationsMap = Arrays.stream(targetTable.getChildTables().toArray(new ChildTableMetadata[0]))
                .collect(Collectors.toMap(
                        rel -> rel.getTable().getTableName().toLowerCase(),
                        rel -> rel
                ));
        for (ChildTableMetadata initialRel : initialTable.getChildTables()) {

            String relKey = initialRel.getTable().getTableName().toLowerCase();
            ChildTableMetadata targetRel = targetRelationsMap.get(relKey);

            if (targetRel == null) {
                hasChanges = true;
                description.add("Child relation on entity '" + initialRel.getTable().getTableName() + "' was removed.");
                break;

            } else if (!initialRel.equals(targetRel)) {
                hasChanges = true;
                description.add("Child relation on entity '" + initialRel.getTable().getTableName() + "' was modified.");
                break;
            }
        }

        if (!hasChanges) {
            for (ChildTableMetadata targetRel : targetTable.getChildTables()) {
                String relKey = targetRel.getTable().getTableName().toLowerCase();
                if (!initialRelationsMap.containsKey(relKey)) {
                    hasChanges = true;
                    description.add("Child relation on entity '" + targetRel.getTable().getTableName() + "' was added.");
                    break;
                }
            }
        }

        if (hasChanges) {
            TableChangeReport tableChangeReport = report.getTableReport(initialTable.getTableName());
            tableChangeReport.onUpdateTable(initialTable, targetTable, description);
            onRelationUpdate(tableChangeReport);
            report.addTableReport(tableChangeReport);
        }
    }

    public void evaluateChildTableRelations(TableMetadata initialTable, TableMetadata targetTable, DatabaseReportManager report){
        boolean hasChanges = false;
        List<String> description = new ArrayList<>();
        Map<String, ParentTableMetadata> initialRelationsMap = Arrays.stream(initialTable.getParentTables().toArray(new ParentTableMetadata[0]))
                .collect(Collectors.toMap(
                        rel -> rel.getTable().getTableName().toLowerCase(),
                        rel -> rel
                ));

        Map<String, ParentTableMetadata> targetRelationsMap = Arrays.stream(targetTable.getParentTables().toArray(new ParentTableMetadata[0]))
                .collect(Collectors.toMap(
                        rel -> rel.getTable().getTableName().toLowerCase(),
                        rel -> rel
                ));
        for (ParentTableMetadata initialRel : initialTable.getParentTables()) {

            String relKey = initialRel.getTable().getTableName().toLowerCase();
            ParentTableMetadata targetRel = targetRelationsMap.get(relKey);

            if (targetRel == null) {
                hasChanges = true;
                description.add("Parent relation on entity '" + initialRel.getTable().getTableName() + "' was removed.");
                break;

            } else if (!initialRel.equals(targetRel)) {
                hasChanges = true;
                description.add("Parent relation on entity '" + initialRel.getTable().getTableName() + "' was modified.");
                break;
            }
        }

        if (!hasChanges) {
            for (ParentTableMetadata targetRel : targetTable.getParentTables()) {
                String relKey = targetRel.getTable().getTableName().toLowerCase();
                if (!initialRelationsMap.containsKey(relKey)) {
                    hasChanges = true;
                    description.add("Parent relation on entity '" + targetRel.getTable().getTableName() + "' was added.");
                    break;
                }
            }
        }

        if (hasChanges) {
            TableChangeReport tableChangeReport = report.getTableReport(initialTable.getTableName());
            tableChangeReport.onUpdateTable(initialTable, targetTable, description);
            onRelationUpdate(tableChangeReport);
            report.addTableReport(tableChangeReport);
        }
    }
    public void evaluateTableRelations(TableMetadata initialTable, TableMetadata targetTable, DatabaseReportManager report) {
        boolean hasChanges = false;
        List<String> description = new ArrayList<>();
        if (initialTable.getIsParent() && !targetTable.getIsParent()) {
            hasChanges = true;
            description.add("Parent table status '" + initialTable.getTableName() + "' was removed.");
        } else if (initialTable.getIsChild() && !targetTable.getIsChild()) {
            hasChanges = true;
            description.add("Child table status '" + initialTable.getTableName() + "' was modified.");
        }

        if (initialTable.getIsParent() && targetTable.getIsParent()) {
            evaluateParentTableRelations(initialTable, targetTable, report);
        }
        else if (initialTable.getIsChild() && targetTable.getIsChild()) {
            evaluateChildTableRelations(initialTable, targetTable, report);
        }

        if (hasChanges) {
            TableChangeReport tableChangeReport = report.getTableReport(initialTable.getTableName());
            tableChangeReport.onUpdateTable(initialTable, targetTable, description);
            onRelationUpdate(tableChangeReport);
            report.addTableReport(tableChangeReport);
        }
    }

}

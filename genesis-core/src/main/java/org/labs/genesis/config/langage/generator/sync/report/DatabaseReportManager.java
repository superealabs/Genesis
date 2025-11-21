package org.labs.genesis.config.langage.generator.sync.report;

import lombok.Getter;
import lombok.Setter;
import org.labs.genesis.config.langage.generator.sync.enums.ReportCategory;
import org.labs.genesis.connexion.model.TableMetadata;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class DatabaseReportManager {
    private Map<String, TableChangeReport> tableReports;

    public DatabaseReportManager(){
        setTableReports(new HashMap<>());
    }

    public void addTableReport(TableChangeReport tableChangeReport){
        if (tableChangeReport.getNewTable() == null && tableChangeReport.getOldTable() == null){
            return;
        }
        this.tableReports.put(tableChangeReport.getNewTable() != null ?
                tableChangeReport.getNewTable().getTableName() :
                tableChangeReport.getOldTable().getTableName(),
                tableChangeReport);
    }

    public TableChangeReport getTableReport(String tableName){
        TableChangeReport report = this.tableReports.get(tableName);
        if (report == null){
            report = new TableChangeReport();
        }
        return report;
    }

    public List<TableMetadata> getAddedTables(){
        return this.tableReports.values().stream()
                .filter(report -> report.getCategory().equals(ReportCategory.ADDITION))
                .map(TableChangeReport::getNewTable)
                .toList();
    }
    public List<TableMetadata> getRemovedTables(){
        return this.tableReports.values().stream()
                .filter(report -> report.getCategory().equals(ReportCategory.REMOVAL))
                .map(TableChangeReport::getNewTable)
                .toList();
    }

    public List<TableMetadata> getUpdatedTables(){
        return this.tableReports.values().stream()
                .filter(report -> report.getCategory().equals(ReportCategory.MODIFICATION))
                .map(TableChangeReport::getNewTable)
                .toList();
    }
}

package org.labs.genesis.config.langage.generator.sync.report;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.labs.genesis.config.langage.generator.sync.enums.ReportCategory;
import org.labs.genesis.connexion.model.TableMetadata;

@Getter
@Setter
@AllArgsConstructor
public class TableChangeReport {
    private WebApiChangeReport webApiChangeReport;
    private ReportCategory category;
    private TableMetadata oldTable;
    private TableMetadata newTable;

    public TableChangeReport() {
        this.webApiChangeReport = new WebApiChangeReport();
    }

    public void onAddTable(TableMetadata tableMetadata){
        this.category = ReportCategory.ADDITION;
        this.newTable = tableMetadata;
        this.oldTable = null;
        this.getWebApiChangeReport().onAddTable();
    }

    public void onRemoveTable(TableMetadata tableMetadata){
        this.category = ReportCategory.REMOVAL;
        this.oldTable = tableMetadata;
        this.newTable = null;
        this.getWebApiChangeReport().onRemoveTable();
    }

    public void onUpdateTable(TableMetadata oldTable, TableMetadata newTable){
        this.category = ReportCategory.MODIFICATION;
        this.oldTable = oldTable;
        this.newTable = newTable;
        this.getWebApiChangeReport().onUpdateTable();
    }
}

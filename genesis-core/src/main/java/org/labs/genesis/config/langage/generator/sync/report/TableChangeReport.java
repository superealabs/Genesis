package org.labs.genesis.config.langage.generator.sync.report;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.labs.genesis.config.langage.generator.sync.enums.ReportCategory;
import org.labs.genesis.connexion.model.TableMetadata;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class TableChangeReport {
    private WebApiChangeReport webApiChangeReport;
    private FrontendChangeReport frontendChangeReport;
    private ReportCategory category;
    private TableMetadata oldTable;
    private TableMetadata newTable;
    private List<String> reportDescriptions;

    public TableChangeReport() {
        this.webApiChangeReport = new WebApiChangeReport();
        this.frontendChangeReport = new FrontendChangeReport();
        this.category = ReportCategory.UNCATEGORISED;
        this.reportDescriptions = new ArrayList<>();
    }

    public void addDescription(String description){
        this.reportDescriptions.add(description);
    }

    public void addDescriptions(List<String> descriptions){
        this.reportDescriptions.addAll(descriptions);
    }

    public void onAddTable(TableMetadata tableMetadata){
        this.category = ReportCategory.ADDITION;
        this.newTable = tableMetadata;
        this.oldTable = null;
        this.getWebApiChangeReport().onAddTable();
        this.getFrontendChangeReport().onAddTable();
        addDescription("Created new table '" + tableMetadata.getTableName() + "'");
    }

    public void onRemoveTable(TableMetadata tableMetadata){
        this.category = ReportCategory.REMOVAL;
        this.oldTable = tableMetadata;
        this.newTable = null;
        this.getWebApiChangeReport().onRemoveTable();
        this.getFrontendChangeReport().onRemoveTable();
        addDescription("Removed table '" + tableMetadata.getTableName() + "'");
    }

    public void onUpdateTable(TableMetadata oldTable, TableMetadata newTable){
        onUpdateTable(oldTable, newTable, null);
    }
    public void onUpdateTable(TableMetadata oldTable, TableMetadata newTable, List<String> descriptions){
        this.category = ReportCategory.MODIFICATION;
        this.oldTable = oldTable;
        this.newTable = newTable;
        if (descriptions != null && !descriptions.isEmpty()){
            addDescriptions(descriptions);
        }
    }

    public void updateWebApiChangeReport(Boolean model, Boolean dao, Boolean service, Boolean controller) {
        this.webApiChangeReport.onUpdateTable(model, dao, service, controller);
    }

    public void updateFrontendChangeReport(Boolean model, Boolean components, Boolean service) {
        this.frontendChangeReport.onUpdateTable(model, components, service);
    }
}

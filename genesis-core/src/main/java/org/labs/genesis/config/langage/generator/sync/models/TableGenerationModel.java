package org.labs.genesis.config.langage.generator.sync.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.labs.genesis.connexion.model.ColumnMetadata;
import org.labs.genesis.connexion.model.TableMetadata;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TableGenerationModel {
    private String tableName;
    private ColumnMetadata primaryColumn;
    private ColumnMetadata[] columns;
    private String className;
    private Boolean hasFk;
    public TableGenerationModel(TableMetadata tableMetadata) {
        this.tableName = tableMetadata.getTableName();
        this.primaryColumn = tableMetadata.getPrimaryColumn();
        this.className = tableMetadata.getClassName();
        this.hasFk = tableMetadata.getHasFk();
        this.columns = tableMetadata.getColumns();
    }


    public TableMetadata generateTableMetadata() {
        TableMetadata tableMetadata = new TableMetadata();
        tableMetadata.setTableName(tableName);
        tableMetadata.setPrimaryColumn(primaryColumn);
        tableMetadata.setColumns(columns);
        tableMetadata.setClassName(className);
        tableMetadata.setHasFk(hasFk);
        return  tableMetadata;
    }
}

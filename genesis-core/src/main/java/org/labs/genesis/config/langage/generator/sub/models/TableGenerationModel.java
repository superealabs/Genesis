package org.labs.genesis.config.langage.generator.sub.models;

import lombok.Getter;
import lombok.Setter;
import org.labs.genesis.config.ProjectGenerationContext;
import org.labs.genesis.connexion.model.ColumnMetadata;
import org.labs.genesis.connexion.model.TableMetadata;

@Getter
@Setter
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
}

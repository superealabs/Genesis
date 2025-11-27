package org.labs.genesis.connexion.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class ParentTableMetadata {
    private TableMetadata table;
    private ColumnMetadata column;

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ParentTableMetadata other) {
            if(this.table.getTableName().equalsIgnoreCase(other.getTable().getTableName())){
                return true;
            }
        }
        return super.equals(obj);
    }
}

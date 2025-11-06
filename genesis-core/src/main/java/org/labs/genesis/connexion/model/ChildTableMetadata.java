package org.labs.genesis.connexion.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class ChildTableMetadata {
    private TableMetadata table;
    private boolean mandatory;
    private ColumnMetadata column;

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ChildTableMetadata other) {
            if(this.table.getTableName().equalsIgnoreCase(other.getTable().getTableName())){
                return true;
            }
        }
        return super.equals(obj);
    }
}

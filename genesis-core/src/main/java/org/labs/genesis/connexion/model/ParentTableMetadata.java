package org.labs.genesis.connexion.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Setter
@Getter
@AllArgsConstructor
public class ParentTableMetadata {
    private TableMetadata table;
    private ColumnMetadata column;

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        boolean same = true;
        ParentTableMetadata other = (ParentTableMetadata) obj;
        same = same && Objects.equals(this.table.getTableName(), other.getTable().getTableName());
        same = same && Objects.equals(this.column, other.column);
        return  same;
    }
}

package org.labs.genesis.connexion.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

@Setter
@Getter
@AllArgsConstructor
public class ChildTableMetadata {
    private TableMetadata table;
    private boolean mandatory;
    private boolean hasForm;
    private ColumnMetadata column;

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        boolean same = true;
        ChildTableMetadata other = (ChildTableMetadata) obj;
        same = same && Objects.equals(this.table.getTableName(), other.table.getTableName());
        same = same && Objects.equals(this.column, other.column);
        return  same;
    }
}

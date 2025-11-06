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
}

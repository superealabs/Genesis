package org.labs.genesis.connexion.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class ColumnMetadata {
    private String name;
    private String type;
    private boolean primary;
    private boolean foreign;
    private String referencedTable;
    private String columnType;
    private String referencedColumn;
    private String referencedColumnType;
}

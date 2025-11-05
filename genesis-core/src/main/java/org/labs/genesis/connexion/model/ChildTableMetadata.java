package org.labs.genesis.connexion.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class ChildTableMetadata {
    private TableMetadata table;
    private boolean mandatory;
}

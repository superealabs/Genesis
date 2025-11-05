package org.labs.genesis.connexion.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class RelationParameter {
    private String parentName;
    private String childName;
    private Boolean mandatory;
}

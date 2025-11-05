package org.labs.genesis.connexion.model;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
public class RelationParameter {
    private TableMetadata parentTable;
    private TableMetadata childTable;
    private Boolean mandatory;

    public Object[] toRow(){
        return new Object[]{parentTable, childTable, mandatory};
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof RelationParameter){
            boolean childCheck = this.getChildTable().getClassName().equals(((RelationParameter) obj).getChildTable().getClassName());
            boolean parentCheck = this.getParentTable().getClassName().equals(((RelationParameter) obj).getParentTable().getClassName());
            return childCheck && parentCheck;
        }
        return  false;
    }
}

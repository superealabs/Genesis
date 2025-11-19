package org.labs.genesis.connexion.model;

import lombok.*;
import org.labs.genesis.config.ProjectGenerationContext;

@Getter
@Setter
@AllArgsConstructor
public class RelationParameter {
    private String parentTable;
    private String childTable;
    private Boolean mandatory;

    public Object[] toRow(){
        return new Object[]{parentTable, childTable, mandatory};
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof RelationParameter){
            boolean childCheck = this.getChildTable().equalsIgnoreCase(((RelationParameter) obj).getChildTable());
            boolean parentCheck = this.getParentTable().equalsIgnoreCase(((RelationParameter) obj).getParentTable());
            return childCheck && parentCheck;
        }
        return  false;
    }

    public void setParameter(ProjectGenerationContext context){
        TableMetadata parentTableMetadata = context.findTableByName(parentTable, context.getEntityTables());
        TableMetadata childTableMetadata = context.findTableByName(childTable, context.getEntityTables());
        parentTableMetadata.addChild(childTableMetadata, mandatory);
        childTableMetadata.setParentTable(parentTableMetadata);
    }
}

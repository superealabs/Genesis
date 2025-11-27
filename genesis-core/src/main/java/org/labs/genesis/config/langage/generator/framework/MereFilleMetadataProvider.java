package org.labs.genesis.config.langage.generator.framework;

import org.labs.genesis.connexion.model.ChildTableMetadata;
import org.labs.genesis.connexion.model.ParentTableMetadata;
import org.labs.genesis.connexion.model.TableMetadata;
import org.labs.utils.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MereFilleMetadataProvider {
    public static HashMap<String, Object> getRelationsHashMap(TableMetadata tableMetadata){
        HashMap<String, Object> metadata = new HashMap<>();
        if (tableMetadata.getIsParent()){
            metadata.put("isParentTable", true);
            metadata.put("isChildTable", false);
            metadata.put("parentPk", tableMetadata.getPrimaryColumn().getName());
            List<HashMap<String, Object>> children = new ArrayList<>();
            List<HashMap<String, Object>> childrenWithForm = new ArrayList<>();
            for (ChildTableMetadata child : tableMetadata.getChildTables()) {
                children.add(getChildHashMap(child));
                if (child.isHasForm()) {
                    childrenWithForm.add(getChildHashMap(child));
                }
            }
            metadata.put("children", children);
            metadata.put("childrenWithForm", childrenWithForm);
            metadata.put("hasChildrenForm", !childrenWithForm.isEmpty());
            metadata.put("notViewAndParent", !tableMetadata.getIsView() && tableMetadata.getIsParent());
        } else if (tableMetadata.getIsChild()) {
            metadata.put("isParentTable", false);
            metadata.put("isChildTable", true);
            metadata.put("notViewAndChild", !tableMetadata.getIsView() && tableMetadata.getIsChild());
            List<HashMap<String, Object>> parents = new ArrayList<>();
            for (ParentTableMetadata parent : tableMetadata.getParentTables()) {
                parents.add(getParentHashMap(parent));
            }
            metadata.put("parents", parents);
        }
        return metadata;
    }
    public static HashMap<String, Object> getChildHashMap(ChildTableMetadata tableMetadata){
        HashMap<String, Object> metadata = new HashMap<>();
        metadata.put("className", tableMetadata.getTable().getClassName());
        metadata.put("isRequired", tableMetadata.isMandatory());
        metadata.put("hasForm", tableMetadata.isHasForm());
        metadata.put("parentName",  tableMetadata.getColumn().getName());
        metadata.put("parentColumnNameFiled", StringUtils.toCamelCase(tableMetadata.getColumn().getReferencedColumn()));
        metadata.put("childPk", tableMetadata.getTable().getPrimaryColumn().getName());
        metadata.putAll(FrameworkMetadataProvider.getTableMetadataHashMap(tableMetadata.getTable()));
        return metadata;
    }

    public static HashMap<String, Object> getParentHashMap(ParentTableMetadata tableMetadata){
        HashMap<String, Object> metadata = new HashMap<>();
        metadata.put("className", tableMetadata.getTable().getClassName());
        metadata.put("parentName",  tableMetadata.getColumn().getName());
        metadata.put("parentColumnNameFiled", StringUtils.toCamelCase(tableMetadata.getColumn().getReferencedColumn()));
        metadata.putAll(FrameworkMetadataProvider.getTableMetadataHashMap(tableMetadata.getTable()));
        return metadata;
    }
}

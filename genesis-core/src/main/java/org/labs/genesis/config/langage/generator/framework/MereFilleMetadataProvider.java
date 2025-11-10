package org.labs.genesis.config.langage.generator.framework;

import org.labs.genesis.connexion.model.ChildTableMetadata;
import org.labs.genesis.connexion.model.TableMetadata;
import org.labs.utils.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MereFilleMetadataProvider {
    public static HashMap<String, Object> getRelationsHashMap(TableMetadata tableMetadata){
        HashMap<String, Object> metadata = new HashMap<>();
        metadata.put("isParentTable", tableMetadata.getIsParent());
        metadata.put("isChildTable", tableMetadata.getIsChild());
        metadata.put("parentPk", tableMetadata.getPrimaryColumn().getName());
        List<HashMap<String, Object>> children = new ArrayList<>();
        for (ChildTableMetadata child : tableMetadata.getChildTables()) {
            children.add(getChildHashMap(child));
        }
        metadata.put("children", children);
        metadata.put("notViewAndParent", !tableMetadata.getIsView() && tableMetadata.getIsParent());
        metadata.put("notViewAndChild", !tableMetadata.getIsView() && tableMetadata.getIsChild());
        return metadata;
    }
    public static HashMap<String, Object> getChildHashMap(ChildTableMetadata tableMetadata){
        HashMap<String, Object> metadata = new HashMap<>();
        metadata.put("className", tableMetadata.getTable().getClassName());
        metadata.put("isRequired", tableMetadata.isMandatory());
        metadata.put("parentFk",  StringUtils.toCamelCase(tableMetadata.getColumn().getReferencedColumn()));
        metadata.put("childPk", tableMetadata.getTable().getPrimaryColumn().getName());
        return metadata;
    }
}

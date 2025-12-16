package org.labs.genesis.apj.filetype.mapping;

import lombok.Getter;
import lombok.Setter;
import org.labs.genesis.apj.component.ApjField;
import org.labs.genesis.apj.filetype.ApjFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class Mapping extends ApjFile {
    private String pk;
    private String superclasse;
    private boolean isMere;
    private boolean isFille;
    private String liaison;
    private String classeLiaison;

    @Override
    public HashMap<String, Object> getPrimaryHashMap() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("imports", this.getImports());
        map.put("body", this.getBody());
        return map;
    }
    @Override
    public HashMap<String, Object> buildMetadata() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("packageMapping", this.getPackageMapping());
        map.put("mapping", this.getMapping());
        map.put("superclasse", this.getSuperclasse());
        map.put("nomTable", this.getNomTable());
        map.put("champs", getChampsList());
        map.put("packageImports", this.getPackageImports());
        map.put("packageImport", this.getPackageMapping());
        map.put("isMere", this.isMere());
        map.put("isFille", this.isFille());
        map.put("liaison", this.getLiaison());
        map.put("classeLiaison", this.getClasseLiaison());
        if (this.getPk()==null || this.getPk().isEmpty()) {
            map.put("pk", "id");
        } else {
            map.put("pk", this.getPk());
        }
        return map;
    }

    private List<Map<String, Object>> getChampsList() {
        List<Map<String, Object>> fields = new ArrayList<>();
        for (ApjField field : this.getChamps()) {
            String type = field.getType();
            if (type.contains(".")){
                this.addPackageImport(type);
                type = type.substring(type.lastIndexOf(".") + 1);
                field.setType(type);
            }
            Map<String, Object> fieldMap = getApjFieldHashMap(field);
            fields.add(fieldMap);
        }
        return fields;
    }

}

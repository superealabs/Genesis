package org.labs.genesis.apj.filetype.pages;

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
public class PageInsertMultiple extends ApjFile {
    private String packageMappingFille;
    private String mappingFille;
    private String nomTableFille;
    private String colonneMere;

    public PageInsertMultiple(){

    }

    @Override
    public HashMap<String, Object> getPrimaryHashMap() {
        return super.getPrimaryHashMap();
    }

    @Override
    public HashMap<String, Object> buildMetadata() {
        super.makeOrdre();
        HashMap<String, Object> map = new HashMap<>();
        map.put("packageMapping", this.getPackageMapping());
        map.put("packageMappingFille", this.getPackageMappingFille());
        map.put("mapping", this.getMapping());
        map.put("nomTable", this.getNomTable());
        map.put("mappingFille", this.getMappingFille());
        map.put("nomTableFille", this.getNomTableFille());
        map.put("colonneMere", this.getColonneMere());
        map.put("titre", this.getTitre());
        map.put("apres", this.getApres());
        map.put("titreUpdate", this.getTitreUpdate());
        map.put("ordre", this.getOrdre());
        map.put("champs", getChampsList());
        map.put("isWithListe", this.isWithListe());
        map.put("listeSize", this.getListes().size());
        map.put("listes", this.getListesList());
        map.put("packageImports", this.getPackageImports());
        return map;
    }

    private List<Map<String, Object>> getChampsList() {
        List<Map<String, Object>> fields = new ArrayList<>();
        for (ApjField field : this.getChamps()) {
            field.checkAutre();
            Map<String, Object> fieldMap = getApjFieldHashMap(field);
            fields.add(fieldMap);
        }
        return fields;
    }

    public void build(){
        super.makeListe();
    }
}

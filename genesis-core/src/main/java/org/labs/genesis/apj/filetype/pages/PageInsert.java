package org.labs.genesis.apj.filetype.pages;

import lombok.Getter;
import lombok.Setter;
import org.labs.genesis.apj.filetype.ApjFile;
import org.labs.genesis.apj.component.ApjField;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class PageInsert extends ApjFile {

    public PageInsert(){

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
        map.put("mapping", this.getMapping());
        map.put("nomTable", this.getNomTable());
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
        super.makeListeAndAutoComplete();
    }
}

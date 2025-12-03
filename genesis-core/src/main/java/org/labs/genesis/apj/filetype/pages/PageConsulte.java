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
public class PageConsulte extends ApjFile {
    private String pageActuel;
    private String pageRetour;
    private String pageModif;
    private String pageApresDelete;
    private boolean withOnglet;

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
        map.put("pageActuel", this.getPageActuel());
        map.put("pageRetour", this.getApres());
        map.put("pageModif", this.getPageModif());
        map.put("pageApresDelete", this.getPageApresDelete());
        map.put("isWithOnglet", this.isWithOnglet());
        map.put("ordre", this.getOrdre());
        map.put("champs", getChampsList());
        return map;
    }

    private List<Map<String, Object>> getChampsList() {
        List<Map<String, Object>> fields = new ArrayList<>();
        for (ApjField field : this.getChamps()) {
            field.checkVisibleAndUpdateLien();
            Map<String, Object> fieldMap = getApjFieldHashMap(field);
            fields.add(fieldMap);
        }
        return fields;
    }

}

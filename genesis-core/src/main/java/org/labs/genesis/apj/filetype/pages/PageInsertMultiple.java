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
    private ApjField[] champsFille;
    private boolean isWithListeFille = false;


    public PageInsertMultiple(){

    }

    @Override
    public HashMap<String, Object> getPrimaryHashMap() {
        return super.getPrimaryHashMap();
    }

    @Override
    public HashMap<String, Object> buildMetadata() {
        super.makeOrdre();
        super.makeColOrdre();
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
        map.put("colOrdre", this.getColOrdre());
        map.put("champs", getChampsList());
        map.put("champsFille", getChampsListFille());
        map.put("isWithListe", this.isWithListe());
        map.put("isWithListeFille", this.isWithListeFille());
        map.put("listeSize", this.getListes().size());
        map.put("listeFilleSize", this.getListesFille().size());
        map.put("listes", this.getListesList());
        map.put("listesFille", this.getListesFilleList());
        map.put("packageImports", this.getPackageImports());
        return map;
    }

    public List<Map<String, Object>> getChampsList() {
        return getChampsList(this.getChamps(),false);
    }

    public List<Map<String, Object>> getChampsListFille() {
        return getChampsList(this.getChampsFille(),true);
    }

    private List<Map<String, Object>> getChampsList(ApjField[] champs,boolean isFille) {
        List<Map<String, Object>> fields = new ArrayList<>();
        for (ApjField field : champs) {
            field.checkAutre();
            if (field.isWithAutre() && isFille) {
                this.addPackageImport("affichage.Champ");
            }
            Map<String, Object> fieldMap = getApjFieldHashMap(field);
            fields.add(fieldMap);
        }
        return fields;
    }

    public void build(){
        super.makeListeAndAutoComplete();
        super.makeListeAndAutoCompleteFille();
        if (!this.getListesFille().isEmpty() || !this.getListes().isEmpty()){
            this.addPackageImport("affichage.Liste");
        }
    }
}

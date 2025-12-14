package org.labs.genesis.apj.filetype.pages;

import lombok.Getter;
import lombok.Setter;
import org.labs.genesis.apj.component.ApjField;
import org.labs.utils.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class PageRechercheGroupe extends PageRecherche {
    private ApjField[] somDefauts;
    private String colGr;
    private String colGrCol;
    private String somDefaut;
    private String lienColGrCol;

    public PageRechercheGroupe(){

    }

    @Override
    public HashMap<String, Object> buildMetadata() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("packageMapping", this.getPackageMapping());
        map.put("mapping", this.getMapping());
        map.put("nomTable", this.getNomTable());
        if (!this.isOnglet()){
            map.put("apres", this.getApres());
            map.put("champs", getChampsList());
            map.put("colSomme", this.getColSomme());
            map.put("listeCrt", this.getListeCrt());
            map.put("listeInt", this.getListeInt());
            map.put("colGr", this.getColGr());
            map.put("colGrCol", this.getColGrCol());
            map.put("somDefaut", this.getSomDefaut());
            map.put("somDefauts", this.getSomDefautsList());
            map.put("lienColGrCol", this.getLienColGrCol());
            map.put("titre", this.getTitre());
            map.put("isWithListe", this.isWithListe());
            map.put("listeSize", this.getListes().size());
            map.put("listes", this.getListesList());
            map.put("packageImports", this.getPackageImports());
        }
        return map;
    }

    private List<Map<String, Object>> getSomDefautsList() {
        List<Map<String, Object>> fields = new ArrayList<>();
        for (ApjField field : this.getSomDefauts()) {
            Map<String, Object> fieldMap = getApjFieldHashMap(field);
            fields.add(fieldMap);
        }
        return fields;
    }

    public void makeSomDefauts(){
        ApjField[] data = this.getRecap();
        if (data.length == 0){
            this.setSomDefaut("null");
            return;
        }
        this.setSomDefauts(this.getRecap());
        String[] colS =  new String[data.length];
        for (int i = 0; i < data.length; i++){
            colS[i] = data[i].getNom();
        }
        this.setSomDefaut("{"+ StringUtils.quoteAndJoin(colS)+"}");
    }

    @Override
    public void build(){
        makeSomDefauts();
        super.makeListeAndAutoComplete();
    }

}

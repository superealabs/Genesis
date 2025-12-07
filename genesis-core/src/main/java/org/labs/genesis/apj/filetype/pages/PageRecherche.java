package org.labs.genesis.apj.filetype.pages;

import lombok.Getter;
import lombok.Setter;
import org.labs.genesis.apj.filetype.ApjFile;
import org.labs.genesis.apj.component.ApjField;

import org.labs.utils.StringUtils;

import java.util.*;

@Getter
@Setter
public class PageRecherche extends ApjFile {
    private String listeCrt;
    private String listeInt;
    private String libEntete;
    private String enteteRecap;
    private String colSomme;
    private String libEnteteAffiche;
    private ApjField[] recap;
    private ApjField[] tableau;
    private boolean withColSomme = false;
    private boolean isOnglet = false;

    public PageRecherche(){

    }

    @Override
    public HashMap<String, Object> getPrimaryHashMap() {
        return super.getPrimaryHashMap();
    }

    @Override
    public HashMap<String, Object> buildMetadata() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("packageMapping", this.getPackageMapping());
        map.put("mapping", this.getMapping());
        map.put("nomTable", this.getNomTable());
        map.put("libEntete", this.getLibEntete());
        map.put("libEnteteAffiche", this.getLibEnteteAffiche());
        if (!this.isOnglet()){
            map.put("apres", this.getApres());
            map.put("champs", getChampsList());
            map.put("colSomme", this.getColSomme());
            map.put("listeCrt", this.getListeCrt());
            map.put("listeInt", this.getListeInt());
            map.put("titre", this.getTitre());
            map.put("isWithColSomme", this.isWithColSomme());
            map.put("isWithListe", this.isWithListe());
            map.put("enteteRecap", this.getEnteteRecap());
            map.put("listeSize", this.getListes().size());
            map.put("listes", this.getListesList());
            map.put("packageImports", this.getPackageImports());
        }
        return map;
    }

    private List<Map<String, Object>> getChampsList() {
        List<Map<String, Object>> fields = new ArrayList<>();
        for (ApjField field : this.getChamps()) {
            Map<String, Object> fieldMap = getApjFieldHashMap(field);
            fields.add(fieldMap);
        }
        return fields;
    }

    public void makeEnteteRecap(){
        ApjField[] data = this.getRecap();
        if (data.length == 0){
            this.setColSomme("null");
            return;
        }
        this.setWithColSomme(true);
        String[] colS =  new String[data.length];
        String[] libColS =  new String[data.length+2];
        libColS[0] = "";
        libColS[1] = "Nombre";
        for (int i = 0; i < data.length; i++){
            colS[i] = data[i].getNom();
            libColS[i+2] = data[i].getLibelle();
        }
        this.setColSomme("{"+StringUtils.quoteAndJoin(colS)+"}");
        this.setEnteteRecap(StringUtils.quoteAndJoin(libColS));
    }

    public void makeTableau(){
        ApjField[] data = this.getTableau();
        if (data.length == 0){
            return;
        }
        String[] libEntete =  new String[data.length];
        String[] libEnteteAffiche =  new String[data.length];
        for (int i = 0; i < data.length; i++){
            libEntete[i] = data[i].getNom();
            libEnteteAffiche[i] = data[i].getLibelle();
        }
        this.setLibEntete(StringUtils.quoteAndJoin(libEntete));
        this.setLibEnteteAffiche(StringUtils.quoteAndJoin(libEnteteAffiche));
    }


    public void makeRecapAndTableau(){
        makeEnteteRecap();
        makeTableau();
    }

    public void build(){
        if (this.isOnglet()){
            makeTableau();
            return;
        }
        makeRecapAndTableau();
        super.makeListe();
    }

}

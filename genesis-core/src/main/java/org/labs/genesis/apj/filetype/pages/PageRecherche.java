package org.labs.genesis.apj.filetype.pages;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.labs.genesis.apj.component.Liste;
import org.labs.genesis.apj.filetype.ApjFile;
import org.labs.genesis.apj.component.ApjField;
import org.labs.genesis.apj.utilitaire.ConstantesApj;
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
    private List<Liste> listes = new ArrayList<>();
    private boolean withColSomme = false;
    private boolean withListe = false;
    private List<Map<String, String>> packageImports = new ArrayList<>();

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
        map.put("listeCrt", this.getListeCrt());
        map.put("listeInt", this.getListeInt());
        map.put("colSomme", this.getColSomme());
        map.put("libEntete", this.getLibEntete());
        map.put("titre", this.getTitre());
        map.put("apres", this.getApres());
        map.put("libEnteteAffiche", this.getLibEnteteAffiche());
        map.put("champs", getChampsList());
        map.put("isWithColSomme", this.isWithColSomme());
        map.put("isWithListe", this.isWithListe());
        map.put("enteteRecap", this.getEnteteRecap());
        map.put("listeSize", this.getListes().size());
        map.put("listes", this.getListesList());
        map.put("packageImports", this.getPackageImports());
        return map;
    }

    public static @NotNull Map<String, Object> getListeHashMap(Liste liste) {
        Map<String, Object> listeMap = new HashMap<>();
        listeMap.put("nom", liste.getNom());
        listeMap.put("index", liste.getIndex());
        listeMap.put("val", liste.getVal());
        listeMap.put("col", liste.getCol());
        listeMap.put("isListeString", liste.isListeString());
        listeMap.put("isOuiNon", liste.isOuiNon());
        listeMap.put("mapping", liste.getMapping());
        listeMap.put("nomTable", liste.getNomTable());
        return listeMap;
    }

    public List<Map<String, Object>> getListesList(){
        List<Map<String, Object>> listesMaps = new ArrayList<>();
        for (Liste liste : this.getListes()) {
            Map<String, Object> listeMap = getListeHashMap(liste);
            listesMaps.add(listeMap);
        }
        return listesMaps;
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

    public void makeListe(){
        this.getListes().clear();
        int index = 0;
        List<Map<String, String>> imports = new ArrayList<>();
        for (ApjField field : this.getChamps()){
            if (field.getType() == null){
                continue;
            }
            if (field.getType().equalsIgnoreCase(ConstantesApj.OUI_NON)){
                Liste liste = new Liste();
                liste.setNom(field.getNom());
                liste.setIndex(index);
                liste.setOuiNon(true);
                index++;
                this.getListes().add(liste);
            } else if (field.getType().equalsIgnoreCase(ConstantesApj.LISTE_STRING)){
                Liste liste = new Liste();
                liste.setNom(field.getNom());
                liste.setValColByDetails(field.getDetails());
                liste.setIndex(index);
                liste.setListeString(true);
                index++;
                this.getListes().add(liste);
            } else if (field.getType().equalsIgnoreCase(ConstantesApj.LISTE)){
                Liste liste = new Liste();
                liste.setNom(field.getNom());
                liste.buildByDetails(field.getDetails());
                Map<String, String> item = new HashMap<>();
                item.put("package", liste.getPackageMapping());
                imports.add(item);
                liste.setIndex(index);
                index++;
                this.getListes().add(liste);
            }
        }
        if (!this.getListes().isEmpty()) {
            this.setPackageImports(imports);
            this.setWithListe(true);
        }
    }

    public void makeRecapAndTableau(){
        makeEnteteRecap();
        makeTableau();
    }

    public void build(){
        makeRecapAndTableau();
        makeListe();
    }

}

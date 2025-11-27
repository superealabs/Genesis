package org.labs.genesis.apj.filetype.pages;

import lombok.Getter;
import lombok.Setter;
import org.labs.genesis.apj.filetype.ApjFile;
import java.util.HashMap;

@Getter
@Setter
public class PageRecherche extends ApjFile {
    private String packageMapping;
    private String mapping;
    private String nomTable;
    private String listeCrt;
    private String listeInt;
    private String libEntete;
    private String colSomme;
    private String libEnteteAffiche;
    private String titre;
    private String apres;

    private String imports;
    private String pr;
    private String html;
    private String basPage;

    public PageRecherche(){

    }

    @Override
    public HashMap<String, Object> getPrimaryHashMap() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("imports", this.getImports());
        map.put("pr", this.getPr());
        map.put("html", this.getHtml());
        map.put("basPage", this.getBasPage());
        return map;
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
        return map;
    }


}

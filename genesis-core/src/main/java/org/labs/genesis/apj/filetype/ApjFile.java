package org.labs.genesis.apj.filetype;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.labs.genesis.apj.component.ApjField;
import org.labs.genesis.apj.component.AutoComplete;
import org.labs.genesis.apj.component.Liste;
import org.labs.genesis.apj.utilitaire.ConstantesApj;
import org.labs.utils.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public abstract class ApjFile implements ApjMetadataProvider{
    private int id;
    private String name;
    private String imports;
    private String template;
    private String fileName;
    private String extension;
    private String jspBlock;
    private String html;
    private String basPage;
    private String titre;
    private String titreUpdate;
    private String packageMapping;
    private String mapping;
    private String nomTable;
    private ApjField[] champs;
    private ApjField[] champsFille;
    private String apres;
    private String ordre;
    private String colOrdre;
    private List<Liste> listes = new ArrayList<>();
    private List<Liste> listesFille = new ArrayList<>();
    private boolean withListe = false;
    private boolean withListeFille = false;
    private List<Map<String, String>> packageImports = new ArrayList<>();

    @Override
    public String toString() {
        return name;
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
        return getListesList(this.getListes());
    }

    public List<Map<String, Object>> getListesFilleList(){
        return getListesList(this.getListesFille());
    }

    public List<Map<String, Object>> getListesList(List<Liste> listes){
        List<Map<String, Object>> listesMaps = new ArrayList<>();
        for (Liste liste : listes) {
            Map<String, Object> listeMap = getListeHashMap(liste);
            listesMaps.add(listeMap);
        }
        return listesMaps;
    }

    @Override
    public HashMap<String, Object> getPrimaryHashMap() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("imports", this.getImports());
        map.put("jspBlock", this.getJspBlock());
        map.put("html", this.getHtml());
        map.put("basPage", this.getBasPage());
        return map;
    }

    public static @NotNull Map<String, Object> getApjFieldHashMap(ApjField field) {
        Map<String, Object> fieldMap = new HashMap<>();
        fieldMap.put("nom", field.getNom());
        fieldMap.put("libelle", field.getLibelle());
        fieldMap.put("type", field.getType());
        fieldMap.put("lien", field.getLien());
        fieldMap.put("isWithAutre", field.isWithAutre());
        fieldMap.put("autre", field.getAutre());
        fieldMap.put("attLien", field.getAttLien());
        fieldMap.put("isVisible", field.isVisible());
        fieldMap.put("isWithLien", field.isWithLien());
        fieldMap.put("isAutoComplete", field.isAutoComplete());
        fieldMap.put("autoCompleteDeclaration", field.getAutoCompleteDeclaration());
        return fieldMap;
    }

    public void makeListeAndAutoComplete(){
        makeListeAndAutoComplete(this.getChamps(), this.getListes(),false);
    }

    public void makeListeAndAutoCompleteFille(){
        makeListeAndAutoComplete(this.getChampsFille(), this.getListesFille(),true);
    }

    public void makeListeAndAutoComplete(ApjField[] fields,List<Liste> listes,boolean isFille){
        listes.clear();
        int index = 0;
        for (ApjField field : fields){
            if (field.getType() == null){
                continue;
            }
            if (field.getType().equalsIgnoreCase(ConstantesApj.OUI_NON)){
                Liste liste = new Liste();
                liste.setNom(field.getNom());
                liste.setIndex(index);
                liste.setOuiNon(true);
                index++;
                listes.add(liste);
            } else if (field.getType().equalsIgnoreCase(ConstantesApj.LISTE_STRING)){
                Liste liste = new Liste();
                liste.setNom(field.getNom());
                liste.setValColByDetails(field.getDetails());
                liste.setIndex(index);
                liste.setListeString(true);
                index++;
                listes.add(liste);
            } else if (field.getType().equalsIgnoreCase(ConstantesApj.LISTE)){
                Liste liste = new Liste();
                liste.setNom(field.getNom());
                liste.buildByDetails(field.getDetails());
                this.addPackageImport(liste.getPackageMapping());
                liste.setIndex(index);
                index++;
                listes.add(liste);
            } else if (field.getType().equalsIgnoreCase(ConstantesApj.AUTO_COMPLETE)){
                AutoComplete autoComplete = new AutoComplete();
                autoComplete.setDetails(field.getDetails());
                autoComplete.setNom(field.getNom());
                autoComplete.setFille(isFille);
                if (isFille){
                    this.addPackageImport("affichage.Champ");
                }
                autoComplete.build();
                field.setAutoComplete(true);
                field.setAutoCompleteDeclaration(autoComplete.getDeclaration());
            }
        }
        if (!listes.isEmpty()) {
            if (isFille) {
                this.setWithListeFille(true);
            } else {
                this.setWithListe(true);
            }
        }
    }

    public void addPackageImport(String pkg) {
        boolean exists = packageImports.stream()
                .anyMatch(m -> pkg.equals(m.get("package")));

        if (!exists) {
            Map<String, String> item = new HashMap<>();
            item.put("package", pkg);
            packageImports.add(item);
        }
    }

    public void makeOrdre(){
        if (this.getChamps() == null) {
            return;
        }
        List<String> ordreList = new ArrayList<>();
        for (ApjField field : this.getChamps()) {
            if (field.isVisible()){
                ordreList.add(field.getNom());
            }
        }
        this.setOrdre(StringUtils.quoteAndJoin(ordreList.toArray(new String[0])));
    }

    public void makeColOrdre(){
        if (this.getChampsFille() == null) {
            return;
        }
        List<String> ordreList = new ArrayList<>();
        for (ApjField field : this.getChampsFille()) {
            if (field.isVisible()){
                ordreList.add(field.getNom());
            }
        }
        this.setColOrdre(StringUtils.quoteAndJoin(ordreList.toArray(new String[0])));
    }
}

package org.labs.genesis.apj.filetype;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.labs.genesis.apj.utilitaire.ApjField;
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
    private String packageMapping;
    private String mapping;
    private String nomTable;
    private ApjField[] champs;
    private String apres;
    private String ordre;

    @Override
    public String toString() {
        return name;
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
        fieldMap.put("attLien", field.getAttLien());
        fieldMap.put("isVisible", field.isVisible());
        fieldMap.put("isWithLien", field.isWithLien());
        return fieldMap;
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
}

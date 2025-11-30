package org.labs.genesis.apj.filetype;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.labs.genesis.apj.utilitaire.ApjField;
import java.util.HashMap;
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

    @Override
    public String toString() {
        return name;
    }

    public static @NotNull Map<String, Object> getApjFieldHashMap(ApjField field) {
        Map<String, Object> fieldMap = new HashMap<>();
        fieldMap.put("nom", field.getNom());
        fieldMap.put("libelle", field.getLibelle());
        fieldMap.put("type", field.getType());
        fieldMap.put("lien", field.getLien());
        fieldMap.put("attLien", field.getAttLien());
        return fieldMap;
    }

}

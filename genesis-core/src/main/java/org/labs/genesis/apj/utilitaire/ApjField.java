package org.labs.genesis.apj.utilitaire;

import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ApjField {
    private String nom;
    private String libelle;
    private String type;
    private String attLien;
    private String lien;
    private boolean visible;

    public static List<ApjField> javaFieldsToApjFields(List<Field> fields) {
        List<ApjField> apjFields = new ArrayList<>();
        for (Field field : fields) {
            ApjField apjField = new ApjField();
            apjField.setNom(field.getName());
            apjField.setType(field.getType().getSimpleName());
            apjFields.add(apjField);
        }
        return  apjFields;
    }

    public boolean isRangeable() {
        return TypeUtil.isRangeable(type);
    }

    public boolean isSummable() {
        return TypeUtil.isSummable(type);
    }

    @Override
    public String toString() {
        return nom + " (" + type + ")";
    }
}

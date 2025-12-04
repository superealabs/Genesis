package org.labs.genesis.apj.component;

import lombok.Getter;
import lombok.Setter;
import org.labs.genesis.apj.utilitaire.TypeUtil;

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
    private String details;
    private String autre;
    private boolean visible;
    private boolean withLien;
    private boolean withAutre;

    public void setLien(String lien) {
        this.lien = lien;
        this.withLien = lien != null && !lien.isEmpty() && !(lien.equals("null"));
    }

    public void checkVisibleAndUpdateLien() {
        if (!this.visible) {
            this.lien = null;
            this.withLien = false;
        }
    }

    public void checkAutre() {
        this.withAutre = this.autre != null && !this.autre.isEmpty() && !(this.autre.equals("null"));
    }

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

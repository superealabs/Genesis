package org.labs.genesis.apj.component;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Liste {
    private String nom;
    private int index;
    private String type;
    private String mapping;
    private String packageMapping;
    private String nomTable;
    private String col;
    private String val;
    private boolean ouiNon;
    private boolean listeString;

    public void setValColByDetails(String details) {
        details = details.trim();
        int mid = details.indexOf("}{");
        String left = details.substring(1, mid).trim();
        String right = details.substring(mid + 2, details.length() - 1).trim();
        String[] cols = left.split("\\s*,\\s*");
        String[] vals = right.split("\\s*,\\s*");
        this.col = "\"" + String.join("\",\"", cols) + "\"";
        this.val = "\"" + String.join("\",\"", vals) + "\"";
    }


    public void buildByDetails(String details) {
        details = details.trim();
        if (details.startsWith("{") && details.endsWith("}")) {
            details = details.substring(1, details.length() - 1).trim();
        }

        String[] parts = details.split("\\s*,\\s*");
        if (parts.length != 4) {
            this.mapping = "TypeObjet";
            this.packageMapping = "bean.TypeObjet";
            this.nomTable = "TypeObjet";
            this.col = "val";
            this.val = "id";
        }

        this.packageMapping = parts[0];
        int lastDot = parts[0].lastIndexOf('.');
        if (lastDot >= 0) {
            this.mapping = parts[0].substring(lastDot + 1);
        } else {
            this.mapping = parts[0];
        }
        this.nomTable = parts[1];
        this.col = parts[2];
        this.val = parts[3];
    }

}

package org.labs.genesis.action.apjwizard.forms.popup;

public class ListeDetailsResult {
    private final String mapping;
    private final String nomTable;
    private final String aff;
    private final String val;

    public ListeDetailsResult(String mapping, String nomTable, String aff, String val) {
        this.mapping = mapping;
        this.nomTable = nomTable;
        this.aff = aff;
        this.val = val;
    }

    public String getMapping() { return mapping; }
    public String getNomTable() { return nomTable; }
    public String getAff() { return aff; }
    public String getVal() { return val; }

    public String toDetailsString() {
        return "{" + mapping + "," + nomTable + "," + aff + "," + val + "}";
    }
}

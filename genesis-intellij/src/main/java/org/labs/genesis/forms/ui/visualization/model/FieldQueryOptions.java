package org.labs.genesis.forms.ui.visualization.model;

/**
 * Options de requête optionnelles rattachées à un champ colonne
 * (Dimension / Measure / Value) : limite, tri, filtre.
 * Non encore consommé par le DataProvider — sert pour l'instant
 * uniquement à la configuration UI.
 */
public class FieldQueryOptions {

    public Integer limit;
    public String sortDirection; // "ASCENDING", "DESCENDING", ou null
    public String filter;

    public boolean isEmpty() {
        return limit == null
                && (sortDirection == null || sortDirection.isBlank())
                && (filter == null || filter.isBlank());
    }
}
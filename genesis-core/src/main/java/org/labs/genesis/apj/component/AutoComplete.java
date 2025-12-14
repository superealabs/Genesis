package org.labs.genesis.apj.component;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AutoComplete {
    private String nom;
    private String mapping;
    private String nomTable;
    private String val;
    private String retour;
    private String retourMapping;
    private String pageInsert;
    private boolean isInsert;
    private String declaration;
    private String details;
    private boolean isFille;

    public void build() {
        if (details == null || details.isEmpty()) {
            declaration = "";
            return;
        }
        if (this.isFille()) {
            buildFille();
            return;
        }
        String[] blocs = details.split("\\}\\{");
        for (int i = 0; i < blocs.length; i++) {
            blocs[i] = blocs[i].replace("{", "").replace("}", "");
        }

        String[] firstBloc = blocs[0].split(",");
        this.setMapping(firstBloc.length > 0 ? firstBloc[0].trim() : "");
        this.setNomTable(firstBloc.length > 1 ? firstBloc[1].trim() : "");

        if (blocs.length == 3) {
            this.setRetour(blocs[1].replace(",", ";"));
            this.setRetourMapping(blocs[2].replace(",", ";"));

            declaration = String.format(
                    ".setPageAppelComplete(\"%s\",\"id\",\"%s\",\"%s\",\"%s\")",
                    this.getMapping(), this.getNomTable(), this.getRetour(), this.getRetourMapping()
            );
            this.setInsert(false);
        }
        else if (blocs.length == 4) {
            this.setRetour(blocs[1].replace(",", ";"));
            this.setRetourMapping(blocs[2].replace(",", ";"));

            String[] pageBloc = blocs[3].split(",");
            this.setPageInsert(pageBloc.length > 0 ? pageBloc[0].trim() : "");
            this.setVal(pageBloc.length > 1 ? pageBloc[1].trim() : "");
            this.setInsert(true);

            declaration = String.format(
                    ".setPageAppelCompleteInsert(\"%s\",\"id\",\"%s\",\"%s\",\"%s\",\"%s.jsp\",\"id;%s\")",
                    this.getMapping(), this.getNomTable(), this.getRetour(), this.getRetourMapping(),
                    this.getPageInsert(), this.getVal()
            );
        }
    }

    public void buildFille(){
        String[] blocs = details.split("\\}\\{");
        for (int i = 0; i < blocs.length; i++) {
            blocs[i] = blocs[i].replace("{", "").replace("}", "");
        }

        String[] firstBloc = blocs[0].split(",");
        this.setMapping(firstBloc.length > 0 ? firstBloc[0].trim() : "");
        this.setNomTable(firstBloc.length > 1 ? firstBloc[1].trim() : "");


        this.setRetour(blocs[1].replace(",", ";"));
        this.setRetourMapping(blocs[2].replace(",", ";"));

        declaration = String.format(
                "Champ.setPageAppelComplete(pi.getFormufle().getChampFille(\"%s\"),\"%s\",\"id\",\"%s\",\"%s\",\"%s\")",
                this.getNom(),this.getMapping(), this.getNomTable(), this.getRetour(), this.getRetourMapping()
        );
        if (blocs.length == 3) {
            this.setInsert(false);
        }
        else if (blocs.length == 4) {
            String[] pageBloc = blocs[3].split(",");
            this.setPageInsert(pageBloc.length > 0 ? pageBloc[0].trim() : "");
            this.setVal(pageBloc.length > 1 ? pageBloc[1].trim() : "");
            this.setInsert(true);

            declaration += String.format(
                    ";{{newline}}{{tab}}Champ.setPageAppelInsert(pi.getFormufle().getChampFille(\"%s\"),\"%s\",\"id;%s\")",
                    this.getNom(),this.getPageInsert(), this.getVal()
            );
        }
    }

}

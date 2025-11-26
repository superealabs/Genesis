package org.labs.genesis.apj.filetype.pages;

import lombok.Getter;
import lombok.Setter;
import org.labs.genesis.apj.filetype.ApjFile;

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
}

package org.labs.genesis.apj;

import affichage.PageInsert;
import lombok.Getter;
import lombok.Setter;
import org.labs.genesis.apj.filetype.mapping.Mapping;
import org.labs.genesis.apj.filetype.mapping.MappingMereFille;
import org.labs.genesis.apj.filetype.pages.PageConsulte;
import org.labs.genesis.apj.filetype.pages.PageInsertMultiple;
import org.labs.genesis.apj.filetype.pages.PageRecherche;

import java.sql.Connection;

@Getter
@Setter
public class ApjGenerationContext {
    public static final String PAGE_RECHERCHE = "PageRecherche";
    public static final String PAGE_INSERT = "PageInsert";
    public static final String PAGE_INSERT_MULTIPLE = "PageInsertMultiple";
    public static final String PAGE_CONSULTE = "PageConsulte";
    public static final String MAPPING = "Mapping";
    public static final String MAPPING_MERE_FILLE = "MappingMereFille";

    private String libDir;
    private String projectJarDir;
    private String locationDir;
    private String apjType;
    private PageRecherche pr;
    private PageConsulte pc;
    private PageInsert pi;
    private PageInsertMultiple pim;
    private Mapping mapping;
    private MappingMereFille mappingMereFille;
    private Connection connection;
}

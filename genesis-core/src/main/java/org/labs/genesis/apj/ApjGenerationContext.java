package org.labs.genesis.apj;

import lombok.Getter;
import lombok.Setter;
import org.labs.genesis.apj.filetype.ApjFile;

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
    private ApjFile apjfile;
    private Connection connection;
}

package org.labs.genesis.config;

import lombok.Getter;
import lombok.Setter;
import org.labs.genesis.config.langage.*;
import org.labs.genesis.connexion.Credentials;
import org.labs.genesis.connexion.Database;
import org.labs.genesis.connexion.model.RelationParameter;
import org.labs.genesis.connexion.model.TableMetadata;
import org.labs.genesis.frontend.FrontendLanguage;
import org.labs.genesis.frontend.generator.FrontendFramework;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

@Getter
@Setter
public class ProjectGenerationContext {

    public static final String COMPONENT_MODEL = "Model";
    public static final String COMPONENT_DAO = "DAO";
    public static final String COMPONENT_SERVICE = "Service";
    public static final String COMPONENT_CONTROLLER = "Controller";
    public String frontendPort;
    private Database database;
    private Language language;
    private Framework framework;
    private Project project;
    private Credentials credentials;
    private String destinationFolder;
    private String projectName;
    private String groupLink;
    private String projectPort;
    private String projectDescription;
    private Map<String, Object> languageConfiguration;
    private Map<String, Object> frameworkConfiguration;
    private List<String> entityNames;
    private List<String> viewNames;
    private List<TableMetadata> entityTables;
    private List<TableMetadata> viewTables;
    private List<RelationParameter> relationParameters;
    private Connection connection;
    private List<String> generationOptions;
    private boolean generateProjectStructure = true;
    // Frontend Generation
    private  boolean generateFrontendApp = true;
    private FrontendFramework frontendFramework;
    private FrontendLanguage frontendLanguage;
    private String webappFolder = "webapp";
    // FrameworkMVC specific configurations
    private ViewsTemplate viewsTemplate;

    public ProjectGenerationContext setDatabase(Database database) {
        this.database = database;
        return this;
    }

    public ProjectGenerationContext setLanguage(Language language) {
        this.language = language;
        return this;
    }

    public ProjectGenerationContext setFramework(Framework framework) {
        this.framework = framework;
        return this;
    }

    public ProjectGenerationContext setProject(Project project) {
        this.project = project;
        return this;
    }

    public ProjectGenerationContext setCredentials(Credentials credentials) {
        this.credentials = credentials;
        return this;
    }

    public ProjectGenerationContext setDestinationFolder(String destinationFolder) {
        this.destinationFolder = destinationFolder;
        return this;
    }

    public ProjectGenerationContext setProjectName(String projectName) {
        this.projectName = projectName;
        return this;
    }

    public ProjectGenerationContext setGroupLink(String groupLink) {
        this.groupLink = groupLink;
        return this;
    }

    public ProjectGenerationContext setConnection(Connection connection) {
        this.connection = connection;
        return this;
    }

    public void setEntityTables(Connection connection) throws SQLException, ClassNotFoundException {

        this.entityTables = database.getEntitiesByNames(this.getEntityNames(), connection, credentials, language, framework);
    }
    public void setViewTables(Connection connection) throws SQLException, ClassNotFoundException {
        this.viewTables = database.getViewsByNames(this.getViewNames(), connection, credentials, language, framework);
    }
    public  void setTables(){
        try {
            setEntityTables(connection);
            setViewTables(connection);
        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
    public Dictionary<String, List<TableMetadata>> splitTableByRelations() {
        Dictionary<String, List<TableMetadata>> splitTables = new java.util.Hashtable<>();
        List<TableMetadata> childTables = new java.util.ArrayList<>();
        Set<TableMetadata> parentTablesSet = new HashSet<>();

        for (TableMetadata table : this.entityTables) {
            if (table.getHasFk()) {
                childTables.add(table);
            }
        }

        // 4. Mettre les résultats dans le dictionnaire
        // Les "PARENTS" sont toutes les tables référencées.
        splitTables.put("PARENTS", this.entityTables);
        // Les "CHILDS" sont toutes les tables qui ont au moins une FK.
        splitTables.put("CHILDS", childTables);

        return splitTables;
    }

    private TableMetadata findTableByName(String tableName, List<TableMetadata> tables) {
        for (TableMetadata table : tables) {
            if (table.getTableName().equalsIgnoreCase(tableName)) {
                return table;
            }
        }
        return null;
    }
    public List<TableMetadata> getAllTables(){
        List<TableMetadata> allTables = new ArrayList<>();
        if(this.entityTables != null){
            allTables.addAll(this.entityTables);
        }
        if(this.viewTables != null){
            allTables.addAll(this.viewTables);
        }
        return allTables;
    }

    public String getDestinationFolder() {
        return destinationFolder+"/"+projectName;
    }
}
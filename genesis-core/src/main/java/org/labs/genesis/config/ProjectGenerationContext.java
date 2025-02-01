package org.labs.genesis.config;

import lombok.Setter;
import org.labs.genesis.config.langage.Framework;
import org.labs.genesis.config.langage.Language;
import org.labs.genesis.config.langage.Project;
import org.labs.genesis.connexion.Credentials;
import org.labs.genesis.connexion.Database;
import lombok.Getter;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class ProjectGenerationContext {

    public static final String COMPONENT_MODEL = "Model";
    public static final String COMPONENT_DAO = "DAO";
    public static final String COMPONENT_SERVICE = "Service";
    public static final String COMPONENT_CONTROLLER = "Controller";

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
    private Connection connection;
    private List<String> generationOptions;
    private boolean generateProjectStructure = true;

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

}
package org.labs.genesis.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.labs.genesis.config.langage.*;
import org.labs.genesis.config.langage.generator.indicator.IntellijGenerationProcess;
import org.labs.genesis.config.langage.generator.project.ProjectGenerator;
import org.labs.genesis.config.langage.generator.sync.report.FrontendChangeReport;
import org.labs.genesis.config.langage.generator.sync.report.WebApiChangeReport;
import org.labs.genesis.connexion.Credentials;
import org.labs.genesis.connexion.Database;
import org.labs.genesis.connexion.model.RelationParameter;
import org.labs.genesis.connexion.model.TableMetadata;
import org.labs.genesis.frontend.FrontendLanguage;
import org.labs.genesis.frontend.generator.FrontendFramework;
import org.labs.utils.StringUtils;

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
    public static  final String FRONTEND_COMPONENT_MODEL = "Model";
    public static  final String FRONTEND_COMPONENT_SERVICE = "Service";
    public static  final String FRONTEND_COMPONENT = "Components";
    public IntellijGenerationProcess generationProcess;
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
    private List<String> frontendGenerationOptions;
    private boolean generateProjectStructure = true;
    // Frontend Generation
    private  boolean generateFrontendApp = true;
    private  boolean generateFrontendSkeletons = true;
    private  boolean generateFrontendStructure = true;
    private FrontendFramework frontendFramework;
    private FrontendLanguage frontendLanguage;
    private String webappFolder = "webapp";
    // FrameworkMVC specific configurations
    private  boolean generateViewsTemplates = true;
    private ViewsTemplate viewsTemplate;

    public ProjectGenerationContext(){
        this.generationProcess = new IntellijGenerationProcess();
        this.frontendGenerationOptions = getAllFrontendGenerationOptions();
    }

    public ProjectGenerationContext duplicateWithNoTables(){
        ProjectGenerationContext copy = new ProjectGenerationContext();
        copy.setFrontendPort(this.frontendPort);
        copy.setDatabase(this.database);
        copy.setLanguage(this.language);
        copy.setFramework(this.framework);
        copy.setProject(this.project);
        copy.setCredentials(this.credentials);
        copy.setDestinationFolder(this.destinationFolder);
        copy.setProjectName(this.projectName);
        copy.setGroupLink(this.groupLink);
        copy.setProjectPort(this.projectPort);
        copy.setProjectDescription(this.projectDescription);
        copy.setFrameworkConfiguration(this.frameworkConfiguration);
        copy.setLanguageConfiguration(this.languageConfiguration);
        copy.setEntityNames(new ArrayList<>());
        copy.setViewNames(new ArrayList<>());
        copy.setRelationParameters(new ArrayList<>(this.getRelationParameters()));
        copy.setGenerationOptions(new ArrayList<>(this.getGenerationOptions()));
        copy.setFrontendGenerationOptions(new ArrayList<>(this.getFrontendGenerationOptions()));
        copy.setGenerationProcess(this.generationProcess);
        copy.setGenerateProjectStructure(this.generateProjectStructure);
        copy.setGenerateFrontendApp(this.generateFrontendApp);
        copy.setFrontendFramework(this.frontendFramework);
        copy.setFrontendLanguage(this.frontendLanguage);
        copy.setConnection(this.connection);
        copy.setViewsTemplate(this.viewsTemplate);
        return copy;
    }

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

    @JsonIgnore
    public void setEntityTables(Connection connection) throws SQLException, ClassNotFoundException {
        this.entityTables = database.getEntitiesByNames(this.getEntityNames(), connection, credentials, language, framework);
    }
    public void setEntityTables(List<TableMetadata> tables){
        this.entityTables = tables;
    }
    public void setViewTables(List<TableMetadata> views){
        this.viewTables = views;
    }

    @JsonIgnore
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

    public void setEntityNamesFromTables(List<TableMetadata> entityTables) {
        List<String> entityNames = new ArrayList<>();
        for (TableMetadata table : entityTables) {
            entityNames.add(table.getTableName());
        }
        setEntityNames(entityNames);
    }

    public void setViewNamesFromTables(List<TableMetadata> viewTables) {
        List<String> viewNames = new ArrayList<>();
        for (TableMetadata table : viewTables) {
            viewNames.add(table.getTableName());
        }
        setViewNames(viewNames);
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

        splitTables.put("PARENTS", this.entityTables);
        splitTables.put("CHILDS", childTables);
        return splitTables;
    }

    public void applyWebApiChangeReport(WebApiChangeReport webApiChangeReport){
        if (this.getFramework() == null){
            return;
        }
        resetFramework();
        if (this.getFramework().getModel() != null){
            this.getFramework().getModel().setToGenerate(webApiChangeReport.getGenerateModel());
        }
        if (this.getFramework().getModelDao() != null){
            this.getFramework().getModelDao().setToGenerate(webApiChangeReport.getGenerateDAO());
        }
        if (this.getFramework().getService() != null){
            this.getFramework().getService().setToGenerate(webApiChangeReport.getGenerateService());
        }
        if (this.getFramework().getController() != null){
            this.getFramework().getController().setToGenerate(webApiChangeReport.getGenerateController());
        }

        if (webApiChangeReport.generateAdditionalFiles){
            setGenerateProjectStructure(true);
        }
    }

    public void applyFrontendChangeReport(FrontendChangeReport frontendChangeReport){
        resetFrontendGenerationOptions();
        if (!frontendChangeReport.generateModel && this.getFrontendGenerationOptions().contains(FRONTEND_COMPONENT_MODEL)){
            this.getFrontendGenerationOptions().add(FRONTEND_COMPONENT_MODEL);
        }

        if (!frontendChangeReport.generateComponents && this.getFrontendGenerationOptions().contains(FRONTEND_COMPONENT)){
            this.getFrontendGenerationOptions().add(FRONTEND_COMPONENT);
        }

        if (!frontendChangeReport.generateService && this.getFrontendGenerationOptions().contains(FRONTEND_COMPONENT_SERVICE)){
            this.getFrontendGenerationOptions().add(FRONTEND_COMPONENT_SERVICE);
        }

        if (frontendChangeReport.generateAdditionalFiles){
            setGenerateFrontendStructure(true);
        }
    }

    public void resetFramework(){
        Framework framework = ProjectGenerator.findFrameworkById(this.getFramework().getId());
        this.setFramework(framework);
    }

    public void resetFrontendGenerationOptions(){
        this.setFrontendGenerationOptions(getAllFrontendGenerationOptions());
    }

    public List<String> getAllFrontendGenerationOptions(){
        List<String> allOptions = new ArrayList<>();
        allOptions.add(FRONTEND_COMPONENT);
        allOptions.add(FRONTEND_COMPONENT_MODEL);
        allOptions.add(FRONTEND_COMPONENT_SERVICE);
        return allOptions;
    }

    public TableMetadata findTableByName(String tableName, List<TableMetadata> tables) {
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
        return destinationFolder+"/"+ StringUtils.toPascalCase(projectName);
    }

    public void applyTableRelations(){
        if (getRelationParameters() == null ){
            return;
        }
        for (RelationParameter relationParameter : this.getRelationParameters()) {
            relationParameter.setParameter(this);
        }
    }
}
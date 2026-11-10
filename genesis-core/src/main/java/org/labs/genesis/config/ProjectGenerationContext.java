package org.labs.genesis.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.labs.genesis.config.form.FieldConfiguration;
import org.labs.genesis.config.tools.DockerConfiguration;
import org.labs.genesis.config.tools.GitConfiguration;
import org.labs.genesis.config.langage.*;
import org.labs.genesis.config.langage.generator.indicator.IntellijGenerationProcess;
import org.labs.genesis.config.langage.generator.project.ProjectGenerator;
import org.labs.genesis.config.langage.generator.sync.report.FrontendChangeReport;
import org.labs.genesis.config.langage.generator.sync.report.WebApiChangeReport;
import org.labs.genesis.connexion.Credentials;
import org.labs.genesis.connexion.Database;
import org.labs.genesis.connexion.SQLRunner;
import org.labs.genesis.connexion.model.ColumnMetadata;
import org.labs.genesis.connexion.model.RelationParameter;
import org.labs.genesis.connexion.model.TableMetadata;
import org.labs.genesis.exceptions.InvalipRelationParameter;
import org.labs.genesis.frontend.FrontendLanguage;
import org.labs.genesis.frontend.generator.FrontendFramework;
import org.labs.utils.StringUtils;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Getter
@Setter
@ToString
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
    private String sqlScript;
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
    private GitConfiguration gitConfiguration;
    private Map<String, Map<String, FieldConfiguration>> formConfiguration;
    private DockerConfiguration dockerConfiguration;

    public ProjectGenerationContext() {
        this.generationProcess = new IntellijGenerationProcess();
        this.frontendGenerationOptions = getAllFrontendGenerationOptions();
    }

    public ProjectGenerationContext duplicateWithNoTables() {
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
        copy.setRelationParameters(this.relationParameters == null ? new ArrayList<>() : new ArrayList<>(this.relationParameters));
        copy.setGenerationOptions(this.generationOptions == null ? new ArrayList<>() : new ArrayList<>(this.generationOptions));
        copy.setFrontendGenerationOptions(this.frontendGenerationOptions == null ? new ArrayList<>() : new ArrayList<>(this.frontendGenerationOptions));
        copy.setGenerationProcess(this.generationProcess);
        copy.setGenerateProjectStructure(this.generateProjectStructure);
        copy.setGenerateFrontendApp(this.generateFrontendApp);
        copy.setFrontendFramework(this.frontendFramework);
        copy.setFrontendLanguage(this.frontendLanguage);
        copy.setConnection(this.connection);
        copy.setSqlScript(this.sqlScript);
        copy.setViewsTemplate(this.viewsTemplate);
        copy.setFormConfiguration(this.formConfiguration);
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
    public void setTables() {
        if (connection != null) {
            try {
                setEntityTables(connection);
                setViewTables(connection);
            } catch (SQLException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        } else if (sqlScript != null && !sqlScript.isBlank()) {
            this.entityTables = buildTablesFromScript(sqlScript, entityNames);
            this.viewTables = buildViewsFromScript(sqlScript, viewNames);
        }
    }

    private List<TableMetadata> buildTablesFromScript(String sql, List<String> selectedNames) {
        List<TableMetadata> tables = new ArrayList<>();
        if (sql == null || sql.isBlank()) return tables;

        String clean = SQLRunner.removeComments(sql);

        for (String query : clean.split(";")) {
            String trimmed = query.trim();
            String upper = trimmed.toUpperCase();
            if (!upper.contains("CREATE TABLE")) continue;

            int idx = upper.indexOf("TABLE");
            String afterTable = trimmed.substring(idx + 5).trim();
            afterTable = afterTable.replaceFirst("(?i)^IF\\s+NOT\\s+EXISTS\\s+", "").trim();

            int openParen = afterTable.indexOf('(');
            if (openParen == -1) continue;

            int depth = 0;
            int closeParen = -1;
            boolean inSingle = false;
            boolean inDouble = false;
            for (int i = openParen; i < afterTable.length(); i++) {
                char c = afterTable.charAt(i);
                if (c == '\'' && !inDouble) {
                    if (inSingle && i + 1 < afterTable.length() && afterTable.charAt(i + 1) == '\'') {
                        i++;
                        continue;
                    }
                    inSingle = !inSingle;
                } else if (c == '"' && !inSingle) {
                    inDouble = !inDouble;
                } else if (!inSingle && !inDouble) {
                    if (c == '(') {
                        depth++;
                    } else if (c == ')') {
                        depth--;
                        if (depth == 0) {
                            closeParen = i;
                            break;
                        }
                    }
                }
            }
            if (closeParen <= openParen) continue;

            String tableName = afterTable.substring(0, openParen).replaceAll("[\"``\\[\\]]", "").trim();
            if (tableName.contains(".")) tableName = tableName.substring(tableName.lastIndexOf('.') + 1);
            if (tableName.isBlank()) continue;
            if (selectedNames != null && !selectedNames.isEmpty() && !selectedNames.contains(tableName)) continue;

            String body = afterTable.substring(openParen + 1, closeParen);

            TableMetadata tm = new TableMetadata();
            tm.setTableName(tableName);
            tm.initializeFromScript(body, database, language);
            tables.add(tm);
        }

        // Support ALTER TABLE ... ADD CONSTRAINT ... FOREIGN KEY ... REFERENCES ...
        for (String query : clean.split(";")) {
            String trimmed = query.trim();
            String upper = trimmed.toUpperCase();
            if (upper.contains("ALTER TABLE") && upper.contains("FOREIGN KEY") && upper.contains("REFERENCES")) {
                Pattern alterFkPat = Pattern.compile(
                        "ALTER\\s+TABLE\\s+(?:ONLY\\s+)?([\\w.`\"\\[\\]]+)\\s+ADD\\s+(?:CONSTRAINT\\s+[\\w.`\"\\[\\]]+\\s+)?FOREIGN\\s+KEY\\s*\\(([^)]+)\\)\\s*REFERENCES\\s+([\\w.`\"\\[\\]]+)\\s*\\(([^)]+)\\)",
                        Pattern.CASE_INSENSITIVE);
                Matcher am = alterFkPat.matcher(trimmed);
                if (am.find()) {
                    String srcTable = am.group(1).replaceAll("[\"``\\[\\]]", "").trim();
                    if (srcTable.contains(".")) srcTable = srcTable.substring(srcTable.lastIndexOf('.') + 1);
                    String fkColName = am.group(2).replaceAll("[\"``\\[\\]]", "").trim();
                    String refTable = am.group(3).replaceAll("[\"``\\[\\]]", "").trim();
                    if (refTable.contains(".")) refTable = refTable.substring(refTable.lastIndexOf('.') + 1);
                    String refCol = am.group(4).replaceAll("[\"``\\[\\]]", "").trim();

                    final String finalSrcTable = srcTable;
                    final String finalRefTable = refTable;
                    final String finalRefCol = refCol;
                    tables.stream().filter(t -> t.getTableName().equalsIgnoreCase(finalSrcTable)).findFirst().ifPresent(t -> {
                        if (t.getColumns() != null) {
                            for (ColumnMetadata col : t.getColumns()) {
                                if (col.getReferencedColumn().equalsIgnoreCase(fkColName)) {
                                    col.setForeign(true);
                                    col.setReferencedTable(finalRefTable);
                                    col.setReferencedPrimaryKeyColumn(StringUtils.toCamelCase(finalRefCol.toLowerCase()));
                                    t.setHasFk(true);
                                }
                            }
                        }
                    });
                }
            }
        }

        for (TableMetadata tm : tables) {
            if (tm.getColumns() == null) continue;
            for (ColumnMetadata col : tm.getColumns()) {
                if (col.isForeign() && col.getReferencedTable() != null) {
                    tm.setHasFk(true);
                    String refTable = col.getReferencedTable();
                    col.setType(Stream.of(refTable).map(String::toLowerCase).map(StringUtils::toCamelCase).map(StringUtils::removeLastS).map(StringUtils::majStart).findFirst().orElse(refTable));
                    col.setName(StringUtils.toCamelCase(col.getReferencedColumn().toLowerCase()) + StringUtils.majStart(StringUtils.toCamelCase(refTable.toLowerCase())));
                    col.setReferencedTable(StringUtils.toCamelCase(refTable));

                    // Resolve referencedColumnType & referencedPrimaryKeyColumn
                    TableMetadata targetTable = tables.stream()
                            .filter(t -> t.getTableName().equalsIgnoreCase(refTable))
                            .findFirst()
                            .orElse(null);

                    if (targetTable != null && targetTable.getColumns() != null) {
                        String targetColName = col.getReferencedPrimaryKeyColumn();
                        ColumnMetadata targetCol = null;
                        if (targetColName != null) {
                            targetCol = Arrays.stream(targetTable.getColumns())
                                    .filter(c -> c.getName().equalsIgnoreCase(targetColName) || c.getReferencedColumn().equalsIgnoreCase(targetColName))
                                    .findFirst()
                                    .orElse(null);
                        }
                        if (targetCol == null && targetTable.getPrimaryColumn() != null) {
                            targetCol = targetTable.getPrimaryColumn();
                        }
                        if (targetCol != null) {
                            col.setReferencedColumnType(targetCol.getType());
                            col.setColumnType(targetCol.getColumnType());
                            if (col.getReferencedPrimaryKeyColumn() == null) {
                                col.setReferencedPrimaryKeyColumn(targetCol.getName());
                            }
                        }
                    }
                }
            }
        }

        return tables;
    }

    private List<TableMetadata> buildViewsFromScript(String sql, List<String> selectedNames) {
        List<TableMetadata> views = new ArrayList<>();
        List<String> viewNames = SQLRunner.extractViewNames(sql);
        for (String viewName : viewNames) {
            if (selectedNames != null && !selectedNames.isEmpty() && !selectedNames.contains(viewName)) continue;
            TableMetadata vm = new TableMetadata();
            vm.setTableName(viewName);
            vm.setDatabase(database);
            vm.setIsView(true);
            vm.setClassName(Stream.of(viewName).map(String::toLowerCase).map(StringUtils::toCamelCase).map(StringUtils::majStart).map(StringUtils::removeLastS).findFirst().orElse(viewName));
            ColumnMetadata idCol = new ColumnMetadata();
            idCol.setReferencedColumn("id");
            idCol.setName("id");
            idCol.setType("String");
            idCol.setColumnType("VARCHAR");
            idCol.setDatabaseColumnType("VARCHAR");
            vm.setColumns(new ColumnMetadata[]{idCol});
            vm.setPKForView();
            views.add(vm);
        }
        return views;
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

    public List<RelationParameter> autoDetectRelationParameters() {
        if (this.relationParameters == null) {
            this.relationParameters = new ArrayList<>();
        }
        if (this.entityTables == null || this.entityTables.isEmpty()) {
            return this.relationParameters;
        }

        for (TableMetadata childTable : this.entityTables) {
            if (childTable == null || childTable.getColumns() == null) continue;
            for (ColumnMetadata col : childTable.getColumns()) {
                if (col != null && col.isForeign() && col.getReferencedTable() != null) {
                    String refTableName = col.getReferencedTable();
                    TableMetadata parentTable = findTableByName(refTableName, this.entityTables);
                    if (parentTable != null && !parentTable.equals(childTable)) {
                        boolean isMandatory = !col.isNullable();
                        boolean hasForm = false;
                        RelationParameter autoRelation = new RelationParameter(
                                parentTable.getTableName(),
                                childTable.getTableName(),
                                isMandatory,
                                hasForm
                        );
                        if (!this.relationParameters.contains(autoRelation)) {
                            this.relationParameters.add(autoRelation);
                        }
                    }
                }
            }
        }
        return this.relationParameters;
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
        List<RelationParameter> invalids = new ArrayList<>();
        for (RelationParameter relationParameter : this.getRelationParameters()) {
            try {
                relationParameter.setParameter(this);
            } catch (InvalipRelationParameter ex){
                invalids.add(relationParameter);
            }
        }

        for (RelationParameter relationParameter : invalids){
            this.getRelationParameters().remove(relationParameter);
        }
    }
}
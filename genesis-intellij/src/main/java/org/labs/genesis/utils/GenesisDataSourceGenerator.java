package org.labs.genesis.utils;

import org.labs.genesis.config.ProjectGenerationContext;
import org.labs.genesis.config.ide.DriverConfig;
import org.labs.genesis.config.ide.IdeConfiguration;
import org.labs.genesis.config.ide.IdeConfigurationLoader;
import org.labs.genesis.connexion.Credentials;
import org.labs.genesis.connexion.Database;
import org.labs.genesis.engine.GenesisTemplateEngine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GenesisDataSourceGenerator {

    private static final GenesisTemplateEngine engine = new GenesisTemplateEngine();

    public static void generateDataSourcesXml(Path targetProjectFolder, ProjectGenerationContext context) {
        if (targetProjectFolder == null || context == null) {
            return;
        }

        Database database = context.getDatabase();
        Credentials credentials = context.getCredentials();
        if (database == null || credentials == null) {
            return;
        }

        IdeConfiguration ideConfig = IdeConfigurationLoader.getIdeConfiguration();
        if (ideConfig == null || ideConfig.getDataSourceTemplate() == null) {
            return;
        }

        String dbName = database.getName();
        DriverConfig driverConfig = IdeConfigurationLoader.findDriverConfig(ideConfig, dbName);

        String host = credentials.getHost() != null && !credentials.getHost().isEmpty() ? credentials.getHost() : "localhost";
        String defaultPort = database.getPort() != null && !database.getPort().isEmpty() ? database.getPort() : "5432";
        String port = credentials.getPort() != null && !credentials.getPort().isEmpty() ? credentials.getPort() : defaultPort;
        String databaseName = credentials.getDatabaseName() != null ? credentials.getDatabaseName() : "";
        String username = credentials.getUser() != null ? credentials.getUser() : "";
        String sid = credentials.getSID() != null ? credentials.getSID() : "xe";

        Map<String, Object> urlVars = new HashMap<>();
        urlVars.put("host", host);
        urlVars.put("port", port);
        urlVars.put("databaseName", databaseName);
        urlVars.put("sid", sid);

        String jdbcUrl = engine.simpleRender(driverConfig.getUrlPattern(), urlVars);
        String projectName = context.getProjectName() != null ? context.getProjectName() : "Genesis";

        Map<String, Object> templateVars = new HashMap<>();
        templateVars.put("projectName", projectName);
        templateVars.put("dbName", dbName != null ? dbName : "Database");
        templateVars.put("databaseName", !databaseName.isEmpty() ? databaseName : (dbName != null ? dbName : "Database"));
        templateVars.put("host", host);
        templateVars.put("uuid", UUID.randomUUID().toString());
        templateVars.put("driverRef", driverConfig.getDriverRef());
        templateVars.put("jdbcDriver", driverConfig.getJdbcDriver());
        templateVars.put("jdbcUrl", jdbcUrl);
        templateVars.put("username", username);

        String xmlContent = engine.simpleRender(ideConfig.getDataSourceTemplate().getContent(), templateVars);

        try {
            Path ideaFolder = targetProjectFolder.resolve(".idea");
            if (!Files.exists(ideaFolder)) {
                Files.createDirectories(ideaFolder);
            }
            Path dataSourcesFile = ideaFolder.resolve("dataSources.xml");
            Files.writeString(dataSourcesFile, xmlContent);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

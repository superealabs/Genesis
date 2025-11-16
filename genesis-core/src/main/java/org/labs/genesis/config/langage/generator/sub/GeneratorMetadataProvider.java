package org.labs.genesis.config.langage.generator.sub;

import org.labs.genesis.config.ProjectGenerationContext;
import org.labs.genesis.config.langage.generator.framework.FrameworkMetadataProvider;
import org.labs.genesis.config.langage.generator.project.ProjectMetadataProvider;
import org.labs.genesis.connexion.Credentials;
import org.labs.genesis.connexion.Database;
import org.labs.genesis.connexion.model.TableMetadata;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.labs.genesis.config.langage.generator.framework.FrameworkMetadataProvider.getHashMapDaoGlobal;
import static org.labs.genesis.config.langage.generator.project.ProjectMetadataProvider.getProjectFilesEditsHashMap;

public class GeneratorMetadataProvider {
    public  static Map<String, Object> getGenesisFileMetadata(ProjectGenerationContext context) throws Exception {
        Map<String, Object> initializeHashMap = getProjectFilesEditsHashMap(
                context.getDestinationFolder(),
                context.getProjectName(),
                context.getGroupLink(),
                context.getProjectPort(),
                context.getDatabase(),
                context.getCredentials(),
                context.getLanguage(),
                context.getProjectDescription(),
                context.getLanguageConfiguration(),
                context.getFramework(),
                context.getFrameworkConfiguration()
        );
        Map<String, Object> metadata = new HashMap<>(initializeHashMap);
        List<TableMetadata> entities = context.getEntityTables();
        if (context.getFramework().getUseDB()) {
            if (context.getFramework().getModelDao() != null) {
                var mapDaoGlobal = getHashMapDaoGlobal(context.getFramework(), entities, context.getProjectName());
                metadata.putAll(mapDaoGlobal);
            } else {
                // For frameworks without ModelDao (like Django), still generate entitiesImports and entitiesAll
                var entitiesMetadata = FrameworkMetadataProvider.generateEntitiesImportsAndAll(entities);
                metadata.putAll(entitiesMetadata);
                metadata.put("entities", context.getEntityNames());
//                metadata.put("allEntities", FrameworkMetadataProvider(entities));
            }
        }
//        metadata.putAll(configFileHashMap);

        HashMap<String, Object> dependenciesFilesHashMap = ProjectMetadataProvider.getDependencyFileHashMap(
                context.getProjectDescription(),
                context.getDatabase(),
                context.getLanguage(),
                context.getFramework(),
                context.getLanguageConfiguration(),
                context.getFrameworkConfiguration()
        );
        metadata.putAll(dependenciesFilesHashMap);

        return metadata;
    }
    public static Map<String, Object> getDatabaseMetadata(ProjectGenerationContext context){
        Database database = context.getDatabase();
        Credentials credentials = database.getCredentials();
        Map<String, Object> map = new HashMap<>();
        map.put("bdd_host", credentials.getHost());
        map.put("bdd_port", credentials.getPort());
        map.put("database", credentials.getDatabaseName());
        map.put("bdd_schema", credentials.getSchemaName());
        map.put("bdd_useSSL", credentials.isUseSSL());
        map.put("bdd_username", credentials.getUser());
        map.put("bdd_password", credentials.getPwd());
        map.put("bdd_driverType", credentials.getDriverType());
        map.put("bdd_sid", database.getSid());
        return map;
    }
}

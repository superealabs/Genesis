package org.labs.genesis.config.langage.generator.sub;

import org.labs.genesis.config.ProjectGenerationContext;
import org.labs.genesis.config.langage.Framework;
import org.labs.genesis.config.langage.Language;
import org.labs.genesis.connexion.Credentials;
import org.labs.genesis.connexion.Database;

import java.util.HashMap;
import java.util.Map;

public class GenerationContextMetadataProvider {
        public  static Map<String, Object> getGenesisFileMetadata(ProjectGenerationContext context){
            Map<String , Object> metadata = new HashMap<>();
            metadata.putAll(getProjectMetadata(context));
            metadata.putAll(getDatabaseMetadata(context));
            return metadata;
        }
        public  static Map<String, Object> getProjectMetadata(ProjectGenerationContext context){
            Map<String , Object> metadata = new HashMap<>();
            metadata.put("projectName", context.getProjectName());
            metadata.put("projectDescription", context.getProjectDescription());
            return metadata;
        }
        public  static Map<String, Object> getBackendMetadata(ProjectGenerationContext context){
            Framework framework = context.getFramework();
            Language language = context.getLanguage();


            Map<String , Object> metadata = new HashMap<>();
            metadata.put("backend.languageId", language.getId());
            metadata.put("backend.frameworkId", framework.getId());
            metadata.put("backend.groupLink", context.getGroupLink());
            context.isGenerateProjectStructure();
            return metadata;
        }
        public static Map<String, Object> getDatabaseMetadata(ProjectGenerationContext context) {
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

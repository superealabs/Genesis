package org.labs.genesis.config.langage.generator.framework;

import org.jetbrains.annotations.NotNull;
import org.labs.genesis.config.langage.Framework;
import org.labs.genesis.config.langage.FrameworkCaching;
import org.labs.genesis.config.langage.Language;
import org.labs.genesis.connexion.Credentials;
import org.labs.genesis.connexion.Database;
import org.labs.genesis.connexion.model.ColumnMetadata;
import org.labs.genesis.connexion.model.TableMetadata;
import org.labs.genesis.engine.GenesisTemplateEngine;
import org.labs.utils.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

public class FrameworkMetadataProvider {
    private static final GenesisTemplateEngine engine = new GenesisTemplateEngine();

    public static @NotNull Map<String, Object> getCredentialsHashMap(Database database) {
        Credentials credentials = database.getCredentials();
        System.out.println("=== DEBUG getCredentialsHashMap ===");
        System.out.println("host=" + credentials.getHost());
        System.out.println("port=" + credentials.getPort());
        System.out.println("database=" + credentials.getDatabaseName());
        System.out.println("schema=" + credentials.getSchemaName());
        System.out.println("useSSL=" + credentials.isUseSSL());
        System.out.println("username=" + credentials.getUser());
        System.out.println("password=" + credentials.getPwd());
        System.out.println("driverType=" + credentials.getDriverType());
        System.out.println("sid=" + database.getSid());
        return new HashMap<>(
                Map.of("host", credentials.getHost(),
                        "port", credentials.getPort(),
                        "database", credentials.getDatabaseName(),
                        "schema", credentials.getSchemaName(),
                        "useSSL", credentials.isUseSSL(),
                        "username", credentials.getUser(),
                        "password", credentials.getPwd(),
                        "driverType", Objects.toString(credentials.getDriverType(), ""),
                        "sid",Objects.toString(database.getSid(), "")
                )
        );
    }

    public static HashMap<String, Object> getRelatedLanguageMetadata(Language language) {
        HashMap<String, Object> metadata = new HashMap<>();

        metadata.put("namespace", language.getSyntax().get("namespace"));
        metadata.put("bracketEnd", language.getSyntax().get("bracketEnd"));
        metadata.put("classKeyword", language.getSyntax().get("classKeyword"));
        metadata.put("bracketStart", language.getSyntax().get("bracketStart"));
        metadata.put("namespaceEnd", language.getSyntax().get("namespaceEnd"));
        metadata.put("namespaceStart", language.getSyntax().get("namespaceStart"));

        return metadata;
    }

    public static HashMap<String, Object> getPrimaryModelHashMap(Framework framework, Language language, TableMetadata tableMetadata) {
        HashMap<String, Object> metadata = new HashMap<>();

        metadata.put("className", tableMetadata.getClassName());
        metadata.put("entityName", tableMetadata.getClassName());
        metadata.put("package", framework.getModel().getModelPackage());
        metadata.put("imports", framework.getModel().getModelImports());
        metadata.put("fields", framework.getModel().getModelFieldContent());
        metadata.put("methods", framework.getModel().getModelGetterSetter());
        metadata.put("constructors", framework.getModel().getModelConstructors());
        metadata.put("classAnnotations", framework.getModel().getModelAnnotations());
        metadata.put("namespaceEnd", language.getSyntax().get("namespaceEnd") == null ? "{{removeLine}}" : language.getSyntax().get("namespaceEnd"));
        metadata.put("bracketEnd", language.getSyntax().get("bracketEnd") == null ? "{{removeLine}}" : language.getSyntax().get("namespaceEnd"));

        return metadata;
    }

    public static HashMap<String, Object> getPrimaryModelDaoHashMap(Framework framework, TableMetadata tableMetadata) {
        HashMap<String, Object> metadata = new HashMap<>();

        metadata.put("className", tableMetadata.getClassName());
        metadata.put("entityName", framework.getModelDao().getModelDaoName());
        metadata.put("package", framework.getModelDao().getModelDaoPackage());
        metadata.put("imports", framework.getModelDao().getModelDaoImports());
        metadata.put("extends", framework.getModelDao().getModelDaoExtends());

        if (tableMetadata.getPrimaryColumn() != null) {
            metadata.put("pkColumnType", tableMetadata.getPrimaryColumn().getType());
        } else {
            metadata.put("pkColumnType", "");
        }

        metadata.put("fields", framework.getModelDao().getModelDaoFieldContent());
        metadata.put("methods", framework.getModelDao().getModelDaoMethodContent());
        metadata.put("constructors", framework.getModelDao().getModelDaoConstructors());
        metadata.put("classAnnotations", framework.getModelDao().getModelDaoAnnotations());

        return metadata;
    }


    public static HashMap<String, Object> getPrimaryServiceHashMap(Framework framework, TableMetadata tableMetadata) {
        HashMap<String, Object> metadata = new HashMap<>();

        metadata.put("className", tableMetadata.getClassName());

        if (tableMetadata.getPrimaryColumn() != null) {
            metadata.put("pkColumn", tableMetadata.getPrimaryColumn().getName());
        } else {
            metadata.put("pkColumn", "");
        }

        metadata.put("entityName", framework.getService().getServiceName());
        metadata.put("package", framework.getService().getServicePackage());
        metadata.put("imports", framework.getService().getServiceImports());
        metadata.put("extends", framework.getService().getServiceExtends());
        metadata.put("fields", framework.getService().getServiceFieldContent());
        metadata.put("methods", framework.getService().getServiceMethodContent());
        metadata.put("constructors", framework.getService().getServiceConstructors());
        metadata.put("classAnnotations", framework.getService().getServiceAnnotations());

        return metadata;
    }


    public static HashMap<String, Object> getPrimaryControllerHashMap(Framework framework, TableMetadata tableMetadata) {
        HashMap<String, Object> metadata = new HashMap<>();

        metadata.put("className", tableMetadata.getClassName());
        metadata.put("entityName", framework.getController().getControllerName());
        metadata.put("package", framework.getController().getControllerPackage());
        metadata.put("imports", framework.getController().getControllerImports());
        metadata.put("extends", framework.getController().getControllerExtends());
        metadata.put("fields", framework.getController().getControllerFieldContent());
        metadata.put("methods", framework.getController().getControllerMethodContent());
        metadata.put("constructors", framework.getController().getControllerConstructors());
        metadata.put("classAnnotations", framework.getController().getControllerAnnotations());

        return metadata;
    }

    public static HashMap<String, Object> getModelHashMap(Framework framework, Language language, TableMetadata tableMetadata) {
        HashMap<String, Object> metadata = new HashMap<>();

        HashMap<String, Object> primaryModelMetadata = getPrimaryModelHashMap(framework, language, tableMetadata);
        HashMap<String, Object> languageMetadata = getRelatedLanguageMetadata(language);

        metadata.putAll(primaryModelMetadata);
        metadata.putAll(languageMetadata);

        return metadata;
    }

    public static HashMap<String, Object> getModelDaoHashMap(Framework framework, Language language, TableMetadata tableMetadata) {
        HashMap<String, Object> metadata = new HashMap<>();

        HashMap<String, Object> primaryModelDaoMetadata = getPrimaryModelDaoHashMap(framework, tableMetadata);
        HashMap<String, Object> languageMetadata = getRelatedLanguageMetadata(language);
        languageMetadata.put("classKeyword", framework.getModelDao().getModelDaoClassKeyword());

        metadata.putAll(primaryModelDaoMetadata);
        metadata.putAll(languageMetadata);

        return metadata;
    }

    public static HashMap<String, Object> getServiceHashMap(Framework framework, Language language, TableMetadata tableMetadata) {
        HashMap<String, Object> metadata = new HashMap<>();

        HashMap<String, Object> primaryServiceMetadata = getPrimaryServiceHashMap(framework, tableMetadata);
        HashMap<String, Object> languageMetadata = getRelatedLanguageMetadata(language);
        languageMetadata.put("classKeyword", framework.getService().getServiceClassKeyword());

        metadata.putAll(primaryServiceMetadata);
        metadata.putAll(languageMetadata);

        return metadata;
    }

    public static HashMap<String, Object> getControllerHashMap(Framework framework, Language language, TableMetadata tableMetadata) {
        HashMap<String, Object> metadata = new HashMap<>();

        HashMap<String, Object> primaryControllerMetadata = getPrimaryControllerHashMap(framework, tableMetadata);
        HashMap<String, Object> languageMetadata = getRelatedLanguageMetadata(language);

        metadata.putAll(primaryControllerMetadata);
        metadata.putAll(languageMetadata);

        return metadata;
    }

    public static HashMap<String, Object> getHashMapIntermediaire(TableMetadata tableMetadata, Framework framework, Map<String, Object> frameworkConfiguration, String destinationFolder, String projectName, String groupLink) {
        HashMap<String, Object> metadata = new HashMap<>();

        addGeneralMetadata(metadata, tableMetadata, framework, frameworkConfiguration, destinationFolder, projectName, groupLink);
        metadata.put("fields", getFieldsList(tableMetadata));
        metadata.put("fieldsPK", getFieldsPKList(tableMetadata));
        metadata.put("fieldsFK", getFieldsFKList(tableMetadata));

        return metadata;
    }

    public static HashMap<String, Object> getHashMapIntermediaire(Language language, TableMetadata tableMetadata, Framework framework, Map<String, Object> frameworkConfiguration, String destinationFolder, String projectName, String groupLink) {
        HashMap<String, Object> metadata = new HashMap<>();

        addGeneralMetadata(metadata, tableMetadata, framework, frameworkConfiguration, destinationFolder, projectName, groupLink);
        metadata.put("fields", getFieldsList(tableMetadata, language));
        metadata.put("fieldsPK", getFieldsPKList(tableMetadata, language));
        metadata.put("fieldsFK", getFieldsFKList(tableMetadata, language));

        return metadata;
    }

    private static void addGeneralMetadata(HashMap<String, Object> metadata, TableMetadata tableMetadata, Framework framework, Map<String, Object> frameworkOptions, String destinationFolder, String projectName, String groupLink) {
        metadata.put("destinationFolder", destinationFolder);
        metadata.put("projectName", projectName);
        metadata.put("groupLink", groupLink);
        metadata.put("groupLinkPath", groupLink.replace(".", "/"));

        if (tableMetadata.getPrimaryColumn() != null) {
            metadata.put("pkColumn", tableMetadata.getPrimaryColumn().getName());
            metadata.put("pkColumnType", tableMetadata.getPrimaryColumn().getType());
        } else {
            metadata.put("pkColumn", "");
            metadata.put("pkColumnType", "");
        }

        metadata.put("tableName", tableMetadata.getTableName());
        metadata.put("className", tableMetadata.getClassName());
        metadata.put("entityName", tableMetadata.getClassName());
        metadata.put("classNameLink", tableMetadata.getClassName() + "s");

        metadata.put("isView", tableMetadata.getIsView());

        String cacheProvider = (String) frameworkOptions.get("cacheProvider");
        Optional<FrameworkCaching> selectedCacheProviderOption = framework.getSelectedCacheProviderByName(cacheProvider);

        String handleSpace = StringUtils.toCamelCase(cacheProvider);
        Object cacheableOption = frameworkOptions.get("entitiesCacheable");

        if (selectedCacheProviderOption.isPresent() && cacheableOption instanceof List<?>) {
            List<String> cacheableEntities = (List<String>) cacheableOption;

            String className = tableMetadata.getClassName();
            boolean isCacheable = cacheableEntities.contains(className);

            metadata.put("cacheableWith" + StringUtils.majStart(handleSpace), isCacheable);
        }

        metadata.putAll(getFrameworkCachingTrueBooleanHashMap(framework,frameworkOptions));
    }


    private static List<Map<String, Object>> getFieldsList(TableMetadata tableMetadata) {
        List<Map<String, Object>> fields = new ArrayList<>();
        for (ColumnMetadata field : tableMetadata.getColumns()) {
            Map<String, Object> fieldMap = getFieldHashMap(field);
            fields.add(fieldMap);
        }
        return fields;
    }

    private static List<Map<String, Object>> getFieldsPKList(TableMetadata tableMetadata) {
        List<Map<String, Object>> fieldsPK = new ArrayList<>();
        for (ColumnMetadata field : tableMetadata.getColumns()) {
            if (!field.isPrimary()) {
                Map<String, Object> fieldMap = getFieldHashMap(field);
                fieldsPK.add(fieldMap);
            }
        }
        return fieldsPK;
    }

    private static List<Map<String, Object>> getFieldsFKList(TableMetadata tableMetadata) {
        List<Map<String, Object>> fieldsFK = new ArrayList<>();
        for (ColumnMetadata field : tableMetadata.getColumns()) {
            if (field.isForeign()) {
                Map<String, Object> fieldMap = getFieldHashMap(field);
                fieldsFK.add(fieldMap);
            }
        }
        return fieldsFK;
    }

    private static List<Map<String, Object>> getFieldsList(TableMetadata tableMetadata, Language language) {
        List<Map<String, Object>> fields = new ArrayList<>();
        for (ColumnMetadata field : tableMetadata.getColumns()) {
            Map<String, Object> fieldMap = getFieldHashMap(field, language);
            fields.add(fieldMap);
        }
        return fields;
    }

    private static List<Map<String, Object>> getFieldsPKList(TableMetadata tableMetadata, Language language) {
        List<Map<String, Object>> fieldsPK = new ArrayList<>();
        for (ColumnMetadata field : tableMetadata.getColumns()) {
            if (!field.isPrimary()) {
                Map<String, Object> fieldMap = getFieldHashMap(field, language);
                fieldsPK.add(fieldMap);
            }
        }
        return fieldsPK;
    }

    private static List<Map<String, Object>> getFieldsFKList(TableMetadata tableMetadata, Language language) {
        List<Map<String, Object>> fieldsFK = new ArrayList<>();
        for (ColumnMetadata field : tableMetadata.getColumns()) {
            if (field.isForeign()) {
                Map<String, Object> fieldMap = getFieldHashMap(field, language);
                fieldsFK.add(fieldMap);
            }
        }
        return fieldsFK;
    }

    public static @NotNull Map<String, Object> getFieldHashMap(ColumnMetadata field) {
        Map<String, Object> fieldMap = new HashMap<>();

        fieldMap.put("withGetters", false);
        fieldMap.put("withSetters", false);
        fieldMap.put("type", field.getType());
        fieldMap.put("name", field.getName());
        fieldMap.put("isPrimaryKey", field.isPrimary());
        fieldMap.put("isForeignKey", field.isForeign());
        fieldMap.put("columnType", field.getColumnType());
        fieldMap.put("columnName", field.getReferencedColumn());
        fieldMap.put("referencedColumnType", field.getReferencedColumnType());
        fieldMap.put("columnNameField", StringUtils.toCamelCase(field.getReferencedColumn()));
        fieldMap.put("defaultValue", field.getDefaultValue());
        fieldMap.put("columnSize", field.getColumnSize());
        fieldMap.put("decimalDigits", field.getDecimalDigits());
        fieldMap.put("isUnique", field.isUnique());
        fieldMap.put("isNullable", field.isNullable());
        fieldMap.put("validationAnnotations", getFieldValidationAnnotations(field));
        fieldMap.put("isIntAndPrimaryKey", field.isNumeric() && field.isPrimary());

        return fieldMap;
    }

    public static @NotNull Map<String, Object> getFieldHashMap(ColumnMetadata field, Language language) {
        Map<String, Object> fieldMap = new HashMap<>();

        fieldMap.put("withGetters", false);
        fieldMap.put("withSetters", false);
        fieldMap.put("type", field.getType());
        fieldMap.put("name", field.getName());
        fieldMap.put("isPrimaryKey", field.isPrimary());
        fieldMap.put("isForeignKey", field.isForeign());
        fieldMap.put("columnType", field.getColumnType());
        fieldMap.put("columnName", field.getReferencedColumn());
        fieldMap.put("referencedColumnType", field.getReferencedColumnType());
        fieldMap.put("columnNameField", StringUtils.toCamelCase(field.getReferencedColumn()));
        fieldMap.put("attributeTypeAnnotations", language.getAttributeTypeAnnotations().get(field.getType()));
        fieldMap.put("mockdata", language.getMockData().get(field.getColumnType()));
        fieldMap.put("criteriaBuildSnippet", language.getCriteriaBuildSnippet().get(field.getColumnType()));
        fieldMap.put("defaultValue", field.getDefaultValue());
        fieldMap.put("columnSize", field.getColumnSize());
        fieldMap.put("decimalDigits", field.getDecimalDigits());
        fieldMap.put("isUnique", field.isUnique());
        fieldMap.put("isNullable", field.isNullable());
        fieldMap.put("validationAnnotations", getFieldValidationAnnotations(field));
        fieldMap.put("isIntAndPrimaryKey", field.isNumeric() && field.isPrimary());

        return fieldMap;
    }

    public static Map<String, Object> getHashMapDaoGlobal(Framework framework, List<TableMetadata> tableMetadata, String projectName) throws Exception {
        String packageDefault = "";
        packageDefault = framework.getModelDao().getModelDaoSavePath();
        System.out.println("Get hashmapDAO global " + packageDefault);
        System.out.println(tableMetadata.size()+ " SIZEEEEE");
        Database database = tableMetadata.getFirst().getDatabase();
        System.out.println("Get databaseee global ");

        String connectionString = database.getConnectionString().get(framework.getLanguageId());
        Map<String, Object> connectionStringMetadata = getCredentialsHashMap(database);

        connectionString = engine.render(connectionString, connectionStringMetadata);
        System.out.println("Renderedeee");

        Map<String, Object> metadata = new HashMap<>(Map.of(
                "projectName", projectName,
                "packageValue", packageDefault,
                "daoName", database.getDaoName().get(framework.getLanguageId()) == null ? "" : database.getDaoName().get(framework.getLanguageId()),
                "addOptions", database.getAddOptions().get(framework.getLanguageId()) == null ? "" : database.getAddOptions().get(framework.getLanguageId()),
                "connectionString", connectionString)
        );

        List<String> fields = new ArrayList<>();
        for (TableMetadata tableMetadatum : tableMetadata) {
            fields.add(tableMetadatum.getClassName());
        }

        metadata.put("entities", fields);
        metadata.put("allEntities", getTableMetadataList(tableMetadata));
        return metadata;
    }

    private static List<Map<String, Object>> getTableMetadataList(List<TableMetadata> tableMetadata) {
        List<Map<String, Object>> tms = new ArrayList<>();
        for (TableMetadata field : tableMetadata) {
            Map<String, Object> tmMap = getTableMetadataHashMap(field);
            tms.add(tmMap);
        }
        return tms;
    }

    public static @NotNull Map<String, Object> getTableMetadataHashMap(TableMetadata tm) {
        Map<String, Object> tmMap = new HashMap<>();

        if (tm.getPrimaryColumn() != null) {
            tmMap.put("pkColumn", tm.getPrimaryColumn().getName());
            tmMap.put("pkColumnType", tm.getPrimaryColumn().getType());
        } else {
            tmMap.put("pkColumn", "");
            tmMap.put("pkColumnType", "");
        }

        tmMap.put("tableName", tm.getTableName());
        tmMap.put("className", tm.getClassName());
        tmMap.put("entityName", tm.getClassName());
        tmMap.put("classNameLink", tm.getClassName() + "s");

        tmMap.put("isView", tm.getIsView());

        return tmMap;
    }

    private static List<String> getFieldValidationAnnotations(ColumnMetadata field){
        return field.getValidationAnnotations().values().stream()
                .map(Object::toString)
                .collect(Collectors.toList());
    }

    private static HashMap<String, Object> getFrameworkCachingTrueBooleanHashMap(Framework framework, Map<String, Object> frameworkConfiguration) {
        HashMap<String, Object> frameworkSelectedCacheProviderBooleanMetadata = new HashMap<>();
        String cacheProvider = (String) frameworkConfiguration.get("cacheProvider");
        Optional<FrameworkCaching> selectedSelectedCacheProviderOption = framework.getSelectedCacheProviderByName(cacheProvider);
        selectedSelectedCacheProviderOption.ifPresent(frameworkCaching -> {
            for(String key : frameworkCaching.getMetadataBooleanTrueKeys()){
                frameworkSelectedCacheProviderBooleanMetadata.put(key, true);
            }
        });
        return frameworkSelectedCacheProviderBooleanMetadata;
    private static HashMap<String, Object> getFrameworkSecurityTrueBooleanHashMap(Framework framework, Map<String, Object> frameworkConfiguration) {
        HashMap<String, Object> frameworkSecurityBooleanMetadata = new HashMap<>();
        String securityType = (String) frameworkConfiguration.get("securityType");
        Optional<FrameworkSecurity> selectedSecurityOption = framework.getSelectedSecurityByName(securityType);
        selectedSecurityOption.ifPresent(security -> {
            for(String key : security.getMetadataBooleanTrueKeys()){
                frameworkSecurityBooleanMetadata.put(key, true);
            }
        });
        return frameworkSecurityBooleanMetadata;
    }
}

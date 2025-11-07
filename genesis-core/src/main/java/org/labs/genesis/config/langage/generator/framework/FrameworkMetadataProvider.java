package org.labs.genesis.config.langage.generator.framework;

import org.jetbrains.annotations.NotNull;
import org.labs.genesis.config.langage.*;
import org.labs.genesis.connexion.Credentials;
import org.labs.genesis.connexion.Database;
import org.labs.genesis.connexion.model.ChildTableMetadata;
import org.labs.genesis.connexion.model.ColumnMetadata;
import org.labs.genesis.connexion.model.TableMetadata;
import org.labs.genesis.engine.GenesisTemplateEngine;
import org.labs.genesis.frontend.FrontendLanguage;
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
        metadata.putAll(MereFilleMetadataProvider.getRelationsHashMap(tableMetadata));

        return metadata;
    }

    public static HashMap<String, Object> getHashMapIntermediaire(Language language, TableMetadata tableMetadata, Framework framework, Map<String, Object> frameworkConfiguration, String destinationFolder, String projectName, String groupLink) {
        HashMap<String, Object> metadata = new HashMap<>();

        addGeneralMetadata(metadata, tableMetadata, framework, frameworkConfiguration, destinationFolder, projectName, groupLink);
        metadata.put("fields", getFieldsList(tableMetadata, language));
        metadata.put("fieldsPK", getFieldsPKList(tableMetadata, language));
        metadata.put("fieldsFK", getFieldsFKList(tableMetadata, language));
        metadata.putAll(MereFilleMetadataProvider.getRelationsHashMap(tableMetadata));

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

        metadata.putAll(getFrameworkSecurityTrueBooleanHashMap(framework,frameworkOptions));
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

                String fieldType = field.getType();

                boolean exists = fieldsFK.stream()
                        .anyMatch(existing -> fieldType.equals(existing.get("type")));

                if (!exists) {
                    fieldsFK.add(fieldMap);
                }
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

                String fieldType = field.getType();

                boolean exists = fieldsFK.stream()
                        .anyMatch(existing -> fieldType.equals(existing.get("type")));

                if (!exists) {
                    fieldsFK.add(fieldMap);
                }
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
        fieldMap.put("referencedPrimaryKeyColumn", field.getReferencedPrimaryKeyColumn());
        fieldMap.put("columnNameField", StringUtils.toCamelCase(field.getReferencedColumn()));
        fieldMap.put("defaultValue", field.getDefaultValue());
        fieldMap.put("columnSize", field.getColumnSize());
        fieldMap.put("decimalDigits", field.getDecimalDigits());
        fieldMap.put("isUnique", field.isUnique());
        fieldMap.put("isNullable", field.isNullable());
        fieldMap.put("validationAnnotations", getFieldValidationAnnotations(field));
        fieldMap.put("isIntAndPrimaryKey", field.isNumeric() && field.isPrimary());
        fieldMap.put("isText",field.isText());
        fieldMap.put("isNumeric",field.isNumeric());
        fieldMap.put("isDate",field.isDate());
        fieldMap.put("isTime",field.isTime());
        fieldMap.put("isTimeTz",field.isTimeTz());
        fieldMap.put("isDateTime",field.isDateTime());
        fieldMap.put("isDateTimeTz",field.isDateTimeTz());
        fieldMap.put("useTimeZone",field.isUseTimeZone());
        fieldMap.put("isInterval",field.isInterval());

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
        fieldMap.put("referencedPrimaryKeyColumn", field.getReferencedPrimaryKeyColumn());
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
        fieldMap.put("isText",field.isText());
        fieldMap.put("isNumeric",field.isNumeric());
        fieldMap.put("isDate",field.isDate());
        fieldMap.put("isTime",field.isTime());
        fieldMap.put("isTimeTz",field.isTimeTz());
        fieldMap.put("isDateTime",field.isDateTime());
        fieldMap.put("isDateTimeTz",field.isDateTimeTz());
        fieldMap.put("useTimeZone",field.isUseTimeZone());
        fieldMap.put("isInterval",field.isInterval());

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
        if(connectionStringMetadata.get("database").equals("genesis"))
        {
            connectionStringMetadata.put("database",database.getSid().toLowerCase());
        }
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

    public static HashMap<String, Object> getMvcHashMapIntermediaire(Language language, TableMetadata tableMetadata, Framework framework, Map<String, Object> frameworkConfiguration, String destinationFolder, String projectName, String groupLink) throws Exception {
        HashMap<String, Object> metadata = getHashMapIntermediaire(language, tableMetadata, framework, frameworkConfiguration, destinationFolder, projectName, groupLink);
        metadata.put("inputs", getInputsList(tableMetadata, language));
        metadata.put("textAreas", getTextAreasList(tableMetadata, language));
        metadata.put("inputsFilter", getFilterInputsList(tableMetadata, language));

        List<Integer> pageSizesList = Arrays.asList(5, 10, 50, 100, 200, 300, 500, 1000);
        metadata.put("pageSizesList", pageSizesList);

        return metadata;
    }

    private static List<Map<String, Object>> getInputsList(TableMetadata tableMetadata, Language language) throws Exception {
        List<Map<String, Object>> inputs = new ArrayList<>();
        for (ColumnMetadata field : tableMetadata.getColumns()) {
            if (!field.isForeign()) {
                if (!field.getColumnType().equals("json")
                        && !field.getColumnType().equals("jsonb")
                        && !field.getColumnType().equals("xml")
                ) {
                    InputTypeMapping.Input input = InputTypeMapping.getInput(field, language, engine);
                    Map<String, Object> inputMap = getInputHashMap(input);
                    if (Boolean.TRUE.equals(inputMap.get("isShowed"))) {
                        inputs.add(inputMap);
                    }
                }
            }
        }
        return inputs;
    }

    private static List<Map<String, Object>> getTextAreasList(TableMetadata tableMetadata, Language language) throws Exception {
        List<Map<String, Object>> textAreas = new ArrayList<>();

        for (ColumnMetadata field : tableMetadata.getColumns()) {
            if (!field.isForeign()) {
                if (field.getColumnType().equals("json")
                        || field.getColumnType().equals("jsonb")
                        || field.getColumnType().equals("xml")
                ) {
                    textAreas.add(getTextAreaHashMap(field, language));
                }
            }
        }

        return textAreas;
    }

    private static List<Map<String, Object>> getFilterInputsList(TableMetadata tableMetadata, Language language) throws Exception {
        List<Map<String, Object>> inputs = new ArrayList<>();

        for (ColumnMetadata field : tableMetadata.getColumns()) {
            boolean isRangeType = field.isNumeric() || field.isDate() || field.isTime()
                    || field.isTimeTz() || field.isDateTime() || field.isDateTimeTz() || field.isInterval();

            boolean excluded = field.isForeign() || field.isPrimary();

            InputTypeMapping.Input input = InputTypeMapping.getInput(field, language, engine);

            if (isRangeType && !excluded) {
                inputs.add(getInputHashMap(input, field.getName() + "Min"));
                inputs.add(getInputHashMap(input, field.getName() + "Max"));
            }
            if (!isRangeType && !excluded) {
                inputs.add(getInputHashMap(input));
            }
        }

        return inputs;
    }


    public static @NotNull Map<String, Object> getInputHashMap(InputTypeMapping.Input input) {
        return getInputHashMap(input, input.getName());
    }

    public static @NotNull Map<String, Object> getInputHashMap(InputTypeMapping.Input input, String inputName) {
        Map<String, Object> inputMap = new HashMap<>();

        inputMap.put("name", inputName);
        inputMap.put("id", input.getId());
        inputMap.put("placeholder", input.getPlaceholder());
        inputMap.put("validations", input.getValidations());
        inputMap.put("type", input.getType());
        inputMap.put("isShowed", input.getIsShowed());
        inputMap.put("isRequired", input.getIsRequired());

        return inputMap;
    }


    public static @NotNull Map<String, Object> getTextAreaHashMap(ColumnMetadata columnMetadata, Language language) {
        Map<String, Object> inputMap = new HashMap<>();

        inputMap.put("name", StringUtils.majStart(columnMetadata.getName()) + StringUtils.majStart(columnMetadata.getColumnType().toLowerCase()));
        inputMap.put("class", columnMetadata.getColumnType().toLowerCase() + "-textarea");
        inputMap.put("placeholder", language.getMockData().get(columnMetadata.getColumnType()));
        inputMap.put("validFormat", language.getMockData().get(columnMetadata.getColumnType()));

        return inputMap;
    }


    public static HashMap<String, Object> getGeneralViewHashMap(FrameworkMVC framework) {
        HashMap<String, Object> metadata = new HashMap<>();
        metadata.put("rootPath", framework.getView().getRootPath());
        metadata.put("backLink", framework.getView().getBackLink());
        metadata.put("previousLink", framework.getView().getPreviousLink());
        metadata.put("staticFilesPath", framework.getView().getStaticFilesPath());
        metadata.put("antiForgeryTokenTagHelper", framework.getView().getAntiForgeryTokenTagHelper());
        return metadata;
    }

    public static HashMap<String, Object>  getViewMainLayoutHashMap (FrameworkMVC framework, Map<String, Object> frameworkConfiguration, List<TableMetadata> tableMetadata, String projectName, String destinationFolder, String groupLink) {
        HashMap<String, Object> metadata = new HashMap<>();

        metadata.putAll(getGeneralViewHashMap(framework));

        addGeneralMetadata(metadata, tableMetadata.get(0), framework, frameworkConfiguration, destinationFolder, projectName, groupLink);
        metadata.put("entities", getTableMetadataList(tableMetadata));

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

    private static HashMap<String, Object> getFrameworkSecurityTrueBooleanHashMap(Framework framework, Map<String, Object> frameworkConfiguration) {
        HashMap<String, Object> frameworkSecurityBooleanMetadata = new HashMap<>();
        // Defaults values:
        frameworkSecurityBooleanMetadata.put("useJWT", false);

        String securityType = (String) frameworkConfiguration.get("securityType");
        Optional<FrameworkSecurity> selectedSecurityOption = framework.getSelectedSecurityByName(securityType);
        selectedSecurityOption.ifPresent(security -> {
            for(String key : security.getMetadataBooleanTrueKeys()){
                frameworkSecurityBooleanMetadata.put(key, true);
            }
        });
        return frameworkSecurityBooleanMetadata;
    }

    private static HashMap<String, Object> getFrontendLanguageMetadata(FrontendLanguage frontendLanguage, Map<String, Object> frontendConfiguration) {
        HashMap<String, Object> frontendLanguageMetadata = new HashMap<>();
        return  frontendLanguageMetadata;
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
    }

    public static HashMap<String, Object> getAltViewMainLayoutHashMap (FrameworkMVC frameworkMVC) {
        HashMap<String, Object> altMap = new HashMap<>();
        altMap.putAll(getGeneralViewHashMap(frameworkMVC));
        altMap.put("assetsImportLink", frameworkMVC.getView().getLayout().getAssetsImportLink());
        altMap.put("viewAnnotations", frameworkMVC.getView().getLayout().getViewAnnotations());
        altMap.put("pageName", frameworkMVC.getView().getLayout().getPageName());
        altMap.put("navLink", frameworkMVC.getView().getLayout().getNavLink());
        altMap.put("callContent", frameworkMVC.getView().getLayout().getCallContent());
        altMap.put("logoutLink", frameworkMVC.getView().getLayout().getLogoutLink());
        return altMap;
    }

    public static HashMap<String, Object> getAltViewErrorHashMap (FrameworkMVC frameworkMVC) {
        HashMap<String, Object> altMap = new HashMap<>(getGeneralViewHashMap(frameworkMVC));
        altMap.put("viewAnnotations", frameworkMVC.getView().getError().getViewAnnotations());
        altMap.put("errorMessage", frameworkMVC.getView().getError().getErrorMessage());
        return altMap;
    }

    public static HashMap<String, Object> getAltViewListHashMap (FrameworkMVC frameworkMVC) {
        HashMap<String, Object> altMap = new HashMap<>(getGeneralViewHashMap(frameworkMVC));
        altMap.put("viewAnnotations", frameworkMVC.getView().getList().getViewAnnotations());
        altMap.put("inputTagHelper", frameworkMVC.getView().getList().getInputTagHelper());
        altMap.put("inputRadioTagHelper", frameworkMVC.getView().getList().getInputRadioTagHelper());
        altMap.put("inputDateTagHelper", frameworkMVC.getView().getList().getInputDateTagHelper());
        altMap.put("selectTagHelper", frameworkMVC.getView().getList().getSelectTagHelper());
        altMap.put("deleteDataTagHelper", frameworkMVC.getView().getList().getDeleteDataTagHelper());
        altMap.put("pageSizeTagHelper", frameworkMVC.getView().getList().getPageSizeTagHelper());
        altMap.put("dataValue", frameworkMVC.getView().getList().getDataValue());
        altMap.put("dataForeignValue", frameworkMVC.getView().getList().getDataForeignValue());
        altMap.put("inlineLoopStatement", frameworkMVC.getView().getList().getInlineLoopStatement());
        altMap.put("blockLoopStatementStart", frameworkMVC.getView().getList().getBlockLoopStatementStart());
        altMap.put("blockLoopStatementEnd", frameworkMVC.getView().getList().getBlockLoopStatementEnd());
        altMap.put("filterLink", frameworkMVC.getView().getList().getFilterLink());
        altMap.put("sortLink", frameworkMVC.getView().getList().getSortLink());
        altMap.put("detailsLink", frameworkMVC.getView().getList().getDetailsLink());
        altMap.put("createLink", frameworkMVC.getView().getList().getCreateLink());
        altMap.put("updateLink", frameworkMVC.getView().getList().getUpdateLink());
        altMap.put("deleteLink", frameworkMVC.getView().getList().getDeleteLink());
        altMap.put("exportLink", frameworkMVC.getView().getList().getExportLink());
        altMap.put("pageSizeChangeLink", frameworkMVC.getView().getList().getPageSizeChangeLink());
        altMap.put("previousPageLink", frameworkMVC.getView().getList().getPreviousPageLink());
        altMap.put("previousClassCondition", frameworkMVC.getView().getList().getPreviousClassCondition());
        altMap.put("pagesListLoop", frameworkMVC.getView().getList().getPagesListLoop());
        altMap.put("nextPageLink", frameworkMVC.getView().getList().getNextPageLink());
        altMap.put("nextClassCondition", frameworkMVC.getView().getList().getNextClassCondition());
        altMap.put("onGoingPageLink", frameworkMVC.getView().getList().getOnGoingPageLink());
        altMap.put("pageNumberValue", frameworkMVC.getView().getList().getPageNumberValue());
        altMap.put("pageSizeValue", frameworkMVC.getView().getList().getPageSizeValue());
        altMap.put("currentSortValue", frameworkMVC.getView().getList().getCurrentSortValue());
        altMap.put("totalElementsTagHelper", frameworkMVC.getView().getList().getTotalElementsTagHelper());
        altMap.put("activeSortAscCondition", frameworkMVC.getView().getList().getActiveSortAscCondition());
        altMap.put("activeSortDescCondition", frameworkMVC.getView().getList().getActiveSortDescCondition());
        altMap.put("onGoingPagesLoop", frameworkMVC.getView().getList().getOnGoingPagesLoop());
        altMap.put("scriptSection", frameworkMVC.getView().getList().getScriptSection());
        return altMap;
    }

    public static HashMap<String, Object> getAltViewDetailHashMap (FrameworkMVC frameworkMVC) {
        HashMap<String, Object> altMap = new HashMap<>(getGeneralViewHashMap(frameworkMVC));
        altMap.put("viewAnnotations", frameworkMVC.getView().getDetail().getViewAnnotations());
        altMap.put("dataValue", frameworkMVC.getView().getDetail().getDataValue());
        altMap.put("dataForeignValue", frameworkMVC.getView().getDetail().getDataForeignValue());
        altMap.put("deleteDataTagHelper", frameworkMVC.getView().getDetail().getDeleteDataTagHelper());
        altMap.put("updateLink", frameworkMVC.getView().getDetail().getUpdateLink());
        altMap.put("deleteLink", frameworkMVC.getView().getDetail().getDeleteLink());
        return altMap;
    }

    public static HashMap<String, Object> getAltViewCreateHashMap (FrameworkMVC frameworkMVC) {
        HashMap<String, Object> altMap = new HashMap<>(getGeneralViewHashMap(frameworkMVC));
        altMap.put("viewAnnotations", frameworkMVC.getView().getCreate().getViewAnnotations());
        altMap.put("validationSection", frameworkMVC.getView().getCreate().getValidationSection());
        altMap.put("validationTagHelper", frameworkMVC.getView().getCreate().getValidationTagHelper());
        altMap.put("selectValidationTagHelper", frameworkMVC.getView().getCreate().getSelectValidationTagHelper());
        altMap.put("inputTagHelper", frameworkMVC.getView().getCreate().getInputTagHelper());
        altMap.put("textAreaTagHelper", frameworkMVC.getView().getCreate().getTextAreaTagHelper());
        altMap.put("textAreaValidationTagHelper", frameworkMVC.getView().getCreate().getTextAreaValidationTagHelper());
        altMap.put("checkedRadioTagHelper", frameworkMVC.getView().getCreate().getCheckedRadioTagHelper());
        altMap.put("selectTagHelper", frameworkMVC.getView().getCreate().getSelectTagHelper());
        altMap.put("createLink", frameworkMVC.getView().getCreate().getCreateLink());
        altMap.put("scriptSection", frameworkMVC.getView().getCreate().getScriptSection());
        return altMap;
    }

    public static HashMap<String, Object> getAltViewEditHashMap (FrameworkMVC frameworkMVC) {
        HashMap<String, Object> altMap = new HashMap<>(getGeneralViewHashMap(frameworkMVC));
        altMap.put("viewAnnotations", frameworkMVC.getView().getEdit().getViewAnnotations());
        altMap.put("validationSection", frameworkMVC.getView().getEdit().getValidationSection());
        altMap.put("validationTagHelper", frameworkMVC.getView().getCreate().getValidationTagHelper());
        altMap.put("selectValidationTagHelper", frameworkMVC.getView().getCreate().getSelectValidationTagHelper());
        altMap.put("inputTagHelper", frameworkMVC.getView().getEdit().getInputTagHelper());
        altMap.put("textAreaTagHelper", frameworkMVC.getView().getEdit().getTextAreaTagHelper());
        altMap.put("textAreaValidationTagHelper", frameworkMVC.getView().getEdit().getTextAreaValidationTagHelper());
        altMap.put("selectTagHelper", frameworkMVC.getView().getEdit().getSelectTagHelper());
        altMap.put("updateLink", frameworkMVC.getView().getEdit().getUpdateLink());
        altMap.put("scriptSection", frameworkMVC.getView().getEdit().getScriptSection());
        return altMap;
    }
}

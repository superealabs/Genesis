package org.labs.genesis.config.langage.generator.framework;

import org.jetbrains.annotations.NotNull;
import org.labs.genesis.config.langage.*;
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

        return new HashMap<>(
                Map.of("host", credentials.getHost(),
                        "port", credentials.getPort(),
                        "database", credentials.getDatabaseName(),
                        "schema", credentials.getSchemaName(),
                        "useSSL", credentials.isUseSSL(),
                        "username", credentials.getUser(),
                        "password", credentials.getPwd(),
                        "driverType", credentials.getDriverType(),
                        "sid", database.getSid()
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

    public static HashMap<String, Object> getViewHashMap(FrameworkMVC framework, Language language, TableMetadata tableMetadata) {
        HashMap<String, Object> metadata = new HashMap<>();

        HashMap<String, Object> languageMetadata = getRelatedLanguageMetadata(language);

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

    public static HashMap<String, Object> getMvcHashMapIntermediaire(Language language, TableMetadata tableMetadata, Framework framework, Map<String, Object> frameworkConfiguration, String destinationFolder, String projectName, String groupLink) throws Exception {
        HashMap<String, Object> metadata = new HashMap<>();

        addGeneralMetadata(metadata, tableMetadata, framework, frameworkConfiguration, destinationFolder, projectName, groupLink);
        metadata.put("fields", getFieldsList(tableMetadata, language));
        metadata.put("fieldsPK", getFieldsPKList(tableMetadata, language));
        metadata.put("fieldsFK", getFieldsFKList(tableMetadata, language));
        metadata.put("inputs", getInputsList(tableMetadata, language));
        metadata.put("textAreas", getTextAreasList(tableMetadata, language));
        metadata.put("inputsFilter", getFilterInputsList(tableMetadata, language));

        List<Integer> pageSizesList = Arrays.asList(5, 10, 50, 100, 200, 300, 500, 1000);
        metadata.put("pageSizesList", pageSizesList);

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

        Database database = tableMetadata.getFirst().getDatabase();
        String connectionString = database.getConnectionString().get(framework.getLanguageId());
        Map<String, Object> connectionStringMetadata = getCredentialsHashMap(database);
        connectionString = engine.render(connectionString, connectionStringMetadata);

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


    public static HashMap<String, Object>  getViewMainLayoutHashMap (FrameworkMVC framework, Map<String, Object> frameworkConfiguration, List<TableMetadata> tableMetadata, String projectName, String destinationFolder, String groupLink) {
        HashMap<String, Object> metadata = new HashMap<>();

        addGeneralMetadata(metadata, tableMetadata.get(1), framework, frameworkConfiguration, destinationFolder, projectName, groupLink);
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
        String securityType = (String) frameworkConfiguration.get("securityType");
        Optional<FrameworkSecurity> selectedSecurityOption = framework.getSelectedSecurityByName(securityType);
        selectedSecurityOption.ifPresent(security -> {
            for(String key : security.getMetadataBooleanTrueKeys()){
                frameworkSecurityBooleanMetadata.put(key, true);
            }
        });
        return frameworkSecurityBooleanMetadata;
    }


    public static HashMap<String, Object> getAltViewMainLayoutHashMap (ViewsTemplateEngine viewsTemplateEngine) {
        HashMap<String, Object> altMap = new HashMap<>();
        altMap.put("navLink", viewsTemplateEngine.getLayout().getNavLink());
        altMap.put("rootPath", viewsTemplateEngine.getLayout().getRootPath());
        altMap.put("assetsImportLink", viewsTemplateEngine.getLayout().getAssetsImportLink());
        altMap.put("pageName", viewsTemplateEngine.getLayout().getPageName());
        altMap.put("callContent", viewsTemplateEngine.getLayout().getCallContent());
        altMap.put("currentViewContext", viewsTemplateEngine.getLayout().getCurrentViewContext());
        altMap.put("logoutTagHelper", viewsTemplateEngine.getLayout().getLogoutTagHelper());
        return altMap;
    }

    public static HashMap<String, Object> getAltViewErrorHashMap (ViewsTemplateEngine viewsTemplateEngine) {
        HashMap<String, Object> altMap = new HashMap<>();
        altMap.put("viewAnnotations", viewsTemplateEngine.getError().getViewAnnotations());
        altMap.put("backLink", viewsTemplateEngine.getError().getBackLink());
        altMap.put("errorMessage", viewsTemplateEngine.getError().getErrorMessage());
        return altMap;
    }

    public static HashMap<String, Object> getAltViewListHashMap (ViewsTemplateEngine viewsTemplateEngine) {
        HashMap<String, Object> altMap = new HashMap<>();
        altMap.put("modelType", viewsTemplateEngine.getList().getModelType());
        altMap.put("dataValue", viewsTemplateEngine.getList().getDataValue());
        altMap.put("dataForeignValue", viewsTemplateEngine.getList().getDataForeignValue());
        altMap.put("blockLoopStatementStart", viewsTemplateEngine.getList().getBlockLoopStatementStart());
        altMap.put("blockLoopStatementEnd", viewsTemplateEngine.getList().getBlockLoopStatementEnd());
        altMap.put("inlineLoopStatement", viewsTemplateEngine.getList().getInlineLoopStatement());
        altMap.put("detailsLink", viewsTemplateEngine.getList().getDetailsLink());
        altMap.put("filterLink", viewsTemplateEngine.getList().getFilterLink());
        altMap.put("inputTagHelper", viewsTemplateEngine.getList().getInputTagHelper());
        altMap.put("selectTagHelper", viewsTemplateEngine.getList().getSelectTagHelper());
        altMap.put("deleteTagHelper", viewsTemplateEngine.getList().getDeleteTagHelper());
        altMap.put("deleteLink", viewsTemplateEngine.getList().getDeleteLink());
        altMap.put("updateLink", viewsTemplateEngine.getList().getUpdateLink());
        altMap.put("createLink", viewsTemplateEngine.getList().getCreateLink());
        altMap.put("backLink", viewsTemplateEngine.getList().getBackLink());
        altMap.put("sortLink", viewsTemplateEngine.getList().getSortLink());
        altMap.put("pageSizeChangeLink", viewsTemplateEngine.getList().getPageSizeChangeLink());
        altMap.put("pageSizeTagHelper", viewsTemplateEngine.getList().getPageSizeTagHelper());
        altMap.put("previousPageLink", viewsTemplateEngine.getList().getPreviousPageLink());
        altMap.put("previousClassCondition", viewsTemplateEngine.getList().getPreviousClassCondition());
        altMap.put("pagesListLoop", viewsTemplateEngine.getList().getPagesListLoop());
        altMap.put("nextPageLink", viewsTemplateEngine.getList().getNextPageLink());
        altMap.put("nextClassCondition", viewsTemplateEngine.getList().getNextClassCondition());
        altMap.put("onGoingPageLink", viewsTemplateEngine.getList().getOnGoingPageLink());
        altMap.put("onGoingPageSizeTagHelper", viewsTemplateEngine.getList().getOnGoingPageSizeTagHelper());
        altMap.put("onGoingSortOrderTagHelper", viewsTemplateEngine.getList().getOnGoingSortOrderTagHelper());
        altMap.put("onGoingPagesLoop", viewsTemplateEngine.getList().getOnGoingPagesLoop());
        altMap.put("scriptSection", viewsTemplateEngine.getList().getScriptSection());
        return altMap;
    }

    public static HashMap<String, Object> getAltViewDetailHashMap (ViewsTemplateEngine viewsTemplateEngine) {
        HashMap<String, Object> altMap = new HashMap<>();
        altMap.put("modelType", viewsTemplateEngine.getDetail().getModelType());
        altMap.put("dataValue", viewsTemplateEngine.getDetail().getDataValue());
        altMap.put("deleteLink", viewsTemplateEngine.getDetail().getDeleteLink());
        altMap.put("updateLink", viewsTemplateEngine.getDetail().getUpdateLink());
        altMap.put("backLink", viewsTemplateEngine.getDetail().getBackLink());
        altMap.put("dataForeignValue", viewsTemplateEngine.getDetail().getDataForeignValue());
        return altMap;
    }

    public static HashMap<String, Object> getAltViewCreateHashMap (ViewsTemplateEngine viewsTemplateEngine) {
        HashMap<String, Object> altMap = new HashMap<>();
        altMap.put("modelType", viewsTemplateEngine.getCreate().getModelType());
        altMap.put("validationSection", viewsTemplateEngine.getCreate().getValidationSection());
        altMap.put("backLink", viewsTemplateEngine.getCreate().getBackLink());
        altMap.put("validationTagHelper", viewsTemplateEngine.getCreate().getValidationTagHelper());
        altMap.put("selectValidationTagHelper", viewsTemplateEngine.getCreate().getSelectValidationTagHelper());
        altMap.put("inputTagHelper", viewsTemplateEngine.getCreate().getInputTagHelper());
        altMap.put("textAreaTagHelper", viewsTemplateEngine.getCreate().getTextAreaTagHelper());
        altMap.put("textAreaValidationTagHelper", viewsTemplateEngine.getCreate().getTextAreaValidationTagHelper());
        altMap.put("checkedRadioTagHelper", viewsTemplateEngine.getCreate().getCheckedRadioTagHelper());
        altMap.put("selectTagHelper", viewsTemplateEngine.getCreate().getSelectTagHelper());
        altMap.put("createLink", viewsTemplateEngine.getCreate().getCreateLink());
        altMap.put("scriptSection", viewsTemplateEngine.getCreate().getScriptSection());
        return altMap;
    }

    public static HashMap<String, Object> getAltViewEditHashMap (ViewsTemplateEngine viewsTemplateEngine) {
        HashMap<String, Object> altMap = new HashMap<>();
        altMap.put("modelType", viewsTemplateEngine.getEdit().getModelType());
        altMap.put("validationSection", viewsTemplateEngine.getEdit().getValidationSection());
        altMap.put("backLink", viewsTemplateEngine.getEdit().getBackLink());
        altMap.put("validationTagHelper", viewsTemplateEngine.getCreate().getValidationTagHelper());
        altMap.put("selectValidationTagHelper", viewsTemplateEngine.getCreate().getSelectValidationTagHelper());
        altMap.put("inputTagHelper", viewsTemplateEngine.getEdit().getInputTagHelper());
        altMap.put("textAreaTagHelper", viewsTemplateEngine.getEdit().getTextAreaTagHelper());
        altMap.put("textAreaValidationTagHelper", viewsTemplateEngine.getEdit().getTextAreaValidationTagHelper());
        altMap.put("selectTagHelper", viewsTemplateEngine.getEdit().getSelectTagHelper());
        altMap.put("updateLink", viewsTemplateEngine.getEdit().getUpdateLink());
        altMap.put("scriptSection", viewsTemplateEngine.getEdit().getScriptSection());
        return altMap;
    }
}

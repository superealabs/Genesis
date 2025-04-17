package org.labs.genesis.config.langage.generator.framework;

import org.jetbrains.annotations.NotNull;
import org.labs.genesis.config.langage.*;
import org.labs.genesis.connexion.Credentials;
import org.labs.genesis.connexion.Database;
import org.labs.genesis.connexion.model.ColumnMetadata;
import org.labs.genesis.connexion.model.TableMetadata;
import org.labs.genesis.engine.GenesisTemplateEngine;
import org.labs.utils.StringUtils;

import org.labs.genesis.config.langage.generator.project.ProjectMetadataProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FrameworkMetadataProvider {
    private static final GenesisTemplateEngine engine = new GenesisTemplateEngine();

    public static @NotNull Map<String, Object> getCredentialsHashMap(Database database) {
        Credentials credentials = database.getCredentials();

        return new HashMap<>(
                Map.of("host", credentials.getHost(),
                        "port", database.getPort(),
                        "database", credentials.getDatabaseName(),
                        "schema", credentials.getSchemaName(),
                        "useSSL", credentials.isUseSSL(),
                        "username", credentials.getUser(),
                        "password", credentials.getPwd(),
                        "driverType", database.getDriverType(),
                        "sid", database.getSid()
                )
        );
    }

    public static List<String> getClassNameHashMap(TableMetadata[] tableMetadata) {
        List<String> fields = new ArrayList<>();

        for (TableMetadata tableMetadatum : tableMetadata) {
            fields.add(tableMetadatum.getClassName());
        }

        return fields;
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

    public static HashMap<String, Object> getPrimaryModelHashMap(FrameworkConfiguration framework, Language language, TableMetadata tableMetadata) {
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

    public static HashMap<String, Object> getPrimaryModelDaoHashMap(FrameworkConfiguration framework, TableMetadata tableMetadata) {
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


    public static HashMap<String, Object> getPrimaryServiceHashMap(FrameworkConfiguration framework, TableMetadata tableMetadata) {
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


    public static HashMap<String, Object> getPrimaryControllerHashMap(FrameworkConfiguration framework, TableMetadata tableMetadata) {
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

    public static HashMap<String, Object> getModelHashMap(FrameworkConfiguration framework, Language language, TableMetadata tableMetadata) {
        HashMap<String, Object> metadata = new HashMap<>();

        HashMap<String, Object> primaryModelMetadata = getPrimaryModelHashMap(framework, language, tableMetadata);
        HashMap<String, Object> languageMetadata = getRelatedLanguageMetadata(language);

        metadata.putAll(primaryModelMetadata);
        metadata.putAll(languageMetadata);

        return metadata;
    }

    public static HashMap<String, Object> getModelDaoHashMap(FrameworkConfiguration framework, Language language, TableMetadata tableMetadata) {
        HashMap<String, Object> metadata = new HashMap<>();

        HashMap<String, Object> primaryModelDaoMetadata = getPrimaryModelDaoHashMap(framework, tableMetadata);
        HashMap<String, Object> languageMetadata = getRelatedLanguageMetadata(language);
        languageMetadata.put("classKeyword", framework.getModelDao().getModelDaoClassKeyword());

        metadata.putAll(primaryModelDaoMetadata);
        metadata.putAll(languageMetadata);

        return metadata;
    }

    public static HashMap<String, Object> getServiceHashMap(FrameworkConfiguration framework, Language language, TableMetadata tableMetadata) {
        HashMap<String, Object> metadata = new HashMap<>();

        HashMap<String, Object> primaryServiceMetadata = getPrimaryServiceHashMap(framework, tableMetadata);
        HashMap<String, Object> languageMetadata = getRelatedLanguageMetadata(language);
        languageMetadata.put("classKeyword", framework.getService().getServiceClassKeyword());

        metadata.putAll(primaryServiceMetadata);
        metadata.putAll(languageMetadata);

        return metadata;
    }

    public static HashMap<String, Object> getControllerHashMap(FrameworkConfiguration framework, Language language, TableMetadata tableMetadata) {
        HashMap<String, Object> metadata = new HashMap<>();

        HashMap<String, Object> primaryControllerMetadata = getPrimaryControllerHashMap(framework, tableMetadata);
        HashMap<String, Object> languageMetadata = getRelatedLanguageMetadata(language);

        metadata.putAll(primaryControllerMetadata);
        metadata.putAll(languageMetadata);

        return metadata;
    }

    public static HashMap<String, Object> getSelectHashMap(ColumnMetadata[] columnMetadonnee, ColumnMetadata columnMetadata) {
        HashMap<String, Object> metadata = new HashMap<>();
        ColumnMetadata data = getColumnAfterPK(columnMetadonnee);

        metadata.put("foreignPrimaryName", data.getName());
        metadata.put("fieldName", columnMetadata.getName());
        metadata.put("foreignName", columnMetadata.getReferencedColumn());

        return metadata;
    }

    public static List<String> getCreateInputsList(ColumnMetadata[] columnMetadatas, ColumnMetadata metaDonnee, TableMetadata tableMetadata, UIViewsConfiguration uiViewsConfiguration) throws Exception {
        List<String> inputContents = new ArrayList<>();
        String select = uiViewsConfiguration.getInsert().getSelect();
        ColumnMetadata[] columns = tableMetadata.getColumns();
        Map<String, Object> creates = uiViewsConfiguration.getInsert().getInput();
        HashMap<String, Object> datas = getSelectHashMap(columnMetadatas, metaDonnee);
        HashMap<String, Object> selectHashMap = ProjectMetadataProvider.getSelectHashMap(uiViewsConfiguration);

        for (ColumnMetadata columnMetadatum : columns) {
            if (!columnMetadatum.isPrimary()) {
                if (columnMetadatum.getReferencedTable() == null) {
                    String updatesInput = engine.render(creates.get(columnMetadatum.getPrimaryType()).toString(), Map.of("fieldName", columnMetadatum.getName()));
                    inputContents.add(updatesInput);
                } else {
                    String foreignUpdateInput = engine.simpleRenderAlt(select, selectHashMap);
                    foreignUpdateInput = engine.render(foreignUpdateInput,datas);
                    inputContents.add(foreignUpdateInput);
                }
            }
        }

        return inputContents;
    }

    public static List<String> getUpdateInputsList(ColumnMetadata[] columnMetadatas, ColumnMetadata metaDonnee, TableMetadata tableMetadata,UIViewsConfiguration editor) throws Exception {
        List<String> inputContents = new ArrayList<>();
        String select = editor.getUpdate().getSelect();
        ColumnMetadata[] columns = tableMetadata.getColumns();
        Map<String, Object> updates = editor.getUpdate().getInput();
        HashMap<String, Object> datas = getSelectHashMap(columnMetadatas, metaDonnee);
        HashMap<String, Object> selectHashMap = ProjectMetadataProvider.getSelectHashMap(editor);

        for (ColumnMetadata columnMetadatum : columns) {
            if (!columnMetadatum.isPrimary()) {
                if (columnMetadatum.getReferencedTable() == null) {
                    try {
                        String updatesInput = engine.render(updates.get(columnMetadatum.getPrimaryType()).toString(), Map.of("fieldName", columnMetadatum.getName()));
                        inputContents.add(updatesInput);

                    } catch (Exception e) {
                        System.out.println(columnMetadatum);
                    }
                } else {
                    String foreignUpdateInput = engine.simpleRenderAlt(select, selectHashMap);
                    foreignUpdateInput = engine.render(foreignUpdateInput,datas);
                    inputContents.add(foreignUpdateInput);
                }
            }
        }

        return inputContents;
    }

    public static HashMap<String, Object> getViewMainLayoutHashMap(TableMetadata[] tableMetadatas, TableMetadata tableMetadata, Language language, UIViewsConfiguration configuration) throws Exception {
        HashMap<String, Object> metadata = new HashMap<>();

        HashMap<String, Object> primaryViewMetadata = getDisplayHashMap(configuration);
        List<String> classNames = getClassNameHashMap(tableMetadatas);
        HashMap<String, Object> languageMetadata = getRelatedLanguageMetadata(language);

        metadata.putAll(primaryViewMetadata);
        metadata.putAll(languageMetadata);
        metadata.put("fields", classNames);

        metadata.put("className", tableMetadata.getClassName());
        return metadata;
    }

    public static HashMap<String, Object> getDisplayHashMap(UIViewsConfiguration editor) {
        HashMap<String, Object> metadata = new HashMap<>();

        /*-- Header --*/
        metadata.put("iconsLink", editor.getLayout().getHeader().getIconsLink());
        metadata.put("coresLink", editor.getLayout().getHeader().getCoresLink());
        metadata.put("themeLink", editor.getLayout().getHeader().getThemeLink());
        metadata.put("assetsLink", editor.getLayout().getHeader().getAssetsLink());
        metadata.put("vendorsLink", editor.getLayout().getHeader().getVendorsLink());
        metadata.put("helpersLink", editor.getLayout().getHeader().getHelpersLink());
        metadata.put("viewAttribute", editor.getLayout().getHeader().getViewAttribute());

        /*-- Content --*/
        metadata.put("callMenu", editor.getLayout().getContent().getCallMenu());
        metadata.put("callContent", editor.getLayout().getContent().getCallContent());

        /*-- Footer --*/
        metadata.put("coresFooterLink", editor.getLayout().getFooter().getCoresFooterLink());
        metadata.put("mainsFooterLink", editor.getLayout().getFooter().getMainsFooterLink());
        metadata.put("pagesFooterLink", editor.getLayout().getFooter().getPagesFooterLink());
        metadata.put("vendorsFooterLink", editor.getLayout().getFooter().getVendorsFooterLink());

        return metadata;
    }

    public static HashMap<String, Object> getListViewHashMap(UIViewsConfiguration editor, TableMetadata tableMetadata, String destinationFolder,  String projectName, String groupLink) throws Exception {
        HashMap<String, Object> metadata = new HashMap<>();

        String createLink = engine.render(editor.getLayout().getMenu().getCreateLink(), getHashMapIntermediaire(tableMetadata, destinationFolder, projectName, groupLink));

        metadata.put("createLink", createLink);
        metadata.put("textFailed", editor.getLayout().getContent().getTextFailed());
        metadata.put("alertFailed", editor.getLayout().getContent().getAlertFailed());
        metadata.put("textSuccess", editor.getLayout().getContent().getTextSuccess());
        metadata.put("alertSuccess", editor.getLayout().getContent().getAlertSuccess());

        return metadata;
    }


    public static HashMap<String, Object> getAllCreateViewHashMap(ColumnMetadata[] columnMetadatas, ColumnMetadata metaDonnee, FrameworkConfiguration framework, UIViewsConfiguration uiViewsConfiguration, TableMetadata tableMetadata, String destinationFolder, String projectName, String groupLink) throws Exception {
        HashMap<String, Object> metadata = new HashMap<>();

        HashMap<String, Object> fieldsCreateMap = getPrimaryModelDaoHashMap(framework, tableMetadata);
        List<String> createInput = getCreateInputsList(columnMetadatas, metaDonnee, tableMetadata, uiViewsConfiguration);
        HashMap<String, Object> languageMetadata = getHashMapIntermediaire(tableMetadata, destinationFolder, projectName, groupLink);
        HashMap<String, Object> primaryListViewHashMap = getListViewHashMap(uiViewsConfiguration, tableMetadata, destinationFolder, projectName, groupLink);

        metadata.putAll(fieldsCreateMap);
        metadata.putAll(languageMetadata);
        metadata.putAll(primaryListViewHashMap);
        metadata.put("inputContents", createInput);


        return metadata;
    }

    public static HashMap<String, Object> getAllListViewHashMap(ColumnMetadata[] columnMetadatas, ColumnMetadata metaDonnee, FrameworkConfiguration frameworkConfiguration, UIViewsConfiguration uiViewsConfiguration, TableMetadata tableMetadata, String destinationFolder, String projectName, String groupLink) throws Exception {
        HashMap<String, Object> metadata = new HashMap<>();

        HashMap<String, Object> fieldsUpdateMap = getPrimaryModelDaoHashMap(frameworkConfiguration, tableMetadata);
        List<String> updatesInput = getUpdateInputsList(columnMetadatas, metaDonnee, tableMetadata, uiViewsConfiguration);
        HashMap<String, Object> languageMetadata = getHashMapIntermediaire(tableMetadata, destinationFolder, projectName, groupLink);
        HashMap<String, Object> primaryListViewHashMap = getListViewHashMap(uiViewsConfiguration, tableMetadata, destinationFolder, projectName, groupLink);

        metadata.putAll(fieldsUpdateMap);
        metadata.putAll(languageMetadata);
        metadata.putAll(primaryListViewHashMap);
        metadata.put("inputContents", updatesInput);

        return metadata;
    }

    public static HashMap<String, Object> getHashMapIntermediaire(TableMetadata tableMetadata, String destinationFolder, String projectName, String groupLink) {
        HashMap<String, Object> metadata = new HashMap<>();

        addGeneralMetadata(metadata, tableMetadata, destinationFolder, projectName, groupLink);
        metadata.put("fields", getFieldsList(tableMetadata));
        metadata.put("fieldsPK", getFieldsPKList(tableMetadata));
        metadata.put("fieldsFK", getFieldsFKList(tableMetadata));

        return metadata;
    }

    private static void addGeneralMetadata(HashMap<String, Object> metadata, TableMetadata tableMetadata, String destinationFolder, String projectName, String groupLink) {
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

        return fieldMap;
    }

    public static Map<String, Object> getHashMapDaoGlobal(Framework framework, List<TableMetadata> tableMetadata, String projectName, FrameworkConfiguration frameworkConfiguration) throws Exception {
        String packageDefault = "";
        packageDefault = frameworkConfiguration
                .getModelDao().getModelDaoSavePath();

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

        return metadata;
    }

    public static ColumnMetadata getColumnAfterPK(ColumnMetadata[] columnMetadata) {
        ColumnMetadata metadata = new ColumnMetadata();
        boolean nextColumnIsMetadata = false;

        for (ColumnMetadata column : columnMetadata) {
            if (nextColumnIsMetadata) {
                metadata = column;
                break;
            }

            if (column.isPrimary()) {
                nextColumnIsMetadata = true;
            }
        }

        return metadata;
    }
}

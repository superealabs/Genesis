package org.labs.genesis.frontend.generator.frameworkFrontend;

import org.jetbrains.annotations.NotNull;
import org.labs.genesis.config.Constantes;
import org.labs.genesis.config.ProjectGenerationContext;
import org.labs.genesis.config.langage.Framework;
import org.labs.genesis.config.langage.Language;
import org.labs.genesis.connexion.model.ColumnMetadata;
import org.labs.genesis.connexion.model.TableMetadata;
import org.labs.genesis.engine.GenesisTemplateEngine;
import org.labs.genesis.frontend.FrontendLanguage;
import org.labs.genesis.frontend.generator.FrontendFramework;
import org.labs.genesis.frontend.generator.model.Component;
import org.labs.genesis.frontend.generator.model.ServiceComponent;
import org.labs.utils.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FrameworkFrontendMetadataProvider {
    private static final GenesisTemplateEngine engine = new GenesisTemplateEngine();


    public static HashMap<String, Object> getHashMapIntermediaire(TableMetadata tableMetadata,String destinationFolder,String projectName) {
        HashMap<String, Object> metadata = new HashMap<>();

        metadata.put("fields", getFieldsList(tableMetadata));
        metadata.put("fieldsPK", getFieldsPKList(tableMetadata));
        metadata.put("fieldsFK", getFieldsFKList(tableMetadata));
        metadata.put("EntityName",tableMetadata.getTableName());

        metadata.putAll(getHashMapComponentSavePath(destinationFolder, projectName, tableMetadata));

        return metadata;
    }


    public static HashMap<String,Object> getHashMapComponentSavePath(String destinationFolder,String projectName,TableMetadata tableMetadata)
    {
        HashMap<String, Object> metadata = new HashMap<>();

        metadata.put("destinationFolder", destinationFolder);
        metadata.put("projectName",projectName);
        metadata.put("EntityName",tableMetadata.getTableName());
        return metadata;
    }

    public  static String  getWebappFolder(ProjectGenerationContext context){
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("destinationFolder", context.getDestinationFolder());
        metadata.put("projectName", context.getProjectName());
        metadata.put("webappFolder", context.getWebappFolder());
        String webappFolder = engine.simpleRender(Constantes.WEBAPP_DIR_TEMPLATE, metadata);
        return  webappFolder;
    }

    private static List<Map<String, Object>> getFieldsList(TableMetadata tableMetadata) {
        List<Map<String, Object>> fields = new ArrayList<>();
        for (ColumnMetadata field : tableMetadata.getColumns()) {
            Map<String, Object> fieldMap = getFieldHashMap(field);
            fields.add(fieldMap);
        }
        return fields;
    }
    public static @NotNull Map<String, Object> getFieldHashMap(ColumnMetadata field) {
        Map<String, Object> fieldMap = new HashMap<>();

        fieldMap.put("typeBase", field.getFrontEndType());
        fieldMap.put("type",field.getType());
        fieldMap.put("name", field.getName());
        fieldMap.put("isPrimaryKey", field.isPrimary());
        fieldMap.put("isForeignKey", field.isForeign());
        fieldMap.put("columnType", field.getColumnType());
        fieldMap.put("columnName", field.getReferencedColumn());
        fieldMap.put("referencedColumnType", field.getFrontEndReferencedColumnType());
        fieldMap.put("columnNameField", StringUtils.toCamelCase(field.getReferencedColumn()));
        fieldMap.put("defaultValue", field.getDefaultValue());
        fieldMap.put("columnSize", field.getColumnSize());
        fieldMap.put("decimalDigits", field.getDecimalDigits());
        fieldMap.put("isUnique", field.isUnique());
        fieldMap.put("isNullable", field.isNullable());
        fieldMap.put("isIntAndPrimaryKey", field.isNumeric() && field.isPrimary());

        return fieldMap;
    }

    public static  HashMap<String,Object> getServiceHashMap(ServiceComponent serviceComponent, FrontendLanguage frontendLanguage, TableMetadata tableMetadata)
    {
        HashMap<String, Object> data = new HashMap<>();

        data.put("importFile",serviceComponent.getImports());
        data.put("export",serviceComponent.getMethods());

        return data;
    }

    public static HashMap<String,Object> getComponentHashMap(Component component, FrontendLanguage frontendLanguage, TableMetadata tableMetadata)
    {
        HashMap<String, Object> data = new HashMap<>();

        data.put("importFile",component.getImportFile());
        data.put("selector",component.getSelector());
        data.put("standalone",component.getStandalone());
        data.put("importComponent",component.getImportComponent());
        data.put("template",component.getTemplate());
        data.put("style",component.getStyle());
        data.put("export",component.getExport());

        return data;
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
}

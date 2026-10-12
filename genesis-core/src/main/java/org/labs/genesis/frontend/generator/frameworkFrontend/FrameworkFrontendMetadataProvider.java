package org.labs.genesis.frontend.generator.frameworkFrontend;

import org.jetbrains.annotations.NotNull;
import org.labs.genesis.config.Constantes;
import org.labs.genesis.config.ProjectGenerationContext;
import org.labs.genesis.config.form.FieldConfiguration;
import org.labs.genesis.config.langage.generator.framework.MereFilleMetadataProvider;
import org.labs.genesis.connexion.model.ColumnMetadata;
import org.labs.genesis.connexion.model.TableMetadata;
import org.labs.genesis.engine.GenesisTemplateEngine;
import org.labs.genesis.frontend.FrontendLanguage;
import org.labs.genesis.frontend.generator.FrontendFramework;
import org.labs.genesis.frontend.generator.model.*;
import org.labs.utils.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FrameworkFrontendMetadataProvider {
    private static final GenesisTemplateEngine engine = new GenesisTemplateEngine();


    public static HashMap<String, Object> getHashMapForSecurity(String securityType,ProjectGenerationContext context) {
        HashMap<String, Object> metadata = getHashMapForSecurity(securityType);
        metadata.putAll(getWebappHashMap(context));
        return metadata;
    }

    public static HashMap<String, Object> getHashMapForSecurity(String securityType) {
        HashMap<String, Object> metadata = new HashMap<>();
        if(securityType.contains("JWT")) {
            metadata.put("useJWT",true);
        }else
        {
            metadata.put("useJWT",false);
        }
        return metadata;
    }

    public static HashMap<String, Object> getHashMapIntermediaire(TableMetadata tableMetadata, String destinationFolder, String projectName) {
        return getHashMapIntermediaire(tableMetadata, destinationFolder, projectName, null);
    }

    public static HashMap<String, Object> getHashMapIntermediaire(
            TableMetadata tableMetadata,
            String destinationFolder,
            String projectName,
            Map<String, Map<String, FieldConfiguration>> formConfiguration) {
        HashMap<String, Object> metadata = new HashMap<>();

        List<Map<String,Object>> fkList=getFieldsFKList(tableMetadata, formConfiguration);
        List<Map<String, Object>> fields = getFieldsList(tableMetadata, formConfiguration);
        boolean containsFile = fields.stream()
                .anyMatch(field -> "file".equalsIgnoreCase(String.valueOf(field.get("uiType"))));
        boolean useMap = fields.stream()
            .anyMatch(field -> "map".equalsIgnoreCase(String.valueOf(field.get("uiType"))));
        boolean useCurrency = fields.stream()
                .anyMatch(field -> "currency".equalsIgnoreCase(String.valueOf(field.get("uiType"))));
        boolean usePhone = fields.stream()
                .anyMatch(field -> "phone".equalsIgnoreCase(String.valueOf(field.get("uiType"))));
        boolean useQuantity = fields.stream()
                .anyMatch(field -> "quantity".equalsIgnoreCase(String.valueOf(field.get("uiType"))));
        boolean useTextarea = fields.stream()
                .anyMatch(field -> {
                    String uiType = String.valueOf(field.get("uiType"));
                    return "textarea".equalsIgnoreCase(uiType)
                            || "ckeditor".equalsIgnoreCase(uiType)
                            || "json-editor".equalsIgnoreCase(uiType);
                });
        metadata.put("fields", fields);
        metadata.put("containsFile", containsFile);
        metadata.put("useMap", useMap);
        metadata.put("useCurrency", useCurrency);
        metadata.put("usePhone", usePhone);
        metadata.put("useQuantity", useQuantity);
        metadata.put("useTextarea", useTextarea);
        metadata.put("fieldsPK", getFieldsPKList(tableMetadata, formConfiguration));
        metadata.put("fieldsFK", fkList);
        metadata.put("simpleFields",getNotFkAndPKFieldsList(tableMetadata, formConfiguration));
        metadata.put("fieldsNotFK",getNotFkFieldsList(tableMetadata, formConfiguration));
        metadata.put("containsForeignKey",!fkList.isEmpty());
        metadata.put("EntityName",tableMetadata.getTableName());
        metadata.put("isView",tableMetadata.getIsView());
        metadata.put("className",tableMetadata.getClassName());
        metadata.put("classNameLink",tableMetadata.getClassName()+"s");

        metadata.putAll(getHashMapComponentSavePath(destinationFolder, projectName, tableMetadata));
        metadata.putAll(MereFilleMetadataProvider.getRelationsHashMap(tableMetadata));

        return metadata;
    }


    public static HashMap<String,Object> getHashMapComponentSavePath(String destinationFolder,String projectName,TableMetadata tableMetadata)
    {
        HashMap<String, Object> metadata = new HashMap<>();

        metadata.put("destinationFolder", destinationFolder);
        metadata.put("projectName",projectName);
        metadata.put("EntityName",tableMetadata.getClassName());
        return metadata;
    }

    public  static String  getWebappFolder(ProjectGenerationContext context){
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("destinationFolder", context.getDestinationFolder());
        metadata.put("projectName", context.getProjectName());
        metadata.put("webappFolder", context.getWebappFolder());
        return engine.simpleRender(Constantes.WEBAPP_DIR_TEMPLATE, metadata);
    }

    public  static  HashMap<String, Object> getInterfaceLangHashMap(InterfaceLang lang){
        HashMap<String, Object> metadata = new HashMap<>();
        metadata.put("name", lang.getName());
        metadata.put("locale", lang.getLocale());
        return metadata;
    }

    public static List<Map<String, Object>> getInerfaceLangList(List<InterfaceLang> langs) {
        List<Map<String, Object>> langList = new ArrayList<>();
        for (InterfaceLang field : langs) {
            Map<String, Object> fieldMap = getInterfaceLangHashMap(field);
            langList.add(fieldMap);
        }
        return langList;
    }

    public static HashMap<String, Object> getLayoutHashMap(FrontendFramework frontendFramework){
        FrontendLayout layout = frontendFramework.getFrontendLayout();
        if (layout == null){
            layout = new FrontendLayout();
        }
        return getLayoutHashMap(layout);
    }

    public static HashMap<String, Object> getLayoutHashMap(FrontendLayout layout){
        HashMap<String, Object> metadata = new HashMap<>();
        metadata.put("additionalCss",layout.additionalCss);
        metadata.put("primaryColor",layout.primaryColor);
        metadata.put("secondaryColor",layout.secondaryColor);
        metadata.put("navbarPreference",layout.navbar);
        metadata.put("langList",getInerfaceLangList(layout.langs));
        return  metadata;
    }

    public static HashMap<String, Object> getBrandingHashMap(FrontendFramework frontendFramework){
        ProjectBranding branding = frontendFramework.getProjectBranding();
        if (branding == null){
            branding = new ProjectBranding();
        }
        return getBrandingHashMap(branding);
    }

    public static HashMap<String, Object> getBrandingHashMap(ProjectBranding branding){
        HashMap<String, Object> metadata = new HashMap<>();
        metadata.put("faviconUrl", branding.getFaviconUrl());
        metadata.put("useFaviconLink", branding.useFaviconLink());
        metadata.put("hasFavicon", branding.hasFavicon());

        metadata.put("logoUrl",branding.getLogoUrl());
        metadata.put("useLogoLink", branding.useLogoLink());
        metadata.put("hasLogo", branding.hasLogo());

        return  metadata;
    }
    public static HashMap<String, Object> getWebappHashMap(ProjectGenerationContext context){
        HashMap<String, Object> metadata = new HashMap<>();
        String webappFolder = getWebappFolder(context);
        metadata.put("destinationFolder", webappFolder);
        metadata.put("projectName", context.getProjectName());
        metadata.put("projectPort", context.getProjectPort());
        metadata.put("title",context.getProjectDescription());

        return metadata;
    }

    private static List<Map<String, Object>> getFieldsList(TableMetadata tableMetadata) {
        return getFieldsList(tableMetadata, null);
    }

    private static List<Map<String, Object>> getFieldsList(
            TableMetadata tableMetadata,
            Map<String, Map<String, FieldConfiguration>> formConfiguration) {
        List<Map<String, Object>> fields = new ArrayList<>();
        for (ColumnMetadata field : tableMetadata.getColumns()) {
            Map<String, Object> fieldMap = getFieldHashMap(field, tableMetadata.getTableName(), formConfiguration);
            fields.add(fieldMap);
        }
        return fields;
    }
    private static List<Map<String, Object>> getNotFkFieldsList(TableMetadata tableMetadata) {
        return getNotFkFieldsList(tableMetadata, null);
        }

        private static List<Map<String, Object>> getNotFkFieldsList(
            TableMetadata tableMetadata,
            Map<String, Map<String, FieldConfiguration>> formConfiguration) {
        List<Map<String, Object>> fields = new ArrayList<>();
        for (ColumnMetadata field : tableMetadata.getColumns()) {
            if(!field.isForeign()) {
                Map<String, Object> fieldMap = getFieldHashMap(field, tableMetadata.getTableName(), formConfiguration);
                fields.add(fieldMap);
            }
        }
        return fields;
    }
    private static List<Map<String, Object>> getNotFkAndPKFieldsList(TableMetadata tableMetadata) {
        return getNotFkAndPKFieldsList(tableMetadata, null);
    }

    private static List<Map<String, Object>> getNotFkAndPKFieldsList(
        TableMetadata tableMetadata,
        Map<String, Map<String, FieldConfiguration>> formConfiguration) {
        List<Map<String, Object>> fields = new ArrayList<>();
        for (ColumnMetadata field : tableMetadata.getColumns()) {
            if (!field.isForeign() && !(field.isPrimary() && field.isAutoGenerated())) {
                fields.add(getFieldHashMap(field, tableMetadata.getTableName(), formConfiguration));
            }
        }
        return fields;
    }
    public static @NotNull Map<String, Object> getFieldHashMap(ColumnMetadata field) {
        return getFieldHashMap(field, null, null);
        }

        public static @NotNull Map<String, Object> getFieldHashMap(
            ColumnMetadata field,
            String tableName,
            Map<String, Map<String, FieldConfiguration>> formConfiguration) {
        Map<String, Object> fieldMap = new HashMap<>();

        fieldMap.put("typeBase", field.getFrontEndType());
        String uiType = field.getUiType();

        if (field.isDateTime() || field.isDateTimeTz()) {
            uiType = "datetime-local"; // DateTime devient ceci
        } else if (field.isTime()  || field.isTimeTz()) {
            uiType = "time"; // Time devient ceci
        } else if (field.isDate()) {
            uiType = "date"; // date reste date
        } else if (field.isNumeric()) {
            uiType = "number"; // Au cas où
        } else if (field.isGeographic()) {
            uiType = "map";
        }

            FieldConfiguration configuredField = formConfiguration == null || tableName == null
                ? null
                : formConfiguration.getOrDefault(tableName, Map.of()).get(field.getReferencedColumn());
        if (configuredField != null) {
            fieldMap.put("input-config", configuredField.getInputConfiguration());
            fieldMap.put("accepted-file", configuredField.getFileConfiguration());
            fieldMap.put("currency", configuredField.getCurrency());
            fieldMap.put("minValue", getConfigurationValue(configuredField, "min"));
            fieldMap.put("maxValue", getConfigurationValue(configuredField, "max"));
            fieldMap.put("stepValue", getConfigurationValue(configuredField, "step"));
            fieldMap.put("currencyCode", getConfigurationValue(configuredField, "currency"));
            fieldMap.put("acceptedExtensions", getConfigurationValue(configuredField, "accept"));
            String latitude = configuredField.getLatitude();
            if(latitude != null) {
                fieldMap.put("longitude", "${minStart(this.name)}");
                fieldMap.put("latitude", StringUtils.minStart(StringUtils.toCamelCase(latitude)));
            } else {
                String longitude = configuredField.getLongitude();
                if(longitude != null) {
                    fieldMap.put("longitude", StringUtils.minStart(StringUtils.toCamelCase(longitude)));
                    fieldMap.put("latitude", "${minStart(this.name)}");
                }
            }
            if (configuredField.getComponent() != null && !configuredField.getComponent().isBlank()) {
                uiType = configuredField.getComponent();
            }
        }

        if (uiType == null || uiType.isBlank()) {
            throw new IllegalArgumentException(
                    "Type frontend non reconnu pour le champ : "
                            + field.getName()
                            + " (type SQL : " + field.getColumnType() + ")"
            );
        }

        fieldMap.put("uiType", uiType);
        fieldMap.put("type",field.getType());
        fieldMap.put("name", field.getName());
        fieldMap.put("isPrimaryKey", field.isPrimary());
        fieldMap.put("isAutoGenerated", field.isAutoGenerated());
        fieldMap.put("isGeneratedPrimaryKey", field.isPrimary() && field.isAutoGenerated());
        fieldMap.put("isManualPrimaryKey", field.isPrimary() && !field.isAutoGenerated());
        fieldMap.put("isForeignKey", field.isForeign());
        fieldMap.put("columnType", field.getColumnType());
        fieldMap.put("columnName", field.getReferencedColumn());
        fieldMap.put("referencedColumnType", field.getFrontEndReferencedColumnType());
        fieldMap.put("referencedColumn", StringUtils.minStart(StringUtils.toPascalCase(field.getReferencedColumn())));
        fieldMap.put("referencedPrimaryKeyColumn", field.getReferencedPrimaryKeyColumn());
        fieldMap.put("columnNameField", StringUtils.toCamelCase(field.getReferencedColumn()));
        fieldMap.put("defaultValue", field.getDefaultValue());
        fieldMap.put("columnSize", field.getColumnSize());
        fieldMap.put("decimalDigits", field.getDecimalDigits());
        fieldMap.put("isUnique", field.isUnique());
        fieldMap.put("isNullable", field.isNullable());
        fieldMap.put("isRequired", field.isPrimary() || !field.isNullable());
        fieldMap.put("isNumeric",field.isNumeric());
        fieldMap.put("isDate",field.isDate());
        fieldMap.put("isTime",field.isTime());
        fieldMap.put("isTimeTz",field.isTimeTz());
        fieldMap.put("isDateTime",field.isDateTime());
        fieldMap.put("isDateTimeTz",field.isDateTimeTz());
        fieldMap.put("useTimeZone",field.isUseTimeZone());
        fieldMap.put("isInterval",field.isInterval());
        fieldMap.put("isGeographic",field.isGeographic());
        fieldMap.put("isText", field.isText() || "text".equalsIgnoreCase(uiType));
        fieldMap.put("isNotForeignKey",!field.isForeign());
        fieldMap.put("isIntAndPrimaryKey", field.isNumeric() && field.isPrimary() && field.isAutoGenerated());
        fieldMap.put("isParentForeignKey",field.getIsParentForeignKey());

        return fieldMap;
    }

    private static String getConfigurationValue(FieldConfiguration fieldConfiguration, String name) {
        if (fieldConfiguration.getConfiguration() == null) {
            return null;
        }
        FieldConfiguration.Configuration configuration =
                fieldConfiguration.getConfiguration().get(name);
        return configuration == null ? null : configuration.getValue();
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
        data.put("router",getRouteHashMap(component.getRouter()));

        return data;
    }

    public static HashMap<String,Object> getModelHashMap(ModelComponent model, FrontendLanguage frontendLanguage, TableMetadata tableMetadata)
    {
        HashMap<String, Object> data = new HashMap<>();

        data.put("imports",model.getImports());
        data.put("exports",model.getExports());

        return data;
    }

    public static HashMap<String,Object> getTableMetaDataHashSimple(TableMetadata tableMetadata)
    {
        HashMap<String, Object> data = new HashMap<>();
        data.put("name",tableMetadata.getTableName());
        data.put("className",tableMetadata.getClassName());
        data.put("isView",tableMetadata.getIsView());
        return  data;
    }
    private static List<Map<String, Object>> getTableMetaDataHashSimpleList(List<TableMetadata> tableMetadatas) {
        List<Map<String, Object>> tableMetadatasAns = new ArrayList<>();
        for (TableMetadata tableMetadata : tableMetadatas) {
            Map<String, Object> tableMetadataMap = getTableMetaDataHashSimple(tableMetadata);
            tableMetadatasAns.add(tableMetadataMap);
        }
        return tableMetadatasAns;
    }
    public  static HashMap<String, Object> getGlobalComponentsHashMap(List<TableMetadata> tableMetadatas, ProjectGenerationContext context){
        FrontendFramework frontendFramework = context.getFrontendFramework();
        String projectPort = context.getProjectPort() ;

        HashMap<String, Object> data = new HashMap<>();
        data.put("routes",getRoutesHashMap(frontendFramework));
        data.put("entities",getTableMetaDataHashSimpleList(tableMetadatas));
        data.put("port",projectPort);
        data.put("frontendPort",context.getFrontendPort());
        data.put("apiUrl", "localhost");
        data.putAll(getRessourceHashMap(frontendFramework));
        HashMap<String,Object> folder=FrameworkFrontendMetadataProvider.getWebappHashMap(context);
        data.putAll(folder);
        return  data;
    }

    public static HashMap<String, Object> getRessourceHashMap(FrontendFramework frontendFramework) {
        HashMap<String, Object> metadata = new HashMap<>();
        if (frontendFramework == null) {
            return metadata;
        }
        if (frontendFramework.getProjectBranding() != null) {
            metadata.putAll(getBrandingHashMap(frontendFramework.getProjectBranding()));
        }
        if (frontendFramework.getFrontendLayout() != null) {
            metadata.putAll(getLayoutHashMap(frontendFramework.getFrontendLayout()));
        }
        return  metadata;
    }

    public static HashMap<String, Object> getLangsHashMap(ProjectGenerationContext context, List<TableMetadata> tableMetadatas) {
        HashMap<String, Object> metadata = new HashMap<>(getWebappHashMap(context));
        metadata.put("entities",getTableMetaDataHashSimpleList(tableMetadatas));
        return  metadata;
    }

    public  static  HashMap<String,Object> getRouteHashMap(ComponentRoute route){
        HashMap<String, Object> data = new HashMap<>();
        data.put("componentName", route.getComponentName());
        data.put("componentSelector", route.getComponentSelector());
        data.put("routerLink", route.getLink());
        data.put("componentImport", route.getComponentImport());
        data.put("componentImportWithoutExtension", route.getComponentImportWithoutExtension());
        data.put("routerLabel", route.getLabel());
        data.put("hasLabel", route.hasLabel());
        data.put("entityName", route.getEntityName());
        return  data;
    }

    public  static  List<Map<String,Object>> getRoutesHashMap(FrontendFramework frontendFramework){
        List<Map<String, Object>> routes = new ArrayList<>();
        if (frontendFramework == null || frontendFramework.getComponentRoutes() == null) {
            return routes;
        }
        for (ComponentRoute route : frontendFramework.getComponentRoutes()) {
            routes.add(getRouteHashMap(route));
        }
        return  routes;
    }

    private static List<Map<String, Object>> getFieldsPKList(TableMetadata tableMetadata) {
        return getFieldsPKList(tableMetadata, null);
    }

    private static List<Map<String, Object>> getFieldsPKList(
            TableMetadata tableMetadata,
            Map<String, Map<String, FieldConfiguration>> formConfiguration) {
        List<Map<String, Object>> fieldsPK = new ArrayList<>();
        for (ColumnMetadata field : tableMetadata.getColumns()) {
            if (field.isPrimary()) {
                Map<String, Object> fieldMap = getFieldHashMap(field, tableMetadata.getTableName(), formConfiguration);
                fieldsPK.add(fieldMap);
            }
        }
        return fieldsPK;
    }

    private static List<Map<String, Object>> getFieldsFKList(TableMetadata tableMetadata) {
        return getFieldsFKList(tableMetadata, null);
    }

    private static List<Map<String, Object>> getFieldsFKList(
            TableMetadata tableMetadata,
            Map<String, Map<String, FieldConfiguration>> formConfiguration) {
        List<Map<String, Object>> fieldsFK = new ArrayList<>();
        for (ColumnMetadata field : tableMetadata.getColumns()) {
            if (field.isForeign()) {
                Map<String, Object> fieldMap = getFieldHashMap(field, tableMetadata.getTableName(), formConfiguration);
                fieldsFK.add(fieldMap);
            }
        }
        return fieldsFK;
    }
}

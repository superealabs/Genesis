package org.labs.genesis.connexion.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.labs.genesis.config.langage.generator.framework.FrameworkMetadataProvider;
import org.labs.genesis.connexion.Database;
import org.labs.genesis.engine.GenesisTemplateEngine;
import org.labs.genesis.frontend.FrontendLanguage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Setter
@Getter
@ToString
public class ColumnMetadata {
    private String name;
    private String type;
    private String frontEndType;
    private String uiType;
    private String frontEndReferencedColumnType;
    private String databaseColumnType;
    private boolean primary;
    private boolean foreign;
    private String referencedTable;
    private String columnType;
    private String referencedColumn;
    private String referencedColumnType;
    private String referencedPrimaryKeyColumn;
    private boolean unique;
    private boolean nullable;
    private boolean isNumeric;
    private boolean isNumericWithPrecision;
    private boolean isText;
    private boolean isDate;
    private boolean isTime;
    private boolean isTimeTz;
    private boolean isDateTime;
    private boolean useTimeZone;
    private boolean isInterval;
    private String defaultValue;
    private int decimalDigits;
    private int columnSize;
    private Map<String, Object> validationAnnotations = new HashMap<>();

    public void setFrontEndType(FrontendLanguage frontendLanguage, Database database)
    {
        this.frontEndType = frontendLanguage.getTypes().get(database.getTypes().get(columnType));
        setUiType(frontendLanguage);
    }

    public void setFrontEndReferencedColumnType(FrontendLanguage frontendLanguage, Database database)
    {
        this.frontEndReferencedColumnType = frontendLanguage.getTypes().get(database.getTypes().get(databaseColumnType));
    }

    public void setUiType(FrontendLanguage frontendLanguage)
    {
        this.uiType = frontendLanguage.getInputTypes().get(this.getFrontEndType());
    }

    public void setNullable(String nullable, Map<String, Object> frameworkValidationAnnotations, GenesisTemplateEngine engine) throws Exception {
        if(nullable.equalsIgnoreCase("YES")){
            this.nullable = true;
            return;
        }
        this.nullable = false;
        Map<String, Object> fieldHashMap = FrameworkMetadataProvider.getFieldHashMap(this);
        String annotationTemplate = (String) frameworkValidationAnnotations.getOrDefault("notNull","{{removeLine}}");
        String annotationResult = engine.render(annotationTemplate, fieldHashMap);
        this.validationAnnotations.put("notNull",annotationResult);
    }

    public void setDefaultValue(String defaultValue, Map<String, Object> frameworkValidationAnnotations, GenesisTemplateEngine engine) throws Exception {
        this.defaultValue = defaultValue;
        if (defaultValue!=null &&
                (!this.isDate
                || !this.isTime
                || !this.isDateTime
                || !this.isInterval)
            )
        {
            Map<String, Object> fieldHashMap = FrameworkMetadataProvider.getFieldHashMap(this);
            String annotationTemplate = (String) frameworkValidationAnnotations.getOrDefault("defaultValue","{{removeLine}}");
            String annotationResult = engine.render(annotationTemplate, fieldHashMap);
            this.validationAnnotations.put("defaultValue",annotationResult);
        }
    }

    public void setColumnSize(int columnSize, Map<String, Object> frameworkValidationAnnotations, GenesisTemplateEngine engine) throws Exception {
        this.columnSize = columnSize;
        if (this.isText) {
            Map<String, Object> fieldHashMap = FrameworkMetadataProvider.getFieldHashMap(this);
            String annotationTemplate = (String) frameworkValidationAnnotations.getOrDefault("maxSize","{{removeLine}}");
            String annotationResult = engine.render(annotationTemplate, fieldHashMap);
            this.validationAnnotations.put("maxSize",annotationResult);
        }
    }

    public void setDecimalDigits(int decimalDigits, Map<String, Object> frameworkValidationAnnotations, GenesisTemplateEngine engine) throws Exception {
        this.decimalDigits = decimalDigits;
        if(this.isNumericWithPrecision){
            Map<String, Object> fieldHashMap = FrameworkMetadataProvider.getFieldHashMap(this);
            String annotationTemplate = (String) frameworkValidationAnnotations.getOrDefault("digits","{{removeLine}}");
            String annotationResult = engine.render(annotationTemplate, fieldHashMap);
            this.validationAnnotations.put("digits",annotationResult);
        }
    }

    public void removeSeparateMinMaxAnnotations(){
        this.validationAnnotations.remove("numericMaximumInclusiveValue");
        this.validationAnnotations.remove("numericMinimumInclusiveValue");
        this.validationAnnotations.remove("numericMaximumValue");
        this.validationAnnotations.remove("numericMinimumValue");
    }

    public void checkAndCreateRangeAnnotation(Map<String, Object> frameworkValidationAnnotations,
                                              Map<String, Object> fieldHashMap,
                                              GenesisTemplateEngine engine,
                                              String currentValue,
                                              boolean isMin) throws Exception {

        if ((isMin && hasMaxConstraint()) || (!isMin && hasMinConstraint())) {
            if (frameworkValidationAnnotations.containsKey("numericMinimumAndMaximumValue")) {
                Object otherValue = getOppositeBoundValue(isMin);
                if (otherValue != null) {
                    String annotationTemplate = (String) frameworkValidationAnnotations.getOrDefault("numericMinimumAndMaximumValue", "{{removeLine}}");
                    fieldHashMap.put("minValue", isMin ? currentValue : otherValue);
                    fieldHashMap.put("maxValue", isMin ? otherValue : currentValue);
                    String annotationResult = engine.render(annotationTemplate, fieldHashMap);
                    validationAnnotations.put("numericMinimumAndMaximumValue", annotationResult);
                    removeSeparateMinMaxAnnotations();
                }
            }
        }
    }

    private Object getOppositeBoundValue(boolean isMin) {
        if (isMin) {
            return validationAnnotations.containsKey("numericMaximumInclusiveValue")
                    ? validationAnnotations.get("numericMaximumInclusiveValueData")
                    : validationAnnotations.get("numericMaximumValueData");
        } else {
            return validationAnnotations.containsKey("numericMinimumInclusiveValue")
                    ? validationAnnotations.get("numericMinimumInclusiveValueData")
                    : validationAnnotations.get("numericMinimumValueData");
        }
    }

    private boolean hasMinConstraint() {
        return validationAnnotations.containsKey("numericMinimumInclusiveValue")
                || validationAnnotations.containsKey("numericMinimumValue");
    }

    private boolean hasMaxConstraint() {
        return validationAnnotations.containsKey("numericMaximumInclusiveValue")
                || validationAnnotations.containsKey("numericMaximumValue");
    }

    public void checkAndCreateNotNullNotBlankCombinedAnnotation(
            Map<String, Object> frameworkValidationAnnotations,
            Map<String, Object> fieldHashMap,
            GenesisTemplateEngine engine) throws Exception {

        if (validationAnnotations.containsKey("notNull")
                && frameworkValidationAnnotations.containsKey("notNullAndNotBlank")) {

            String annotationTemplate = (String) frameworkValidationAnnotations.getOrDefault(
                    "notNullAndNotBlank", "{{removeLine}}"
            );
            String annotationResult = engine.render(annotationTemplate, fieldHashMap);
            validationAnnotations.put("notNullAndNotBlank", annotationResult);
            validationAnnotations.remove("notNull");
            validationAnnotations.remove("notBlank");
        }
    }
}

package org.labs.genesis.connexion.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.labs.genesis.config.langage.generator.framework.FrameworkMetadataProvider;
import org.labs.genesis.engine.GenesisTemplateEngine;

import java.util.HashMap;
import java.util.Map;

@Setter
@Getter
@ToString
public class ColumnMetadata {
    private String name;
    private String type;
    private boolean primary;
    private boolean foreign;
    private String referencedTable;
    private String columnType;
    private String referencedColumn;
    private String referencedColumnType;
    private boolean unique;
    private boolean nullable;
    private boolean isNumeric;
    private boolean isNumericWithPrecision;
    private boolean isText;
    private boolean isDate;
    private String defaultValue;
    private int decimalDigits;
    private int columnSize;
    private Map<String, Object> validationAnnotations=new HashMap<>();

    private boolean hasStrictPastDateConstraint;
    private boolean hasPastDateConstraint;

    private boolean hasStrictFutureDateConstraint;
    private boolean hasFutureDateConstraint;

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
        if (defaultValue!=null && !this.isDate){
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
}

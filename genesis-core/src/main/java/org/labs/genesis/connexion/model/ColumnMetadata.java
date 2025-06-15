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

    public void setNullable(String nullable) {
        if(nullable.equalsIgnoreCase("YES")){
            this.nullable = true;
            return;
        }
        this.nullable = false;
    }
}

package org.labs.genesis.connexion.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

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
    private boolean nullable;
    private String defaultValue;
    private int decimalDigits;
    private int columnSize;
    private boolean unique;
    private boolean isNumeric;
    private boolean isNumericWithPrecision;
    private boolean isText;
    private boolean isDate;

    private boolean hasStrictMinimumConstraint;
    private String strictMinimumConstraint;

    private boolean hasMinimumConstraint;
    private String minimumConstraint;

    private boolean hasStrictMaximumConstraint;
    private String strictMaximumConstraint;

    private boolean hasMaximumConstraint;
    private String maximumConstraint;

    private boolean hasStrictPastDateConstraint;
    private boolean hasPastDateConstraint;

    private boolean hasStrictFutureDateConstraint;
    private boolean hasFutureDateConstraint;
    private boolean hasNotBlankConstraint;

    private boolean hasMinimumLengthConstraint;
    private String minimumLengthConstraint;

    private boolean hasRegexConstraint;
    private String regexConstraint;

    public void setNullable(String nullable) {
        if(nullable.equalsIgnoreCase("YES")){
            this.nullable = true;
            return;
        }
        this.nullable = false;
    }
}

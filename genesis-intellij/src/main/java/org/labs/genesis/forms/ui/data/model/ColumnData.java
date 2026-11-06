package org.labs.genesis.forms.ui.data.model;

public class ColumnData {
    public final String name;
    public final String type;
    public final boolean isPrimaryKey;
    public final boolean isNullable;
    public final boolean isGeographic;
    public ColumnData(String name, String type, boolean isPrimaryKey, boolean isNullable) {
        this(name, type, isPrimaryKey, isNullable, false);
    }
    public ColumnData(String name, String type, boolean isPrimaryKey, boolean isNullable, boolean isGeographic) {
        this.name = name;
        this.type = type;
        this.isPrimaryKey = isPrimaryKey;
        this.isNullable = isNullable;
        this.isGeographic = isGeographic;
    }
    @Override
    public String toString() { return name + "  " + type; }
}

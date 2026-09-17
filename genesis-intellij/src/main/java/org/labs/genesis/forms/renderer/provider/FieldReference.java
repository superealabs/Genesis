package org.labs.genesis.forms.renderer.provider;

import java.util.Objects;

/** A qualified database field selected by a visualization. */
public record FieldReference(
        String datasource,
        String schema,
        String table,
        String column,
        String dataType
) {
    public FieldReference {
        if (table == null || table.isBlank() || column == null || column.isBlank()) {
            throw new IllegalArgumentException("A field must contain a table and column");
        }
    }

    public String qualifiedName() {
        return table + "." + column;
    }

    public static FieldReference parse(String value, String defaultTable) {
        if (value == null || value.isBlank()) return null;
        String raw = value.trim();
        if (raw.regionMatches(true, 0, "COLUMN:", 0, 7)) raw = raw.substring(7).trim();
        String[] parts = raw.split("\\.");
        if (parts.length >= 2) {
            String table = parts[parts.length - 2];
            String column = parts[parts.length - 1];
            String schema = parts.length >= 3 ? parts[parts.length - 3] : null;
            String datasource = parts.length >= 4 ? parts[parts.length - 4] : null;
            return new FieldReference(datasource, schema, table, column, null);
        }
        return defaultTable == null || defaultTable.isBlank()
                ? null
                : new FieldReference(null, null, defaultTable, raw, null);
    }

    @Override
    public String toString() {
        return qualifiedName();
    }
}

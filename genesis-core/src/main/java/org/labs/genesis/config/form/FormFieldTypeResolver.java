package org.labs.genesis.config.form;

import org.labs.genesis.connexion.model.ColumnMetadata;

import java.util.List;
import java.util.Map;

/**
 * Catégorie de formulaire d'une colonne (clé de {@code types} dans form-field-configuration.json).
 *
 * Logique partagée par le plugin IntelliJ (FormConfigurationForm) et genesis-api,
 * pour que les deux proposent exactement les mêmes composants pour une colonne.
 */
public final class FormFieldTypeResolver {

    public static final String TYPE_NUMBER = "number";
    public static final String TYPE_TEXT = "text";
    public static final String TYPE_BINARY = "binary";
    public static final String TYPE_JSON = "json";
    public static final String TYPE_GEOGRAPHIC = "geographic";

    private FormFieldTypeResolver() {
    }

    /**
     * @return la catégorie ("number", "text", "binary", "json", "geographic") ou null
     *         si la colonne n'est pas configurable (clé étrangère, type inconnu).
     */
    public static String resolve(ColumnMetadata column) {
        if (column == null || column.isForeign()) {
            return null;
        }


        String sqlType = column.getDatabaseColumnType() != null
                ? column.getDatabaseColumnType()
                : column.getColumnType();

        if (sqlType != null) {
            String type = sqlType.toLowerCase();

            if (column.isGeographic()) {
                return TYPE_GEOGRAPHIC;
            }
            if (type.contains("json")) {
                return TYPE_JSON;
            }
            if (type.contains("bytea") || type.contains("binary") || type.contains("blob")) {
                return TYPE_BINARY;
            }
        }

        if (column.isNumeric()) {
            return TYPE_NUMBER;
        }
        if (column.isText()) {
            return TYPE_TEXT;
        }
        return null;
    }

    public static List<String> components(ColumnMetadata column, FormConfiguration catalog) {
        String type = resolve(column);
        if (type == null || catalog == null || catalog.getTypes() == null) {
            return List.of();
        }
        FormConfiguration.Type entry = catalog.getTypes().get(type);
        return entry == null || entry.getComponents() == null ? List.of() : List.copyOf(entry.getComponents());
    }

    public static boolean isConfigurable(ColumnMetadata column, FormConfiguration catalog) {
        String type = resolve(column);
        Map<String, FormConfiguration.Type> types = catalog == null ? null : catalog.getTypes();
        return type != null && types != null && types.containsKey(type);
    }
}

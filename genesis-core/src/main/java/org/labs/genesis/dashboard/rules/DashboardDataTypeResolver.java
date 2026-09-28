package org.labs.genesis.dashboard.rules;

import org.labs.genesis.connexion.model.ColumnMetadata;
import org.labs.genesis.dashboard.model.DashboardDataType;

import java.util.Locale;

/**
 * Converts Genesis ColumnMetadata into a generic dashboard data type.
 */
public final class DashboardDataTypeResolver {

    private DashboardDataTypeResolver() {
    }

    public static DashboardDataType resolve(ColumnMetadata column) {

        if (column == null) {
            return DashboardDataType.OTHER;
        }

        if (column.isNumeric()) {
            return DashboardDataType.NUMERIC;
        }

        if (column.isDate()
                || column.isTime()
                || column.isTimeTz()
                || column.isDateTime()
                || column.isDateTimeTz()) {
            return DashboardDataType.TEMPORAL;
        }

        if (isBoolean(column)) {
            return DashboardDataType.BOOLEAN;
        }

        if (column.isText()) {
            return DashboardDataType.TEXT;
        }

        return DashboardDataType.OTHER;
    }

    private static boolean isBoolean(ColumnMetadata column) {
        return isBooleanType(column.getColumnType())
                || isBooleanType(column.getType())
                || isBooleanType(column.getDatabaseColumnType());
    }

    private static boolean isBooleanType(String type) {

        if (type == null || type.isBlank()) {
            return false;
        }

        String normalized = type.trim().toLowerCase(Locale.ROOT);

        return normalized.equals("boolean")
                || normalized.equals("bool")
                || normalized.equals("bit")
                || normalized.equals("java.lang.boolean")
                || normalized.equals("system.boolean");
    }
}
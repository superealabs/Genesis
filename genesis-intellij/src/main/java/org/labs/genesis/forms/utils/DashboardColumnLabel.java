package org.labs.genesis.forms.utils;

public final class DashboardColumnLabel {

    private DashboardColumnLabel() {
    }

    public static String clean(String column) {
        if (column == null) {
            return "";
        }

        column = column.trim();

        if (column.startsWith("COLUMN:")) {
            column = column.substring("COLUMN:".length());
        }

        if (column.startsWith("FORMULA:")) {
            column = column.substring("FORMULA:".length());
        }

        int lastDot = column.lastIndexOf('.');
        if (lastDot >= 0 && lastDot < column.length() - 1) {
            column = column.substring(lastDot + 1);
        }

        if (!column.isEmpty()) {
            column = Character.toUpperCase(column.charAt(0)) + column.substring(1);
        }

        return column;
    }
}

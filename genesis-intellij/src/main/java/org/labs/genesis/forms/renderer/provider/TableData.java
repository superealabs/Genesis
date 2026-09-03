package org.labs.genesis.forms.renderer.provider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public record TableData(
        List<String> columns,
        List<List<Object>> rows
) {

    public static final String CONFIG_KEY = "__TableData";

    public static final String ERROR_KEY = "__tableError";

    public static final String LOADING_KEY = "__table   Loading";


    public TableData {

        columns = columns != null
                ? Collections.unmodifiableList(
                new ArrayList<>(columns)
        )
                : Collections.emptyList();

        rows = copyRows(rows);
    }


    public static TableData of(
            List<String> columns,
            List<List<Object>> rows
    ) {
        return new TableData(columns, rows);
    }


    public static TableData empty() {
        return new TableData(
                Collections.emptyList(),
                Collections.emptyList()
        );
    }


    public boolean isEmpty() {
        return rows.isEmpty();
    }


    public int rowCount() {
        return rows.size();
    }


    public int columnCount() {
        return columns.size();
    }


    public Object valueAt(int row, int column) {

        if (row < 0 || row >= rows.size()) {
            return null;
        }

        List<Object> values = rows.get(row);

        if (column < 0 || column >= values.size()) {
            return null;
        }

        return values.get(column);
    }


    public List<Object> rowAt(int row) {

        if (row < 0 || row >= rows.size()) {
            return Collections.emptyList();
        }

        return rows.get(row);
    }


    private static List<List<Object>> copyRows(
            List<List<Object>> source
    ) {

        if (source == null || source.isEmpty()) {
            return Collections.emptyList();
        }

        List<List<Object>> result =
                new ArrayList<>(source.size());

        for (List<Object> row : source) {

            if (row == null) {
                result.add(Collections.emptyList());
            } else {
                result.add(
                        Collections.unmodifiableList(
                                new ArrayList<>(row)
                        )
                );
            }
        }

        return Collections.unmodifiableList(result);
    }
}
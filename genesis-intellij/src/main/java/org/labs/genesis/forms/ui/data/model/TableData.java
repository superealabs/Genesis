package org.labs.genesis.forms.ui.data.model;

import org.labs.genesis.forms.ui.data.DataPanelTree;
import java.util.List;

public class TableData {

    public final String name;

    private List<ColumnData> columns;
    private boolean columnsLoaded;

    public TableData(String name) {
        this.name = name;
        this.columns = List.of();
        this.columnsLoaded = false;
    }

    public TableData(String name, List<ColumnData> columns) {
        this.name = name;
        this.columns = columns != null ? columns : List.of();
        this.columnsLoaded = true;
    }

    public List<ColumnData> getColumns() {
        return columns;
    }

    public void setColumns(List<ColumnData> columns) {
        this.columns = columns != null ? columns : List.of();
        this.columnsLoaded = true;
    }

    public boolean isColumnsLoaded() {
        return columnsLoaded;
    }

    @Override
    public String toString() {
        return name;
    }
}

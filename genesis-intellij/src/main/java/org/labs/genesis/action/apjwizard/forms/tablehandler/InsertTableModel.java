package org.labs.genesis.action.apjwizard.forms.tablehandler;

import javax.swing.table.DefaultTableModel;

public class InsertTableModel extends DefaultTableModel {

    public InsertTableModel(Object[] columns) {
        super(columns, 0);
    }

    public InsertTableModel(Object[] columns, int rows) {
        super(columns, rows);
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return column != 1 && column != 2 && column != 4 && column != 5;
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        if (columnIndex == 0) return Boolean.class;
        return String.class;
    }

}

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
        if (column == 0) {
            return true;
        }

        Boolean visible = (Boolean) getValueAt(row, 0);
        if (visible != null && !visible) {
            return false;
        }

        return column != 1 && column != 4 && column != 5;
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        if (columnIndex == 0) return Boolean.class;
        return String.class;
    }

}

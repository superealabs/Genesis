package org.labs.genesis.action.apjwizard.forms.tablehandler;

import javax.swing.table.DefaultTableModel;

public class TableauTableModel extends DefaultTableModel {

    public TableauTableModel(Object[] columns) {
        super(columns, 0);
    }

    public TableauTableModel(Object[] columns, int rows) {
        super(columns, rows);
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        if (column == 0) {
            return false;
        }

        if (column == 3) {
            Object col3Value = getValueAt(row, 2);
            return col3Value != null && !col3Value.toString().trim().isEmpty();
        }

        return true;
    }

}

package org.labs.genesis.action.apjwizard.forms.tablehandler;

import javax.swing.table.DefaultTableModel;

public class BasicTableModel extends DefaultTableModel {

    public BasicTableModel(Object[] columns) {
        super(columns, 0);
    }

    public BasicTableModel(Object[] columns, int rows) {
        super(columns, rows);
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return column != 0;
    }
}

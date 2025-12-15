package org.labs.genesis.action.apjwizard.forms.tablehandler;

import javax.swing.table.DefaultTableModel;

public class MappingTableModel extends DefaultTableModel {

    public MappingTableModel(Object[] columns) {
        super(columns, 0);
    }

    public MappingTableModel(Object[] columns, int rows) {
        super(columns, rows);
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return column != 0 && column != 1 && column != 3;
    }
}

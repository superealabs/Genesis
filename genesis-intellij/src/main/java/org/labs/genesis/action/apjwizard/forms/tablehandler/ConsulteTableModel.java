package org.labs.genesis.action.apjwizard.forms.tablehandler;

import javax.swing.table.DefaultTableModel;


public class ConsulteTableModel extends DefaultTableModel {

    public ConsulteTableModel(Object[] columns) {
        super(columns, 0);
    }

    public ConsulteTableModel(Object[] columns, int rows) {
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
        return column != 1;
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        if (columnIndex == 0) return Boolean.class;
        return String.class;
    }

}

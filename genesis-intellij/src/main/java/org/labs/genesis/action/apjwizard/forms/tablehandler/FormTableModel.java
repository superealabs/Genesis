package org.labs.genesis.action.apjwizard.forms.tablehandler;

import javax.swing.table.DefaultTableModel;

public class FormTableModel extends DefaultTableModel {

    public FormTableModel(Object[] columns) {
        super(columns, 0);
    }

    public FormTableModel(Object[] columns, int rows) {
        super(columns, rows);
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return column != 0;
    }
}

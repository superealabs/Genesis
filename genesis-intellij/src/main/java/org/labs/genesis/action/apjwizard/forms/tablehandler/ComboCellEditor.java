package org.labs.genesis.action.apjwizard.forms.tablehandler;

import javax.swing.*;

public class ComboCellEditor extends DefaultCellEditor {

    public ComboCellEditor(String[] values) {
        super(new JComboBox<>(values));
    }
}

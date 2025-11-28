package org.labs.genesis.action.apjwizard.forms.renderer;

import com.intellij.ui.JBColor;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class TableRenderer extends DefaultTableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(
            JTable table, Object value, boolean isSelected, boolean hasFocus,
            int row, int column) {

        Component c = super.getTableCellRendererComponent(
                table, value, isSelected, hasFocus, row, column);

        boolean editable = table.isCellEditable(row, column);

        c.setForeground(editable ? JBColor.BLACK : JBColor.GRAY);

        return c;
    }
}

package org.labs.genesis.renderer;

import org.labs.genesis.connexion.model.TableMetadata;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class TableMetadataTableCellRenderer extends DefaultTableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(
            JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        if (value instanceof TableMetadata) {
            TableMetadata tableMetadata = (TableMetadata) value;
            setText(tableMetadata.getTableName());
        } else {
            setText(value != null ? value.toString() : "");
        }

        return this;
    }
}

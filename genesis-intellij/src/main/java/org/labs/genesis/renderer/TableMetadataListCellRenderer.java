package org.labs.genesis.renderer;

import org.labs.genesis.connexion.model.TableMetadata;

import javax.swing.*;
import java.awt.*;

public class TableMetadataListCellRenderer extends DefaultListCellRenderer {

    @Override
    public Component getListCellRendererComponent(
            JList list,
            Object value, // Ici, 'value' sera un objet TableMetadata
            int index,
            boolean isSelected,
            boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        if (value instanceof TableMetadata) {
            TableMetadata table = (TableMetadata) value;
            setText(table.getTableName());
        } else if (value != null) {
            setText(value.toString());
        }
        return this;
    }
}
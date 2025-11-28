package org.labs.genesis.action.apjwizard.forms.tablehandler;

import com.intellij.ui.table.JBTable;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.datatransfer.*;

public class TableRowTransferHandler extends TransferHandler {

    private final JBTable table;
    private int[] rowsToRemove;

    public TableRowTransferHandler(JBTable table) {
        this.table = table;
    }

    @Override
    public int getSourceActions(JComponent c) {
        return MOVE;
    }

    @Override
    protected Transferable createTransferable(JComponent c) {
        rowsToRemove = table.getSelectedRows();
        return new StringSelection("");
    }

    @Override
    public boolean canImport(TransferSupport support) {
        if (!support.isDrop()) return false;
        support.setShowDropLocation(true);
        return support.isDataFlavorSupported(DataFlavor.stringFlavor);
    }

    @Override
    public boolean importData(TransferSupport support) {
        if (!canImport(support)) return false;

        JTable.DropLocation dl = (JTable.DropLocation) support.getDropLocation();
        int dropRow = dl.getRow();
        DefaultTableModel model = (DefaultTableModel) table.getModel();

        Object[][] movedData = new Object[rowsToRemove.length][model.getColumnCount()];
        for (int i = 0; i < rowsToRemove.length; i++) {
            for (int j = 0; j < model.getColumnCount(); j++) {
                movedData[i][j] = model.getValueAt(rowsToRemove[i], j);
            }
        }

        for (int i = rowsToRemove.length - 1; i >= 0; i--) {
            model.removeRow(rowsToRemove[i]);
            if (rowsToRemove[i] < dropRow) dropRow--;
        }

        for (int i = 0; i < movedData.length; i++) {
            model.insertRow(dropRow + i, movedData[i]);
        }
        table.getSelectionModel().clearSelection();
        table.getSelectionModel().addSelectionInterval(dropRow, dropRow + movedData.length - 1);
        return true;
    }

    @Override
    protected void exportDone(JComponent source, Transferable data, int action) {
        rowsToRemove = null;
    }
}

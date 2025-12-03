package org.labs.genesis.action.apjwizard.forms.popup;

import com.intellij.ui.table.JBTable;
import lombok.Getter;
import lombok.Setter;
import org.labs.genesis.action.apjwizard.forms.helper.TableToolbarHelper;
import org.labs.genesis.action.apjwizard.forms.renderer.TableRenderer;
import org.labs.genesis.action.apjwizard.forms.tablehandler.TableRowTransferHandler;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

@Getter
@Setter
public class ListeStringDialog extends JDialog {
    private JPanel mainPanel;
    private JBTable tableValues;
    private JButton okButton;
    private JButton cancelButton;
    private JPanel tablePanel;
    private JScrollPane scroll;
    private boolean validated;
    private DefaultTableModel tableModel;

    public ListeStringDialog(JComponent parent) {
        super(SwingUtilities.getWindowAncestor(parent));
        setModal(true);
        setTitle("MakeListeString");
        setContentPane(mainPanel);
        pack();
        setLocationRelativeTo(parent);
        initTable();
        initEvents();
    }

    private JBTable initTable(JBTable table,DefaultTableModel tableModel,JScrollPane scroll){
        table = new JBTable(tableModel);
        table.setDefaultRenderer(Object.class, new TableRenderer());
        table.setDragEnabled(true);
        table.setDropMode(DropMode.INSERT_ROWS);
        table.setTransferHandler(new TableRowTransferHandler(table));
        scroll.setViewportView(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setViewportBorder(null);
        return table;
    }

    private void initTable() {
        tableModel = new DefaultTableModel(new Object[]{"Aff","Val"},0);
        tableValues = initTable(tableValues, tableModel, scroll);
        tableValues.setModel(tableModel);
        TableToolbarHelper.builder()
            .table(tableValues)
            .panel(tablePanel)
            .addAction((t) -> tableModel.addRow(new Object[]{"", ""}))
            .removeAction(() -> {
                int selected = tableValues.getSelectedRow();
                if (selected >= 0) tableModel.removeRow(selected);
            })
            .build().init(1,1,1,1);
    }

    private void initEvents() {
        okButton.addActionListener(e -> {
            validated = true;
            dispose();
        });

        cancelButton.addActionListener(e -> {
            validated = false;
            dispose();
        });
    }

    public void showDialog() {
        setVisible(true);
    }

    public String getDetails() {
        if (!validated) return null;

        if (tableValues.isEditing()) {
            tableValues.getCellEditor().stopCellEditing();
        }

        StringBuilder aff = new StringBuilder("{");
        StringBuilder val = new StringBuilder("{");
        boolean first = true;

        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String a = tableModel.getValueAt(i, 0) != null ? tableModel.getValueAt(i, 0).toString().trim() : "";
            String v = tableModel.getValueAt(i, 1) != null ? tableModel.getValueAt(i, 1).toString().trim() : "";
            if (a.isEmpty() && v.isEmpty()) continue;

            if (a.isEmpty()) a = v;
            if (v.isEmpty()) v = a;

            if (!first) {
                aff.append(",");
                val.append(",");
            } else {
                first = false;
            }

            aff.append(a);
            val.append(v);
        }

        aff.append("}");
        val.append("}");

        return aff.toString() + val.toString();
    }

}

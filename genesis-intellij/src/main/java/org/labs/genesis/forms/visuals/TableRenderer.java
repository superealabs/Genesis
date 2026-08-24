package org.labs.genesis.forms.visuals;

import org.labs.genesis.forms.theme.DashboardTheme;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class TableRenderer implements VisualizationRenderer {

    @Override
    public JComponent createComponent() {
        String[] columns = {"Date", "Commande", "Client", "Produit", "Montant"};
        Object[][] data = {
                {"2026-08-22 14:32", "CMD-1042", "Rakoto J.", "Produit A", "194,00 €"},
                {"2026-08-22 13:15", "CMD-1043", "Rasoa M.", "Produit B", "159,00 €"},
                {"2026-08-22 11:08", "CMD-1044", "Andry R.", "Produit C", "312,00 €"},
                {"2026-08-21 18:45", "CMD-1045", "Fara N.", "Produit D", "206,00 €"}
        };

        DefaultTableModel model = new DefaultTableModel(data, columns) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        JTable table = new JTable(model);
        table.setRowHeight(28);
        table.setShowGrid(false);
        table.setBackground(DashboardTheme.ACCENT_LIGHT);
        table.setForeground(DashboardTheme.TEXT_DARK);
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setForeground(DashboardTheme.TEXT_SECONDARY);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setOpaque(false);

        return scrollPane;
    }
}
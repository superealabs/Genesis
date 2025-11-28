package org.labs.genesis.forms;

import lombok.Getter;
import lombok.Setter;
import org.labs.genesis.config.langage.generator.sync.report.DatabaseReportManager;
import org.labs.genesis.config.langage.generator.sync.report.TableChangeReport;
import org.labs.genesis.renderer.TableReportModel;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.util.Map;

@Getter
@Setter
public class SyncGenerationForm {
    private JPanel mainPanel;
    private JPanel summaryTitlePanel;
    private JTable reportTable;
    private TableReportModel tableModel;

    public SyncGenerationForm() {
        initializeTable();
    }

    private void initializeTable() {
        // Créer le modèle personnalisé
        tableModel = new TableReportModel();
        reportTable.setModel(tableModel);

        // Configuration de l'apparence de la table
        reportTable.setFillsViewportHeight(true);
        reportTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        reportTable.getTableHeader().setReorderingAllowed(false);
        reportTable.setRowHeight(30);

        // Ajuster les largeurs des colonnes
        TableColumnModel columnModel = reportTable.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(150);  // Table
        columnModel.getColumn(1).setPreferredWidth(120);  // Category
        columnModel.getColumn(2).setPreferredWidth(400);  // Descriptions

        reportTable.setDefaultRenderer(Object.class, new CategoryCellRenderer());
    }

    public void populateTableReport(DatabaseReportManager reportManager) {
        tableModel.clearReports();

        if (reportManager == null || reportManager.getTableReports() == null) {
            return;
        }
        Map<String, TableChangeReport> reports = reportManager.getTableReports();
        for (TableChangeReport report : reports.values()) {
            tableModel.addReport(report);
        }
    }

    public void clearReport() {
        tableModel.clearReports();
    }

    public int getReportCount() {
        return tableModel.getRowCount();
    }

    public TableChangeReport getSelectedReport() {
        int selectedRow = reportTable.getSelectedRow();
        if (selectedRow >= 0) {
            return tableModel.getReportAt(selectedRow);
        }
        return null;
    }

    private static class CategoryCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            if (!isSelected) {
                Object categoryValue = table.getValueAt(row, 1);
                if (categoryValue != null) {
                    String category = categoryValue.toString();

                    switch (category) {
                        case "ADDITION":
                            c.setBackground(new Color(220, 255, 220)); // Vert clair
                            break;
                        case "REMOVAL":
                            c.setBackground(new Color(255, 220, 220)); // Rouge clair
                            break;
                        case "MODIFICATION":
                            c.setBackground(new Color(255, 250, 200)); // Jaune clair
                            break;
                        case "UNCATEGORISED":
                            c.setBackground(new Color(240, 240, 240)); // Gris clair
                            break;
                        default:
                            c.setBackground(Color.WHITE);
                    }
                } else {
                    c.setBackground(Color.WHITE);
                }
            }

            // Alignement du texte
            if (column == 1) { // Category - centré
                setHorizontalAlignment(CENTER);
            } else {
                setHorizontalAlignment(LEFT);
            }

            return c;
        }
    }
}
package org.labs.genesis.forms;

import lombok.Getter;
import lombok.Setter;
import org.labs.genesis.config.langage.generator.sync.report.DatabaseReportManager;
import org.labs.genesis.config.langage.generator.sync.report.TableChangeReport;
import org.labs.genesis.renderer.TableReportModel;
import org.labs.genesis.wizards.SynchGenerationWizardStep;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

@Getter
@Setter
public class SyncGenerationForm {
    private JPanel mainPanel;
    private JPanel summaryTitlePanel;
    private JTable reportTable;
    private JButton refreshButton;
    private TableReportModel tableModel;
    private final SynchGenerationWizardStep parentStep;


    public SyncGenerationForm(SynchGenerationWizardStep synchGenerationWizardStep) {
        this.parentStep = synchGenerationWizardStep;
        initializeTable();
        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                parentStep.refreshData();
            }
        });
    }

    private void initializeTable() {
        tableModel = new TableReportModel();
        reportTable.setModel(tableModel);

        reportTable.setFillsViewportHeight(true);
        reportTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        reportTable.getTableHeader().setReorderingAllowed(false);
        reportTable.setRowHeight(30);

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
                // Récupérer la catégorie de la ligne
                Object categoryValue = table.getValueAt(row, 1);
                if (categoryValue != null) {
                    String category = categoryValue.toString();

                    switch (category) {
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
package org.labs.genesis.renderer;

import org.labs.genesis.config.langage.generator.sync.report.TableChangeReport;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class TableReportModel extends AbstractTableModel {
    private final String[] columnNames = {"Table", "Category", "Descriptions"};
    private final List<TableChangeReport> reports;

    public TableReportModel() {
        this.reports = new ArrayList<>();
    }

    @Override
    public int getRowCount() {
        return reports.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        TableChangeReport report = reports.get(rowIndex);

        switch (columnIndex) {
            case 0: // Table
                return getTableName(report);
            case 1: // Category
                return report.getCategory().name();
            case 2: // Descriptions
                return formatDescriptions(report.getReportDescriptions());
            default:
                return null;
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return String.class;
    }

    public void addReport(TableChangeReport report) {
        int row = reports.size();
        reports.add(report);
        fireTableRowsInserted(row, row);
    }

    public void setReports(List<TableChangeReport> reports) {
        this.reports.clear();
        this.reports.addAll(reports);
        fireTableDataChanged();
    }

    public void clearReports() {
        int size = reports.size();
        if (size > 0) {
            reports.clear();
            fireTableRowsDeleted(0, size - 1);
        }
    }

    public TableChangeReport getReportAt(int rowIndex) {
        if (rowIndex >= 0 && rowIndex < reports.size()) {
            return reports.get(rowIndex);
        }
        return null;
    }

    private String getTableName(TableChangeReport report) {
        if (report.getNewTable() != null) {
            return report.getNewTable().getTableName();
        } else if (report.getOldTable() != null) {
            return report.getOldTable().getTableName();
        }
        return "Unknown";
    }

    private String formatDescriptions(List<String> descriptions) {
        if (descriptions == null || descriptions.isEmpty()) {
            return "";
        }

        if (descriptions.size() == 1) {
            return descriptions.get(0);
        }

        return String.join("\n", descriptions);
    }
}
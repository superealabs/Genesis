package org.labs.genesis.forms;

import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBList;
import com.intellij.util.ui.JBUI;
import org.labs.genesis.connexion.model.ColumnMetadata;
import org.labs.genesis.connexion.model.TableMetadata;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FormConfigurationForm {

    private JPanel mainPanel;

    private JLabel titleLabel;

    private JLabel tableLabel;
    private JLabel columnLabel;

    private JBList<TableMetadata> tableList;
    private JBList<ColumnMetadata> columnList;

    private JLabel tableInfoLabel;
    private JLabel columnInfoLabel;

    private final List<TableMetadata> tables = new ArrayList<>();

    private Border inactiveColumnBorder;
    private Border activeColumnBorder;


    public FormConfigurationForm() {
        setupUI();
        setupListeners();
        clearSelection();
        mainPanel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                intializeData();
            }
        });
    }

    private void intializeData() {

    }

    /**
     * -------------------------------------------------------------------------
     * UI
     * -------------------------------------------------------------------------
     */
    private void setupUI() {

        /*
         * Main panel
         */
        mainPanel.setPreferredSize(new Dimension(900, 650));
        mainPanel.setMinimumSize(new Dimension(700, 500));


        /*
         * Title
         */
        titleLabel.setText("Select data");
        titleLabel.setFont(
                titleLabel.getFont().deriveFont(Font.BOLD, 20f)
        );


        /*
         * Section labels
         */
        tableLabel.setText("Tables");
        columnLabel.setText("Columns");

        tableLabel.setFont(
                tableLabel.getFont().deriveFont(Font.BOLD, 13f)
        );

        columnLabel.setFont(
                columnLabel.getFont().deriveFont(Font.BOLD, 13f)
        );


        /*
         * Tables
         */
        tableList.setModel(
                new DefaultListModel<>()
        );

        DefaultListModel<TableMetadata> tableModel =
                (DefaultListModel<TableMetadata>) tableList.getModel();

        for (TableMetadata table : tables) {
            tableModel.addElement(table);
        }

        tableList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        tableList.setCellRenderer(new TableRenderer());

        tableList.setFixedCellHeight(42);


        /*
         * Columns
         */
        columnList.setModel(
                new DefaultListModel<>()
        );

        columnList.setSelectionMode(
                ListSelectionModel.MULTIPLE_INTERVAL_SELECTION
        );

        columnList.setCellRenderer(new ColumnRenderer());

        columnList.setFixedCellHeight(38);

        columnList.setEnabled(false);


        /*
         * Borders
         */
        inactiveColumnBorder =
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                JBColor.border(),
                                1
                        ),
                        JBUI.Borders.empty(4)
                );

        activeColumnBorder =
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                JBColor.namedColor(
                                        "Focus.defaultColor",
                                        new JBColor(
                                                new Color(78, 137, 255),
                                                new Color(78, 137, 255)
                                        )
                                ),
                                2
                        ),
                        JBUI.Borders.empty(3)
                );


        /*
         * Informations
         */
        tableInfoLabel.setText(
                "No table selected"
        );

        columnInfoLabel.setText(
                "Select a table to see its columns"
        );

        tableInfoLabel.setForeground(
                JBColor.GRAY
        );

        columnInfoLabel.setForeground(
                JBColor.GRAY
        );


        /*
         * Empty initial state
         */
        clearSelection();
    }


    /**
     * -------------------------------------------------------------------------
     * EVENTS
     * -------------------------------------------------------------------------
     */
    private void setupListeners() {

        tableList.addListSelectionListener(event -> {

            if (event.getValueIsAdjusting()) {
                return;
            }

            TableMetadata selectedTable =
                    tableList.getSelectedValue();

            if (selectedTable == null) {
                clearSelection();
                return;
            }

            showColumns(selectedTable);
        });


        columnList.addListSelectionListener(event -> {

            if (event.getValueIsAdjusting()) {
                return;
            }

            updateColumnInfo();
        });
    }


    /**
     * -------------------------------------------------------------------------
     * TABLE SELECTION
     * -------------------------------------------------------------------------
     */
    private void showColumns(TableMetadata table) {

        if (table == null) {
            clearSelection();
            return;
        }


        /*
         * Update table information
         */
        tableInfoLabel.setText(
                "<html>"
                        + "<b>" + table.getTableName() + "</b>"
                        + " &nbsp; · &nbsp; "
                        + table.getColumns().length
                        + " columns"
                        + "</html>"
        );

        tableInfoLabel.setForeground(
                UIManager.getColor("Label.foreground")
        );


        /*
         * Populate columns
         */
        DefaultListModel<ColumnMetadata> model =
                new DefaultListModel<>();

        if (table.getColumns() != null) {

            Arrays.stream(table.getColumns())
                    .forEach(model::addElement);
        }

        columnList.setModel(model);

        columnList.setEnabled(true);

        columnList.setBorder(activeColumnBorder);


        /*
         * Column information
         */
        columnInfoLabel.setText(
                "Select one or more columns"
        );

        columnInfoLabel.setForeground(
                UIManager.getColor("Label.foreground")
        );
    }


    /**
     * -------------------------------------------------------------------------
     * EMPTY STATE
     * -------------------------------------------------------------------------
     */
    private void clearSelection() {

        tableList.clearSelection();

        DefaultListModel<ColumnMetadata> emptyModel =
                new DefaultListModel<>();

        columnList.setModel(emptyModel);

        columnList.clearSelection();

        columnList.setEnabled(false);

        columnList.setBorder(inactiveColumnBorder);


        tableInfoLabel.setText(
                "No table selected"
        );

        columnInfoLabel.setText(
                "Select a table to see its columns"
        );


        tableInfoLabel.setForeground(
                JBColor.GRAY
        );

        columnInfoLabel.setForeground(
                JBColor.GRAY
        );
    }


    /**
     * -------------------------------------------------------------------------
     * COLUMN INFO
     * -------------------------------------------------------------------------
     */
    private void updateColumnInfo() {

        int count = columnList.getSelectedIndices().length;

        if (count == 0) {

            columnInfoLabel.setText(
                    "Select one or more columns"
            );

            return;
        }

        columnInfoLabel.setText(
                count == 1
                        ? "1 column selected"
                        : count + " columns selected"
        );
    }


    /**
     * -------------------------------------------------------------------------
     * RENDERERS
     * -------------------------------------------------------------------------
     */

    private static class TableRenderer
            extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(
                JList<?> list,
                Object value,
                int index,
                boolean isSelected,
                boolean cellHasFocus
        ) {

            super.getListCellRendererComponent(
                    list,
                    value,
                    index,
                    isSelected,
                    cellHasFocus
            );

            if (value instanceof TableMetadata table) {

                String tableName =
                        table.getTableName() != null
                                ? table.getTableName()
                                : "Unnamed table";

                String text =
                        "<html>"
                                + "<b>" + tableName + "</b>"
                                + "<br>"
                                + "</html>";

                setText(text);
            }

            setBorder(
                    JBUI.Borders.empty(
                            5,
                            8
                    )
            );

            return this;
        }
    }


    private static class ColumnRenderer
            extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(
                JList<?> list,
                Object value,
                int index,
                boolean isSelected,
                boolean cellHasFocus
        ) {

            super.getListCellRendererComponent(
                    list,
                    value,
                    index,
                    isSelected,
                    cellHasFocus
            );

            if (value instanceof ColumnMetadata column) {

                String name =
                        column.getName() != null
                                ? column.getName()
                                : "Unnamed";

                String type =
                        column.getDatabaseColumnType() != null
                                ? column.getDatabaseColumnType()
                                : column.getType();

                StringBuilder badges =
                        new StringBuilder();

                if (column.isPrimary()) {
                    badges.append(" · PK");
                }

                if (column.isForeign()) {
                    badges.append(" · FK");
                }

                if (column.isNullable()) {
                    badges.append(" · nullable");
                }

                setText(
                        "<html>"
                                + "<b>" + name + "</b>"
                                + " &nbsp; "
                                + "<span style='color:#888888'>"
                                + type
                                + "</span>"
                                + "<span style='color:#888888'>"
                                + badges
                                + "</span>"
                                + "</html>"
                );
            }

            setBorder(
                    JBUI.Borders.empty(
                            5,
                            8
                    )
            );

            return this;
        }
    }


    /**
     * -------------------------------------------------------------------------
     * GETTERS
     * -------------------------------------------------------------------------
     */

    public JPanel getMainPanel() {
        return mainPanel;
    }


    public TableMetadata getSelectedTable() {
        return tableList.getSelectedValue();
    }


    public List<ColumnMetadata> getSelectedColumns() {

        return columnList.getSelectedValuesList();
    }


    public List<TableMetadata> getTables() {
        return tables;
    }


    public void setTables(List<TableMetadata> newTables) {

        tables.clear();

        if (newTables != null) {
            tables.addAll(newTables);
        }

        DefaultListModel<TableMetadata> model =
                new DefaultListModel<>();

        for (TableMetadata table : tables) {
            model.addElement(table);
        }

        tableList.setModel(model);

        tableList.setCellRenderer(new TableRenderer());

        clearSelection();
    }
}
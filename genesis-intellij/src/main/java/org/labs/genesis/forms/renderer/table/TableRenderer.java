package org.labs.genesis.forms.renderer.table;

import lombok.Getter;
import org.labs.genesis.forms.renderer.VisualizationRenderer;
import org.labs.genesis.forms.renderer.provider.TableData;
import org.labs.genesis.forms.theme.DashboardTheme;
import org.labs.genesis.forms.ui.visualization.model.VisualizationConfig;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class TableRenderer implements VisualizationRenderer {

    private static final int MAX_DISPLAY_ROWS = 100;

    private static final NumberFormat NUMBER_FORMAT =
            NumberFormat.getInstance(Locale.US);

    static {
        NUMBER_FORMAT.setGroupingUsed(true);
        NUMBER_FORMAT.setMaximumFractionDigits(2);
        NUMBER_FORMAT.setMinimumFractionDigits(0);
    }

    @Getter
    private TableData tableData;
    @Getter
    private List<String> columns = new ArrayList<>();
    @Getter
    private String title;

    private JPanel rootPanel;
    private JTable table;
    private JScrollPane scrollPane;

    @Override
    public JComponent createComponent() {
        rootPanel = new JPanel(new BorderLayout());
        rootPanel.setBackground(DashboardTheme.CANVAS_BG);
        rootPanel.setOpaque(true);
        rootPanel.setBorder(
                BorderFactory.createLineBorder(
                        DashboardTheme.BORDER_SUBTLE,
                        1
                )
        );

        table = new JTable();
        configureTable(table);

        scrollPane = createScrollPane(table);
        rootPanel.add(scrollPane, BorderLayout.CENTER);

        refreshTable();

        return rootPanel;
    }

    @Override
    public JComponent createComponent(VisualizationConfig config) {
        updateConfig(config);

        return createComponent();
    }

    @Override
    public void updateConfig(VisualizationConfig config) {

        Object dataObj =
                config.getValue(TableData.CONFIG_KEY);

        if (dataObj instanceof TableData data) {
            tableData = data;
        } else {
            tableData = null;
        }

        Object columnsObj =
                config.getValue("columns");

        if (columnsObj instanceof List<?> list) {

            columns = new ArrayList<>();

            for (Object value : list) {

                if (value == null) {
                    continue;
                }

                String stringValue =
                        value.toString().trim();

                if (!stringValue.isEmpty()) {
                    columns.add(stringValue);
                }
            }

        } else {
            columns = new ArrayList<>();
        }

        title = config.getString("title");

        if (table != null) {
            SwingUtilities.invokeLater(
                    this::refreshTable
            );
        }
    }

    private void refreshTable() {

        if (table == null) {
            return;
        }

        String[] columnNames = buildColumnNames();

        Object[][] data = buildTableData(columnNames);

        DefaultTableModel model = new DefaultTableModel(
                data,
                columnNames
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table.setModel(model);

        configureHeader();

        if (columnNames.length > 0) {
            adjustColumnWidths();
        }

        updateTitle();

        table.revalidate();
        table.repaint();

        if (scrollPane != null) {
            scrollPane.revalidate();
            scrollPane.repaint();
        }

        if (rootPanel != null) {
            rootPanel.revalidate();
            rootPanel.repaint();
        }
    }

    private String[] buildColumnNames() {

        if (columns == null || columns.isEmpty()) {
            return new String[]{"Message"};
        }

        String[] result =
                new String[columns.size()];

        for (int i = 0; i < columns.size(); i++) {

            result[i] =
                    cleanColumnName(columns.get(i));
        }

        return result;
    }

    private Object[][] buildTableData(String[] columnNames) {

        if (columns == null || columns.isEmpty()) {

            return new Object[][]{
                    {"Aucune colonne configurée"}
            };
        }

        if (tableData == null ||
                tableData.isEmpty()) {

            return new Object[][]{
                    {"Aucune donnée à afficher"}
            };
        }

        int rowCount =
                Math.min(
                        tableData.rowCount(),
                        MAX_DISPLAY_ROWS
                );

        int columnCount =
                columnNames.length;

        Object[][] data =
                new Object[rowCount][columnCount];

        for (int row = 0; row < rowCount; row++) {

            for (int column = 0;
                 column < columnCount;
                 column++) {

                Object value =
                        tableData.valueAt(
                                row,
                                column
                        );

                data[row][column] =
                        formatTableValue(value);
            }
        }

        return data;
    }

    private Object formatTableValue(Object value) {

        if (value == null) {
            return "";
        }

        if (value instanceof Number number) {

            double doubleValue =
                    number.doubleValue();

            if (!Double.isFinite(doubleValue)) {
                return "N/A";
            }

            if (doubleValue == (long) doubleValue) {
                return NUMBER_FORMAT.format(
                        (long) doubleValue
                );
            }

            return NUMBER_FORMAT.format(
                    doubleValue
            );
        }

        return value.toString();
    }

    private String cleanColumnName(String column) {

        if (column == null) {
            return "";
        }

        column = column.trim();

        if (column.startsWith("COLUMN:")) {
            column = column.substring("COLUMN:".length());
        }

        if (column.startsWith("FORMULA:")) {
            column = column.substring("FORMULA:".length());
        }

        int lastDot = column.lastIndexOf('.');

        if (lastDot >= 0 &&
                lastDot < column.length() - 1) {

            column =
                    column.substring(lastDot + 1);
        }

        if (!column.isEmpty()) {

            column =
                    Character.toUpperCase(column.charAt(0))
                            + column.substring(1);
        }

        return column;
    }

    private void configureTable(JTable table) {

        table.setRowHeight(30);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));

        table.setBackground(DashboardTheme.ACCENT_LIGHT);
        table.setForeground(DashboardTheme.TEXT_DARK);

        table.setFont(
                table.getFont().deriveFont(
                        Font.PLAIN,
                        13f
                )
        );

        table.setSelectionBackground(
                new Color(99, 102, 241, 50)
        );

        table.setSelectionForeground(
                DashboardTheme.TEXT_DARK
        );

        table.setDefaultRenderer(
                Object.class,
                new DefaultTableCellRenderer() {

                    @Override
                    public Component getTableCellRendererComponent(
                            JTable table,
                            Object value,
                            boolean selected,
                            boolean focused,
                            int row,
                            int column
                    ) {

                        Component component =
                                super.getTableCellRendererComponent(
                                        table,
                                        value,
                                        selected,
                                        focused,
                                        row,
                                        column
                                );

                        if (!selected) {

                            component.setBackground(
                                    row % 2 == 0
                                            ? DashboardTheme.ACCENT_LIGHT
                                            : DashboardTheme.SURFACE
                            );
                        }

                        setForeground(
                                DashboardTheme.TEXT_DARK
                        );

                        setBorder(
                                BorderFactory.createEmptyBorder(
                                        2,
                                        8,
                                        2,
                                        8
                                )
                        );

                        return component;
                    }
                }
        );

        configureHeader();
    }

    private void configureHeader() {

        if (table == null) {
            return;
        }

        JTableHeader header =
                table.getTableHeader();

        header.setReorderingAllowed(false);
        header.setResizingAllowed(true);

        header.setBackground(
                DashboardTheme.SURFACE_2
        );

        header.setForeground(
                DashboardTheme.TEXT_SECONDARY
        );

        header.setFont(
                header.getFont().deriveFont(
                        Font.BOLD,
                        12f
                )
        );

        header.setBorder(
                BorderFactory.createMatteBorder(
                        0,
                        0,
                        1,
                        0,
                        DashboardTheme.BORDER_SUBTLE
                )
        );
    }

    private JScrollPane createScrollPane(JTable table) {

        JScrollPane pane =
                new JScrollPane(table);

        pane.setBorder(null);
        pane.setOpaque(false);

        pane.getViewport().setOpaque(false);
        pane.getViewport().setBackground(
                DashboardTheme.CANVAS_BG
        );

        pane.getVerticalScrollBar()
                .setUnitIncrement(16);

        return pane;
    }

    private void adjustColumnWidths() {

        if (table == null) {
            return;
        }

        TableColumnModel model =
                table.getColumnModel();

        int count =
                model.getColumnCount();

        if (count == 0) {
            return;
        }

        int width =
                Math.max(
                        80,
                        table.getWidth() / count
                );

        for (int i = 0; i < count; i++) {

            model.getColumn(i)
                    .setPreferredWidth(width);

            model.getColumn(i)
                    .setMinWidth(60);
        }
    }

    private void updateTitle() {

        if (rootPanel == null) {
            return;
        }

        Component north =
                ((BorderLayout) rootPanel.getLayout())
                        .getLayoutComponent(
                                BorderLayout.NORTH
                        );

        if (north != null) {
            rootPanel.remove(north);
        }

        if (title != null &&
                !title.isBlank()) {

            JLabel titleLabel =
                    new JLabel(title);

            titleLabel.setForeground(
                    DashboardTheme.TEXT_DARK
            );

            titleLabel.setFont(
                    DashboardTheme.boldFont(13)
            );

            titleLabel.setBorder(
                    BorderFactory.createEmptyBorder(
                            8,
                            12,
                            6,
                            12
                    )
            );

            titleLabel.setOpaque(true);
            titleLabel.setBackground(
                    DashboardTheme.SURFACE_2
            );

            rootPanel.add(
                    titleLabel,
                    BorderLayout.NORTH
            );
        }

        rootPanel.revalidate();
    }

    private String formatValue(double value) {

        if (!Double.isFinite(value)) {
            return "N/A";
        }

        if (value == (long) value) {
            return NUMBER_FORMAT.format(
                    (long) value
            );
        }

        return NUMBER_FORMAT.format(value);
    }

    public void setColumns(List<String> columns) {

        this.columns =
                columns != null
                        ? new ArrayList<>(columns)
                        : new ArrayList<>();

        refreshTable();
    }

    public void setTitle(String title) {
        this.title = title;
        updateTitle();
    }
}
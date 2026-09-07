    package org.labs.genesis.forms.ui.visualization.configuration.editor;

    import com.intellij.icons.AllIcons;
    import lombok.Getter;
    import org.labs.genesis.forms.theme.DashboardTheme;
    import org.labs.genesis.forms.ui.visualization.model.FieldQueryOptions;
    import org.labs.genesis.forms.ui.visualization.model.VisualizationParameter;

    import javax.swing.*;
    import javax.swing.border.EmptyBorder;
    import javax.swing.event.DocumentEvent;
    import javax.swing.event.DocumentListener;
    import java.awt.*;
    import java.awt.event.MouseAdapter;
    import java.awt.event.MouseEvent;
    import java.util.ArrayList;
    import java.util.LinkedHashMap;
    import java.util.List;
    import java.util.Map;

    public class ColumnsEditorPanel extends JPanel {

        private final JPanel columnsContainer;
        private final JLabel countLabel;

        private final FieldAdvancedOptionsPanel advancedOptionsPanel;

        private final List<ColumnOrFormulaRow> rows = new ArrayList<>();

        private Runnable changeListener;
        private int rowCounter = 0;
        private boolean columnOnly = true;

        @Getter
        private final List<JTextField> headerFields = new ArrayList<>();

        public ColumnsEditorPanel() {
            this(false);
        }

        public ColumnsEditorPanel(boolean columnOnly) {
            this.columnOnly = columnOnly;

            setLayout(new BorderLayout());
            setOpaque(false);

            JPanel mainPanel = new JPanel();
            mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
            mainPanel.setOpaque(false);

            columnsContainer = new JPanel();
            columnsContainer.setLayout(new BoxLayout(columnsContainer, BoxLayout.Y_AXIS));
            columnsContainer.setOpaque(false);
            columnsContainer.setAlignmentX(Component.LEFT_ALIGNMENT);

            countLabel = new JLabel();
            countLabel.setForeground(DashboardTheme.TEXT_SECONDARY);
            countLabel.setFont(countLabel.getFont().deriveFont(10f));
            countLabel.setBorder(new EmptyBorder(0, 2, 4, 0));
            countLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

            JButton addButton = createAddButton();

            mainPanel.add(countLabel);
            mainPanel.add(columnsContainer);
            mainPanel.add(Box.createVerticalStrut(6));
            mainPanel.add(addButton);

            advancedOptionsPanel = new FieldAdvancedOptionsPanel();
            advancedOptionsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            advancedOptionsPanel.setVisible(false);

            mainPanel.add(Box.createVerticalStrut(10));
            mainPanel.add(createAdvancedOptionsHeader());
            mainPanel.add(Box.createVerticalStrut(6));
            mainPanel.add(advancedOptionsPanel);

            advancedOptionsPanel.setChangeListener(this::notifyChange);

            add(mainPanel, BorderLayout.CENTER);

            addColumnRow(null);
            updateCountLabel();
        }

        // =========================================================================
        // ADVANCED OPTIONS
        // =========================================================================

        private JPanel createAdvancedOptionsHeader() {
            JPanel panel = new JPanel(new BorderLayout());
            panel.setOpaque(false);
            panel.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel label = new JLabel("Query options");
            label.setForeground(DashboardTheme.TEXT);
            label.setFont(label.getFont().deriveFont(Font.BOLD, 11f));

            JToggleButton toggle = createOptionsToggle();

            toggle.addActionListener(e -> {
                advancedOptionsPanel.setVisible(toggle.isSelected());

                revalidate();
                repaint();
            });

            panel.add(label, BorderLayout.WEST);
            panel.add(toggle, BorderLayout.EAST);

            return panel;
        }

        private JToggleButton createOptionsToggle() {
            JToggleButton button = new JToggleButton(AllIcons.General.Filter);

            button.setToolTipText("Limit / Sort / Filter");
            button.setBorderPainted(false);
            button.setContentAreaFilled(false);
            button.setFocusPainted(false);
            button.setFocusable(false);
            button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            button.setMargin(new Insets(0, 0, 0, 0));

            Dimension size = new Dimension(22, 22);
            button.setPreferredSize(size);
            button.setMinimumSize(size);
            button.setMaximumSize(size);

            return button;
        }

        // =========================================================================
        // ADD / REMOVE COLUMNS
        // =========================================================================

        private JButton createAddButton() {
            JButton button = new JButton("+ Add Column");

            button.setHorizontalAlignment(SwingConstants.LEFT);
            button.setForeground(DashboardTheme.TEXT);
            button.setBackground(DashboardTheme.SURFACE_2);
            button.setFont(button.getFont().deriveFont(Font.PLAIN, 11f));
            button.setFocusPainted(false);
            button.setFocusable(false);
            button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            button.setContentAreaFilled(true);
            button.setOpaque(true);

            button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(
                            DashboardTheme.BORDER_SUBTLE, 1
                    ),
                    new EmptyBorder(5, 8, 5, 8)
            ));

            button.setAlignmentX(Component.LEFT_ALIGNMENT);

            button.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    button.setBackground(DashboardTheme.SURFACE_ACTIVE);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    button.setBackground(DashboardTheme.SURFACE_2);
                }
            });

            button.addActionListener(e -> {
                addColumnRow(null);

                revalidate();
                repaint();

                notifyChange();
            });

            return button;
        }

        private void addColumnRow(String initialValue) {
            VisualizationParameter param = VisualizationParameter.measureOrFormula(
                    "column_" + (++rowCounter),
                    "Column",
                    true
            );

            if (columnOnly) {
                param.setMode("COLUMN", true);
            }

            ColumnOrFormulaRow row = new ColumnOrFormulaRow(param, columnOnly);

            if (initialValue != null && !initialValue.isBlank()) {
                row.restoreValue(initialValue);
            }

            row.setChangeListener(this::notifyChange);
            rows.add(row);
            columnsContainer.add(createRowWrapper(row));
            updateCountLabel();
        }

        private JPanel createRowWrapper(ColumnOrFormulaRow row) {
            JPanel rowWrapper = new JPanel(new BorderLayout(0, 6));
            rowWrapper.setOpaque(false);
            rowWrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
            rowWrapper.setBorder(new EmptyBorder(0, 0, 8, 0));

            JPanel mainRow = new JPanel(new BorderLayout(6, 0));
            mainRow.setOpaque(false);

            JButton removeButton = createRemoveButton(row);
            mainRow.add(row, BorderLayout.CENTER);
            mainRow.add(removeButton, BorderLayout.EAST);

            JTextField headerField = new JTextField();
            headerField.setToolTipText("Modifier l'en-tête de la colonne");
            headerField.getDocument().addDocumentListener(new DocumentListener() {
                @Override public void insertUpdate(DocumentEvent e) { notifyChange(); }
                @Override public void removeUpdate(DocumentEvent e) { notifyChange(); }
                @Override public void changedUpdate(DocumentEvent e) { notifyChange(); }
            });
            headerFields.add(headerField); // Stocker la référence

            JPanel headerPanel = new JPanel(new BorderLayout(6, 0));
            headerPanel.setOpaque(false);

            JLabel headerLabel = new JLabel("En-tête");
            headerLabel.setForeground(DashboardTheme.TEXT_SECONDARY);

            headerPanel.add(headerLabel, BorderLayout.WEST);
            headerPanel.add(headerField, BorderLayout.CENTER);

            rowWrapper.add(mainRow, BorderLayout.CENTER);
            rowWrapper.add(headerPanel, BorderLayout.SOUTH);

            int preferredHeight = rowWrapper.getPreferredSize().height;
            rowWrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, preferredHeight));

            return rowWrapper;
        }

        private JButton createRemoveButton(ColumnOrFormulaRow row) {
            JButton removeButton =
                    new JButton(AllIcons.Actions.Close);

            removeButton.setToolTipText("Remove column");
            removeButton.setFocusPainted(false);
            removeButton.setFocusable(false);
            removeButton.setBorderPainted(false);
            removeButton.setContentAreaFilled(false);
            removeButton.setForeground(DashboardTheme.TEXT_SECONDARY);
            removeButton.setCursor(
                    Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
            );

            Dimension btnSize =
                    new Dimension(28, ColumnDropField.HEIGHT);

            removeButton.setPreferredSize(btnSize);
            removeButton.setMinimumSize(btnSize);
            removeButton.setMaximumSize(btnSize);

            removeButton.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    removeButton.setForeground(DashboardTheme.ERROR);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    removeButton.setForeground(
                            DashboardTheme.TEXT_SECONDARY
                    );
                }
            });

            removeButton.addActionListener(e -> removeColumn(row));

            return removeButton;
        }

        private void removeColumn(ColumnOrFormulaRow row) {
            if (rows.size() <= 1) {
                return;
            }

            int index = rows.indexOf(row);
            if (index >= 0 && index < headerFields.size()) {
                headerFields.remove(index);
            }

            rows.remove(row);
            refreshRows();
            notifyChange();
        }

        // =========================================================================
        // COLUMNS VALUE
        // =========================================================================

        public void setColumns(List<String> columns) {

            columnsContainer.removeAll();
            rows.clear();
            headerFields.clear(); // IMPORTANT
            rowCounter = 0;

            if (columns == null || columns.isEmpty()) {
                addColumnRow(null);
            } else {
                for (String column : columns) {
                    addColumnRow(column);
                }
            }

            columnsContainer.revalidate();
            columnsContainer.repaint();

            updateCountLabel();
        }

        public void setHeaders(Map<String, String> headers) {
            System.out.println("restore: " + headers);
            if (headers == null) {
                return;
            }

            for (int i = 0;
                 i < rows.size() && i < headerFields.size();
                 i++) {

                ColumnOrFormulaRow row = rows.get(i);
                JTextField headerField = headerFields.get(i);

                String columnKey = row.getStorageValue();
                System.out.println(columnKey);
                if (columnKey == null) {
                    headerField.setText("");
                    continue;
                }

                String header = headers.get(columnKey);
                System.out.println(header);
                headerField.setText(
                        header != null ? header : ""
                );
            }
        }

        public List<String> getColumns() {
            List<String> result = new ArrayList<>();

            for (ColumnOrFormulaRow row : rows) {
                String storageValue = row.getStorageValue();

                if (storageValue != null) {
                    result.add(storageValue);
                }
            }

            return result;
        }

        // =========================================================================
        // ADVANCED OPTIONS API
        // =========================================================================

        public void setQueryOptions(FieldQueryOptions options) {
            if (options == null) {
                options = new FieldQueryOptions();
            }

            advancedOptionsPanel.setOptions(options);

            boolean active = !options.isEmpty();

            advancedOptionsPanel.setVisible(active);
        }

        public FieldQueryOptions getQueryOptions() {
            return advancedOptionsPanel.getOptions();
        }

        // =========================================================================
        // LISTENER
        // =========================================================================

        public void setColumnsChangeListener(Runnable listener) {
            this.changeListener = listener;
        }

        private void notifyChange() {
            updateCountLabel();

            if (changeListener != null) {
                changeListener.run();
            }
        }

        public Map<String, String> getColumnsWithHeaders() {
            Map<String, String> result = new LinkedHashMap<>();

            for (int i = 0; i < rows.size() && i < headerFields.size(); i++) {
                ColumnOrFormulaRow row = rows.get(i);
                JTextField headerField = headerFields.get(i);

                String columnName = row.getStorageValue();
                String header = headerField.getText().trim();

                if (columnName != null && !columnName.isBlank()) {
                    // Si l'en-tête est vide, utiliser le nom de colonne
                    result.put(columnName, header.isEmpty() ? columnName : header);
                }
            }

            return result;
        }

        // =========================================================================
        // UI
        // =========================================================================

        private void updateCountLabel() {
            int count = rows.size();

            countLabel.setText(
                    count + (count > 1 ? " columns" : " column")
            );
        }

        private void refreshRows() {
            columnsContainer.removeAll();
            headerFields.clear();

            for (ColumnOrFormulaRow row : rows) {
                columnsContainer.add(createRowWrapper(row));
            }

            columnsContainer.revalidate();
            columnsContainer.repaint();
        }

        // =========================================================================
        // HELPERS
        // =========================================================================

        public boolean hasCompleteSelection() {
            for (ColumnOrFormulaRow row : rows) {
                if (!row.hasValue()) {
                    return false;
                }
            }

            return true;
        }

        @Override
        public void setEnabled(boolean enabled) {
            super.setEnabled(enabled);

            for (ColumnOrFormulaRow row : rows) {
                row.setEnabled(enabled);
            }

            advancedOptionsPanel.setEnabled(enabled);
        }

        public int getColumnCount() {
            return rows.size();
        }

        public List<String> getColumnHeaders() {
            List<String> headers = new ArrayList<>();
            for (JTextField headerField : headerFields) {
                String header = headerField.getText().trim();
                headers.add(header.isEmpty() ? null : header);
            }
            return headers;
        }

        public void clearAllColumns() {
            columnsContainer.removeAll();
            rows.clear();
            headerFields.clear();
            rowCounter = 0;

            addColumnRow(null);

            columnsContainer.revalidate();
            columnsContainer.repaint();

            notifyChange();
        }

    }
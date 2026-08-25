package org.labs.genesis.forms.components;

import com.intellij.icons.AllIcons;
import org.labs.genesis.forms.data.VisualizationParameter;
import org.labs.genesis.forms.data.VisualizationParameterType;
import org.labs.genesis.forms.theme.DashboardTheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.*;
import java.util.List;

public class VisualizationConfigurationPanel extends JPanel {

    private static final int EDITOR_HEIGHT = 30;
    private static final int ROW_HEIGHT_MIN = 52;

    private final VisualizationPanel.VisualizationItem item;
    private final DashboardVisualComponent targetComponent;
    private final Runnable onBack;
    private final Runnable onDelete;
    private final ScrollableContentPanel contentPanel;
    private final Map<String, JComponent> editorMap = new HashMap<>();
    private final Map<String, JPanel> rowMap = new HashMap<>();

    public VisualizationConfigurationPanel(
            DashboardVisualComponent targetComponent,
            VisualizationPanel.VisualizationItem item,
            Runnable onBack,
            Runnable onDelete
    ) {
        this.item = item;
        this.targetComponent = targetComponent;
        this.onBack = onBack;
        this.onDelete = onDelete;

        setLayout(new BorderLayout());
        setOpaque(false);
        add(createHeader(), BorderLayout.NORTH);

        contentPanel = new ScrollableContentPanel();
        contentPanel.setOpaque(false);
        buildParameters();

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(scrollPane, BorderLayout.CENTER);

        // Appliquer la visibilité initiale
        updateConditionalVisibility();
    }

    private void buildParameters() {
        for (VisualizationParameter parameter : item.parameters) {
            JComponent editor = createEditor(parameter);
            editorMap.put(parameter.getKey(), editor);

            // Liaison dynamique pour tous les types de champs
            bindEditorToConfig(parameter.getKey(), editor);

            JPanel row = createParameterRow(parameter, editor);
            rowMap.put(parameter.getKey(), row);
            contentPanel.add(row);
            contentPanel.add(Box.createVerticalStrut(10));
        }
        contentPanel.add(Box.createVerticalGlue());
    }

    /**
     * Met à jour la visibilité conditionnelle des champs.
     */
    private void updateConditionalVisibility() {
        // Gérer le cas de la Map : valueColumn visible seulement si markerType est "buffer"
        JComponent markerTypeEditor = editorMap.get("markerType");
        JPanel valueColumnRow = rowMap.get("valueColumn");

        if (markerTypeEditor instanceof JComboBox<?> markerTypeCombo && valueColumnRow != null) {
            String selectedType = (String) markerTypeCombo.getSelectedItem();
            boolean isBuffer = "buffer".equalsIgnoreCase(selectedType);
            valueColumnRow.setVisible(isBuffer);

            // Mettre à jour le conteneur parent
            contentPanel.revalidate();
            contentPanel.repaint();
        }
    }

    /**
     * Lie un éditeur à la configuration du composant cible.
     */
    private void bindEditorToConfig(String key, JComponent editor) {
        if (targetComponent == null) return;

        // Gestion du titre spécial
        if ("title".equals(key) && editor instanceof JTextField titleField) {
            titleField.setText(targetComponent.getTitle());
            titleField.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) { updateTitle(); }
                @Override
                public void removeUpdate(DocumentEvent e) { updateTitle(); }
                @Override
                public void changedUpdate(DocumentEvent e) { updateTitle(); }

                private void updateTitle() {
                    targetComponent.setTitle(titleField.getText());
                }
            });
            return;
        }

        // Gestion du type de marqueur pour la Map
        if ("markerType".equals(key) && editor instanceof JComboBox<?> markerTypeCombo) {
            markerTypeCombo.addActionListener(e -> {
                if (targetComponent != null) {
                    targetComponent.updateConfig(key, markerTypeCombo.getSelectedItem());
                }
                updateConditionalVisibility();
            });
            return;
        }

        // Gestion des éditeurs de colonnes multiples
        if ("columns".equals(key) && editor instanceof ColumnsEditorPanel columnsPanel) {
            columnsPanel.setColumnsChangeListener(() -> {
                if (targetComponent != null) {
                    targetComponent.updateConfig(key, columnsPanel.getColumns());
                }
            });
            return;
        }

        // Gestion du nombre optionnel (limit)
        if ("limit".equals(key) && editor instanceof OptionalNumberEditor optionalNumberEditor) {
            optionalNumberEditor.setValueChangeListener(value -> {
                if (targetComponent != null) {
                    targetComponent.updateConfig(key, value);
                }
            });
            return;
        }

        // Gestion des champs texte génériques
        if (editor instanceof JTextField textField) {
            String existingValue = targetComponent.getConfigValue(key);
            if (existingValue != null) {
                textField.setText(existingValue);
            }

            textField.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) { updateValue(); }
                @Override
                public void removeUpdate(DocumentEvent e) { updateValue(); }
                @Override
                public void changedUpdate(DocumentEvent e) { updateValue(); }

                private void updateValue() {
                    if (targetComponent != null) {
                        targetComponent.updateConfig(key, textField.getText());
                    }
                }
            });
        }
        // Gestion des combos
        else if (editor instanceof JComboBox<?> comboBox) {
            comboBox.addActionListener(e -> {
                if (targetComponent != null) {
                    targetComponent.updateConfig(key, comboBox.getSelectedItem());
                }
            });
        }
        // Gestion des spinners
        else if (editor instanceof JSpinner spinner) {
            spinner.addChangeListener(e -> {
                if (targetComponent != null) {
                    targetComponent.updateConfig(key, spinner.getValue());
                }
            });
        }
    }

    private static class ScrollableContentPanel extends JPanel implements Scrollable {

        ScrollableContentPanel() {
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        }

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 16;
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 64;
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
    }

    private JComponent createHeader() {
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBorder(new EmptyBorder(8, 10, 10, 10));

        JPanel titleRow = new JPanel(new BorderLayout(8, 0));
        titleRow.setOpaque(false);
        titleRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton backButton = createBackButton();
        JLabel title = new JLabel(item.name);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 13f));
        title.setForeground(DashboardTheme.TEXT);

        titleRow.add(backButton, BorderLayout.WEST);
        titleRow.add(title, BorderLayout.CENTER);

        header.add(titleRow);

        if (onDelete != null) {
            header.add(Box.createVerticalStrut(8));
            header.add(createDeleteButton());
        }

        return header;
    }

    private JButton createBackButton() {
        JButton backButton = new JButton(AllIcons.Actions.Back);
        backButton.setBorderPainted(false);
        backButton.setContentAreaFilled(false);
        backButton.setFocusPainted(false);
        backButton.setFocusable(false);
        backButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        backButton.setToolTipText("Back");
        backButton.addActionListener(e -> onBack.run());
        return backButton;
    }

    private JButton createDeleteButton() {
        JButton button = new JButton();
        button.setLayout(new BorderLayout(6, 0));

        JLabel icon = new JLabel(AllIcons.Actions.Cancel);
        JLabel text = new JLabel("Delete");
        text.setForeground(DashboardTheme.TEXT_SECONDARY);
        text.setFont(text.getFont().deriveFont(Font.PLAIN, 11f));

        button.add(icon, BorderLayout.WEST);
        button.add(text, BorderLayout.CENTER);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(DashboardTheme.BORDER_SUBTLE, 1),
                new EmptyBorder(5, 8, 5, 8)
        ));
        button.setBackground(DashboardTheme.SURFACE_2);
        button.setContentAreaFilled(true);
        button.setOpaque(true);
        button.setFocusPainted(false);
        button.setFocusable(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setToolTipText("Delete this visualization from the canvas");
        button.addActionListener(e -> onDelete.run());

        Dimension preferred = button.getPreferredSize();
        Dimension fitSize = new Dimension(preferred.width, 32);
        button.setPreferredSize(fitSize);
        button.setMinimumSize(fitSize);
        button.setMaximumSize(fitSize);
        button.setAlignmentX(Component.LEFT_ALIGNMENT);

        return button;
    }

    private JPanel createParameterRow(VisualizationParameter parameter, JComponent editor) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(0, 10, 0, 10));

        JLabel label = new JLabel(parameter.getLabel());
        label.setForeground(DashboardTheme.TEXT);
        label.setFont(label.getFont().deriveFont(11f));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        editor.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(label);
        panel.add(Box.createVerticalStrut(5));
        panel.add(editor);

        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, panel.getPreferredSize().height));

        return panel;
    }

    private JComponent createEditor(VisualizationParameter parameter) {
        // Cas spécial pour le type de marqueur de la carte
        if ("markerType".equals(parameter.getKey()) && parameter.getType() == VisualizationParameterType.TEXT) {
            return createMarkerTypeEditor();
        }

        return switch (parameter.getType()) {
            case TEXT -> createTextEditor();
            case NUMBER -> createNumberEditor(parameter.getKey());
            case DB_COLUMN -> createColumnEditor();
            case FORMULA -> createFormulaEditor();
            case DB_COLUMN_OR_FORMULA -> createColumnOrFormulaEditor();
            case SORT -> createSortEditor();
            case COLUMNS -> createColumnsEditor();
            case CONDITION -> createConditionEditor();
        };
    }

    private JComponent createMarkerTypeEditor() {
        JComboBox<String> combo = new JComboBox<>(new String[]{"pin", "buffer"});
        combo.setSelectedItem("pin"); // Par défaut : pin
        combo.setForeground(DashboardTheme.TEXT);
        combo.setBackground(DashboardTheme.SURFACE_2);
        setFixedHeight(combo, EDITOR_HEIGHT);
        return combo;
    }

    private JComponent createTextEditor() {
        JTextField field = new JTextField();
        styleField(field);
        return field;
    }

    private JComponent createNumberEditor(String key) {
        if ("limit".equals(key)) {
            return new OptionalNumberEditor();
        }

        JSpinner spinner = new JSpinner(new SpinnerNumberModel(10, 0, Integer.MAX_VALUE, 1));
        setFixedHeight(spinner, EDITOR_HEIGHT);
        return spinner;
    }

    private JComponent createColumnEditor() {
        JComboBox<String> combo = new JComboBox<>();
        combo.addItem("Select column...");
        styleCombo(combo);
        return combo;
    }

    private JComponent createFormulaEditor() {
        JTextField field = new JTextField();
        field.putClientProperty("JTextField.placeholderText", "Enter formula...");
        styleField(field);
        return field;
    }

    private JComponent createColumnOrFormulaEditor() {
        return new ColumnOrFormulaRow();
    }

    private JComponent createSortEditor() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JComboBox<String> column = new JComboBox<>();
        column.addItem("Select column...");

        JComboBox<String> direction = new JComboBox<>(new String[]{"Ascending", "Descending"});

        styleCombo(column);
        styleCombo(direction);

        column.setAlignmentX(Component.LEFT_ALIGNMENT);
        direction.setAlignmentX(Component.LEFT_ALIGNMENT);

        column.setMaximumSize(new Dimension(Integer.MAX_VALUE, EDITOR_HEIGHT));
        direction.setMaximumSize(new Dimension(Integer.MAX_VALUE, EDITOR_HEIGHT));

        panel.add(column);
        panel.add(Box.createVerticalStrut(6));
        panel.add(direction);

        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, panel.getPreferredSize().height));
        return panel;
    }

    private JComponent createColumnsEditor() {
        return new ColumnsEditorPanel();
    }

    private JComponent createConditionEditor() {
        JTextField field = new JTextField();
        field.putClientProperty("JTextField.placeholderText", "Ex: total > 100 AND status = 'active'");
        styleField(field);
        return field;
    }

    private void styleField(JTextField field) {
        field.setForeground(DashboardTheme.TEXT);
        field.setCaretColor(DashboardTheme.TEXT);
        field.setBackground(DashboardTheme.SURFACE_2);
        setFixedHeight(field, EDITOR_HEIGHT);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(DashboardTheme.BORDER_SUBTLE, 1),
                new EmptyBorder(0, 8, 0, 8)
        ));
    }

    private void styleCombo(JComboBox<?> combo) {
        combo.setForeground(DashboardTheme.TEXT);
        combo.setBackground(DashboardTheme.SURFACE_2);
        setFixedHeight(combo, EDITOR_HEIGHT);
    }

    private void setFixedHeight(JComponent component, int height) {
        Dimension preferred = component.getPreferredSize();
        component.setPreferredSize(new Dimension(preferred.width, height));
        component.setMinimumSize(new Dimension(0, height));
        component.setMaximumSize(new Dimension(Integer.MAX_VALUE, height));
    }

    // ============================================================
    // INNER CLASSES
    // ============================================================

    /**
     * Éditeur de nombre optionnel (peut être vide).
     */
    private class OptionalNumberEditor extends JPanel {
        private final JTextField numberField;
        private final JLabel hintLabel;
        private java.util.function.Consumer<Object> valueChangeListener;

        public OptionalNumberEditor() {
            setLayout(new BorderLayout());
            setOpaque(false);
            setAlignmentX(Component.LEFT_ALIGNMENT);

            numberField = new JTextField();
            numberField.putClientProperty("JTextField.placeholderText", "No limit");
            styleField(numberField);
            numberField.setAlignmentX(Component.LEFT_ALIGNMENT);

            hintLabel = new JLabel("Leave empty for no limit");
            hintLabel.setForeground(DashboardTheme.TEXT_SECONDARY);
            hintLabel.setFont(DashboardTheme.getFont(9));
            hintLabel.setBorder(new EmptyBorder(2, 2, 0, 0));

            numberField.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) { notifyChange(); }
                @Override
                public void removeUpdate(DocumentEvent e) { notifyChange(); }
                @Override
                public void changedUpdate(DocumentEvent e) { notifyChange(); }
            });

            add(numberField, BorderLayout.NORTH);
            add(hintLabel, BorderLayout.SOUTH);
        }

        public void setValueChangeListener(java.util.function.Consumer<Object> listener) {
            this.valueChangeListener = listener;
        }

        private void notifyChange() {
            if (valueChangeListener != null) {
                String text = numberField.getText().trim();
                if (text.isEmpty()) {
                    valueChangeListener.accept(null);
                } else {
                    try {
                        int value = Integer.parseInt(text);
                        if (value >= 0) {
                            valueChangeListener.accept(value);
                        } else {
                            valueChangeListener.accept(null);
                        }
                    } catch (NumberFormatException e) {
                        valueChangeListener.accept(null);
                    }
                }
            }
        }
    }

    /**
     * Ligne pour un éditeur column-or-formula.
     */
    private class ColumnOrFormulaRow extends JPanel {
        private final JComboBox<String> mode;
        private final JComboBox<String> column;
        private final JTextField formula;
        private final JPanel dynamicPanel;

        public ColumnOrFormulaRow() {
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setOpaque(false);
            setAlignmentX(Component.LEFT_ALIGNMENT);

            mode = new JComboBox<>(new String[]{"Database Column", "Formula"});
            styleCombo(mode);
            mode.setAlignmentX(Component.LEFT_ALIGNMENT);
            mode.setMaximumSize(new Dimension(Integer.MAX_VALUE, EDITOR_HEIGHT));

            column = new JComboBox<>();
            column.addItem("Select column...");
            styleCombo(column);
            column.setAlignmentX(Component.LEFT_ALIGNMENT);
            column.setMaximumSize(new Dimension(Integer.MAX_VALUE, EDITOR_HEIGHT));

            formula = new JTextField();
            formula.putClientProperty("JTextField.placeholderText", "Enter formula...");
            styleField(formula);
            formula.setAlignmentX(Component.LEFT_ALIGNMENT);
            formula.setMaximumSize(new Dimension(Integer.MAX_VALUE, EDITOR_HEIGHT));

            dynamicPanel = new JPanel(new BorderLayout());
            dynamicPanel.setOpaque(false);
            dynamicPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

            add(mode);
            add(Box.createVerticalStrut(6));
            add(dynamicPanel);

            updateDynamicPanel(false);

            mode.addActionListener(e -> {
                boolean isFormula = mode.getSelectedIndex() == 1;
                updateDynamicPanel(isFormula);
                revalidate();
                repaint();
                if (getParent() != null) {
                    getParent().revalidate();
                    getParent().repaint();
                }
            });
        }

        private void updateDynamicPanel(boolean isFormula) {
            dynamicPanel.removeAll();
            if (isFormula) {
                dynamicPanel.add(formula, BorderLayout.CENTER);
            } else {
                dynamicPanel.add(column, BorderLayout.CENTER);
            }
            dynamicPanel.revalidate();
            dynamicPanel.repaint();
        }

        public String getValue() {
            if (mode.getSelectedIndex() == 1) {
                return formula.getText();
            } else {
                return (String) column.getSelectedItem();
            }
        }
    }

    /**
     * Panneau d'édition de colonnes multiples.
     */
    private class ColumnsEditorPanel extends JPanel {
        private final JPanel columnsContainer;
        private Runnable columnsChangeListener;

        public ColumnsEditorPanel() {
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setOpaque(false);
            setAlignmentX(Component.LEFT_ALIGNMENT);

            columnsContainer = new JPanel();
            columnsContainer.setLayout(new BoxLayout(columnsContainer, BoxLayout.Y_AXIS));
            columnsContainer.setOpaque(false);
            columnsContainer.setAlignmentX(Component.LEFT_ALIGNMENT);

            JButton addButton = new JButton("+ Add column");
            addButton.setAlignmentX(Component.LEFT_ALIGNMENT);
            addButton.setFocusable(false);
            addButton.setBorderPainted(false);
            addButton.setContentAreaFilled(false);
            addButton.setOpaque(false);
            addButton.setForeground(DashboardTheme.ACCENT);
            addButton.setFont(DashboardTheme.boldFont(11));
            addButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addButton.addActionListener(e -> addColumnRow());

            add(columnsContainer);
            add(Box.createVerticalStrut(4));
            add(addButton);

            addColumnRow();
            addColumnRow();
        }

        public void setColumnsChangeListener(Runnable listener) {
            this.columnsChangeListener = listener;
        }

        private void addColumnRow() {
            ColumnOrFormulaRow row = new ColumnOrFormulaRow();

            JPanel rowWrapper = new JPanel(new BorderLayout(4, 0));
            rowWrapper.setOpaque(false);
            rowWrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
            rowWrapper.setBorder(new EmptyBorder(0, 0, 6, 0));

            JButton removeButton = new JButton("×");
            removeButton.setPreferredSize(new Dimension(24, 24));
            removeButton.setFocusable(false);
            removeButton.setBorderPainted(false);
            removeButton.setContentAreaFilled(false);
            removeButton.setOpaque(false);
            removeButton.setForeground(DashboardTheme.TEXT_SECONDARY);
            removeButton.setFont(removeButton.getFont().deriveFont(Font.PLAIN, 14f));
            removeButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            removeButton.setToolTipText("Remove column");
            removeButton.addActionListener(e -> {
                if (columnsContainer.getComponentCount() > 1) {
                    columnsContainer.remove(rowWrapper);
                    columnsContainer.revalidate();
                    columnsContainer.repaint();
                    notifyChange();
                }
            });

            rowWrapper.add(row, BorderLayout.CENTER);
            rowWrapper.add(removeButton, BorderLayout.EAST);

            columnsContainer.add(rowWrapper);
            columnsContainer.revalidate();
            columnsContainer.repaint();
            notifyChange();
        }

        private void notifyChange() {
            if (columnsChangeListener != null) {
                columnsChangeListener.run();
            }
        }

        public List<String> getColumns() {
            List<String> columns = new ArrayList<>();
            for (Component comp : columnsContainer.getComponents()) {
                if (comp instanceof JPanel wrapper) {
                    for (Component inner : wrapper.getComponents()) {
                        if (inner instanceof ColumnOrFormulaRow row) {
                            String value = row.getValue();
                            if (value != null && !value.isEmpty() && !"Select column...".equals(value)) {
                                columns.add(value);
                            }
                        }
                    }
                }
            }
            return columns;
        }
    }
}
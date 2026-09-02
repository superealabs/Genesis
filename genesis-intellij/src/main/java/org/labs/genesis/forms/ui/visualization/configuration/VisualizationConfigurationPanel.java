package org.labs.genesis.forms.ui.visualization.configuration;

import com.intellij.icons.AllIcons;
import org.labs.genesis.forms.theme.DashboardTheme;
import org.labs.genesis.forms.ui.common.ScrollableContentPanel;
import org.labs.genesis.forms.ui.visualization.DashboardVisualComponent;
import org.labs.genesis.forms.ui.visualization.configuration.editor.ColumnDropField;
import org.labs.genesis.forms.ui.visualization.configuration.editor.ColumnOrFormulaRow;
import org.labs.genesis.forms.ui.visualization.configuration.editor.ColumnsEditorPanel;
import org.labs.genesis.forms.ui.visualization.configuration.editor.OptionalNumberEditor;
import org.labs.genesis.forms.ui.visualization.model.VisualizationItem;
import org.labs.genesis.forms.ui.visualization.model.VisualizationParameter;
import org.labs.genesis.forms.ui.visualization.model.VisualizationParameterType;

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

    private final VisualizationItem item;
    private final DashboardVisualComponent targetComponent;
    private final Runnable onBack;
    private final Runnable onDelete;
    private final ScrollableContentPanel contentPanel;
    private final Map<String, JComponent> editorMap = new HashMap<>();
    private final Map<String, JPanel> rowMap = new HashMap<>();

    public VisualizationConfigurationPanel(
            DashboardVisualComponent targetComponent,
            VisualizationItem item,
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

        updateConditionalVisibility();
    }

    private void buildParameters() {
        for (VisualizationParameter parameter : item.parameters) {
            JComponent editor = createEditor(parameter);
            editorMap.put(parameter.getKey(), editor);

            bindEditorToConfig(parameter.getKey(), editor);

            JPanel row = createParameterRow(parameter, editor);
            rowMap.put(parameter.getKey(), row);
            contentPanel.add(row);
            contentPanel.add(Box.createVerticalStrut(10));
        }
        contentPanel.add(Box.createVerticalGlue());
    }

    private void updateConditionalVisibility() {
        JComponent markerTypeEditor = editorMap.get("markerType");
        JPanel valueColumnRow = rowMap.get("valueColumn");

        if (markerTypeEditor instanceof JComboBox<?> markerTypeCombo && valueColumnRow != null) {
            String selectedType = (String) markerTypeCombo.getSelectedItem();
            boolean isBuffer = "buffer".equalsIgnoreCase(selectedType);
            valueColumnRow.setVisible(isBuffer);
            contentPanel.revalidate();
            contentPanel.repaint();
        }
    }

    private void bindEditorToConfig(String key, JComponent editor) {
        if (targetComponent == null) return;

        // Gestion du titre
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
            String existingMarkerType = targetComponent.getConfigValue(key);
            if (existingMarkerType != null) {
                markerTypeCombo.setSelectedItem(existingMarkerType);
            }

            markerTypeCombo.addActionListener(e -> {
                if (targetComponent != null) {
                    targetComponent.updateConfig(key, markerTypeCombo.getSelectedItem());
                }
                updateConditionalVisibility();
            });
            return;
        }

        // Gestion des colonnes multiples
        if ("columns".equals(key) && editor instanceof ColumnsEditorPanel columnsPanel) {
            // Récupérer les colonnes existantes
            Object existingColumns = targetComponent.getConfigValue(key);
            if (existingColumns instanceof List<?> columnsList) {
                columnsPanel.setColumns((List<String>) columnsList);
            }

            columnsPanel.setColumnsChangeListener(() -> {
                if (targetComponent != null) {
                    targetComponent.updateConfig(key, columnsPanel.getColumns());
                }
            });
            return;
        }

        // Gestion du nombre optionnel (limit)
        if ("limit".equals(key) && editor instanceof OptionalNumberEditor optionalNumberEditor) {
            Object existingLimit = targetComponent.getConfigValue(key);
            if (existingLimit != null) {
                optionalNumberEditor.setValue(existingLimit);
            }

            optionalNumberEditor.setValueChangeListener(value -> {
                if (targetComponent != null) {
                    targetComponent.updateConfig(key, value);
                }
            });
            return;
        }

        // Gestion des champs de drop de colonnes
        if (editor instanceof ColumnDropField columnDropField) {
            String existingColumn = targetComponent.getConfigValue(key);
            if (existingColumn != null && !existingColumn.isEmpty()) {
                columnDropField.setColumn(existingColumn);
            }

            columnDropField.setColumnChangeListener(value -> {
                if (targetComponent != null) {
                    targetComponent.updateConfig(key, value);
                }
            });
            return;
        }

        // Gestion des champs texte
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
            String existingValue = targetComponent.getConfigValue(key);
            if (existingValue != null) {
                comboBox.setSelectedItem(existingValue);
            }

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
        if ("markerType".equals(parameter.getKey()) && parameter.getType() == VisualizationParameterType.TEXT) {
            return createMarkerTypeEditor();
        }

        return switch (parameter.getType()) {
            case TEXT -> createTextEditor();
            case NUMBER -> createNumberEditor(parameter.getKey());
            case DB_COLUMN -> createColumnEditor();
            case FORMULA -> createFormulaEditor();
            case DB_COLUMN_OR_FORMULA -> createColumnOrFormulaEditor(parameter);
            case SORT -> createSortEditor();
            case COLUMNS -> createColumnsEditor();
            case CONDITION -> createConditionEditor();
        };
    }

    private JComponent createMarkerTypeEditor() {
        JComboBox<String> combo = new JComboBox<>(new String[]{"pin", "buffer"});
        combo.setSelectedItem("pin");
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
        return new ColumnDropField();
    }

    private JComponent createFormulaEditor() {
        JTextField field = new JTextField();
        field.putClientProperty("JTextField.placeholderText", "Enter formula...");
        styleField(field);
        return field;
    }

    private JComponent createColumnOrFormulaEditor(VisualizationParameter parameter) {
        return new ColumnOrFormulaRow(parameter, targetComponent, parameter.getKey());
    }

    private JComponent createSortEditor() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        ColumnDropField column = new ColumnDropField();
        column.setAlignmentX(Component.LEFT_ALIGNMENT);
        column.setMaximumSize(new Dimension(Integer.MAX_VALUE, EDITOR_HEIGHT));

        JComboBox<String> direction = new JComboBox<>(new String[]{"Ascending", "Descending"});
        styleCombo(direction);
        direction.setAlignmentX(Component.LEFT_ALIGNMENT);
        direction.setMaximumSize(new Dimension(Integer.MAX_VALUE, EDITOR_HEIGHT));

        panel.add(column);
        panel.add(Box.createVerticalStrut(6));
        panel.add(direction);

        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, panel.getPreferredSize().height));
        return panel;
    }

    private JComponent createColumnsEditor() {
        // Vérifier si targetComponent est null
        if (targetComponent == null) {
            JPanel fallbackPanel = new JPanel();
            fallbackPanel.setOpaque(false);
            fallbackPanel.add(new JLabel("No component available"));
            return fallbackPanel;
        }

        // Créer avec targetComponent et la clé "columns"
        ColumnsEditorPanel panel = new ColumnsEditorPanel(targetComponent, "columns");

        // Charger les valeurs existantes
        try {
            Object existingColumns = targetComponent.getConfigValue("columns");
            if (existingColumns instanceof List<?> columnsList) {
                // Convertir en List<String> si nécessaire
                List<String> stringList = new ArrayList<>();
                for (Object obj : columnsList) {
                    if (obj instanceof String) {
                        stringList.add((String) obj);
                    }
                }
                panel.setColumns(stringList);
            }
        } catch (Exception e) {
            // Ignorer l'erreur et utiliser les valeurs par défaut
        }

        panel.setColumnsChangeListener(() -> {
            if (targetComponent != null) {
                targetComponent.revalidate();
                targetComponent.repaint();

                // Forcer la mise à jour du rendu
                targetComponent.getParent().revalidate();
                targetComponent.getParent().repaint();
            }
        });

        return panel;
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
}
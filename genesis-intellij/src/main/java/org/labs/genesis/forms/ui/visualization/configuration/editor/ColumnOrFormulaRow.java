package org.labs.genesis.forms.ui.visualization.configuration.editor;

import org.labs.genesis.forms.theme.DashboardTheme;
import org.labs.genesis.forms.ui.visualization.model.VisualizationParameter;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;

/**
 * Ligne d'édition "colonne ou formule".
 *
 * Ce composant ne connaît ni DashboardVisualComponent, ni configKey : il gère
 * uniquement son état local (mode + valeur) et notifie ses changements via un
 * simple Runnable. La question "qui persiste, et où" est entièrement déléguée
 * à l'appelant :
 *  - VisualizationConfigurationPanel.bindEditorToConfig() pour un usage seul
 *  - ColumnsEditorPanel pour un usage en liste
 * Un seul constructeur, un seul comportement, quel que soit le contexte.
 */
public class ColumnOrFormulaRow extends JPanel {

    private static final int EDITOR_HEIGHT = ColumnDropField.HEIGHT;

    private final VisualizationParameter parameter;
    private final JComboBox<String> modeCombo;
    private final ColumnDropField columnField;
    private final JTextField formulaField;
    private final JPanel dynamicPanel;
    private final CardLayout dynamicLayout;
    private Runnable changeListener;

    public ColumnOrFormulaRow(VisualizationParameter parameter) {
        this.parameter = parameter;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setOpaque(false);
        setAlignmentX(Component.LEFT_ALIGNMENT);

        if (parameter.getMode() == null) {
            parameter.setMode("COLUMN", true);
        }

        modeCombo = new JComboBox<>(new String[]{"Database Column", "Formula"});
        styleCombo(modeCombo);
        modeCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
        modeCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, EDITOR_HEIGHT));
        modeCombo.setSelectedIndex(parameter.isFormulaMode() ? 1 : 0);

        columnField = new ColumnDropField();
        columnField.setAlignmentX(Component.LEFT_ALIGNMENT);
        columnField.setMaximumSize(new Dimension(Integer.MAX_VALUE, EDITOR_HEIGHT));

        formulaField = new JTextField();
        formulaField.putClientProperty("JTextField.placeholderText", "Enter formula...");
        styleField(formulaField);
        formulaField.setAlignmentX(Component.LEFT_ALIGNMENT);
        formulaField.setMaximumSize(new Dimension(Integer.MAX_VALUE, EDITOR_HEIGHT));

        dynamicLayout = new CardLayout();
        dynamicPanel = new JPanel(dynamicLayout);
        dynamicPanel.setOpaque(false);
        dynamicPanel.add(columnField, "COLUMN");
        dynamicPanel.add(formulaField, "FORMULA");
        dynamicPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        dynamicPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, EDITOR_HEIGHT));
        dynamicPanel.setPreferredSize(new Dimension(EDITOR_HEIGHT, EDITOR_HEIGHT));

        add(modeCombo);
        add(Box.createVerticalStrut(6));
        add(dynamicPanel);

        updateDynamicPanel(parameter.isFormulaMode());

        modeCombo.addActionListener(e -> {
            boolean isFormula = modeCombo.getSelectedIndex() == 1;
            parameter.setMode(isFormula ? "FORMULA" : "COLUMN", true);
            updateDynamicPanel(isFormula);

            if (isFormula) {
                columnField.clearColumn();
            } else {
                formulaField.setText(null);
            }
            fireChange();
        });

        columnField.setColumnChangeListener(value -> {
            if (parameter.isColumnMode()) {
                parameter.setValue(value);
                fireChange();
            }
        });

        formulaField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { onFormulaEdited(); }
            @Override public void removeUpdate(DocumentEvent e) { onFormulaEdited(); }
            @Override public void changedUpdate(DocumentEvent e) { onFormulaEdited(); }

            private void onFormulaEdited() {
                if (parameter.isFormulaMode()) {
                    String value = formulaField.getText();
                    parameter.setValue((value != null && !value.isBlank()) ? value : null);
                    fireChange();
                }
            }
        });
    }

    /**
     * Restaure une valeur (potentiellement préfixée "COLUMN:"/"FORMULA:") sans
     * déclencher le changeListener. À appeler AVANT setChangeListener().
     */
    public void restoreValue(String value) {
        if (value == null || value.isBlank()) {
            return;
        }

        if (value.startsWith("FORMULA:")) {
            String formulaValue = value.substring(8);
            switchToFormula();
            formulaField.setText(formulaValue);
            parameter.setValue(formulaValue);
            parameter.setMode("FORMULA", true);
        } else if (value.startsWith("COLUMN:")) {
            String columnValue = value.substring(7);
            switchToColumn();
            columnField.setColumn(columnValue);
            parameter.setValue(columnValue);
            parameter.setMode("COLUMN", true);
        } else {
            switchToColumn();
            columnField.setColumn(value);
            parameter.setValue(value);
            parameter.setMode("COLUMN", true);
        }
    }

    /**
     * Valeur prête à être persistée, avec préfixe de mode. Seul et unique endroit
     * du module qui construit "COLUMN:"/"FORMULA:" — plus de duplication de cette
     * logique entre ColumnOrFormulaRow, ColumnsEditorPanel et VisualizationConfigurationPanel.
     */
    public String getStorageValue() {
        String value = getValue();
        return value == null ? null : (parameter.isFormulaMode() ? "FORMULA:" : "COLUMN:") + value;
    }

    private void updateDynamicPanel(boolean isFormula) {
        dynamicLayout.show(dynamicPanel, isFormula ? "FORMULA" : "COLUMN");
    }

    private void fireChange() {
        if (changeListener != null) {
            changeListener.run();
        }
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

    public void setChangeListener(Runnable listener) {
        this.changeListener = listener;
    }

    public String getValue() {
        if (parameter.isFormulaMode()) {
            String text = formulaField.getText();
            return (text != null && !text.isBlank()) ? text : null;
        }
        String columnName = columnField.getColumnName();
        return (columnName != null && !columnName.isBlank()) ? columnName : null;
    }

    public String getMode() {
        return parameter.getMode();
    }

    public void switchToFormula() {
        if (modeCombo.getSelectedIndex() != 1) {
            modeCombo.setSelectedIndex(1);
        } else {
            updateDynamicPanel(true);
        }
    }

    public void switchToColumn() {
        if (modeCombo.getSelectedIndex() != 0) {
            modeCombo.setSelectedIndex(0);
        } else {
            updateDynamicPanel(false);
        }
    }

    public boolean hasValue() {
        return getValue() != null;
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        modeCombo.setEnabled(enabled);
        columnField.setEnabled(enabled);
        formulaField.setEnabled(enabled);
    }
}
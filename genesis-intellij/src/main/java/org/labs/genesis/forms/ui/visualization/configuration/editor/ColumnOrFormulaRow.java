package org.labs.genesis.forms.ui.visualization.configuration.editor;

import org.labs.genesis.forms.ui.visualization.model.VisualizationParameter;
import org.labs.genesis.forms.theme.DashboardTheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;

/**
 * Ligne pour un éditeur column-or-formula.
 * Utilise explicitement le mode stocké dans VisualizationParameter.
 */
public class ColumnOrFormulaRow extends JPanel {

    private final VisualizationParameter parameter;
    private final JComboBox<String> modeCombo;
    private final ColumnDropField column;
    private final JTextField formula;
    private final JPanel dynamicPanel;
    private Runnable changeListener;

    public ColumnOrFormulaRow(VisualizationParameter parameter) {
        this.parameter = parameter;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setOpaque(false);
        setAlignmentX(Component.LEFT_ALIGNMENT);

        // Initialiser le mode par défaut si null
        if (parameter.getMode() == null) {
            parameter.setMode("COLUMN", true);
        }

        // Combo pour le mode
        modeCombo = new JComboBox<>(new String[]{"Database Column", "Formula"});
        styleCombo(modeCombo);
        modeCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
        modeCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        // Sélectionner l'élément en fonction du mode stocké
        if (parameter.isFormulaMode()) {
            modeCombo.setSelectedIndex(1);  // Sélectionne "Formula"
        } else {
            modeCombo.setSelectedIndex(0);  // Sélectionne "Database Column"
        }

        // Champ colonne
        column = new ColumnDropField();
        column.setAlignmentX(Component.LEFT_ALIGNMENT);
        column.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        // Récupérer la valeur existante si c'est une colonne
        if (parameter.isColumnMode() && parameter.getValue() != null) {
            column.setColumn(parameter.getValue());
        }

        // Champ formule
        formula = new JTextField();
        formula.putClientProperty("JTextField.placeholderText", "Enter formula...");
        styleField(formula);
        formula.setAlignmentX(Component.LEFT_ALIGNMENT);
        formula.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        // Récupérer la valeur existante si c'est une formule
        if (parameter.isFormulaMode() && parameter.getValue() != null) {
            formula.setText(parameter.getValue());
        }

        // Panneau dynamique
        dynamicPanel = new JPanel(new BorderLayout());
        dynamicPanel.setOpaque(false);
        dynamicPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        add(modeCombo);
        add(Box.createVerticalStrut(6));
        add(dynamicPanel);

        // Initialiser l'affichage
        updateDynamicPanel(parameter.isFormulaMode());

        // Écouteur pour le changement de mode
        modeCombo.addActionListener(e -> {
            boolean isFormula = modeCombo.getSelectedIndex() == 1;

            // Mettre à jour le mode dans le paramètre
            if (isFormula) {
                parameter.setMode("FORMULA", true);
            } else {
                parameter.setMode("COLUMN", true);
            }

            updateDynamicPanel(isFormula);
            revalidate();
            repaint();
            notifyChange();

            if (getParent() != null) {
                getParent().revalidate();
                getParent().repaint();
            }
        });

        // Écouteur pour les changements de la colonne
        column.setColumnChangeListener(value -> {
            if (parameter.isColumnMode() && value != null && !value.isBlank()) {
                parameter.setValue(value);
                notifyChange();
            }
        });

        // Écouteur pour les changements de la formule
        formula.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { updateFormula(); }
            @Override
            public void removeUpdate(DocumentEvent e) { updateFormula(); }
            @Override
            public void changedUpdate(DocumentEvent e) { updateFormula(); }

            private void updateFormula() {
                String value = formula.getText();
                if (parameter.isFormulaMode() && value != null && !value.isBlank()) {
                    parameter.setValue(value);
                    notifyChange();
                }
            }
        });
    }

    private void updateDynamicPanel(boolean isFormula) {
        dynamicPanel.removeAll();
        if (isFormula) {
            dynamicPanel.add(formula, BorderLayout.CENTER);
            column.clearColumn();
        } else {
            dynamicPanel.add(column, BorderLayout.CENTER);
            formula.setText(null);
        }
        dynamicPanel.revalidate();
        dynamicPanel.repaint();
    }

    private void styleField(JTextField field) {
        field.setForeground(DashboardTheme.TEXT);
        field.setCaretColor(DashboardTheme.TEXT);
        field.setBackground(DashboardTheme.SURFACE_2);
        setFixedHeight(field, 30);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(DashboardTheme.BORDER_SUBTLE, 1),
                new EmptyBorder(0, 8, 0, 8)
        ));
    }

    private void styleCombo(JComboBox<?> combo) {
        combo.setForeground(DashboardTheme.TEXT);
        combo.setBackground(DashboardTheme.SURFACE_2);
        setFixedHeight(combo, 30);
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

    private void notifyChange() {
        if (changeListener != null) {
            changeListener.run();
        }
    }

    public String getValue() {
        if (parameter.isFormulaMode()) {
            return formula.getText();
        } else {
            return column.getColumnName();
        }
    }

    public String getMode() {
        return parameter.getMode();
    }
}
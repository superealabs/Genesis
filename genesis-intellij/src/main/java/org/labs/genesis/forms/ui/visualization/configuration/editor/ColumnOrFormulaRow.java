package org.labs.genesis.forms.ui.visualization.configuration.editor;

import org.labs.genesis.forms.ui.visualization.DashboardVisualComponent;
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
 * Notifie le DashboardVisualComponent des changements pour mettre à jour l'affichage.
 */
public class ColumnOrFormulaRow extends JPanel {

    private static final int EDITOR_HEIGHT = 30;

    private final VisualizationParameter parameter;
    private final JComboBox<String> modeCombo;
    private final ColumnDropField columnField;
    private final JTextField formulaField;
    private final JPanel dynamicPanel;
    private Runnable changeListener;
    private final String configKey;
    private final DashboardVisualComponent targetComponent;

    /**
     * Constructeur principal.
     *
     * @param parameter         Le paramètre de visualisation
     * @param targetComponent   Le composant cible pour mettre à jour la configuration
     * @param configKey         La clé de configuration dans le DashboardVisualComponent
     */
    public ColumnOrFormulaRow(VisualizationParameter parameter,
                              DashboardVisualComponent targetComponent,
                              String configKey) {
        this.parameter = parameter;
        this.targetComponent = targetComponent;
        this.configKey = configKey;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setOpaque(false);
        setAlignmentX(Component.LEFT_ALIGNMENT);

        // Initialiser le mode par défaut si null
        if (parameter.getMode() == null) {
            parameter.setMode("COLUMN", true);
        }

        // Créer le combo pour le mode
        modeCombo = new JComboBox<>(new String[]{"Database Column", "Formula"});
        styleCombo(modeCombo);
        modeCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
        modeCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, EDITOR_HEIGHT));

        // Sélectionner l'élément en fonction du mode stocké
        if (parameter.isFormulaMode()) {
            modeCombo.setSelectedIndex(1);
        } else {
            modeCombo.setSelectedIndex(0);
        }

        // Créer le champ colonne
        columnField = new ColumnDropField();
        columnField.setAlignmentX(Component.LEFT_ALIGNMENT);
        columnField.setMaximumSize(new Dimension(Integer.MAX_VALUE, EDITOR_HEIGHT));

        // Créer le champ formule
        formulaField = new JTextField();
        formulaField.putClientProperty("JTextField.placeholderText", "Enter formula...");
        styleField(formulaField);
        formulaField.setAlignmentX(Component.LEFT_ALIGNMENT);
        formulaField.setMaximumSize(new Dimension(Integer.MAX_VALUE, EDITOR_HEIGHT));

        // Charger les valeurs existantes
        loadExistingValues();

        // Panneau dynamique qui contient soit le champ colonne, soit le champ formule
        dynamicPanel = new JPanel(new BorderLayout());
        dynamicPanel.setOpaque(false);
        dynamicPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        add(modeCombo);
        add(Box.createVerticalStrut(6));
        add(dynamicPanel);

        // Initialiser l'affichage
        updateDynamicPanel(parameter.isFormulaMode());

        // --- Écouteurs ---

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

            // Vider la valeur si on change de mode
            if (isFormula) {
                columnField.clearColumn();
                // Notifier avec la valeur de la formule si elle existe
                String formulaText = formulaField.getText();
                if (formulaText != null && !formulaText.isBlank()) {
                    notifyChange();
                } else {
                    // Notifier avec null pour déclencher l'erreur
                    notifyChangeWithNull();
                }
            } else {
                formulaField.setText(null);
                String columnValue = columnField.getColumnName();
                if (columnValue != null && !columnValue.isBlank()) {
                    notifyChange();
                } else {
                    notifyChangeWithNull();
                }
            }

            revalidate();
            repaint();

            if (getParent() != null) {
                getParent().revalidate();
                getParent().repaint();
            }
        });

        // Écouteur pour les changements de la colonne
        columnField.setColumnChangeListener(value -> {
            if (parameter.isColumnMode()) {
                if (value != null && !value.isBlank()) {
                    parameter.setValue(value);
                    notifyChange();
                } else {
                    parameter.setValue(null);
                    notifyChangeWithNull();
                }
            }
        });

        // Écouteur pour les changements de la formule
        formulaField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { updateFormula(); }
            @Override
            public void removeUpdate(DocumentEvent e) { updateFormula(); }
            @Override
            public void changedUpdate(DocumentEvent e) { updateFormula(); }

            private void updateFormula() {
                if (parameter.isFormulaMode()) {
                    String value = formulaField.getText();
                    if (value != null && !value.isBlank()) {
                        parameter.setValue(value);
                        notifyChange();
                    } else {
                        parameter.setValue(null);
                        notifyChangeWithNull();
                    }
                }
            }
        });
    }

    /**
     * Charge les valeurs existantes depuis le DashboardVisualComponent.
     */
    private void loadExistingValues() {
        if (targetComponent == null || configKey == null) {
            return;
        }

        String existingValue = targetComponent.getConfigValue(configKey);

        if (existingValue != null && !existingValue.isEmpty()) {
            // Détecter si c'est une formule (stockée avec préfixe)
            if (existingValue.startsWith("FORMULA:")) {
                String formulaValue = existingValue.substring(8);
                formulaField.setText(formulaValue);
                parameter.setValue(formulaValue);
                parameter.setMode("FORMULA", true);
                if (modeCombo.getSelectedIndex() != 1) {
                    modeCombo.setSelectedIndex(1);
                }
            } else if (existingValue.startsWith("COLUMN:")) {
                String columnValue = existingValue.substring(7);
                columnField.setColumn(columnValue);
                parameter.setValue(columnValue);
                parameter.setMode("COLUMN", true);
                if (modeCombo.getSelectedIndex() != 0) {
                    modeCombo.setSelectedIndex(0);
                }
            } else {
                // Par défaut, on considère que c'est une colonne
                columnField.setColumn(existingValue);
                parameter.setValue(existingValue);
                parameter.setMode("COLUMN", true);
                if (modeCombo.getSelectedIndex() != 0) {
                    modeCombo.setSelectedIndex(0);
                }
            }
        } else if (parameter.getValue() != null) {
            // Si le paramètre a une valeur mais pas le targetComponent
            String paramValue = parameter.getValue();
            if (paramValue != null && !paramValue.isBlank()) {
                if (parameter.isFormulaMode()) {
                    formulaField.setText(paramValue);
                } else {
                    columnField.setColumn(paramValue);
                }
            }
        }
    }

    /**
     * Met à jour le panneau dynamique en fonction du mode.
     */
    private void updateDynamicPanel(boolean isFormula) {
        dynamicPanel.removeAll();
        if (isFormula) {
            dynamicPanel.add(formulaField, BorderLayout.CENTER);
            // Vider le champ colonne mais garder la valeur stockée
            if (!columnField.getColumnName().isBlank()) {
                columnField.clearColumn();
            }
        } else {
            dynamicPanel.add(columnField, BorderLayout.CENTER);
            // Vider le champ formule mais garder la valeur stockée
            if (formulaField.getText() != null && !formulaField.getText().isBlank()) {
                formulaField.setText(null);
            }
        }
        dynamicPanel.revalidate();
        dynamicPanel.repaint();
    }

    /**
     * Style pour les champs de texte.
     */
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

    /**
     * Style pour les combobox.
     */
    private void styleCombo(JComboBox<?> combo) {
        combo.setForeground(DashboardTheme.TEXT);
        combo.setBackground(DashboardTheme.SURFACE_2);
        setFixedHeight(combo, EDITOR_HEIGHT);
    }

    /**
     * Fixe la hauteur d'un composant.
     */
    private void setFixedHeight(JComponent component, int height) {
        Dimension preferred = component.getPreferredSize();
        component.setPreferredSize(new Dimension(preferred.width, height));
        component.setMinimumSize(new Dimension(0, height));
        component.setMaximumSize(new Dimension(Integer.MAX_VALUE, height));
    }

    /**
     * Définit l'écouteur de changement.
     */
    public void setChangeListener(Runnable listener) {
        this.changeListener = listener;
    }

    /**
     * Notifie les écouteurs des changements avec la valeur actuelle.
     */
    private void notifyChange() {
        String value = getValue();

        // Mettre à jour la configuration du composant cible
        if (targetComponent != null && configKey != null) {
            String storageValue = value;

            // Stocker avec préfixe si c'est une formule pour pouvoir la reconnaître au chargement
            if (parameter.isFormulaMode() && value != null && !value.isBlank()) {
                storageValue = "FORMULA:" + value;
            } else if (parameter.isColumnMode() && value != null && !value.isBlank()) {
                storageValue = "COLUMN:" + value;
            }

            targetComponent.updateConfig(configKey, storageValue);
        }

        if (changeListener != null) {
            changeListener.run();
        }
    }

    /**
     * Notifie les écouteurs des changements avec une valeur nulle.
     */
    private void notifyChangeWithNull() {
        // Mettre à jour la configuration du composant cible avec null
        if (targetComponent != null && configKey != null) {
            targetComponent.updateConfig(configKey, null);
        }

        if (changeListener != null) {
            changeListener.run();
        }
    }

    /**
     * Récupère la valeur actuelle (colonne ou formule).
     */
    public String getValue() {
        if (parameter.isFormulaMode()) {
            String text = formulaField.getText();
            return (text != null && !text.isBlank()) ? text : null;
        } else {
            String columnName = columnField.getColumnName();
            return (columnName != null && !columnName.isBlank()) ? columnName : null;
        }
    }

    /**
     * Récupère le mode actuel.
     */
    public String getMode() {
        return parameter.getMode();
    }

    /**
     * Bascule vers le mode formule.
     */
    public void switchToFormula() {
        if (modeCombo.getSelectedIndex() != 1) {
            modeCombo.setSelectedIndex(1);
        }
    }

    /**
     * Bascule vers le mode colonne.
     */
    public void switchToColumn() {
        if (modeCombo.getSelectedIndex() != 0) {
            modeCombo.setSelectedIndex(0);
        }
    }

    /**
     * Définit une valeur de colonne.
     */
    public void setColumn(String value) {
        if (value != null && !value.isBlank()) {
            columnField.setColumn(value);
            parameter.setValue(value);
            parameter.setMode("COLUMN", true);
            if (modeCombo.getSelectedIndex() != 0) {
                switchToColumn();
            }
            // Mettre à jour la configuration
            if (targetComponent != null && configKey != null) {
                targetComponent.updateConfig(configKey, "COLUMN:" + value);
            }
        }
    }

    /**
     * Définit une valeur de formule.
     */
    public void setFormula(String value) {
        if (value != null && !value.isBlank()) {
            formulaField.setText(value);
            parameter.setValue(value);
            parameter.setMode("FORMULA", true);
            if (modeCombo.getSelectedIndex() != 1) {
                switchToFormula();
            }
            // Mettre à jour la configuration
            if (targetComponent != null && configKey != null) {
                targetComponent.updateConfig(configKey, "FORMULA:" + value);
            }
        }
    }

    /**
     * Vérifie si une valeur est définie.
     */
    public boolean hasValue() {
        return getValue() != null && !getValue().isBlank();
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        modeCombo.setEnabled(enabled);
        columnField.setEnabled(enabled);
        formulaField.setEnabled(enabled);
    }
}
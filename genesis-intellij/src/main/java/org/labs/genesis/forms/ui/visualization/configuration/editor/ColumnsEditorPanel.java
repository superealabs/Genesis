package org.labs.genesis.forms.ui.visualization.configuration.editor;

import org.labs.genesis.forms.theme.DashboardTheme;
import org.labs.genesis.forms.ui.visualization.DashboardVisualComponent;
import org.labs.genesis.forms.ui.visualization.model.VisualizationParameter;
import org.labs.genesis.forms.ui.visualization.model.VisualizationParameterType;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Panneau d'édition pour les colonnes multiples (ColumnsEditorPanel)
 * Gère une liste de colonnes/formules avec possibilité d'ajouter/supprimer
 */
public class ColumnsEditorPanel extends JPanel {

    private final JPanel columnsContainer;
    private final JButton addButton;
    private final List<ColumnOrFormulaRow> rows = new ArrayList<>();
    private final String configKey;
    private final DashboardVisualComponent targetComponent;
    private Runnable changeListener;

    /**
     * Constructeur par défaut (pour compatibilité)
     */
    public ColumnsEditorPanel() {
        this(null, null);
    }

    /**
     * Constructeur principal.
     *
     * @param targetComponent Le composant cible pour la configuration
     * @param configKey       La clé de configuration
     */
    public ColumnsEditorPanel(DashboardVisualComponent targetComponent, String configKey) {
        this.targetComponent = targetComponent;
        this.configKey = configKey;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setOpaque(false);
        setAlignmentX(Component.LEFT_ALIGNMENT);

        // Conteneur pour les lignes de colonnes
        columnsContainer = new JPanel();
        columnsContainer.setLayout(new BoxLayout(columnsContainer, BoxLayout.Y_AXIS));
        columnsContainer.setOpaque(false);
        columnsContainer.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Bouton pour ajouter une colonne
        addButton = new JButton("+ Add Column");
        addButton.setForeground(DashboardTheme.TEXT);
        addButton.setBackground(DashboardTheme.SURFACE_2);
        addButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(DashboardTheme.BORDER_SUBTLE, 1),
                new EmptyBorder(4, 10, 4, 10)
        ));
        addButton.setFocusPainted(false);
        addButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        addButton.addActionListener(e -> addColumnRow(null));

        add(columnsContainer);
        add(Box.createVerticalStrut(4));
        add(addButton);
        add(Box.createVerticalGlue());

        // Ajouter une ligne par défaut
        addColumnRow(null);
    }

    /**
     * Ajoute une nouvelle ligne de colonne.
     *
     * @param initialValue La valeur initiale (peut être null)
     */
    private void addColumnRow(String initialValue) {
        // Créer un paramètre pour cette colonne
        VisualizationParameter param = VisualizationParameter.columnOrFormula(
                "column_" + System.currentTimeMillis(),
                "Column",
                true
        );

        if (initialValue != null) {
            // Déterminer si c'est une colonne ou une formule
            if (initialValue.startsWith("FORMULA:")) {
                param.setMode("FORMULA");
                param.setValue(initialValue.substring(8));
            } else if (initialValue.startsWith("COLUMN:")) {
                param.setMode("COLUMN");
                param.setValue(initialValue.substring(7));
            } else {
                // Par défaut, considérer comme une colonne
                param.setMode("COLUMN");
                param.setValue(initialValue);
            }
        }

        // Créer la ligne avec targetComponent et configKey
        ColumnOrFormulaRow row = new ColumnOrFormulaRow(
                param,
                targetComponent,
                configKey != null ? configKey : param.getKey()
        );
        rows.add(row);

        // Créer un wrapper pour la ligne avec le bouton de suppression
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
                rows.remove(row);
                columnsContainer.revalidate();
                columnsContainer.repaint();
                notifyChange();
            }
        });

        // Connecter le changement de la ligne à la notification
        row.setChangeListener(this::notifyChange);

        rowWrapper.add(row, BorderLayout.CENTER);
        rowWrapper.add(removeButton, BorderLayout.EAST);

        columnsContainer.add(rowWrapper);
        columnsContainer.revalidate();
        columnsContainer.repaint();
        notifyChange();
    }

    /**
     * Définit les colonnes initiales.
     *
     * @param columns La liste des colonnes/formules
     */
    public void setColumns(List<String> columns) {
        // Vider le conteneur
        columnsContainer.removeAll();
        rows.clear();

        if (columns == null || columns.isEmpty()) {
            // Ajouter une ligne vide par défaut
            addColumnRow(null);
            return;
        }

        // Ajouter chaque colonne
        for (String column : columns) {
            addColumnRow(column);
        }

        columnsContainer.revalidate();
        columnsContainer.repaint();
        notifyChange();
    }

    /**
     * Récupère la liste des colonnes/formules.
     *
     * @return La liste des valeurs
     */
    public List<String> getColumns() {
        List<String> result = new ArrayList<>();
        for (ColumnOrFormulaRow row : rows) {
            String value = row.getValue();
            String mode = row.getMode();

            if (value != null && !value.isBlank()) {
                // Stocker avec le préfixe approprié
                if ("FORMULA".equals(mode)) {
                    result.add("FORMULA:" + value);
                } else {
                    result.add("COLUMN:" + value);
                }
            }
        }
        return result;
    }

    /**
     * Définit l'écouteur de changement.
     */
    public void setColumnsChangeListener(Runnable listener) {
        this.changeListener = listener;
    }

    /**
     * Notifie les écouteurs des changements.
     */
    private void notifyChange() {
        // Mettre à jour la configuration du composant cible
        if (targetComponent != null && configKey != null) {
            List<String> columns = getColumns();
            targetComponent.updateConfig(configKey, columns);
        }

        if (changeListener != null) {
            changeListener.run();
        }
    }

    /**
     * Vérifie si toutes les colonnes sont valides.
     *
     * @return true si toutes les colonnes ont une valeur, false sinon
     */
    public boolean isValid() {
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
        addButton.setEnabled(enabled);
    }

    /**
     * Récupère le nombre de colonnes.
     */
    public int getColumnCount() {
        return rows.size();
    }
}
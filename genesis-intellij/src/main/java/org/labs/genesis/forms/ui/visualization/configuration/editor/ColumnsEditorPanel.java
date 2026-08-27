package org.labs.genesis.forms.ui.visualization.configuration.editor;

import org.labs.genesis.forms.ui.visualization.model.VisualizationParameter;
import org.labs.genesis.forms.theme.DashboardTheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Panneau d'édition de colonnes multiples.
 */
public class ColumnsEditorPanel extends JPanel {

    private final JPanel columnsContainer;
    private Runnable columnsChangeListener;
    private List<String> initialColumns = new ArrayList<>();

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
    }

    public void setColumns(List<String> columns) {
        this.initialColumns = new ArrayList<>(columns);
        columnsContainer.removeAll();

        if (columns.isEmpty()) {
            // Ajouter une ligne par défaut si la liste est vide
            addColumnRow();
        } else {
            for (String column : columns) {
                addColumnRow(column);
            }
        }
        columnsContainer.revalidate();
        columnsContainer.repaint();
    }

    private void addColumnRow() {
        addColumnRow(null);
    }

    private void addColumnRow(String initialValue) {
        // Créer un paramètre pour cette colonne
        VisualizationParameter param = VisualizationParameter.columnOrFormula(
                "column_" + System.currentTimeMillis(),
                "Column"
        );

        if (initialValue != null) {
            // Déterminer si c'est une colonne ou une formule
            if (initialValue.startsWith("FORMULA:")) {
                param.setMode("FORMULA");
                param.setValue(initialValue.substring(8));
            } else {
                param.setMode("COLUMN");
                param.setValue(initialValue);
            }
        }

        ColumnOrFormulaRow row = new ColumnOrFormulaRow(param);

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

        row.setChangeListener(this::notifyChange);

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

    public void setColumnsChangeListener(Runnable listener) {
        this.columnsChangeListener = listener;
    }

    public List<String> getColumns() {
        List<String> columns = new ArrayList<>();
        for (Component comp : columnsContainer.getComponents()) {
            if (comp instanceof JPanel wrapper) {
                for (Component inner : wrapper.getComponents()) {
                    if (inner instanceof ColumnOrFormulaRow row) {
                        String value = row.getValue();
                        String mode = row.getMode();
                        if (value != null && !value.isEmpty()) {
                            // Stocker avec un préfixe si c'est une formule
                            if ("FORMULA".equals(mode)) {
                                columns.add("FORMULA:" + value);
                            } else {
                                columns.add(value);
                            }
                        }
                    }
                }
            }
        }
        return columns;
    }
}
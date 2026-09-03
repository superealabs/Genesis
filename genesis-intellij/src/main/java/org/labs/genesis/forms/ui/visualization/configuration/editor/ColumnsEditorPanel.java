package org.labs.genesis.forms.ui.visualization.configuration.editor;

import com.intellij.icons.AllIcons;
import org.labs.genesis.forms.theme.DashboardTheme;
import org.labs.genesis.forms.ui.visualization.model.VisualizationParameter;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Éditeur d'une liste de colonnes/formules.
 *
 * Ce panel ne connaît PAS DashboardVisualComponent : il expose une liste de
 * valeurs ("COLUMN:x"/"FORMULA:x") et un listener de changement. La persistance
 * est de la seule responsabilité de l'appelant (VisualizationConfigurationPanel),
 * comme pour tout autre éditeur — plus de double écriture de configuration.
 */
public class ColumnsEditorPanel extends JPanel {

    private final JPanel columnsContainer;
    private final JLabel countLabel;
    private final List<ColumnOrFormulaRow> rows = new ArrayList<>();
    private Runnable changeListener;
    private int rowCounter = 0;

    public ColumnsEditorPanel() {
        setLayout(new BorderLayout());
        setOpaque(false);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setOpaque(false);

        columnsContainer = new JPanel();
        columnsContainer.setLayout(new BoxLayout(columnsContainer, BoxLayout.Y_AXIS));
        columnsContainer.setOpaque(false);

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

        add(mainPanel, BorderLayout.CENTER);

        addColumnRow(null);
        updateCountLabel();
    }

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
                BorderFactory.createLineBorder(DashboardTheme.BORDER_SUBTLE, 1),
                new EmptyBorder(5, 8, 5, 8)
        ));
        button.setAlignmentX(Component.LEFT_ALIGNMENT);

        button.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                button.setBackground(DashboardTheme.SURFACE_ACTIVE);
            }
            @Override public void mouseExited(MouseEvent e) {
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
                "column_" + (++rowCounter), "Column", true
        );

        ColumnOrFormulaRow row = new ColumnOrFormulaRow(param);

        // Restauration AVANT l'attache du listener : aucune notification "fantôme"
        // pendant l'initialisation, plus besoin d'un flag "loading" de sécurité.
        if (initialValue != null && !initialValue.isBlank()) {
            row.restoreValue(initialValue);
        }
        row.setChangeListener(this::notifyChange);

        rows.add(row);
        columnsContainer.add(createRowWrapper(row));
        updateCountLabel();
    }

    private JPanel createRowWrapper(ColumnOrFormulaRow row) {
        JPanel rowWrapper = new JPanel(new BorderLayout(6, 0));
        rowWrapper.setOpaque(false);
        rowWrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
        rowWrapper.setBorder(new EmptyBorder(0, 0, 8, 0));

        JButton removeButton = createRemoveButton(row);

        rowWrapper.add(row, BorderLayout.CENTER);
        rowWrapper.add(removeButton, BorderLayout.EAST);

        // Hauteur bornée à ce dont la ligne a réellement besoin (calculée à chaque
        // (re)création, jamais figée) : plus de clipping possible si le contenu grandit.
        int preferredHeight = rowWrapper.getPreferredSize().height;
        rowWrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, preferredHeight));

        return rowWrapper;
    }

    private JButton createRemoveButton(ColumnOrFormulaRow row) {
        JButton removeButton = new JButton(AllIcons.Actions.Close);
        removeButton.setToolTipText("Remove column");
        removeButton.setFocusPainted(false);
        removeButton.setFocusable(false);
        removeButton.setBorderPainted(false);
        removeButton.setContentAreaFilled(false);
        removeButton.setForeground(DashboardTheme.TEXT_SECONDARY);
        removeButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        Dimension btnSize = new Dimension(28, ColumnDropField.HEIGHT);
        removeButton.setPreferredSize(btnSize);
        removeButton.setMinimumSize(btnSize);
        removeButton.setMaximumSize(btnSize);

        removeButton.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                removeButton.setForeground(DashboardTheme.ERROR);
            }
            @Override public void mouseExited(MouseEvent e) {
                removeButton.setForeground(DashboardTheme.TEXT_SECONDARY);
            }
        });

        removeButton.addActionListener(e -> removeColumn(row));
        return removeButton;
    }

    private void removeColumn(ColumnOrFormulaRow row) {
        if (rows.size() <= 1) {
            return;
        }
        rows.remove(row);
        refreshRows();
        notifyChange();
    }

    public void setColumns(List<String> columns) {
        columnsContainer.removeAll();
        rows.clear();
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

    public void setColumnsChangeListener(Runnable listener) {
        this.changeListener = listener;
    }

    private void notifyChange() {
        updateCountLabel();
        if (changeListener != null) {
            changeListener.run();
        }
    }

    private void updateCountLabel() {
        int count = rows.size();
        countLabel.setText(count + (count > 1 ? " columns" : " column"));
    }

    private void refreshRows() {
        columnsContainer.removeAll();
        for (ColumnOrFormulaRow row : rows) {
            columnsContainer.add(createRowWrapper(row));
        }
        columnsContainer.revalidate();
        columnsContainer.repaint();
    }

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
    }

    public int getColumnCount() {
        return rows.size();
    }

    public void clearAllColumns() {
        columnsContainer.removeAll();
        rows.clear();
        rowCounter = 0;
        addColumnRow(null);
        columnsContainer.revalidate();
        columnsContainer.repaint();
        notifyChange();
    }
}
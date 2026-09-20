package org.labs.genesis.forms.ui.visualization.configuration.editor;

import com.intellij.icons.AllIcons;
import org.labs.genesis.forms.renderer.provider.DataProvider;
import org.labs.genesis.forms.theme.DashboardTheme;
import org.labs.genesis.forms.ui.common.RoundedBorder;
import org.labs.genesis.forms.ui.visualization.model.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GlobalVisualizationOptionsPanel extends JPanel {

    private static final String[] SORT_OPTIONS = new String[]{"Asc", "Desc"};
    private static final String[] FILTER_OPERATORS = new String[]{
            "is",
            "is not",
            "contains",
            "does not contain",
            "greater than",
            "greater than or equal",
            "less than",
            "less than or equal",
            "is empty",
            "is not empty"
    };

    private static final int FIELD_HEIGHT = 32;
    private static final int SECTION_RADIUS = 10;
    private static final int CARD_RADIUS = 8;

    private final OptionalNumberEditor limitEditor = new OptionalNumberEditor();
    private final JComboBox<String> aggregationCombo = new JComboBox<>(DataProvider.AGGREGATIONS);
    private final ColumnDropField sortColumnField = new ColumnDropField();
    private final JComboBox<String> sortDirectionCombo = new JComboBox<>(SORT_OPTIONS);
    private final JPanel filtersContainer = new JPanel();
    private final List<FilterEditorRow> filterRows = new ArrayList<>();
    private final JButton addFilterButton = new JButton("+ Add filter");
    private Runnable changeListener;

    public GlobalVisualizationOptionsPanel() {
        setOpaque(false);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setAlignmentX(Component.LEFT_ALIGNMENT);
        setBorder(new EmptyBorder(0, 0, 0, 0));

        JPanel limitPanel = createSectionPanel("Limit");
        limitEditor.setAlignmentX(Component.LEFT_ALIGNMENT);
        limitPanel.add(limitEditor);
        add(limitPanel);

        add(Box.createVerticalStrut(10));

        JPanel aggregationPanel = createSectionPanel("Aggregation");
        aggregationPanel.add(styleField(aggregationCombo));
        add(aggregationPanel);

        add(Box.createVerticalStrut(10));

        JPanel sortPanel = createSectionPanel("Sort");
        styleField(sortColumnField);
        sortColumnField.setAlignmentX(Component.LEFT_ALIGNMENT);
        sortPanel.add(sortColumnField);
        sortPanel.add(Box.createVerticalStrut(6));
        sortPanel.add(styleField(sortDirectionCombo));
        add(sortPanel);

        add(Box.createVerticalStrut(10));

        JPanel filtersPanel = createSectionPanel("Filters");
        filtersContainer.setLayout(new BoxLayout(filtersContainer, BoxLayout.Y_AXIS));
        filtersContainer.setOpaque(false);
        filtersContainer.setAlignmentX(Component.LEFT_ALIGNMENT);
        filtersPanel.add(filtersContainer);
        filtersPanel.add(Box.createVerticalStrut(8));
        filtersPanel.add(createAddFilterButton());
        add(filtersPanel);

        addFilterButton.addActionListener(e -> addFilterRow());
        limitEditor.setValueChangeListener(value -> notifyChange());
        aggregationCombo.addActionListener(e -> notifyChange());
        sortColumnField.setColumnChangeListener(value -> notifyChange());
        sortDirectionCombo.addActionListener(e -> notifyChange());

        addFilterRow();
    }

    public Object getLimitValue() {
        return limitEditor.getValue();
    }

    public String getSortColumn() {
        return sortColumnField.getColumnName();
    }

    public String getAggregation() {
        Object selected = aggregationCombo.getSelectedItem();
        return selected == null ? DataProvider.DEFAULT_AGGREGATION : selected.toString();
    }

    public String getSortDirection() {
        Object selected = sortDirectionCombo.getSelectedItem();
        return selected == null ? null : ("Asc".equals(selected) ? "ASC" : "DESC");
    }

    public List<VisualizationFilterCondition> getFilters() {
        List<VisualizationFilterCondition> result = new ArrayList<>();
        for (FilterEditorRow row : filterRows) {
            VisualizationFilterCondition condition = row.toCondition();
            if (condition == null) {
                continue;
            }
            result.add(condition);
        }
        return result;
    }

    public void setLimitValue(Object value) {
        limitEditor.setValue(value);
    }

    public void setSortColumn(String value) {
        if (value == null || value.isBlank()) {
            sortColumnField.clearColumn();
        } else {
            sortColumnField.setColumn(value);
        }
    }

    public void setAggregation(String aggregation) {
        if (aggregation == null || aggregation.isBlank()) {
            aggregationCombo.setSelectedItem(DataProvider.DEFAULT_AGGREGATION);
            return;
        }

        String normalized = aggregation.trim().toUpperCase().replace('_', ' ');
        aggregationCombo.setSelectedItem("COUNT DISTINCT".equals(normalized)
                ? "COUNT DISTINCT"
                : normalized);

        if (aggregationCombo.getSelectedItem() == null) {
            aggregationCombo.setSelectedItem(DataProvider.DEFAULT_AGGREGATION);
        }
    }

    public void setSortDirection(String direction) {
        if (direction == null || direction.isBlank()) {
            sortDirectionCombo.setSelectedItem("Asc");
            return;
        }
        if ("DESC".equalsIgnoreCase(direction) || "DESCENDING".equalsIgnoreCase(direction)) {
            sortDirectionCombo.setSelectedItem("Desc");
        } else {
            sortDirectionCombo.setSelectedItem("Asc");
        }
    }

    public void setFilters(List<VisualizationFilterCondition> conditions) {
        clearFilterRows();
        if (conditions == null || conditions.isEmpty()) {
            addFilterRow();
            return;
        }
        for (VisualizationFilterCondition condition : conditions) {
            FilterEditorRow row = addFilterRow();
            row.applyCondition(condition);
        }
        refreshFilterRelations();
    }

    public void setChangeListener(Runnable listener) {
        this.changeListener = listener;
    }

    public void restoreFromConfig(Object configValue) {
        if (configValue instanceof List<?> list) {
            List<VisualizationFilterCondition> conditions = new ArrayList<>();
            for (Object item : list) {
                if (item instanceof VisualizationFilterCondition condition) {
                    conditions.add(condition);
                } else if (item instanceof Map<?, ?> map) {
                    VisualizationFilterCondition condition = new VisualizationFilterCondition();
                    Object column = map.get("column");
                    Object operator = map.get("operator");
                    Object value = map.get("value");
                    Object relation = map.get("relationToPrevious");
                    if (column != null) condition.setColumn(column.toString());
                    if (operator != null) condition.setOperator(operator.toString());
                    if (value != null) condition.setValue(value.toString());
                    condition.setRelationToPrevious(FilterLogicalOperator.fromValue(relation));
                    conditions.add(condition);
                }
            }
            setFilters(conditions);
        } else {
            setFilters(new ArrayList<>());
        }
    }

    // ---------------------------------------------------------------
    // Styling helpers
    // ---------------------------------------------------------------

    /**
     * Section "carte" : fond légèrement surélevé, coins arrondis, titre en petites
     * majuscules. Toujours en BoxLayout vertical pour que le contenu s'empile
     * proprement quelle que soit la largeur disponible.
     */
    private JPanel createSectionPanel(String title) {
        JPanel section = new JPanel();
        section.setOpaque(true);
        section.setBackground(DashboardTheme.SURFACE_2);
        section.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(DashboardTheme.BORDER_SUBTLE, 1, SECTION_RADIUS),
                new EmptyBorder(10, 12, 12, 12)
        ));
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        JLabel label = new JLabel(title.toUpperCase());
        label.setForeground(DashboardTheme.TEXT_SECONDARY);
        label.setFont(DashboardTheme.boldFont(10));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        label.setBorder(new EmptyBorder(0, 0, 8, 0));
        section.add(label);
        return section;
    }

    /**
     * Applique une hauteur fixe mais une largeur libre (s'étire pour occuper
     * tout l'espace disponible dans le conteneur vertical parent). Évite les
     * champs coupés quand la sidebar est étroite.
     */
    private <T extends JComponent> T styleField(T component) {
        component.setAlignmentX(Component.LEFT_ALIGNMENT);
        component.setForeground(DashboardTheme.TEXT);
        component.setFont(DashboardTheme.getFont(11));
        if (component instanceof JComboBox<?> combo) {
            combo.setBackground(DashboardTheme.SURFACE);
            combo.setBorder(BorderFactory.createCompoundBorder(
                    new RoundedBorder(DashboardTheme.BORDER_SUBTLE, 1, 6),
                    new EmptyBorder(0, 4, 0, 4)
            ));
        } else if (component instanceof JTextField field) {
            field.setCaretColor(DashboardTheme.TEXT);
            field.setBackground(DashboardTheme.SURFACE);
            field.setBorder(BorderFactory.createCompoundBorder(
                    new RoundedBorder(DashboardTheme.BORDER_SUBTLE, 1, 6),
                    new EmptyBorder(0, 8, 0, 8)
            ));
        }
        component.setPreferredSize(new Dimension(0, FIELD_HEIGHT));
        component.setMinimumSize(new Dimension(0, FIELD_HEIGHT));
        component.setMaximumSize(new Dimension(Integer.MAX_VALUE, FIELD_HEIGHT));
        return component;
    }

    private JButton createAddFilterButton() {
        addFilterButton.setHorizontalAlignment(SwingConstants.CENTER);
        addFilterButton.setForeground(DashboardTheme.TEXT);
        addFilterButton.setBackground(DashboardTheme.SURFACE_ACTIVE);
        addFilterButton.setFont(addFilterButton.getFont().deriveFont(Font.BOLD, 11f));
        addFilterButton.setFocusPainted(false);
        addFilterButton.setFocusable(false);
        addFilterButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addFilterButton.setContentAreaFilled(true);
        addFilterButton.setOpaque(true);
        addFilterButton.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(DashboardTheme.BORDER_SUBTLE, 1, 6),
                new EmptyBorder(6, 8, 6, 8)
        ));
        addFilterButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        addFilterButton.setPreferredSize(new Dimension(0, FIELD_HEIGHT));
        addFilterButton.setMinimumSize(new Dimension(0, FIELD_HEIGHT));
        addFilterButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, FIELD_HEIGHT));
        return addFilterButton;
    }

    // ---------------------------------------------------------------
    // Filter rows
    // ---------------------------------------------------------------

    private FilterEditorRow addFilterRow() {
        FilterEditorRow row = new FilterEditorRow();
        filterRows.add(row);
        if (filtersContainer.getComponentCount() > 0) {
            filtersContainer.add(Box.createVerticalStrut(8));
        }
        filtersContainer.add(row.getComponent());
        refreshFilterRelations();
        filtersContainer.revalidate();
        filtersContainer.repaint();
        notifyChange();
        return row;
    }

    private void clearFilterRows() {
        filterRows.clear();
        filtersContainer.removeAll();
    }

    private void refreshFilterRelations() {
        for (int i = 0; i < filterRows.size(); i++) {
            FilterEditorRow row = filterRows.get(i);
            row.setRelationVisible(i > 0);
        }
    }

    private void notifyChange() {
        if (changeListener != null) {
            changeListener.run();
        }
    }

    /**
     * Une ligne de filtre = une petite carte à part entière, avec son propre
     * fond et sa bordure arrondie, pour bien la distinguer visuellement des
     * autres filtres et du fond de section. Tous les champs sont empilés
     * verticalement pour ne jamais être coupés dans une sidebar étroite.
     */
    private class FilterEditorRow {
        private final JPanel component = new JPanel();
        private final JPanel relationPanel = new JPanel(new BorderLayout());
        private final JComboBox<FilterLogicalOperator> relationCombo =
                new JComboBox<>(new FilterLogicalOperator[]{FilterLogicalOperator.AND, FilterLogicalOperator.OR});
        private final ColumnDropField columnField = new ColumnDropField();
        private final JComboBox<String> operatorCombo = new JComboBox<>(FILTER_OPERATORS);
        private final JTextField valueField = new JTextField();
        private final JButton removeButton = new JButton(AllIcons.Actions.Close);

        FilterEditorRow() {
            component.setOpaque(true);
            component.setLayout(new BoxLayout(component, BoxLayout.Y_AXIS));
            component.setAlignmentX(Component.LEFT_ALIGNMENT);
            component.setBorder(BorderFactory.createCompoundBorder(
                    new RoundedBorder(DashboardTheme.BORDER_SUBTLE, 1, CARD_RADIUS),
                    new EmptyBorder(8, 8, 8, 8)
            ));
            component.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

            // Ligne de relation (AND/OR), seulement visible à partir du 2e filtre.
            relationPanel.setOpaque(false);
            relationPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            relationPanel.setVisible(false);
            relationPanel.setBorder(new EmptyBorder(0, 0, 6, 0));
            relationCombo.setForeground(DashboardTheme.TEXT);
            relationCombo.setBackground(DashboardTheme.SURFACE_2);
            relationCombo.setFont(DashboardTheme.boldFont(10));
            relationCombo.setPreferredSize(new Dimension(90, 24));
            relationCombo.setMaximumSize(new Dimension(90, 24));
            relationCombo.setBorder(BorderFactory.createCompoundBorder(
                    new RoundedBorder(DashboardTheme.BORDER_SUBTLE, 1, 6),
                    new EmptyBorder(0, 2, 0, 2)
            ));
            relationCombo.addActionListener(e -> notifyChange());
            relationPanel.add(relationCombo, BorderLayout.WEST);

            // En-tête de carte : bouton supprimer aligné à droite.
            JPanel headerRow = new JPanel(new BorderLayout());
            headerRow.setOpaque(false);
            headerRow.setAlignmentX(Component.LEFT_ALIGNMENT);
            headerRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));

            JLabel conditionLabel = new JLabel("Condition");
            conditionLabel.setForeground(DashboardTheme.TEXT_SECONDARY);
            conditionLabel.setFont(DashboardTheme.getFont(9));
            headerRow.add(conditionLabel, BorderLayout.WEST);

            removeButton.setFocusable(false);
            removeButton.setBorderPainted(false);
            removeButton.setContentAreaFilled(false);
            removeButton.setOpaque(false);
            removeButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            removeButton.setToolTipText("Remove filter");
            removeButton.setPreferredSize(new Dimension(20, 20));
            removeButton.setMargin(new Insets(0, 0, 0, 0));
            removeButton.addActionListener(e -> onRemove());
            headerRow.add(removeButton, BorderLayout.EAST);

            columnField.putClientProperty("JTextField.placeholderText", "Column");
            styleField(columnField);

            operatorCombo.setFont(DashboardTheme.getFont(11));
            styleField(operatorCombo);
            operatorCombo.addActionListener(e -> {
                updateValueState();
                notifyChange();
            });

            valueField.putClientProperty("JTextField.placeholderText", "Value");
            styleField(valueField);
            valueField.setEnabled(false);
            valueField.getDocument().addDocumentListener(new DocumentListener() {
                @Override public void insertUpdate(DocumentEvent e) { notifyChange(); }
                @Override public void removeUpdate(DocumentEvent e) { notifyChange(); }
                @Override public void changedUpdate(DocumentEvent e) { notifyChange(); }
            });
            columnField.setColumnChangeListener(value -> notifyChange());

            component.add(relationPanel);
            component.add(headerRow);
            component.add(Box.createVerticalStrut(6));
            component.add(columnField);
            component.add(Box.createVerticalStrut(6));
            component.add(operatorCombo);
            component.add(Box.createVerticalStrut(6));
            component.add(valueField);
            updateValueState();
        }

        private void updateValueState() {
            String selected = (String) operatorCombo.getSelectedItem();
            boolean needsValue = selected != null
                    && !(selected.equals("is empty") || selected.equals("is not empty"));
            valueField.setEnabled(needsValue);
            valueField.setVisible(needsValue);
            if (!needsValue) {
                valueField.setText("");
            }
        }

        public JPanel getComponent() {
            return component;
        }

        public void setRelationVisible(boolean visible) {
            relationPanel.setVisible(visible);
            if (visible && relationCombo.getSelectedItem() == null) {
                relationCombo.setSelectedItem(FilterLogicalOperator.AND);
            }
        }

        public void applyCondition(VisualizationFilterCondition condition) {
            if (condition == null) {
                return;
            }
            columnField.setColumn(condition.getColumn());
            if (condition.getOperator() != null) {
                operatorCombo.setSelectedItem(condition.getOperator());
            }
            valueField.setText(condition.getValue() == null ? "" : condition.getValue());
            relationCombo.setSelectedItem(condition.getRelationToPrevious() == null
                    ? FilterLogicalOperator.AND
                    : condition.getRelationToPrevious());
            updateValueState();
        }

        public VisualizationFilterCondition toCondition() {
            String column = columnField.getColumnName();
            if (column == null || column.isBlank()) {
                return null;
            }

            String operator = (String) operatorCombo.getSelectedItem();
            String value = valueField.getText();
            if (operator == null || operator.isBlank()) {
                operator = "is";
            }

            if (operator.equals("is empty") || operator.equals("is not empty")) {
                value = "";
            }

            VisualizationFilterCondition condition = new VisualizationFilterCondition();
            condition.setColumn(column);
            condition.setOperator(operator);
            condition.setValue(value);
            condition.setRelationToPrevious((FilterLogicalOperator) relationCombo.getSelectedItem());
            return condition;
        }

        private void onRemove() {
            Container parent = component.getParent();
            if (parent == null) {
                return;
            }
            parent.remove(component);
            parent.revalidate();
            parent.repaint();
            filterRows.remove(this);
            refreshFilterRelations();
            notifyChange();
        }
    }
}
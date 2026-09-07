package org.labs.genesis.forms.ui.visualization.configuration.editor;

import org.labs.genesis.forms.theme.DashboardTheme;
import org.labs.genesis.forms.ui.common.RoundedBorder;
import org.labs.genesis.forms.ui.visualization.model.FieldQueryOptions;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;

/**
 * Panneau compact et repliable regroupant Limit / Sort / Filter
 * pour un champ colonne donné. Affiché sous l'éditeur principal du champ.
 */
public class FieldAdvancedOptionsPanel extends JPanel {

    private final OptionalNumberEditor limitEditor = new OptionalNumberEditor();
    private final JComboBox<String> sortCombo = new JComboBox<>(new String[]{"None", "Ascending", "Descending"});
    private final JTextField filterField = new JTextField();

    private Runnable changeListener;
    private boolean updating = false;

    public FieldAdvancedOptionsPanel() {
        setOpaque(true);
        setBackground(DashboardTheme.SURFACE_2);
        setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(DashboardTheme.BORDER_SUBTLE, 1, 10),
                new EmptyBorder(8, 8, 8, 8)
        ));
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.insets = new Insets(2, 2, 2, 2);

        gbc.gridy = 0; add(miniLabel("Limit"), gbc);
        gbc.gridy = 1; add(limitEditor, gbc);

        gbc.gridy = 2; add(miniLabel("Sort"), gbc);
        gbc.gridy = 3; styleCombo(sortCombo); add(sortCombo, gbc);

        gbc.gridy = 4; add(miniLabel("Filter"), gbc);
        gbc.gridy = 5;
        styleField(filterField);
        filterField.putClientProperty("JTextField.placeholderText", "ex: > 100, = 'active'...");
        add(filterField, gbc);

        limitEditor.setValueChangeListener(v -> fireChange());
        sortCombo.addActionListener(e -> fireChange());
        filterField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { fireChange(); }
            @Override public void removeUpdate(DocumentEvent e) { fireChange(); }
            @Override public void changedUpdate(DocumentEvent e) { fireChange(); }
        });
    }

    private JLabel miniLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(label.getFont().deriveFont(10f));
        label.setForeground(DashboardTheme.TEXT_SECONDARY);
        return label;
    }

    private void styleCombo(JComboBox<?> combo) {
        combo.setForeground(DashboardTheme.TEXT);
        combo.setBackground(DashboardTheme.SURFACE_2);
        Dimension d = combo.getPreferredSize();
        combo.setPreferredSize(new Dimension(d.width, 26));
        combo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));
    }

    private void styleField(JTextField field) {
        field.setForeground(DashboardTheme.TEXT);
        field.setCaretColor(DashboardTheme.TEXT);
        field.setBackground(DashboardTheme.SURFACE_2);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(DashboardTheme.BORDER_SUBTLE, 1),
                new EmptyBorder(0, 6, 0, 6)
        ));
        Dimension d = field.getPreferredSize();
        field.setPreferredSize(new Dimension(d.width, 26));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));
    }

    public void setChangeListener(Runnable listener) {
        this.changeListener = listener;
    }

    private void fireChange() {
        if (!updating && changeListener != null) changeListener.run();
    }

    public FieldQueryOptions getOptions() {
        FieldQueryOptions options = new FieldQueryOptions();
        Object limitValue = limitEditor.getValue();
        if (limitValue instanceof Number number) {
            options.limit = number.intValue();
        }
        String sort = (String) sortCombo.getSelectedItem();
        options.sortDirection = "None".equals(sort) ? null : (sort != null ? sort.toUpperCase() : null);
        String filterText = filterField.getText();
        options.filter = (filterText != null && !filterText.isBlank()) ? filterText : null;
        return options;
    }

    public void setOptions(FieldQueryOptions options) {
        updating = true;
        try {
            if (options == null) options = new FieldQueryOptions();
            limitEditor.setValue(options.limit);
            String direction = options.sortDirection;
            sortCombo.setSelectedItem(direction == null ? "None"
                    : ("ASCENDING".equalsIgnoreCase(direction) ? "Ascending" : "Descending"));
            filterField.setText(options.filter != null ? options.filter : "");
        } finally {
            updating = false;
        }
    }

    public boolean hasActiveOptions() {
        return !getOptions().isEmpty();
    }
}
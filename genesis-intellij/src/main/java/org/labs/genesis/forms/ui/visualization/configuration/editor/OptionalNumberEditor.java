package org.labs.genesis.forms.ui.visualization.configuration.editor;

import org.labs.genesis.forms.theme.DashboardTheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.function.Consumer;

/**
 * Éditeur de nombre optionnel (peut être vide).
 */
public class OptionalNumberEditor extends JPanel {

    private final JTextField numberField;
    private final JLabel hintLabel;
    private Consumer<Object> valueChangeListener;

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

    public void setValueChangeListener(Consumer<Object> listener) {
        this.valueChangeListener = listener;
    }

    /**
     * Définit la valeur du champ.
     */
    public void setValue(Object value) {
        if (value == null) {
            numberField.setText("");
        } else {
            numberField.setText(value.toString());
        }
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

    private void setFixedHeight(JComponent component, int height) {
        Dimension preferred = component.getPreferredSize();
        component.setPreferredSize(new Dimension(preferred.width, height));
        component.setMinimumSize(new Dimension(0, height));
        component.setMaximumSize(new Dimension(Integer.MAX_VALUE, height));
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
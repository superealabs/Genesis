package org.labs.genesis.action.apjwizard.forms.listener;

import com.intellij.ui.JBColor;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

public class PlaceholderTextFieldHelper extends FocusAdapter implements DocumentListener {

    private final JTextField textField;
    private final JLabel label;
    private final String suffix;
    private final String placeholder;
    private final Color originalForeground;

    public PlaceholderTextFieldHelper(JTextField textField, JLabel label, String suffix, String placeholder) {
        this.textField = textField;
        this.label = label;
        this.suffix = suffix;
        this.placeholder = placeholder;
        this.originalForeground = textField.getForeground();
        init();
    }

    private void init() {
        textField.setText(placeholder);
        textField.setForeground(JBColor.GRAY);
        textField.addFocusListener(this);

        if (label != null) {
            label.setForeground(JBColor.GRAY);
            textField.getDocument().addDocumentListener(this);
        }

        updateLabel();
    }

    @Override
    public void focusGained(FocusEvent e) {
        if (textField.getText().equals(placeholder)) {
            textField.setText("");
            textField.setForeground(originalForeground);
        }
    }

    @Override
    public void focusLost(FocusEvent e) {
        if (textField.getText().isEmpty()) {
            textField.setText(placeholder);
            textField.setForeground(JBColor.GRAY);
        }
        updateLabel();
    }

    private void updateLabel() {
        if (label != null) {
            Color fg = textField.getForeground();
            if (!fg.equals(JBColor.GRAY) && !textField.getText().isEmpty()) {
                label.setText(textField.getText() + suffix);
            } else {
                label.setText("");
            }
        }
    }


    @Override
    public void insertUpdate(DocumentEvent e) {
        updateLabel();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        updateLabel();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        updateLabel();
    }
}

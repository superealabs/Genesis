package org.labs.genesis.action.apjwizard.forms.popup;

import com.intellij.openapi.options.ConfigurationException;

import javax.swing.*;
import java.awt.*;

public class PopUtils {

    public static void showValidationError(JComponent parent, ConfigurationException e) {
        String message = e.getLocalizedMessage();
        JLabel label = new JLabel("<html>" + message + "</html>");
        label.setPreferredSize(new Dimension(300, 75));
        JOptionPane.showMessageDialog(parent, label, "Validation Error", JOptionPane.ERROR_MESSAGE);
    }
}

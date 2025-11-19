package org.labs.genesis.action.apjwizard.forms.popup;

import javax.swing.*;
import java.awt.*;

public final class EmptyPanelFactory {

    private EmptyPanelFactory() {

    }

    public static JPanel create(String message, int width, int height) {
        JLabel label = new JLabel(message);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(label, BorderLayout.CENTER);
        panel.setPreferredSize(new Dimension(width, height));
        return panel;
    }

    public static JPanel createDefault() {
        return create("Nothing to show", 400, 250);
    }
}

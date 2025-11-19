package org.labs.genesis.action.apjwizard.forms.popup;

import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class EmptyDialog extends DialogWrapper {
    private final String message;

    public EmptyDialog(Component parent, String message) {
        super(parent, true);
        this.message = message;
        setTitle("Information");
        init();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        JLabel label = new JLabel(message);
        label.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(label, BorderLayout.CENTER);
        panel.setPreferredSize(new Dimension(400, 250));
        return panel;
    }

    @Override
    protected Action @NotNull [] createActions() {
        Action ok = getOKAction();
        Action cancel = getCancelAction();
        ok.putValue(DEFAULT_ACTION, null);
        cancel.putValue(DEFAULT_ACTION, null);
        return new Action[]{ ok };
    }

    public void showDialog() {
        show();
    }
}

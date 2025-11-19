package org.labs.genesis.action.apjwizard.forms.popup;

import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBScrollPane;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class FieldSelectionDialog extends DialogWrapper {
    private final List<String> options;
    private JBList<String> list;

    public FieldSelectionDialog(Component parent, List<String> options) {
        super(parent, true);
        this.options = options;
        setTitle("Select Fields");
        init();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        if (options == null || options.isEmpty()) {
            return EmptyPanelFactory.createDefault();
        }

        list = new JBList<>(new java.util.Vector<>(options));
        list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        JBScrollPane scroll = new JBScrollPane(list);
        scroll.setPreferredSize(new Dimension(400, 250));
        return scroll;
    }

    @Override
    protected Action @NotNull [] createActions() {
        Action ok = getOKAction();
        Action cancel = getCancelAction();
        ok.putValue(DEFAULT_ACTION, null);
        cancel.putValue(DEFAULT_ACTION, null);
        return new Action[]{ok, cancel};
    }

    public List<String> getSelected() {
        return list != null ? list.getSelectedValuesList() : List.of();
    }

    public void triggerOK() {
        doOKAction();
    }
}

package org.labs.genesis.action.apjwizard.forms.helper;

import com.intellij.openapi.actionSystem.*;
import com.intellij.ui.JBColor;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.table.JBTable;
import lombok.Builder;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

@Builder
public class TableToolbarHelper {

    private final JBTable table;
    private final JPanel panel;
    private final Runnable removeAction;
    private final Consumer<JBTable> addAction;
    private final DefaultActionGroup addActionGroup;

    public void init() {
        ToolbarDecorator decorator = ToolbarDecorator.createDecorator(table);
        if (addActionGroup != null) {
            decorator.setAddAction(button -> {
                ActionPopupMenu popup = ActionManager.getInstance()
                        .createActionPopupMenu("AddFieldMenu", addActionGroup);
                Component addButton = decorator.getActionsPanel().getComponent(0);
                popup.getComponent().show(addButton, 0, addButton.getHeight());
            });
        } else if (addAction != null) {
            decorator.setAddAction(button -> addAction.accept(table));
        }

        if (removeAction != null) {
            decorator.setRemoveAction(button -> removeAction.run());
        }

        JPanel decoPanel = decorator.createPanel();
        decoPanel.setBorder(BorderFactory.createMatteBorder(0, 1, 1, 1, JBColor.border()));

        panel.removeAll();
        panel.setLayout(new BorderLayout());
        panel.add(decoPanel, BorderLayout.CENTER);
    }
}

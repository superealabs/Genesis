package org.labs.genesis.action.apjwizard.forms.helper;

import com.intellij.openapi.actionSystem.*;
import com.intellij.ui.JBColor;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.table.JBTable;
import lombok.Builder;
import org.jetbrains.annotations.NotNull;
import org.labs.genesis.icon.SdkIcons;

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
    private final DefaultActionGroup updateActionGroup;
    private final Runnable updateAction;
    private final String customButtonText;
    private final Runnable customButtonAction;

    public void init(int top, int left, int bottom, int right) {
        ToolbarDecorator decorator = ToolbarDecorator.createDecorator(table);
        decorator.setAddActionName("Ajouter");
        decorator.setRemoveActionName("Supprimer");
        decorator.setEditActionName("Modifier");
        int index = 0;
        if (addActionGroup != null) {
            decorator.setAddAction(button -> {
                ActionPopupMenu popup = ActionManager.getInstance()
                        .createActionPopupMenu("AddFieldMenu", addActionGroup);
                Component addButton = decorator.getActionsPanel().getComponent(0);
                popup.getComponent().show(addButton, 0, addButton.getHeight());
            });
            index++;
        } else if (addAction != null) {
            decorator.setAddAction(button -> addAction.accept(table));
            index++;
        }

        if (removeAction != null) {
            decorator.setRemoveAction(button -> removeAction.run());
            index++;
        }

        if (updateActionGroup != null) {
            int finalIndex = index;
            decorator.setEditAction(button -> {
                ActionPopupMenu popup = ActionManager.getInstance()
                        .createActionPopupMenu("UpdateFieldMenu", updateActionGroup);
                Component editButton = decorator.getActionsPanel().getComponent(finalIndex);
                popup.getComponent().show(editButton, 0, editButton.getHeight());
            });
        } else if (updateAction != null) {
            decorator.setEditAction(button -> updateAction.run());
        }

        if (customButtonText != null && customButtonAction != null) {
            decorator.addExtraAction(new AnAction(customButtonText, "Generate labels using AI", SdkIcons.IA_ICON) {
                @Override
                public void actionPerformed(@NotNull AnActionEvent e) {
                    customButtonAction.run();
                }

                @Override
                public void update(@NotNull AnActionEvent e) {
                    super.update(e);
                    boolean enabled = table.getSelectedRow() != -1;
                    e.getPresentation().setEnabled(enabled);
                }
            });
        }


        JPanel decoPanel = decorator.createPanel();
        decoPanel.setBorder(BorderFactory.createMatteBorder(top, left, bottom, right, JBColor.border()));

        panel.removeAll();
        panel.setLayout(new BorderLayout());
        panel.add(decoPanel, BorderLayout.CENTER);
    }

    public void init() {
        init(0,1,1,1);
    }
}

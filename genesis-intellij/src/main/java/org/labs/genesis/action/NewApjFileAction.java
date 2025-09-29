package org.labs.genesis.action;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;
import org.labs.genesis.icon.SdkIcons;

import javax.swing.*;

public class NewApjFileAction extends AnAction {

    public NewApjFileAction() {
        super();
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        //
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setText("APJ File");
        e.getPresentation().setIcon(SdkIcons.GEN_APJ_ICON);
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.EDT;
    }
}


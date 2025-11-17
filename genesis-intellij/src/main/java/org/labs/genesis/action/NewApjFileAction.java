package org.labs.genesis.action;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.labs.genesis.action.apjwizard.ApjWizardDialog;
import org.labs.genesis.icon.SdkIcons;

import javax.swing.*;

public class NewApjFileAction extends AnAction {

    public NewApjFileAction() {
        super();
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) return;
        VirtualFile file = e.getData(CommonDataKeys.VIRTUAL_FILE);
        ApjWizardDialog dialog = new ApjWizardDialog(project,file);
        dialog.show();
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


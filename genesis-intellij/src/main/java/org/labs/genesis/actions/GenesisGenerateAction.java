package org.labs.genesis.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

public class GenesisGenerateAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        GenesisWizardDialog dialog = new GenesisWizardDialog(e.getProject());
        dialog.show();
    }
}

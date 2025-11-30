package org.labs.genesis.wizards;

import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import org.labs.genesis.config.ProjectGenerationContext;
import org.labs.genesis.context.GenerationContextManager;
import org.labs.genesis.forms.SQLRunnerForm;

import javax.naming.ConfigurationException;
import javax.swing.*;

public class SQLRunnerWizardStep extends ModuleWizardStep {
    private final SQLRunnerForm newProjectPanel;
    private final GenerationContextManager generationContextManager;

    public SQLRunnerWizardStep(GenerationContextManager generationContextManager) {
        this.generationContextManager = generationContextManager;
        newProjectPanel = new SQLRunnerForm(generationContextManager.getContext());
    }


    @Override
    public JComponent getComponent() {
        return newProjectPanel.getMainPanel();
    }

    @Override
    public void updateDataModel() {}

    @Override
    public boolean isStepVisible() {
        return this.generationContextManager.getContext().getGenerationProcess().isGenerateProjectProcess();
    }
}

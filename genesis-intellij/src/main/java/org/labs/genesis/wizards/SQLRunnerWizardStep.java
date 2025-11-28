package org.labs.genesis.wizards;

import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import org.labs.genesis.config.ProjectGenerationContext;
import org.labs.genesis.forms.SQLRunnerForm;

import javax.naming.ConfigurationException;
import javax.swing.*;

public class SQLRunnerWizardStep extends ModuleWizardStep {
    private final SQLRunnerForm newProjectPanel;
    private final ProjectGenerationContext projectGenerationContext;

    public SQLRunnerWizardStep(ProjectGenerationContext projectGenerationContext) {
        newProjectPanel = new SQLRunnerForm(projectGenerationContext);
        this.projectGenerationContext = projectGenerationContext;
    }


    @Override
    public JComponent getComponent() {
        return newProjectPanel.getMainPanel();
    }

    @Override
    public void updateDataModel() {}

    @Override
    public boolean isStepVisible() {
        return this.projectGenerationContext.getGenerationProcess().isGenerateProjectProcess();
    }
}

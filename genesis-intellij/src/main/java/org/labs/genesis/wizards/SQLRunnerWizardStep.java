package org.labs.genesis.wizards;

import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import org.labs.genesis.config.ProjectGenerationContext;
import org.labs.genesis.config.langage.Framework;
import org.labs.genesis.forms.SQLRunnerForm;
import org.labs.genesis.wizards.conditionals.InitConditionalWizardStep;

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

    //@Override
    //public boolean validate() throws ConfigurationException {}

    @Override
    public boolean isStepVisible() {
        Framework framework = projectGenerationContext.getFramework();
        return framework != null
                && framework.getUseDB();
    }
}

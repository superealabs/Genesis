package org.labs.genesis.wizards.conditionals;

import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import org.labs.genesis.config.ProjectGenerationContext;
import org.labs.genesis.config.langage.Framework;

import javax.swing.*;

public class GenConfigConditionalWizardStep extends ModuleWizardStep {
    private final ProjectGenerationContext context;
    private final ModuleWizardStep actualStep;

    public GenConfigConditionalWizardStep(ProjectGenerationContext projectGenerationContext, ModuleWizardStep actualStep) {
        this.context = projectGenerationContext;
        this.actualStep = actualStep;
    }

    @Override
    public JComponent getComponent() {
        if (isStepVisible()) {
            return actualStep.getComponent();
        } else {
            return new JLabel("This step is not visible");
        }
    }

    @Override
    public void updateDataModel() {
        if (isStepVisible()) {
            actualStep.updateDataModel();
        }
    }

    @Override
    public boolean isStepVisible() {
        Framework framework = context.getFramework();
        return framework != null && framework.getUseDB();
    }

}

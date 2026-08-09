package org.labs.genesis.wizards.conditionals;

import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.openapi.options.ConfigurationException;
import org.labs.genesis.config.ProjectGenerationContext;
import org.labs.genesis.config.langage.Framework;
import org.labs.genesis.context.GenerationContextManager;

import javax.swing.*;

public class GenConfigConditionalWizardStep extends ModuleWizardStep {
    private final GenerationContextManager generationContextManager;
    private final ModuleWizardStep actualStep;

    public GenConfigConditionalWizardStep(GenerationContextManager generationContextManager, ModuleWizardStep actualStep) {
        this.generationContextManager = generationContextManager;
        this.actualStep = actualStep;
    }
    @Override
    public void updateStep() {
        if (isStepVisible()) {
            actualStep.updateStep();
        }
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
    public boolean validate() throws ConfigurationException {
        if (isStepVisible()) {
            return actualStep.validate();
        }
        return super.validate();
    }

    @Override
    public boolean isStepVisible() {
        Framework framework = generationContextManager.getContext().getFramework();
        return framework != null && framework.getUseDB() && actualStep.isStepVisible();
    }

}

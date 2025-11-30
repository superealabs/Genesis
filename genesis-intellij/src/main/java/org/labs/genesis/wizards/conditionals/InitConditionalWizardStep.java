/**
 * MIT License
 * Copyright (c) 2024 nomena
 */

package org.labs.genesis.wizards.conditionals;

import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import org.labs.genesis.config.ProjectGenerationContext;
import org.labs.genesis.config.langage.Framework;
import org.labs.genesis.context.GenerationContextManager;

import javax.swing.*;

public class InitConditionalWizardStep extends ModuleWizardStep {
    private final GenerationContextManager generationContextManager;
    private final ModuleWizardStep actualStep;

    public InitConditionalWizardStep(GenerationContextManager generationContextManager, ModuleWizardStep actualStep) {
        this.generationContextManager = generationContextManager;
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
        Framework framework = generationContextManager.getContext().getFramework();
        return framework != null
                && framework.getUseDB() && actualStep.isStepVisible();
    }

}

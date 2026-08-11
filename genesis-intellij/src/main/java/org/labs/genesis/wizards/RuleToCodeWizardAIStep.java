package org.labs.genesis.wizards;

import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import org.labs.genesis.config.ProjectGenerationContext;
import org.labs.genesis.config.langage.Framework;
import org.labs.genesis.context.GenerationContextManager;
import org.labs.genesis.forms.RuleToCodeAIForm;

import javax.swing.*;

public class RuleToCodeWizardAIStep extends ModuleWizardStep {

    private final GenerationContextManager generationContextManager;
    private final RuleToCodeAIForm form;

    public RuleToCodeWizardAIStep(GenerationContextManager generationContextManager) {
        this.generationContextManager = generationContextManager;
        this.form = new RuleToCodeAIForm(generationContextManager.getContext());
    }
    @Override
    public void updateDataModel() {
    }

    @Override
    public JComponent getComponent() {
        return form.getMainPanel();
    }

    @Override
    public boolean isStepVisible() {
        return generationContextManager.getContext().getGenerationProcess().isRunToCodeGenerationProcess();
    }

}
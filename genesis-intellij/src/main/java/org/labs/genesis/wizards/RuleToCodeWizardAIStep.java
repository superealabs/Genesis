package org.labs.genesis.wizards;

import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import org.labs.genesis.config.ProjectGenerationContext;
import org.labs.genesis.config.langage.Framework;
import org.labs.genesis.forms.RuleToCodeAIForm;

import javax.swing.*;

public class RuleToCodeWizardAIStep extends ModuleWizardStep {

    private final ProjectGenerationContext context;
    private final RuleToCodeAIForm form;
    private final  FirstWizardStep initStep ;


    public RuleToCodeWizardAIStep(ProjectGenerationContext context , FirstWizardStep initStep) {
        this.context = context;
        this.form = new RuleToCodeAIForm( context );
        this.initStep = initStep;
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
        return initStep.getFirstForm().ruleTodCodeSelected();
    }

}
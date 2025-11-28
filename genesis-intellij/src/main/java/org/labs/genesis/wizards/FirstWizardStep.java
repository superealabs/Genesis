package org.labs.genesis.wizards;

import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.openapi.options.ConfigurationException;
import org.labs.genesis.config.ProjectGenerationContext;
import org.labs.genesis.forms.FirstForm;


import javax.swing.*;

public class FirstWizardStep extends ModuleWizardStep {
    private final ProjectGenerationContext context;
    private final FirstForm form;

    public FirstWizardStep(ProjectGenerationContext context ) {
        this.context = context;
        this.form = new FirstForm();
    }

    public FirstForm getFirstForm() {
        try {
            return form;
        }catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return null;
       }

    @Override
    public JComponent getComponent() {
        try {
            return form.getMainPanel();

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    @Override
    public void updateDataModel() {
        try {
            // New project
            context.getGenerationProcess().setGenerateProjectProcess(form.getGenerateNewProject().isSelected());
            // Rule to code
            context.getGenerationProcess().setRunToCodeGenerationProcess(form.ruleTodCodeSelected());
            context.getGenerationProcess().setGenerateProjectProcess(form.ruleTodCodeSelected());
            // Sync project
            context.getGenerationProcess().setSynchGenerationProcess(form.getSyncProject().isSelected());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

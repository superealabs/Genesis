package org.labs.genesis.wizards;

import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.openapi.options.ConfigurationException;
import org.labs.genesis.config.ProjectGenerationContext;
import org.labs.genesis.context.GenerationContextManager;
import org.labs.genesis.forms.FirstForm;


import javax.swing.*;

public class FirstWizardStep extends ModuleWizardStep {
    private final GenerationContextManager generationContextManager;
    private final FirstForm form;

    public FirstWizardStep(GenerationContextManager generationContextManager ) {
        this.generationContextManager = generationContextManager;
        if(this.generationContextManager.getContext() != null && this.generationContextManager.getContext().getFrontendFramework() != null)
            this.generationContextManager.getContext().getFrontendFramework().clearRoutes();
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
            generationContextManager.getContext().getGenerationProcess().setGenerateProjectProcess(form.getGenerateNewProject().isSelected());
            // Rule to code
            generationContextManager.getContext().getGenerationProcess().setRunToCodeGenerationProcess(form.ruleTodCodeSelected());
            if (form.ruleTodCodeSelected()) {
                generationContextManager.getContext().getGenerationProcess().setGenerateProjectProcess(true);
            }
            // Sync project
            generationContextManager.getContext().getGenerationProcess().setSynchGenerationProcess(form.getSyncProject().isSelected());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean validate() throws ConfigurationException {
        if( !form.getAddRuleToCode().isSelected() && !form.getGenerateNewProject().isSelected()) {
            throw new ConfigurationException("Error : choose option !!");
        }
        return true;
    }

//    @Override
//    public boolean isStepVisible() {
//
//    }
}

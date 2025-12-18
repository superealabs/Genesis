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
    }

    @Override
    public boolean validate() throws ConfigurationException {
        if( !form.getAddRuleToCode().isSelected() && !form.getCreateProject().isSelected()) {
            throw new ConfigurationException("Error : choose option !!");
        }
        return true;
    }

//    @Override
//    public boolean isStepVisible() {
//
//    }
}

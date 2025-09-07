package org.labs.genesis.wizards;

import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.openapi.options.ConfigurationException;
import org.labs.genesis.config.ProjectGenerationContext;
import org.labs.genesis.forms.FrontendConfigurationForm;

import javax.swing.*;

public class FrontendConfigurationWizardStep extends ModuleWizardStep {
    private final FrontendConfigurationForm frontendConfigurationForm;
    private final ProjectGenerationContext projectGenerationContext;

    public FrontendConfigurationWizardStep(ProjectGenerationContext projectGenerationContext){
        this.frontendConfigurationForm = new FrontendConfigurationForm();
        this.projectGenerationContext = projectGenerationContext;
    }

    @Override
    public JComponent getComponent() {
        return this.frontendConfigurationForm.getMainPanel();
    }

    @Override
    public void updateDataModel() {

    }

    @Override
    public boolean validate() throws ConfigurationException {
        return super.validate();
    }
}

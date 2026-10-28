package org.labs.genesis.wizards;

import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.openapi.options.ConfigurationException;
import org.labs.genesis.context.GenerationContextManager;
import org.labs.genesis.forms.DataSourceChoiceForm;

import javax.swing.*;

public class DataSourceChoiceWizardStep extends ModuleWizardStep {

    private final DataSourceChoiceForm form;
    private final GenerationContextManager generationContextManager;

    public DataSourceChoiceWizardStep(GenerationContextManager generationContextManager) {
        this.generationContextManager = generationContextManager;
        this.form = new DataSourceChoiceForm();
    }

    public DataSourceChoiceForm getForm() {
        return form;
    }

    @Override
    public JComponent getComponent() {
        return form.getMainPanel();
    }

    @Override
    public boolean isStepVisible() {
        return this.generationContextManager.getContext().getGenerationProcess().isGenerateProjectProcess()
                && !this.generationContextManager.getContext().getGenerationProcess().isRunToCodeGenerationProcess()
                && !this.generationContextManager.getContext().getGenerationProcess().isSynchGenerationProcess();
    }

    @Override
    public boolean validate() throws ConfigurationException {
        if (!form.isDatabaseSelected() && !form.isScriptSelected()) {
            throw new ConfigurationException("Veuillez sélectionner au moins une option : 'Avec la base de données' ou 'Avec un script'.");
        }
        return true;
    }

    public boolean isDatabaseSelected() {
        return form != null && form.isDatabaseSelected();
    }

    public boolean isScriptSelected() {
        return form != null && form.isScriptSelected();
    }

    @Override
    public void updateDataModel() {
    }
}

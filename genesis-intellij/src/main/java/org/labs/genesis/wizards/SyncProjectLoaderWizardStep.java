package org.labs.genesis.wizards;

import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.openapi.options.ConfigurationException;
import org.labs.genesis.config.ProjectGenerationContext;
import org.labs.genesis.config.langage.generator.sync.SyncGenerator;
import org.labs.genesis.forms.SyncProjectLoaderForm;

import javax.swing.*;

public class SyncProjectLoaderWizardStep extends ModuleWizardStep {
    private final SyncProjectLoaderForm form;
    public SyncGenerator syncGenerator;
    private final FirstWizardStep firstWizardStep;
    private ProjectGenerationContext projectGenerationContext;

    public SyncProjectLoaderWizardStep(ProjectGenerationContext context, FirstWizardStep firstWizardStep) {
        this.syncGenerator = new SyncGenerator();
        this.firstWizardStep = firstWizardStep;
        this.form = new SyncProjectLoaderForm();
        this.projectGenerationContext = context;
    }
    @Override
    public JComponent getComponent() {
        return this.form.getMainPanel();
    }

    @Override
    public void updateDataModel() {
        try {
            projectGenerationContext = syncGenerator.loadProjectContext(form.getFolderField().getText());
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public boolean validate() throws ConfigurationException {
        try {
            boolean validation = form.validateFolder();
            if (!validation) {
                throw new ConfigurationException("Genesis context file not found. Please select a valid project directory");
            }
        } catch (Exception e) {
            throw new ConfigurationException(e.getMessage());
        }
        return true;
    }

    @Override
    public boolean isStepVisible() {
        return firstWizardStep.getFirstForm().syncSelected();
    }
}

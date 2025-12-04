package org.labs.genesis.wizards;

import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.openapi.options.ConfigurationException;
import org.labs.genesis.config.ProjectGenerationContext;
import org.labs.genesis.config.langage.generator.sync.SyncGenerator;
import org.labs.genesis.context.GenerationContextManager;
import org.labs.genesis.forms.SyncProjectLoaderForm;
import javax.swing.*;

public class SyncProjectLoaderWizardStep extends ModuleWizardStep {
    private final SyncProjectLoaderForm form;
    public final SyncGenerator syncGenerator;
    private final RelationshipConfigurationWizardStep relationshipConfigurationWizardStep;
    private final SynchGenerationWizardStep synchGenerationWizardStep;
    GenerationContextManager generationContextManager;

    public SyncProjectLoaderWizardStep(GenerationContextManager generationContextManager, RelationshipConfigurationWizardStep relationshipConfigurationWizardStep, SynchGenerationWizardStep synchGenerationWizardStep) {
        this.generationContextManager = generationContextManager;
        this.syncGenerator = new SyncGenerator();
        this.form = new SyncProjectLoaderForm();
        this.relationshipConfigurationWizardStep = relationshipConfigurationWizardStep;
        this.synchGenerationWizardStep = synchGenerationWizardStep;
    }
    @Override
    public JComponent getComponent() {
        return this.form.getMainPanel();
    }

    @Override
    public void updateDataModel() {
        try {
            ProjectGenerationContext loadProjectContext = syncGenerator.loadProjectContext(form.getFolderField().getText());
            loadProjectContext.setGenerationProcess(generationContextManager.getContext().generationProcess);
            generationContextManager.setContext(loadProjectContext);
            syncGenerator.evaluateDatabaseChanges(loadProjectContext);
            relationshipConfigurationWizardStep.updateTableSelects(syncGenerator.getEvaluationContext());
            synchGenerationWizardStep.setSyncGenerator(syncGenerator);
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
        return generationContextManager.getContext().getGenerationProcess().isSynchGenerationProcess();
    }
}

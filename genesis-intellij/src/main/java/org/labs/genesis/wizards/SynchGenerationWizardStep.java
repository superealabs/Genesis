package org.labs.genesis.wizards;

import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.openapi.ui.Messages;
import org.labs.genesis.config.ProjectGenerationContext;
import org.labs.genesis.config.langage.generator.sync.SyncGenerator;
import org.labs.genesis.forms.SyncGenerationForm;

import javax.swing.*;

public class SynchGenerationWizardStep extends ModuleWizardStep {
    public SyncGenerationForm syncGenerationForm;
    public ProjectGenerationContext context;
    private SyncGenerator syncGenerator;
    private boolean isInitialized = false;

    public SynchGenerationWizardStep() {
        this.syncGenerationForm = new SyncGenerationForm();
    }

    @Override
    public JComponent getComponent() {
        if (!isInitialized && syncGenerator != null) {
            initializeData();
        }
        return syncGenerationForm.getMainPanel();
    }

    @Override
    public void updateDataModel() {
    }

    public void setContext(ProjectGenerationContext context, SyncGenerator syncGenerator) {
        this.context = context;
        this.syncGenerator = syncGenerator;
        this.isInitialized = false;
    }

    @Override
    public boolean isStepVisible() {
        return context.getGenerationProcess().isSynchGenerationProcess() && syncGenerator != null;
    }

    @Override
    public void _init() {
        super._init();
        if (syncGenerator != null) {
            initializeData();
        }
    }

    private void initializeData() {
        if (isInitialized) {
            return;
        }

        try {
            syncGenerator.evaluateDatabaseChanges(context);
            if (syncGenerator.getDatabaseReportManager() != null) {
                syncGenerationForm.populateTableReport(syncGenerator.getDatabaseReportManager());
            }
            isInitialized = true;
        } catch (Exception e) {
            Messages.showErrorDialog(
                    "Failed to synchronization data: " + e.getMessage(),
                    "Synchronization Error"
            );
        }
    }

    public void refreshData() {
        isInitialized = false;
        if (syncGenerator != null) {
            initializeData();
        }
    }
}
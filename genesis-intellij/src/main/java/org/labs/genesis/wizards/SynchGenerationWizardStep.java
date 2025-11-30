package org.labs.genesis.wizards;

import com.intellij.ide.impl.ProjectUtil;
import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;
import org.labs.genesis.config.ProjectGenerationContext;
import org.labs.genesis.config.langage.generator.sync.SyncGenerator;
import org.labs.genesis.context.GenerationContextManager;
import org.labs.genesis.forms.SyncGenerationForm;
import org.labs.genesis.indicator.IntelliJProgressAdapter;

import javax.swing.*;

public class SynchGenerationWizardStep extends ModuleWizardStep {
    public SyncGenerationForm syncGenerationForm;
    public GenerationContextManager generationContextManager;
    private SyncGenerator syncGenerator;
    private boolean isInitialized = false;

    public SynchGenerationWizardStep(GenerationContextManager generationContextManager) {
        this.syncGenerationForm = new SyncGenerationForm();
        this.generationContextManager = generationContextManager;
    }

    @Override
    public JComponent getComponent() {
        initializeData();
        return syncGenerationForm.getMainPanel();
    }

    @Override
    public void updateDataModel() {
            Project project = ProjectUtil.getProjectForComponent(syncGenerationForm.getMainPanel());
            if (project == null) {
                project = ProjectUtil.getActiveProject();
            }

            if (project == null) {
                throw new IllegalStateException("Impossible de déterminer le contexte Project IntelliJ.");
            }
            try {
                ProgressManager.getInstance().run(new Task.Modal(project, "Génération du Projet", true) {
                    @Override
                    public void run(@NotNull ProgressIndicator indicator) {
                        IntelliJProgressAdapter processIndicator = new IntelliJProgressAdapter(indicator);
                        indicator.setText("Processing ...");
                        indicator.setFraction(0);
                        try {
                            syncGenerator.generateProject(generationContextManager.getContext(),processIndicator);
                            indicator.setText("Synchronization completed");
                            indicator.setFraction(1.0);
                        } catch (Exception e) {
                            throw new RuntimeException("Project generation failed: " + e.getMessage(), e);
                        }
                    }
                });
                Messages.showInfoMessage(project,
                        "Project synchronization completed successfully",
                        "Success");

            } catch (Exception e) {
                Messages.showErrorDialog(project,
                        "Une erreur inattendue est survenue lors de la génération : " + e.getMessage() + "\n\n" +
                                "Veuillez consulter la console d'exécution ou les **logs d'IntelliJ** pour plus de détails (Help -> Show Log in Explorer/Finder).",
                        "Échec de la Génération du Projet");
                throw new RuntimeException("Project generation failed: " + e.getMessage(), e);
            }
    }

    public void setSyncGenerator(SyncGenerator syncGenerator) {
        this.syncGenerator = syncGenerator;
        this.isInitialized = false;
    }

    @Override
    public boolean isStepVisible() {
        return generationContextManager.getContext().getGenerationProcess().isSynchGenerationProcess() && syncGenerator != null;
    }

    @Override
    public void _init() {
        super._init();
        if (syncGenerator != null) {
            initializeData();
        }
    }

    private void initializeData() {
        if (syncGenerator == null) {
            return;
        }
        try {
            syncGenerator.evaluateDatabaseChanges(generationContextManager.getContext());
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
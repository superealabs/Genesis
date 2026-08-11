package org.labs.genesis.wizards;

import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.openapi.options.ConfigurationException;
import lombok.Setter;
import org.labs.genesis.config.ProjectGenerationContext;
import org.labs.genesis.connexion.model.RelationParameter;
import org.labs.genesis.connexion.model.TableMetadata;
import org.labs.genesis.context.GenerationContextManager;
import org.labs.genesis.forms.RelationshipConfigurationForm;

import javax.swing.*;
import java.util.Dictionary;
import java.util.List;
import java.util.Objects;

public class RelationshipConfigurationWizardStep extends ModuleWizardStep {
    private final RelationshipConfigurationForm relationshipConfigurationForm;
    @Setter
    private GenerationContextManager generationContextManager;

    public RelationshipConfigurationWizardStep(GenerationContextManager generationContextManager){
        this.generationContextManager = generationContextManager;
        this.relationshipConfigurationForm = new RelationshipConfigurationForm();
    }

    @Override
    public JComponent getComponent() {
        return this.relationshipConfigurationForm.getMainPanel();
    }

    @Override
    public void updateDataModel() {
        try {
            List<RelationParameter> relations = relationshipConfigurationForm.getRelationParameters();
            generationContextManager.getContext().setRelationParameters(relations);
        }
        catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    public void updateTableSelects(Dictionary<String,List<TableMetadata>> relations) {
        this.relationshipConfigurationForm.populateSelect(relations);
        if (generationContextManager != null && generationContextManager.getContext() != null) {
            List<RelationParameter> autoRelations = generationContextManager.getContext().autoDetectRelationParameters();
            this.relationshipConfigurationForm.setTableData(autoRelations);
        }
    }
    public void updateTableSelects() {
        updateTableSelects(generationContextManager.getContext());
    }

    public void updateTableSelects(ProjectGenerationContext context) {
        this.relationshipConfigurationForm.populateSelect(context.splitTableByRelations());
        List<RelationParameter> autoRelations = context.autoDetectRelationParameters();
        this.relationshipConfigurationForm.setTableData(autoRelations);
    }

    @Override
    public boolean isStepVisible() {
        return this.generationContextManager.getContext().getGenerationProcess().isGenerateProjectProcess() ||
               this.generationContextManager.getContext().getGenerationProcess().isSynchGenerationProcess();
    }
}

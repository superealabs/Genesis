package org.labs.genesis.wizards;

import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.openapi.options.ConfigurationException;
import org.labs.genesis.config.ProjectGenerationContext;
import org.labs.genesis.connexion.model.RelationParameter;
import org.labs.genesis.connexion.model.TableMetadata;
import org.labs.genesis.forms.RelationshipConfigurationForm;

import javax.swing.*;
import java.util.Dictionary;
import java.util.List;

public class RelationshipConfigurationWizardStep extends ModuleWizardStep {
    private final RelationshipConfigurationForm relationshipConfigurationForm;
    private final ProjectGenerationContext projectGenerationContext;

    public RelationshipConfigurationWizardStep(ProjectGenerationContext projectGenerationContext){
        this.projectGenerationContext = projectGenerationContext;
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
            projectGenerationContext.setRelationParameters(relations);
        }
        catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    public void updateTableSelects(Dictionary<String,List<TableMetadata>> relations) {
        this.relationshipConfigurationForm.populateSelect(relations);
    }
}

package org.labs.genesis.wizards;

import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.openapi.options.ConfigurationException;
import org.labs.genesis.config.ProjectGenerationContext;
import org.labs.genesis.forms.RelationshipConfigurationForm;

import javax.swing.*;

public class RelationshipConfigurationWizardStep extends ModuleWizardStep {
    private final RelationshipConfigurationForm relationshipConfigurationForm;
    private final ProjectGenerationContext projectGenerationContext;

    public RelationshipConfigurationWizardStep(ProjectGenerationContext projectGenerationContext){
        this.projectGenerationContext = projectGenerationContext;
        this.relationshipConfigurationForm = new RelationshipConfigurationForm(projectGenerationContext);
    }

    @Override
    public JComponent getComponent() {
        return this.relationshipConfigurationForm.getMainPanel();
    }

    @Override
    public void updateDataModel() {

    }
}

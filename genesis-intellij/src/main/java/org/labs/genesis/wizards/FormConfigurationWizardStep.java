package org.labs.genesis.wizards;

import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.ui.Messages;
import lombok.Getter;
import org.labs.genesis.config.form.FieldConfiguration;
import org.labs.genesis.connexion.model.ColumnMetadata;
import org.labs.genesis.connexion.model.TableMetadata;
import org.labs.genesis.context.GenerationContextManager;
import org.labs.genesis.forms.FormConfigurationForm;

import javax.swing.*;
import java.util.List;
import java.util.Map;

public class FormConfigurationWizardStep extends ModuleWizardStep {

    @Getter
    private final FormConfigurationForm form;

    private final GenerationContextManager generationContextManager;

    public FormConfigurationWizardStep(
            GenerationContextManager generationContextManager
    ) {
        this.generationContextManager = generationContextManager;
        this.form = new FormConfigurationForm(generationContextManager.getContext());
    }

    @Override
    public JComponent getComponent() {
        return form.getMainPanel();
    }

    @Override
    public void updateStep() {
        if(generationContextManager.getContext() == null) return;
        Map<String, Map<String, FieldConfiguration>> tables = form.getTablesConfiguration();
        generationContextManager.getContext().setFormConfiguration(tables);
    }

    @Override
    public boolean isStepVisible() {
        if(this.generationContextManager == null
                || this.generationContextManager.getContext() == null)
            return false;
        return this.generationContextManager.getContext().isGenerateFrontendApp();
    }

    @Override
    public void updateDataModel() {

        TableMetadata selectedTable =
                form.getSelectedTable();

        List<ColumnMetadata> selectedColumns =
                form.getSelectedColumns();

        if (selectedTable == null) {
            return;
        }

        /*
         * Plus tard, ici :
         *
         * projectGenerationContext.set...
         *
         * Mais pour cette version de démonstration,
         * on ne modifie pas encore le contexte.
         */
    }

    @Override
    public boolean validate() throws ConfigurationException {
        return true;
    }

}
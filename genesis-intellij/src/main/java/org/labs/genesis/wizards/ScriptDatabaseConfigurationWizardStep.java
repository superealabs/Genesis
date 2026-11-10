package org.labs.genesis.wizards;

import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.openapi.options.ConfigurationException;
import org.labs.genesis.connexion.Database;
import org.labs.genesis.context.GenerationContextManager;
import org.labs.genesis.forms.ScriptDatabaseConfigurationForm;

import javax.swing.*;

public class ScriptDatabaseConfigurationWizardStep extends ModuleWizardStep {

    private final ScriptDatabaseConfigurationForm form;
    private final GenerationContextManager generationContextManager;
    private final DataSourceChoiceWizardStep dataSourceChoiceWizardStep;

    public ScriptDatabaseConfigurationWizardStep(GenerationContextManager generationContextManager,
            DataSourceChoiceWizardStep dataSourceChoiceWizardStep) {
        this.generationContextManager = generationContextManager;
        this.dataSourceChoiceWizardStep = dataSourceChoiceWizardStep;
        this.form = new ScriptDatabaseConfigurationForm(generationContextManager.getContext());
    }

    public ScriptDatabaseConfigurationWizardStep(GenerationContextManager generationContextManager) {
        this(generationContextManager, null);
    }

    public ScriptDatabaseConfigurationForm getForm() {
        return form;
    }

    public String getSqlScript() {
        return form != null && form.getSqlTextArea() != null ? form.getSqlTextArea().getText() : "";
    }

    @Override
    public JComponent getComponent() {
        return form.getMainPanel();
    }

    @Override
    public boolean isStepVisible() {
        boolean isScript = dataSourceChoiceWizardStep != null && dataSourceChoiceWizardStep.isScriptSelected();
        return this.generationContextManager.getContext().getGenerationProcess().isGenerateProjectProcess()
                && isScript;
    }

    @Override
    public boolean validate() throws ConfigurationException {
        if (form.getSelectedDatabase() == null) {
            throw new ConfigurationException("Veuillez sélectionner un type de base de données.");
        }
        String sql = form.getSqlTextArea().getText();
        if (sql == null || sql.trim().isEmpty()) {
            throw new ConfigurationException(
                    "Veuillez fournir un script SQL (en chargeant un fichier, en le saisissant ou en le générant avec l'IA).");
        }
        return true;
    }

    @Override
    public void updateDataModel() {
        Database selectedDatabase = form.getSelectedDatabase();
        if (selectedDatabase != null) {
            generationContextManager.getContext().setDatabase(selectedDatabase);
        }
        generationContextManager.getContext().setSqlScript(getSqlScript());
    }
}

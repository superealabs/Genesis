package org.labs.genesis.wizards;

import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.openapi.options.ConfigurationException;
import org.labs.genesis.config.ProjectGenerationContext;
import org.labs.genesis.forms.DashboardConfigurationForm;
import org.labs.genesis.context.GenerationContextManager;
import org.labs.genesis.dashboard.model.DashboardConfiguration;
import org.labs.genesis.dashboard.model.DashboardVisualization;
import org.labs.genesis.dashboard.rules.DashboardConfigurationValidator;
import org.labs.genesis.dashboard.rules.DashboardMetadataValidator;
import org.labs.genesis.dashboard.rules.DashboardVisualizationRules;
import org.labs.genesis.connexion.model.TableMetadata;

import javax.swing.*;
import java.util.List;

public class DashboardConfigurationWizardStep
        extends ModuleWizardStep {

    private final DashboardConfigurationForm form;

    private final GenerationContextManager generationContextManager;

    public DashboardConfigurationWizardStep(
            GenerationContextManager generationContextManager
    ) {

        this.generationContextManager = generationContextManager;

        ProjectGenerationContext context = generationContextManager.getContext();
        this.form = new DashboardConfigurationForm(context);
    }

    // =========================================================
    // COMPONENT
    // =========================================================

    @Override
    public JComponent getComponent() {

        return form.getMainPanel();
    }

    // =========================================================
    // VISIBILITY
    // =========================================================

    @Override
    public boolean isStepVisible() {

        return generationContextManager
                .getContext()
                .getGenerationProcess()
                .isGenerateProjectProcess()
                && generationContextManager.
                getContext()
                .isGenerateFrontendApp();
    }

    // =========================================================
    // UPDATE DATA MODEL
    // =========================================================

    @Override
    public void updateDataModel() {
        generationContextManager.getContext().setDashboardConfiguration(form.toDashboardConfiguration());
    }

    // =========================================================
    // VALIDATION
    // =========================================================

    @Override
    public boolean validate()
            throws ConfigurationException {

        DashboardConfiguration configuration;
        try {
            configuration = form.toDashboardConfiguration();
        } catch (IllegalArgumentException exception) {
            throw new ConfigurationException("Les options du dashboard contiennent une valeur invalide.");
        }
        if (!configuration.isEnabled()) {
            generationContextManager.getContext().setDashboardConfiguration(configuration);
            return true;
        }
        List<DashboardVisualization> visualizations = configuration.getPages().stream()
                .flatMap(page -> page.getVisualizations().stream()).toList();
        if (visualizations.stream().anyMatch(visualization -> !DashboardVisualizationRules.isValid(visualization)))
            throw new ConfigurationException("Une visualisation du dashboard est incomplète ou invalide.");
        if (!DashboardConfigurationValidator.isValid(configuration))
            throw new ConfigurationException("La configuration du dashboard est invalide.");
        List<TableMetadata> availableTables = generationContextManager.getContext().getAllTables();
        for (DashboardVisualization visualization : visualizations) {
            String source = visualization.getDataSource() == null ? null : visualization.getDataSource().getName();
            TableMetadata table = availableTables.stream()
                    .filter(candidate -> matchesSource(candidate.getTableName(), source)).findFirst().orElse(null);
            if (!DashboardMetadataValidator.isValid(visualization, table, availableTables))
                throw new ConfigurationException("Les champs d'une visualisation ne correspondent pas aux métadonnées de sa source.");
        }
        generationContextManager.getContext().setDashboardConfiguration(configuration);
        return true;
    }

    private boolean matchesSource(String tableName, String sourceName) {
        return tableName != null && sourceName != null && (tableName.equalsIgnoreCase(sourceName)
                || sourceName.toLowerCase(java.util.Locale.ROOT).endsWith("." + tableName.toLowerCase(java.util.Locale.ROOT)));
    }
}

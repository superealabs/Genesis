package org.labs.genesis.wizards;

import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.openapi.options.ConfigurationException;
import org.labs.genesis.config.ProjectGenerationContext;
import org.labs.genesis.forms.DashboardConfigurationForm;
import org.labs.genesis.context.GenerationContextManager;

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
                .isGenerateProjectProcess();
    }

    // =========================================================
    // UPDATE DATA MODEL
    // =========================================================

    @Override
    public void updateDataModel() {

        /*
         * Brancher ici ton DashboardConfiguration
         * lorsque ton modèle sera défini.
         *
         * Exemple :
         *
         * DashboardConfiguration config =
         *         new DashboardConfiguration();
         *
         * config.setLeftSidebarCollapsed(
         *         form.isLeftSidebarCollapsed()
         * );
         *
         * config.setRightSidebarCollapsed(
         *         form.isRightSidebarCollapsed()
         * );
         *
         * generationContextManager
         *         .getContext()
         *         .setDashboardConfiguration(config);
         */
    }

    // =========================================================
    // VALIDATION
    // =========================================================

    @Override
    public boolean validate()
            throws ConfigurationException {

        /*
         * Pour l'instant il n'y a aucun champ obligatoire
         * dans le dashboard.
         */

        return true;
    }
}
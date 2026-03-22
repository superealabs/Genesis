package org.labs.genesis.actions;

import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.wizard.AbstractWizard;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.Nullable;
import org.labs.genesis.config.ProjectGenerationContext;
import org.labs.genesis.wizards.*;
import org.labs.genesis.wizards.conditionals.GenConfigConditionalWizardStep;
import org.labs.genesis.wizards.conditionals.InitConditionalWizardStep;

import java.util.ArrayList;
import java.util.List;

public class GenesisWizardDialog extends AbstractWizard<ModuleWizardStep> {
    private final List<ModuleWizardStep> stepsList = new ArrayList<>();

    public GenesisWizardDialog(@Nullable Project project) {
        super("Genesis Project Generator", project);

        // Instanciate a fresh context so isolated calls don't leak logic.
        ProjectGenerationContext context = new ProjectGenerationContext();

        SpecificConfigurationWizardStep specificConfigurationWizardStep = new SpecificConfigurationWizardStep(context);
        DatabaseConfigurationWizardStep databaseConfigurationWizardStep = new DatabaseConfigurationWizardStep(context);
        InitConditionalWizardStep initConditionalWizardStep = new InitConditionalWizardStep(context, databaseConfigurationWizardStep);
        SQLRunnerWizardStep sqlRunnerWizardStep = new SQLRunnerWizardStep(context);
        GenerationOptionWizardStep generationOptionWizardStep = new GenerationOptionWizardStep(context);
        GenConfigConditionalWizardStep genConfigConditionalWizardStep = new GenConfigConditionalWizardStep(context, generationOptionWizardStep);
        FrontendConfigurationWizardStep frontendConfigurationWizardStep = new FrontendConfigurationWizardStep(context);

        stepsList.add(new InitializationWizardStep(context, specificConfigurationWizardStep, frontendConfigurationWizardStep));
        stepsList.add(initConditionalWizardStep);
        stepsList.add(sqlRunnerWizardStep);
        stepsList.add(genConfigConditionalWizardStep);
        stepsList.add(frontendConfigurationWizardStep);
        stepsList.add(specificConfigurationWizardStep);

        for (ModuleWizardStep step : stepsList) {
            addStep(step);
        }

        init();
    }

    @Override
    protected void doNextAction() {
        ModuleWizardStep currentStep = stepsList.get(getCurrentStep());
        try {
            if (!currentStep.validate()) return;
            currentStep.updateDataModel();
        } catch (ConfigurationException e) {
            Messages.showErrorDialog(getContentPane(), e.getMessage(), "Validation Error");
            return;
        }
        super.doNextAction();
    }

    @Override
    protected void doOKAction() {
        ModuleWizardStep currentStep = stepsList.get(getCurrentStep());
        try {
            if (!currentStep.validate()) return;
            currentStep.updateDataModel(); // This will trigger the actual code generation in SpecificConfigurationWizardStep
        } catch (ConfigurationException e) {
            Messages.showErrorDialog(getContentPane(), e.getMessage(), "Validation Error");
            return;
        }
        super.doOKAction();
    }

    @Override
    protected @Nullable String getHelpID() {
        return null;
    }
}

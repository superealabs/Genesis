package org.labs.genesis.actions;

import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.wizard.AbstractWizard;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.Nullable;
import org.labs.genesis.config.ProjectGenerationContext;
import org.labs.genesis.context.GenerationContextManager;
import org.labs.genesis.wizards.*;
import org.labs.genesis.wizards.conditionals.FrontendConditionalWizardStep;
import org.labs.genesis.wizards.conditionals.GenConfigConditionalWizardStep;
import org.labs.genesis.wizards.conditionals.InitConditionalWizardStep;

import org.labs.genesis.wizards.router.GenesisWizardRouter;
import org.labs.genesis.wizards.router.WizardMode;

import java.util.ArrayList;
import java.util.List;

public class GenesisWizardDialog extends AbstractWizard<ModuleWizardStep> {
    private final List<ModuleWizardStep> stepsList = new ArrayList<>();

    public GenesisWizardDialog(@Nullable Project project) {
        super("Genesis Project Generator", project);

        ProjectGenerationContext context = new ProjectGenerationContext();
        List<ProjectGenerationContext> listContexts = new ArrayList<>();
        GenerationContextManager manager = new GenerationContextManager(context);

        GitConfigurationWizardStep gitConfigurationWizardStep = new GitConfigurationWizardStep(manager);
        SpecificConfigurationWizardStep specificConfigurationWizardStep = new SpecificConfigurationWizardStep(manager, listContexts);
        DatabaseConfigurationWizardStep databaseConfigurationWizardStep = new DatabaseConfigurationWizardStep(manager, listContexts);
        InitConditionalWizardStep initConditionalWizardStep = new InitConditionalWizardStep(manager, databaseConfigurationWizardStep);
        SQLRunnerWizardStep sqlRunnerWizardStep = new SQLRunnerWizardStep(manager);
        RelationshipConfigurationWizardStep relationshipConfigurationWizardStep = new RelationshipConfigurationWizardStep(manager);
        GenerationOptionWizardStep generationOptionWizardStep = new GenerationOptionWizardStep(manager, listContexts, specificConfigurationWizardStep, relationshipConfigurationWizardStep);
        GenConfigConditionalWizardStep genConfigConditionalWizardStep = new GenConfigConditionalWizardStep(manager, generationOptionWizardStep);

        FirstWizardStep firstWizardStep = new FirstWizardStep(manager);
        RuleToCodeWizardStep ruleToCodeWizardStep = new RuleToCodeWizardStep(manager, firstWizardStep);
        RuleToCodeWizardAIStep ruleToCodeWizardAIStep = new RuleToCodeWizardAIStep(manager, firstWizardStep);

        SynchGenerationWizardStep syncGenerationWizardStep = new SynchGenerationWizardStep(manager);
        SyncProjectLoaderWizardStep syncProjectLoaderWizardStep = new SyncProjectLoaderWizardStep(manager, relationshipConfigurationWizardStep, syncGenerationWizardStep);

        FrontendConfigurationWizardStep frontendConfigurationWizardStep = new FrontendConfigurationWizardStep(manager, listContexts);
        FrontendConditionalWizardStep frontendConditionalWizardStep = new FrontendConditionalWizardStep(manager, frontendConfigurationWizardStep);
        InitializationWizardStep initializationWizardStep = new InitializationWizardStep(manager, listContexts, specificConfigurationWizardStep, frontendConfigurationWizardStep);

        stepsList.add(firstWizardStep);
        stepsList.add(ruleToCodeWizardStep);
        stepsList.add(ruleToCodeWizardAIStep);
        stepsList.add(syncProjectLoaderWizardStep);
        stepsList.add(initializationWizardStep);
        stepsList.add(initConditionalWizardStep);
        stepsList.add(sqlRunnerWizardStep);
        stepsList.add(genConfigConditionalWizardStep);
        stepsList.add(relationshipConfigurationWizardStep);
        stepsList.add(frontendConditionalWizardStep);
        stepsList.add(gitConfigurationWizardStep);
        stepsList.add(specificConfigurationWizardStep);
        stepsList.add(syncGenerationWizardStep);

        for (ModuleWizardStep step : stepsList) {
            addStep(step);
        }

        init();
    }

    @Override
    protected int getNextStep(int step) {
        FirstWizardStep firstStep = (FirstWizardStep) stepsList.getFirst();
        WizardMode mode = GenesisWizardRouter.getMode(firstStep.getFirstForm());
        return GenesisWizardRouter.getNextStepIndex(step, mode, stepsList);
    }

    @Override
    protected int getPreviousStep(int step) {
        FirstWizardStep firstStep = (FirstWizardStep) stepsList.getFirst();
        WizardMode mode = GenesisWizardRouter.getMode(firstStep.getFirstForm());
        return GenesisWizardRouter.getPreviousStepIndex(step, mode, stepsList);
    }

    @Override
    protected void doNextAction() {
        ModuleWizardStep currentStep = stepsList.get(getCurrentStep());
        try {
            if (!currentStep.validate()) return;
            currentStep.updateDataModel();
        } catch (ConfigurationException e) {
            Messages.showErrorDialog(getContentPane(), e.getLocalizedMessage(), "Validation Error");
            return;
        }
        super.doNextAction();
    }

    @Override
    protected void doOKAction() {
        ModuleWizardStep currentStep = stepsList.get(getCurrentStep());
        try {
            if (!currentStep.validate()) return;
            currentStep.updateDataModel();
        } catch (ConfigurationException e) {
            Messages.showErrorDialog(getContentPane(), e.getLocalizedMessage(), "Validation Error");
            return;
        }
        super.doOKAction();
    }
}

package org.labs.genesis.wizards.router;

import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import org.labs.genesis.forms.FirstForm;
import org.labs.genesis.wizards.*;

import java.util.List;

public class GenesisWizardRouter {

    public static WizardMode getMode(FirstForm form) {
        if (form == null) {
            return WizardMode.GENERATE_NEW_PROJECT;
        }
        if (form.syncSelected()) {
            return WizardMode.SYNC_PROJECT;
        }
        if (form.ruleTodCodeSelected()) {
            return WizardMode.RULE_TO_CODE;
        }
        return WizardMode.GENERATE_NEW_PROJECT;
    }

    public static int getNextStepIndex(int currentStep, WizardMode mode, List<ModuleWizardStep> steps) {
        int next = currentStep + 1;
        while (next < steps.size()) {
            ModuleWizardStep step = steps.get(next);
            if (isStepAllowedInMode(step, mode) && step.isStepVisible()) {
                return next;
            }
            next++;
        }
        return currentStep;
    }

    public static int getPreviousStepIndex(int currentStep, WizardMode mode, List<ModuleWizardStep> steps) {
        int prev = currentStep - 1;
        while (prev >= 0) {
            ModuleWizardStep step = steps.get(prev);
            if (isStepAllowedInMode(step, mode) && step.isStepVisible()) {
                return prev;
            }
            prev--;
        }
        return 0;
    }

    public static boolean isStepAllowedInMode(ModuleWizardStep step, WizardMode mode) {
        if (step instanceof FirstWizardStep) {
            return true;
        }

        return switch (mode) {
            case GENERATE_NEW_PROJECT -> !(step instanceof RuleToCodeWizardStep)
                    && !(step instanceof RuleToCodeWizardAIStep)
                    && !(step instanceof SyncProjectLoaderWizardStep)
                    && !(step instanceof SynchGenerationWizardStep);
            case RULE_TO_CODE -> !(step instanceof SyncProjectLoaderWizardStep)
                    && !(step instanceof SynchGenerationWizardStep);
            case SYNC_PROJECT -> (step instanceof SyncProjectLoaderWizardStep)
                    || (step instanceof RelationshipConfigurationWizardStep)
                    || (step instanceof SynchGenerationWizardStep);
            default -> true;
        };
    }
}

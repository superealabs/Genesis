import { ref, computed } from 'vue';

export interface WizardConfig {
    totalSteps: number;
    skippableStepsConfig?: Record<number, number[]>;
    
    /** 
     * Hook appelé AVANT de passer à l'étape suivante (sauf en cas de skip explicite).
     */
    onBeforeNext?: (currentStep: number, context: { skippedSteps: Set<number> }) => boolean | Promise<boolean>;
    
    /** 
     * Hook appelé AVANT de revenir à l'étape précédente.
     */
    onBeforePrevious?: (currentStep: number) => boolean | Promise<boolean>;

    /** 
     *  NOUVEAU : Hook appelé À CHAQUE FOIS qu'on arrive sur une étape (après le changement).
     * Idéal pour déclencher le chargement des données spécifiques à cette étape.
     */
    onStepEnter?: (step: number) => void | Promise<void>;
}

export function useGenesisWizard(config: WizardConfig) {
    const currentStep = ref(1);
    const totalSteps = ref(config.totalSteps);
    const skippedSteps = ref<Set<number>>(new Set());
    const stepDependencies = config.skippableStepsConfig || {};

    const isFirstStep = computed(() => currentStep.value === 1);
    const isLastStep = computed(() => currentStep.value === totalSteps.value);

    // ═══ LOGIQUE DE RÉSOLUTION DES DÉPENDANCES (BFS) ═══
    function resolveDependencies(startStep: number): number[] {
        const steps = new Set<number>();
        const queue: number[] = [startStep];
        
        while (queue.length > 0) {
            const current = queue.shift()!;
            if (!steps.has(current)) {
                steps.add(current);
                const dependencies = stepDependencies[current] || [];
                for (const dep of dependencies) {
                    if (!steps.has(dep)) {
                        queue.push(dep);
                    }
                }
            }
        }
        return Array.from(steps);
    }

    function unskipBranch(startStep: number) {
        const stepsToUnskip = resolveDependencies(startStep);
        stepsToUnskip.forEach(step => skippedSteps.value.delete(step));
    }

    // ═══ ACTIONS DE NAVIGATION ═══
    
    async function goToNextStep(isSkip = false): Promise<boolean> {
        if (!isSkip && config.onBeforeNext) {
            const canProceed = await config.onBeforeNext(currentStep.value, { skippedSteps: skippedSteps.value });
            if (!canProceed) return false;
        }

        if (currentStep.value < totalSteps.value) {
            let nextStep = currentStep.value + 1;
            
            if (!isSkip && skippedSteps.value.has(nextStep)) {
                unskipBranch(nextStep);
            }

            while (nextStep <= totalSteps.value && skippedSteps.value.has(nextStep)) {
                nextStep++;
            }
            
            if (nextStep <= totalSteps.value) {
                currentStep.value = nextStep;
                
                //  APPEL DU NOUVEAU HOOK APRÈS LE CHANGEMENT D'ÉTAPE
                if (config.onStepEnter) {
                    await config.onStepEnter(currentStep.value);
                }
                
                return true;
            }
        }
        return false;
    }
    
    async function goToPreviousStep(): Promise<boolean> {
        if (config.onBeforePrevious) {
            const canProceed = await config.onBeforePrevious(currentStep.value);
            if (!canProceed) return false;
        }

        if (currentStep.value > 1) {
            let prevStep = currentStep.value - 1;
            
            if (skippedSteps.value.has(prevStep)) {
                while (prevStep >= 1 && skippedSteps.value.has(prevStep)) {
                    prevStep--;
                }
                currentStep.value = prevStep + 1;
            } else {
                currentStep.value = prevStep;
            }
            
            //  APPEL DU NOUVEAU HOOK APRÈS LE CHANGEMENT D'ÉTAPE
            if (config.onStepEnter) {
                await config.onStepEnter(currentStep.value);
            }
            
            return true;
        }
        return false;
    }

    function skipCurrentStep() {
        const stepsToSkip = resolveDependencies(currentStep.value);
        stepsToSkip.forEach(step => skippedSteps.value.add(step));
        goToNextStep(true); 
    }

    function resetWizard() {
        currentStep.value = 1;
        skippedSteps.value.clear();
        
        //  APPEL DU NOUVEAU HOOK LORS DE LA RÉINITIALISATION
        if (config.onStepEnter) {
            config.onStepEnter(currentStep.value);
        }
    }

    return {
        currentStep,
        totalSteps,
        isFirstStep,
        isLastStep,
        skippedSteps,
        goToNextStep,
        goToPreviousStep,
        skipCurrentStep,
        resetWizard
    };
}
import { ref, computed } from 'vue';

export interface WizardConfig {
    totalSteps: number;
    /** 
     * Configuration des dépendances de skip. 
     * Ex: { 8: [9] } signifie "si on skip l'étape 8, l'étape 9 est aussi skipée".
     */
    skippableStepsConfig?: Record<number, number[]>;
    /** 
     * Hook appelé AVANT de passer à l'étape suivante (sauf en cas de skip explicite).
     * Permet la validation ou l'exécution d'actions spécifiques à l'étape.
     */
    onBeforeNext?: (currentStep: number, context: { skippedSteps: Set<number> }) => boolean | Promise<boolean>;
    /** 
     * Hook appelé AVANT de revenir à l'étape précédente.
     */
    onBeforePrevious?: (currentStep: number) => boolean | Promise<boolean>;
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
    
    //  AJOUT : Paramètre isSkip pour bypasser la validation
    async function goToNextStep(isSkip = false): Promise<boolean> {
        // 1. Hook personnalisé (Ignoré SI l'action est un skip explicite)
        if (!isSkip && config.onBeforeNext) {
            const canProceed = await config.onBeforeNext(currentStep.value, { skippedSteps: skippedSteps.value });
            if (!canProceed) return false; // Navigation bloquée par la validation
        }

        // 2. Logique de saut des étapes
        if (currentStep.value < totalSteps.value) {
            let nextStep = currentStep.value + 1;
            
            // Si l'utilisateur avance NORMALEMENT vers une étape précédemment skipée, on annule le skip
            if (!isSkip && skippedSteps.value.has(nextStep)) {
                unskipBranch(nextStep);
            }

            // Trouve la prochaine étape valide (non skipée)
            while (nextStep <= totalSteps.value && skippedSteps.value.has(nextStep)) {
                nextStep++;
            }
            
            if (nextStep <= totalSteps.value) {
                currentStep.value = nextStep;
                return true;
            }
        }
        return false; // Fin du wizard atteinte
    }
    
    async function goToPreviousStep(): Promise<boolean> {
        if (config.onBeforePrevious) {
            const canProceed = await config.onBeforePrevious(currentStep.value);
            if (!canProceed) return false;
        }

        if (currentStep.value > 1) {
            let prevStep = currentStep.value - 1;
            
            //  CORRECTION MAJEURE : Gestion intelligente du retour arrière
            if (skippedSteps.value.has(prevStep)) {
                // L'étape précédente immédiate est skipée. 
                // On veut revenir au DÉBUT de ce bloc skipé (le déclencheur).
                // On recule tant que l'étape est dans le set des skipés.
                while (prevStep >= 1 && skippedSteps.value.has(prevStep)) {
                    prevStep--;
                }
                // À la sortie de la boucle, prevStep est la dernière étape NON skipée avant le bloc.
                // Le début du bloc skipé est donc prevStep + 1.
                currentStep.value = prevStep + 1;
            } else {
                // L'étape précédente n'est pas skipée, on y va normalement.
                currentStep.value = prevStep;
            }
            return true;
        }
        return false;
    }

    function skipCurrentStep() {
        const stepsToSkip = resolveDependencies(currentStep.value);
        stepsToSkip.forEach(step => skippedSteps.value.add(step));
        
        //  CORRECTION : On passe true pour indiquer que c'est un skip, 
        // ce qui bypassera le hook onBeforeNext (pas de validation bloquante).
        goToNextStep(true); 
    }

    function resetWizard() {
        currentStep.value = 1;
        skippedSteps.value.clear();
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
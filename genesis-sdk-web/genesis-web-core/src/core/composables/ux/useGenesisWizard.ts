// genesis-sdk-web/genesis-web-core/src/core/composables/ux/useGenesisWizard.ts
import { ref, computed } from 'vue';

// ============================================================================
// TYPES & INTERFACES
// ============================================================================

export interface WizardConfig {
  totalSteps: number;
  skippableStepsConfig?: Record<number, number[]>;
  
  /** 
   * Hook appelé AVANT de passer à l'étape suivante (sauf en cas de saut explicite).
   * Permet de valider les données de l'étape actuelle.
   */
  onBeforeNext?: (currentStep: number, context: { skippedSteps: Set<number> }) => boolean | Promise<boolean>;
  
  /** 
   * Hook appelé AVANT de revenir à l'étape précédente.
   */
  onBeforePrevious?: (currentStep: number) => boolean | Promise<boolean>;

  /** 
   * Hook appelé À CHAQUE FOIS qu'on arrive sur une étape (après le changement d'index).
   * Idéal pour déclencher le chargement des données spécifiques à cette étape.
   */
  onStepEnter?: (step: number) => void | Promise<void>;
}

// ============================================================================
// COMPOSABLE PRINCIPAL
// ============================================================================

export function useGenesisWizard(config: WizardConfig) {
  // --- 1. État Réactif ---
  const currentStep = ref(1);
  const totalSteps = ref(config.totalSteps);
  const skippedSteps = ref<Set<number>>(new Set());
  const stepDependencies = config.skippableStepsConfig || {};

  // --- 2. Computeds (Données dérivées) ---
  const isFirstStep = computed(() => currentStep.value === 1);
  const isLastStep = computed(() => currentStep.value === totalSteps.value);

  // --- 3. Fonctions Utilitaires ---

  /**
   * Résout toutes les dépendances d'une étape donnée en utilisant un parcours en largeur (BFS).
   * Cela permet de savoir quelles étapes doivent être ignorées (ou réactivées) en cascade 
   * lorsqu'une étape parente est ignorée (ou revisitée).
   */
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

  /**
   * Réactive (unskip) une branche entière d'étapes dépendantes.
   */
  function unskipBranch(startStep: number) {
    const stepsToUnskip = resolveDependencies(startStep);
    stepsToUnskip.forEach(step => skippedSteps.value.delete(step));
  }

  /**
   * Déclenche le hook onStepEnter de manière centralisée pour éviter la duplication de code.
   */
  async function triggerStepEnter(step: number) {
    if (config.onStepEnter) {
      await config.onStepEnter(step);
    }
  }

  // --- 4. Actions de Navigation ---

  async function goToNextStep(isSkip = false): Promise<boolean> {
    // Validation avant de passer à l'étape suivante (sauf si c'est un saut forcé)
    if (!isSkip && config.onBeforeNext) {
      const canProceed = await config.onBeforeNext(currentStep.value, { skippedSteps: skippedSteps.value });
      if (!canProceed) return false;
    }

    if (currentStep.value < totalSteps.value) {
      let nextStep = currentStep.value + 1;
      
      // Si on n'a pas explicitement ignoré l'étape, mais qu'elle est marquée comme ignorée,
      // on réactive sa branche de dépendances.
      if (!isSkip && skippedSteps.value.has(nextStep)) {
        unskipBranch(nextStep);
      }

      // Avance jusqu'à la prochaine étape qui n'est pas ignorée
      while (nextStep <= totalSteps.value && skippedSteps.value.has(nextStep)) {
        nextStep++;
      }
      
      if (nextStep <= totalSteps.value) {
        currentStep.value = nextStep;
        await triggerStepEnter(currentStep.value);
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
      let targetStep = currentStep.value - 1;
      
      // Recule jusqu'à trouver la première étape qui n'est pas ignorée
      while (targetStep >= 1 && skippedSteps.value.has(targetStep)) {
        targetStep--;
      }
      
      currentStep.value = targetStep;
      await triggerStepEnter(currentStep.value);
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
    triggerStepEnter(currentStep.value);
  }

  // --- 5. Initialisation ---
  
  // Déclenche le hook pour l'étape initiale dès la création du composable
  triggerStepEnter(currentStep.value);

  // --- 6. Retour ---
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
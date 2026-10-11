// genesis-sdk-web/genesis-web-core/src/core/composables/ux/useGenesisWizard.ts
import { ref, computed } from 'vue';

// ============================================================================
// TYPES & INTERFACES
// ============================================================================

export interface WizardConfig {
  totalSteps: number;
  skippableStepsConfig?: Record<number, number[]>;
  
  onBeforeNext?: (currentStep: number, context: { skippedSteps: Set<number> }) => boolean | Promise<boolean>;
  onBeforePrevious?: (currentStep: number) => boolean | Promise<boolean>;
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

  // ✅ NOUVEAU : Map inverse pour retrouver le déclencheur d'une étape sautée
  // Exemple : si skippableStepsConfig est { 5: [6] }, cette map contiendra { 6: 5 }
  const reverseDependencies = new Map<number, number>();
  for (const [triggerStr, deps] of Object.entries(stepDependencies)) {
    const trigger = Number(triggerStr);
    for (const dep of deps) {
      reverseDependencies.set(dep, trigger);
    }
  }

  // --- 2. Computeds (Données dérivées) ---
  const isFirstStep = computed(() => currentStep.value === 1);
  const isLastStep = computed(() => currentStep.value === totalSteps.value);

  // --- 3. Fonctions Utilitaires ---

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

  async function triggerStepEnter(step: number) {
    if (config.onStepEnter) {
      await config.onStepEnter(step);
    }
  }

  // --- 4. Actions de Navigation ---

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
        await triggerStepEnter(currentStep.value);
        return true;
      }
    }
    return false;
  }
  
  // ✅ CORRECTION MAJEURE : Gestion intelligente du retour arrière
  async function goToPreviousStep(): Promise<boolean> {
    if (config.onBeforePrevious) {
      const canProceed = await config.onBeforePrevious(currentStep.value);
      if (!canProceed) return false;
    }

    if (currentStep.value > 1) {
      let targetStep = currentStep.value - 1;
      
      // Si l'étape précédente immédiate est sautée, on applique la logique de retour au déclencheur
      if (targetStep >= 1 && skippedSteps.value.has(targetStep)) {
        // On regarde si cette étape sautée est une dépendance d'un déclencheur précédent
        const trigger = reverseDependencies.get(targetStep);
        
        if (trigger !== undefined) {
          // C'est une dépendance (ex: étape 6), on remonte directement au déclencheur (ex: étape 5)
          targetStep = trigger;
        }
        
        // Dans tous les cas (que targetStep soit la dépendance ou le déclencheur lui-même),
        // on réactive la branche pour que l'utilisateur puisse reconsidérer son choix.
        unskipBranch(targetStep);
      }
      
      // Sécurité : ne pas descendre en dessous de 1
      if (targetStep < 1) targetStep = 1;

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
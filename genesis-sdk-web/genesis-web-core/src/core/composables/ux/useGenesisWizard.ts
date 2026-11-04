// genesis-sdk-web/genesis-web-core/src/core/composables/ux/useGenesisWizard.ts
import { ref, computed, watch } from 'vue';

// ============================================================================
// TYPES & INTERFACES
// ============================================================================

export interface WizardConfig {
  totalSteps: number;
  skippableStepsConfig?: Record<number, number[]>;
  
  onBeforeNext?: (currentStep: number, context: { skippedSteps: Set<number> }) => boolean | Promise<boolean>;
  onBeforePrevious?: (currentStep: number) => boolean | Promise<boolean>;
  onStepEnter?: (step: number) => void | Promise<void>;
  onSkip?: (step: number) => void | Promise<void>;
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
  const isAdvancing = ref(false);

  const visitedSteps = ref<Set<number>>(new Set([1]));

  watch(currentStep, (newStep) => {
    visitedSteps.value.add(newStep);
  });  

  // NOUVEAU : Map inverse pour retrouver le déclencheur d'une étape sautée
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

  async function goToStep(targetStep: number): Promise<boolean> {
    if (isAdvancing.value) return false;
    if (targetStep < 1 || targetStep > totalSteps.value) return false;

    isAdvancing.value = true;
    try {
      if (targetStep < currentStep.value) {
        unskipBranch(targetStep);
      }
      currentStep.value = targetStep;
      await triggerStepEnter(currentStep.value);
      return true;
    } finally {
      isAdvancing.value = false;
    }
  }

  async function doGoToNextStep(isSkip = false): Promise<boolean> {
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

  async function goToNextStep(isSkip = false): Promise<boolean> {
    if (isAdvancing.value) return false;
    isAdvancing.value = true;
    try {
      return await doGoToNextStep(isSkip);
    } finally {
      isAdvancing.value = false;
    }
  }
  
  // ✅ CORRECTION MAJEURE : Gestion intelligente du retour arrière
  async function goToPreviousStep(): Promise<boolean> {
    if (isAdvancing.value) return false;
    isAdvancing.value = true;
    try {
      if (config.onBeforePrevious) {
        const canProceed = await config.onBeforePrevious(currentStep.value);
        if (!canProceed) return false;
      }

      if (currentStep.value > 1) {
        let targetStep = currentStep.value - 1;
        if (targetStep >= 1 && skippedSteps.value.has(targetStep)) {
          const trigger = reverseDependencies.get(targetStep);
          if (trigger !== undefined) targetStep = trigger;
          unskipBranch(targetStep);
        }
        if (targetStep < 1) targetStep = 1;

        currentStep.value = targetStep;
        await triggerStepEnter(currentStep.value);
        return true;
      }
      return false;
    } finally {
      isAdvancing.value = false;
    }
  }

  async function skipCurrentStep() {   // ✅ devient async
    if (isAdvancing.value) return;
    isAdvancing.value = true;
    try {
      if (config.onSkip) {
        await config.onSkip(currentStep.value);   // ✅ await
      }
      const stepsToSkip = resolveDependencies(currentStep.value);
      stepsToSkip.forEach(step => skippedSteps.value.add(step));
      await doGoToNextStep(true);                  // ✅ await + non protégé
    } finally {
      isAdvancing.value = false;
    }
  }

  async function resetWizard() {
    currentStep.value = 1;
    skippedSteps.value.clear();
    await triggerStepEnter(currentStep.value);
  }

  // --- 5. Initialisation ---
  triggerStepEnter(currentStep.value);

  /** Retire des steps de la liste des steps visités (leurs données ont été réinitialisées) */
  function invalidateSteps(steps: number[]) {
    steps.forEach(step => {
      if (step !== currentStep.value) visitedSteps.value.delete(step); // l'étape courante reste visitée
    });
  }

  // --- 6. Retour ---
  return {
    currentStep,
    totalSteps,
    isFirstStep,
    isLastStep,
    skippedSteps,
    visitedSteps,
    goToStep,
    goToNextStep,
    goToPreviousStep,
    skipCurrentStep,
    resetWizard,
    invalidateSteps,
    isAdvancing
  };
}
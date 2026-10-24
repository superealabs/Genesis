// genesis-sdk-web/genesis-web-core/src/features/generator/composables/useGenerator.ts
import { inject, computed } from 'vue';
import { storeToRefs } from 'pinia';

// Types & Interfaces
import type { 
  GeneratorData, 
  IDatabaseService, 
  IFrameworkService, 
  IFrontendService, 
  IRepositoryService
} from '@genesis-labs/shared-types';

// Stores
import { useGeneratorStore } from '@genesis-labs/web-core/features/generator/store/useGenerator.store';
import { useFrontendStore } from '@genesis-labs/web-core/features/frontend/store/useFrontend.store';

// Services & Manifests
import { GENERATOR_SERVICE_KEY, type IGeneratorService } from '../types/generator.service.interface';
import { FRONTEND_SERVICE_KEY } from '../../frontend/manifest';
import { DATABASE_SERVICE_KEY } from '../../database/manifest';
import { FRAMEWORK_SERVICE_KEY } from '../../frameworks/manifest';
import { RECENT_SERVICE_KEY } from '@genesis-labs/web-core/core/features/recent/manifest';
import { REPOSITORY_SERVICE_KEY } from '../../repository/manifest';
import { IRecentService } from '@genesis-labs/shared-types';

// Core & Config
import { useGenesisWizard } from '@genesis-labs/web-core/core/composables/ux/useGenesisWizard';
import { 
  WIZARD_STEP_CONFIG, 
  type WizardServices,
  getTotalSteps,
  getStepIdByNumber,
  getSkippableStepsConfig
} from './wizard-step-config';

export function useGenerator() {
  // ==========================================================================
  // 1. INJECTION DES DÉPENDANCES
  // ==========================================================================
  const generatorService = inject(GENERATOR_SERVICE_KEY);
  if (!generatorService) {
    throw new Error('[useGenerator] IGeneratorService non fourni.');
  }

  const svc = generatorService as IGeneratorService;
  const fdsvc = inject(FRONTEND_SERVICE_KEY) as IFrontendService;
  const dbsvc = inject(DATABASE_SERVICE_KEY) as IDatabaseService;
  const fsvc = inject(FRAMEWORK_SERVICE_KEY) as IFrameworkService;
  const recentSvc = inject(RECENT_SERVICE_KEY) as IRecentService;
  const repoSvc = inject(REPOSITORY_SERVICE_KEY) as IRepositoryService;

  const store = useGeneratorStore();
  const frontendStore = useFrontendStore();

  // ==========================================================================
  // 2. CONFIGURATION DU WIZARD (DYNAMIQUE)
  // ==========================================================================
  const wizardServices: WizardServices = { svc, fdsvc, dbsvc, fsvc, recentSvc, repoSvc };

  const wizard = useGenesisWizard({
    totalSteps: getTotalSteps(),
    skippableStepsConfig: getSkippableStepsConfig(),
    
    /**
     * Hook déclenché à l'entrée d'une étape.
     * Traduit le numéro d'étape en ID sémantique avant d'exécuter la logique.
     */
    onStepEnter: async (currentStep: number) => {
      const stepId = getStepIdByNumber(currentStep);
      if (!stepId) return;

      const stepConfig = WIZARD_STEP_CONFIG[stepId];
      if (stepConfig?.onEnter) {
        console.log(`[Wizard] Entrée dans l'étape : ${stepId} (Étape ${currentStep})`);
        await stepConfig.onEnter(store, wizardServices);
      }
    },

    /**
     * Hook déclenché avant de passer à l'étape suivante.
     * Traduit le numéro d'étape en ID sémantique avant de valider.
     */
    onBeforeNext: async (currentStep: number) => {
      store.clearWizardError();
      const stepId = getStepIdByNumber(currentStep);
      if (!stepId) return true;
      
      const stepConfig = WIZARD_STEP_CONFIG[stepId];
      if (stepConfig?.beforeNext) {
        console.log(`[Wizard] Validation de l'étape : ${stepId} (Étape ${currentStep})...`);
        const canProceed = await stepConfig.beforeNext(store, wizardServices);
        if (!canProceed) {
          console.warn(`[Wizard] Navigation bloquée à l'étape : ${stepId}.`);
          return false;
        }
      }
      return true; 
    }
  });

  // ==========================================================================
  // 3. ÉTAT RÉACTIF (Depuis le store)
  // ==========================================================================
  const { 
    stepperData, 
    getTablesParents, 
    getTablesChilds, 
    getRelations,
    tables, 
    views, 
    availableTables, 
    getAvailableLoggingLevels,
    getAvailableSecurityTypes,
    getAvailableCacheProviders,
    getAvailableLanguageVersions,
    getAvailableFrameworkVersions,
    getAvailableBuildTools,
    getAvailableHibernateDdlAutoOptions,
  } = storeToRefs(store);

  // ==========================================================================
  // 4. COMPUTEDS
  // ==========================================================================
  
  /**
   * Détermine si l'étape actuelle peut être ignorée (skippée) par l'utilisateur.
   * Vérifie dynamiquement si le numéro de l'étape actuelle est une clé dans la config de saut.
   */
  const isCurrentStepSkippable = computed(() => {
    const skippableConfig = getSkippableStepsConfig();
    return Object.keys(skippableConfig).map(Number).includes(wizard.currentStep.value);
  });

  // ==========================================================================
  // 5. ACTIONS
  // ==========================================================================

  // --- Navigation du Wizard ---
  function handleComplete(): GeneratorData | null {
    return stepperData.value;
  }

  async function goToNextStep(): Promise<GeneratorData | null> {
    const didMove = await wizard.goToNextStep();
    if (!didMove && wizard.isLastStep.value) {
      return handleComplete();
    }
    return null;
  }

  function resetWizard() {
    store.reset();
    wizard.resetWizard();
  }

  // --- Fetching de Données (Délégation aux services) ---
  async function fetchTablesMetadata() { 
    store.setAvailableTables(await svc.fetchTablesMetadata()); 
  }
  
  async function fetchTablesMetadataParents() { 
    store.setTablesParents(await svc.fetchTablesMetadataParents()); 
  }
  
  async function fetchTablesMetadataChilds() { 
    store.setTablesChilds(await svc.fetchTablesMetadataChilds()); 
  }
  
  async function fetchRelations() { 
    store.setRelations(await svc.fetchRelations()); 
  }
  
  async function fetchAvailableLanguages() { 
    const data = await fdsvc.fetchInterfaceLanguages();
    frontendStore.setAvailableInterfaceLanguages(data); 
  }

  async function testDatabaseConnection(): Promise<{ success: boolean; message: string }> {
    try {
      return await dbsvc.testDatabaseConnection(stepperData.value.database);
    } catch (error) {
      return { 
        success: false, 
        message: error instanceof Error ? error.message : 'Erreur inconnue' 
      };
    }
  }

  // Fetching des options spécifiques au Framework (via Framework Service)
  async function fetchLoggingLevels(frameworkId: number) {
    store.setAvailableLoggingLevels(await fsvc.fetchLoggingLevels(frameworkId));
  }

  async function fetchSecurityTypes(frameworkId: number) {
    store.setAvailableSecurityTypes(await fsvc.fetchSecurityTypes(frameworkId));
  }

  async function fetchCacheProviders(frameworkId: number) {
    store.setAvailableCacheProviders(await fsvc.fetchCacheProviders(frameworkId));
  }

  async function fetchLanguageVersions(languageId: number) {
    store.setAvailableLanguageVersions(await fsvc.fetchLanguageVersions(languageId));
  }

  async function fetchFrameworkVersions(frameworkId: number) {
    store.setAvailableFrameworkVersions(await fsvc.fetchFrameworkVersions(frameworkId));
  }

  async function fetchBuildTools(frameworkId: number) {
    store.setAvailableBuildTools(await fsvc.fetchBuildTools(frameworkId));
  }

  async function fetchHibernateDdlAutoOptions(frameworkId: number) {
    store.setAvailableHibernateDdlAutoOptions(await fsvc.fetchHibernateDdlAutoOptions(frameworkId));
  }

  // ==========================================================================
  // 6. RETOUR FINAL (Ordonné par domaine)
  // ==========================================================================
  return {
    // État du Wizard
    currentStep: wizard.currentStep,
    totalSteps: wizard.totalSteps,
    isFirstStep: wizard.isFirstStep,
    isLastStep: wizard.isLastStep,
    skippedSteps: wizard.skippedSteps,
    isCurrentStepSkippable,

    // Données du Store (Refs)
    stepperData,
    tables, 
    views, 
    availableTables,
    getTablesParents, 
    getTablesChilds, 
    getRelations,
    
    // Options de configuration (Getters)
    availableLoggingLevels: getAvailableLoggingLevels,
    availableSecurityTypes: getAvailableSecurityTypes,
    availableCacheProviders: getAvailableCacheProviders,
    availableLanguageVersions: getAvailableLanguageVersions,
    availableFrameworkVersions: getAvailableFrameworkVersions,
    availableBuildTools: getAvailableBuildTools,
    availableHibernateDdlAutoOptions: getAvailableHibernateDdlAutoOptions,

    // Navigation
    goToNextStep,
    goToPreviousStep: wizard.goToPreviousStep,
    skipCurrentStep: wizard.skipCurrentStep,
    reset: resetWizard,

    // Mutations d'état (Délégation au store)
    setFramework: store.setFramework,
    setPendingFramework: store.setPendingFramework,
    setPendingDatabaseEngine: store.setPendingDatabaseEngine,
    setPendingFrontendFramework: store.setPendingFrontendFramework,
    setPendingRepositoryProvider: store.setPendingRepositoryProvider,
    setDatabaseEngine: store.setDatabaseEngine,
    setSelectedFrontendFramework: store.setSelectedFrontendFramework,
    
    updateConfig: store.updateConfig,
    updateDatabase: store.updateDatabase,
    updateScript: store.updateScript,
    updateFrontendLayout: store.updateFrontendLayout,
    updateGitConfig: store.updateGitConfig,
    
    toggleLanguage: store.toggleLanguage,
    
    addRelation: store.addRelation,
    removeRelation: store.removeRelation,

    // Actions de Fetching
    fetchTablesMetadata, 
    fetchTablesMetadataParents, 
    fetchTablesMetadataChilds,
    fetchRelations, 
    fetchAvailableLanguages, 
    testDatabaseConnection,
    fetchLoggingLevels,
    fetchSecurityTypes,
    fetchCacheProviders,
    fetchLanguageVersions,
    fetchFrameworkVersions,
    fetchBuildTools,
    fetchHibernateDdlAutoOptions,
  };
}
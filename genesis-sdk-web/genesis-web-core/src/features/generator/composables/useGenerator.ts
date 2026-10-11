// genesis-sdk-web/genesis-web-core/src/features/generator/composables/useGenerator.ts
import { inject, computed } from 'vue';
import { storeToRefs } from 'pinia';

// Types & Interfaces
import type { 
  GeneratorData, 
  IDatabaseService, 
  IFrameworkService, 
  IFrontendService 
} from '@genesis-labs/shared-types';

// Stores
import { useGeneratorStore } from '@genesis-labs/web-core/features/generator/store/useGenerator.store';
import { useFrontendStore } from '@genesis-labs/web-core/features/frontend/store/useFrontend.store';

// Services & Manifests
import { GENERATOR_SERVICE_KEY, type IGeneratorService } from '../types/generator.service.interface';
import { FRONTEND_SERVICE_KEY } from '../../frontend/manifest';
import { DATABASE_SERVICE_KEY } from '../../database/manifest';
import { FRAMEWORK_SERVICE_KEY } from '../../frameworks/manifest';

// Core & Config
import { useGenesisWizard } from '@genesis-labs/web-core/core/composables/ux/useGenesisWizard';
import { WIZARD_STEP_CONFIG, type WizardServices } from './wizard-step-config';

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

  const store = useGeneratorStore();
  const frontendStore = useFrontendStore();

  // ==========================================================================
  // 2. CONFIGURATION DU WIZARD
  // ==========================================================================
  const SKIPPABLE_CONFIG = { 
    5: [], 
    8: [9], 
    10: [] 
  };

  const wizardServices: WizardServices = { svc, fdsvc, dbsvc, fsvc };

  const wizard = useGenesisWizard({
    totalSteps: 10,
    skippableStepsConfig: SKIPPABLE_CONFIG,
    
    /**
     * Hook déclenché à l'entrée d'une étape.
     * Délègue le chargement des données spécifiques à l'étape à la configuration.
     */
    onStepEnter: async (currentStep: number) => {
      const stepConfig = WIZARD_STEP_CONFIG[currentStep];
      if (stepConfig?.onEnter) {
        console.log(`[Wizard] Entrée dans l'étape ${currentStep} : Chargement...`);
        await stepConfig.onEnter(store, wizardServices);
      }
    },

    /**
     * Hook déclenché avant de passer à l'étape suivante.
     * Délègue la validation des données à la configuration.
     */
    onBeforeNext: async (currentStep: number) => {
      store.clearWizardError();
      const stepConfig = WIZARD_STEP_CONFIG[currentStep];
      
      if (stepConfig?.beforeNext) {
        console.log(`[Wizard] Validation de l'étape ${currentStep}...`);
        const canProceed = await stepConfig.beforeNext(store, wizardServices);
        if (!canProceed) {
          console.warn(`[Wizard] Navigation bloquée à l'étape ${currentStep}.`);
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
   */
  const isCurrentStepSkippable = computed(() => 
    Object.keys(SKIPPABLE_CONFIG).map(Number).includes(wizard.currentStep.value)
  );

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
    setDatabaseEngine: store.setDatabaseEngine,
    setSelectedFrontendFramework: store.setSelectedFrontendFramework,
    
    updateConfig: store.updateConfig,
    updateDatabase: store.updateDatabase,
    updateScript: store.updateScript,
    updateFrontendLayout: store.updateFrontendLayout,
    updateGitConfig: store.updateGitConfig,
    
    toggleTable: store.toggleTable,
    toggleView: store.toggleView,
    toggleComponent: store.toggleComponent,
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
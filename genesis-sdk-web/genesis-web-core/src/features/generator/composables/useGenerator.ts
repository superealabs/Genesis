// genesis-sdk-web/genesis-web-core/src/features/generator/composables/useGenerator.ts
import { inject, computed } from 'vue';
import { storeToRefs } from 'pinia';
import { useGeneratorStore } from '@genesis-labs/web-core/features/generator/store/useGenerator.store';
import { GENERATOR_SERVICE_KEY, type IGeneratorService } from '@genesis-labs/web-core/features/generator/types/generator.service.interface';
import { useGenesisWizard } from '@genesis-labs/web-core/core/composables/ux/useGenesisWizard';

import type { GeneratorData, IDatabaseService, IFrameworkService } from '@genesis-labs/shared-types';
import { FRONTEND_SERVICE_KEY, IFrontendService } from '../../frontend/manifest';
import { DATABASE_SERVICE_KEY } from '../../database/manifest';
import { FRAMEWORK_SERVICE_KEY } from '../../frameworks/manifest';
import { WIZARD_STEP_CONFIG, type WizardServices } from './wizard-step-config';



export function useGenerator() {
    const service = inject(GENERATOR_SERVICE_KEY);
    const frontendService = inject(FRONTEND_SERVICE_KEY);
    const databaseService = inject(DATABASE_SERVICE_KEY);
    const frameworkService = inject(FRAMEWORK_SERVICE_KEY);

    if (!service) throw new Error('[useGenerator] IGeneratorService non fourni.');
    
    const svc = service as IGeneratorService;
    const fdsvc = frontendService as IFrontendService;
    const dbsvc = databaseService as IDatabaseService;
    const fsvc = frameworkService as IFrameworkService;

    const store = useGeneratorStore();

    const SKIPPABLE_CONFIG = { 
        5: [], 
        8: [9], 
        10: [] 
    };

    const wizardServices: WizardServices = { svc, fdsvc, dbsvc, fsvc };

    const wizard = useGenesisWizard({
        totalSteps: 10,
        skippableStepsConfig: SKIPPABLE_CONFIG,
        
        // ✅ HOOK AVANT NEXT : On délègue à la config de l'étape
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
        },

        // ✅ HOOK ON ENTER : On délègue à la config de l'étape
        onStepEnter: async (currentStep: number) => {
            const stepConfig = WIZARD_STEP_CONFIG[currentStep];
            if (stepConfig?.onEnter) {
                console.log(`[Wizard] Entrée dans l'étape ${currentStep} : Chargement...`);
                await stepConfig.onEnter(store, wizardServices);
            }
        }
    });

    const { 
        stepperData, 
        getTablesParents, getTablesChilds, getRelations,
        getAvailableFrontendFrameworks, getAvailableLanguages,
        tables, views, availableTables, 
        
        getAvailableLoggingLevels,
        getAvailableSecurityTypes,
        getAvailableCacheProviders,
        getAvailableLanguageVersions,
        getAvailableFrameworkVersions,
        getAvailableBuildTools,
        getAvailableHibernateDdlAutoOptions,
    } = storeToRefs(store);

    function handleComplete(): GeneratorData | null {
        return stepperData.value;
    }

    const isCurrentStepSkippable = computed(() => Object.keys(SKIPPABLE_CONFIG).map(Number).includes(wizard.currentStep.value));

    async function goToNextStep(): Promise<GeneratorData | null> {
        const didMove = await wizard.goToNextStep();
        if (!didMove && wizard.isLastStep.value) return handleComplete();
        return null;
    }

    // ═══ 4. ACTIONS MÉTIER ASYNCHRONES ═══
    async function fetchTablesMetadata() { store.setAvailableTables(await svc.fetchTablesMetadata()); }
    async function fetchTablesMetadataParents() { store.setTablesParents(await svc.fetchTablesMetadataParents()); }
    async function fetchTablesMetadataChilds() { store.setTablesChilds(await svc.fetchTablesMetadataChilds()); }
    async function fetchRelations() { store.setRelations(await svc.fetchRelations()); }
    async function fetchAvailableLanguages() { store.setAvailableLanguages(await fdsvc.fetchInterfaceLanguages()); }

    async function testDatabaseConnection(): Promise<{ success: boolean; message: string }> {
        try {
            return await dbsvc.testDatabaseConnection(stepperData.value.database);
        } catch (error) {
            return { success: false, message: error instanceof Error ? error.message : 'Erreur inconnue' };
        }
    }

    // CORRECTION : Prend maintenant frameworkId comme le contrat l'exige
    async function fetchLoggingLevels(frameworkId: number) {
        const data = await svc.fetchLoggingLevels(frameworkId);
        store.setAvailableLoggingLevels(data);
    }

    async function fetchSecurityTypes(frameworkId: number) {
        const data = await svc.fetchSecurityTypes(frameworkId);
        store.setAvailableSecurityTypes(data);
    }

    async function fetchCacheProviders(frameworkId: number) {
        const data = await svc.fetchCacheProviders(frameworkId);
        store.setAvailableCacheProviders(data);
    }

    async function fetchLanguageVersions(languageId: number) {
        const data = await svc.fetchLanguageVersions(languageId);
        store.setAvailableLanguageVersions(data);
    }

    async function fetchFrameworkVersions(frameworkId: number) {
        const data = await svc.fetchFrameworkVersions(frameworkId);
        store.setAvailableFrameworkVersions(data);
    }

    async function fetchBuildTools(frameworkId: number) {
        const data = await svc.fetchBuildTools(frameworkId);
        store.setAvailableBuildTools(data);
    }

    async function fetchHibernateDdlAutoOptions(frameworkId: number) {
        const data = await svc.fetchHibernateDdlAutoOptions(frameworkId);
        store.setAvailableHibernateDdlAutoOptions(data);
    }


    // ═══ 5. RETOUR FINAL ═══
    return {
        currentStep: wizard.currentStep,
        totalSteps: wizard.totalSteps,
        isFirstStep: wizard.isFirstStep,
        isLastStep: wizard.isLastStep,
        skippedSteps: wizard.skippedSteps,

        stepperData,
        getTablesParents, getTablesChilds, getRelations,
        availableFrontendFrameworks: getAvailableFrontendFrameworks, 
        availableLanguages: getAvailableLanguages,
        tables, views, availableTables,
        
        availableLoggingLevels: getAvailableLoggingLevels,
        availableSecurityTypes: getAvailableSecurityTypes,
        availableCacheProviders: getAvailableCacheProviders,
        availableLanguageVersions: getAvailableLanguageVersions,
        availableFrameworkVersions: getAvailableFrameworkVersions,
        availableBuildTools: getAvailableBuildTools,
        availableHibernateDdlAutoOptions: getAvailableHibernateDdlAutoOptions,

        goToNextStep,
        goToPreviousStep: wizard.goToPreviousStep,
        skipCurrentStep: wizard.skipCurrentStep,
        reset: () => { store.reset(); wizard.resetWizard(); },

        setFramework: store.setFramework,
        setPendingFramework: store.setPendingFramework,
        setDatabaseEngine: store.setDatabaseEngine,
        setSelectedFrontendFramework: store.setSelectedFrontendFramework,
        fetchTablesMetadata, fetchTablesMetadataParents, fetchTablesMetadataChilds,
        fetchRelations, fetchAvailableLanguages, testDatabaseConnection,

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
        isCurrentStepSkippable,

        // Exports des fonctions de fetch avec leurs signatures corrigées
        fetchLoggingLevels,
        fetchSecurityTypes,
        fetchCacheProviders,
        fetchLanguageVersions,
        fetchFrameworkVersions,
        fetchBuildTools,
        fetchHibernateDdlAutoOptions,
    };
}
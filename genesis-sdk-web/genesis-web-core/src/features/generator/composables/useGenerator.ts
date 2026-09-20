// genesis-sdk-web/genesis-web-core/src/features/generator/composables/useGenerator.ts
import { inject, computed } from 'vue';
import { storeToRefs } from 'pinia';
import { useGeneratorStore } from '@genesis-labs/web-core/features/generator/store/useGenerator.store';
import { GENERATOR_SERVICE_KEY, type IGeneratorService } from '@genesis-labs/web-core/features/generator/types/generator.service.interface';
import { useGenesisWizard } from '@genesis-labs/web-core/core/composables/ux/useGenesisWizard';

import type { GeneratorData, IDatabaseService } from '@genesis-labs/shared-types';
import { FRONTEND_SERVICE_KEY, IFrontendService } from '../../frontend/manifest';
import { DATABASE_SERVICE_KEY } from '../../database/manifest';

export function useGenerator() {
    const service = inject(GENERATOR_SERVICE_KEY);
    const frontendService = inject(FRONTEND_SERVICE_KEY);
    const databaseService = inject(DATABASE_SERVICE_KEY);

    if (!service) throw new Error('[useGenerator] IGeneratorService non fourni.');
    
    const svc = service as IGeneratorService;
    const fdsvc = frontendService as IFrontendService;
    const dbsvc = databaseService as IDatabaseService;
    const store = useGeneratorStore();

    const SKIPPABLE_CONFIG = { 5: [], 8: [9], 10: [] };

    const wizard = useGenesisWizard({
        totalSteps: 10,
        skippableStepsConfig: SKIPPABLE_CONFIG,
        onBeforeNext: async (currentStep: number) => {
            // gfhghgf
            console.log(`last step : ${currentStep}`);

            // COrriger afin de faire en sorte que le framework dans le storeFramework deviennent celui dans le stepData
            if (currentStep === 1) {
                const fw = store.pendingFramework; // On récupère le brouillon
                
                if (!fw) {
                    console.warn('[Wizard] ⚠️ Aucun framework sélectionné. Navigation bloquée.');
                    return false; // Bloque la navigation
                }

                try {
                    console.log(`[Wizard] 🔄 Commit et sauvegarde du framework: ${fw.name}`);
                    
                    // 1. On affecte officiellement au store du générateur (stepperData)
                    store.setFramework(fw);
                    
                    // 2. On sauvegarde côté API
                    // await svc.selectFramework(fw.id);
                    
                    console.log('[Wizard] ✅ Framework validé et sauvegardé avec succès.');
                } catch (error) {
                    const msg = error instanceof Error ? error.message : 'Erreur inconnue lors de la sélection du framework.';
                    console.error('[Wizard] ❌ Échec:', msg);
                    
                    // ✅ NOUVEAU : On notifie l'interface utilisateur via le store
                    store.setWizardError(msg); 
                    return false; 
                }
            }

            if (currentStep === 2) {
                const config = stepperData.value.config;
                
                // Validation basique des champs obligatoires avant d'appeler l'API
                if (!config.projectName || !config.projectLocation) {
                    console.warn('[Wizard] ⚠️ Nom du projet ou localisation manquants.');
                    // Idéalement, déclenchez ici un toast d'erreur UI
                    return false; // Bloque la navigation
                }

                try {
                    console.log('[Wizard] 🔄 Sauvegarde de la configuration du projet vers l\'API...');
                    await svc.saveProjectConfig(config);
                    console.log('[Wizard] ✅ Configuration du projet sauvegardée avec succès.');
                } catch (error) {
                    console.error('[Wizard] ❌ Échec de la sauvegarde de la configuration:', error);
                    
                    // Optionnel : Déclencher une popup d'erreur globale ici si vous avez un système de toast
                    // ex: store.showError("Impossible de sauvegarder la configuration: " + (error as Error).message);
                    
                    return false; // ❌ Bloque la navigation vers l'étape 3 tant que l'API n'a pas répondu OK
                }
            }


            return true; 
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
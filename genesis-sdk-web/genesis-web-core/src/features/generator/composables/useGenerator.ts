import { inject, computed } from 'vue';
import { storeToRefs } from 'pinia';
import { useGeneratorStore } from '@genesis-labs/web-core/features/generator/store/useGenerator.store';
import { GENERATOR_SERVICE_KEY, type IGeneratorService } from '@genesis-labs/web-core/features/generator/types/generator.service.interface';
import { useGenesisWizard } from '@genesis-labs/web-core/core/composables/ux/useGenesisWizard';

import type { GeneratorData } from '@genesis-labs/shared-types';

export function useGenerator() {
    const service = inject(GENERATOR_SERVICE_KEY);
    if (!service) {
        throw new Error('[useGenerator] IGeneratorService non fourni. Vérifiez app.provide() dans main.ts');
    }
    
    const svc = service as IGeneratorService;
    const store = useGeneratorStore();

    const SKIPPABLE_CONFIG = {
        5: [],      // L'étape 5 peut être skipée (sans dépendance)
        8: [9],     // Si on skip l'étape 8, l'étape 9 est aussi skipée
        10: []      // L'étape 10 peut être skipée (fin du processus)
    };

    // ═══ 1. INITIALISATION DU WIZARD GÉNÉRIQUE ═══
    const wizard = useGenesisWizard({
        totalSteps: 10,
        skippableStepsConfig: SKIPPABLE_CONFIG,
        onBeforeNext: async (currentStep: number) => {
            console.log(`Étape précédente : ${currentStep}`);
            
            // On accède directement à wizard.skippedSteps.value
            const skippedArray = Array.from(wizard.skippedSteps.value);
            console.log(`Étapes ignorées :`, skippedArray.length > 0 ? skippedArray : 'Aucune');            

            return true; 
        }
    });

    // ═══ 2. ÉTAT DU STORE (Réactif) ═══
    const { 
        stepperData, 
        getTablesParents, getTablesChilds, getRelations,
        getAvailableFrontendFrameworks, getAvailableLanguages,
        tables, views, availableTables
    } = storeToRefs(store);

    // ═══ 3. WRAPPERS DE NAVIGATION (Avec logique métier spécifique) ═══
    function handleComplete(): GeneratorData | null {
        console.log('Données finales prêtes pour la génération:', stepperData.value);
        return stepperData.value;
    }

    const isCurrentStepSkippable = computed(() => {
        return Object.keys(SKIPPABLE_CONFIG).map(Number).includes(wizard.currentStep.value);
    });

    async function goToNextStep(): Promise<GeneratorData | null> {
        // Le wizard gère déjà onBeforeNext. S'il retourne true, on avance.
        const didMove = await wizard.goToNextStep();
        
        if (!didMove && wizard.isLastStep.value) {
            return handleComplete(); // On est à la fin et on valide
        }
        return null;
    }

    function goToPreviousStep() {
        wizard.goToPreviousStep();
    }

    function skipCurrentStep() {
        wizard.skipCurrentStep();
    }

    function reset() {
        store.reset();
        wizard.resetWizard();
    }

    // ═══ 4. ACTIONS MÉTIER ASYNCHRONES ═══
    async function fetchTablesMetadata() { store.setAvailableTables(await svc.fetchTablesMetadata()); }
    async function fetchTablesMetadataParents() { store.setTablesParents(await svc.fetchTablesMetadataParents()); }
    async function fetchTablesMetadataChilds() { store.setTablesChilds(await svc.fetchTablesMetadataChilds()); }
    async function fetchRelations() { store.setRelations(await svc.fetchRelations()); }
    async function fetchAvailableLanguages() { store.setAvailableLanguages(await svc.fetchAvailableLanguages()); }

    async function testDatabaseConnection(): Promise<{ success: boolean; message: string }> {
        try {
            const result = await svc.testDatabaseConnection(stepperData.value.database);
            return result;
        } catch (error) {
            console.error('[useGenerator] Erreur lors du test de connexion:', error);
            return { 
                success: false, 
                message: error instanceof Error ? error.message : 'Une erreur inconnue est survenue.' 
            };
        }
    }

    // ═══ 5. RETOUR FINAL ═══
    return {
        // État du Wizard
        currentStep: wizard.currentStep,
        totalSteps: wizard.totalSteps,
        isFirstStep: wizard.isFirstStep,
        isLastStep: wizard.isLastStep,
        skippedSteps: wizard.skippedSteps,

        // État des Données
        stepperData,
        getTablesParents, getTablesChilds, getRelations,
        availableFrontendFrameworks: getAvailableFrontendFrameworks, 
        availableLanguages: getAvailableLanguages,
        tables, views, availableTables,

        // Navigation
        goToNextStep,
        goToPreviousStep,
        skipCurrentStep,
        reset,

        // Sélections & Actions Métier
        setFramework: store.setFramework,
        setDatabaseEngine: store.setDatabaseEngine,
        setSelectedFrontendFramework: store.setSelectedFrontendFramework,
        fetchTablesMetadata, fetchTablesMetadataParents, fetchTablesMetadataChilds,
        fetchRelations, fetchAvailableLanguages,
        testDatabaseConnection,

        // Mutations directes
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
    };
}
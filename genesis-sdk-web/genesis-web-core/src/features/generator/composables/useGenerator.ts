import { inject } from 'vue';
import { storeToRefs } from 'pinia';
import { useGeneratorStore } from '../store/useGenerator.store';
import { GENERATOR_SERVICE_KEY, type IGeneratorService } from '../types/generator.service.interface';
import type { Framework } from '../../frameworks/types/framework.types';
import type { GeneratorData } from '@/features/generator/types/generator.types';
import type { FrontendFramework  } from '@/features/frontend/types/frontend.types'

export function useGenerator() {
    const service = inject(GENERATOR_SERVICE_KEY);
    if (!service) {
        throw new Error('[useGenerator] IGeneratorService non fourni. Vérifiez app.provide() dans main.ts');
    }
    
    const svc = service as IGeneratorService;
    const store = useGeneratorStore();
    
    // ✅ CORRECTION : On inclut les getters dans storeToRefs pour garder la réactivité
    const { 
        currentStep, totalSteps, stepperData, isFirstStep, isLastStep, 
        getTablesParents, getTablesChilds, getRelations,
        getAvailableFrontendFrameworks,
        getAvailableLanguages,
        tables,
        views,
        availableTables
    } = storeToRefs(store);

    // ═══════════════════════════════════════════════════════════
    // NAVIGATION & VALIDATION
    // ═══════════════════════════════════════════════════════════
    function validateCurrentStep(): boolean {
        // ... (ton code de validation existant)
        return true; 
    }

    function handleComplete(): GeneratorData {
        console.log('Données finales prêtes pour la génération:', stepperData.value);
        return stepperData.value;
    }

    function goToNextStep(): GeneratorData | null {
        if (!validateCurrentStep()) return null;
        if (!isLastStep.value) {
            store.goToNextStep();
            return null;
        } else {
            return handleComplete();
        }
    }

    function goToPreviousStep() {
        store.goToPreviousStep();
    }

    // ═══════════════════════════════════════════════════════════
    // ACTIONS MÉTIER ASYNCHRONES
    // ═══════════════════════════════════════════════════════════
    async function fetchTablesMetadata() {
        const data = await svc.fetchTablesMetadata();
        store.setAvailableTables(data);
    }


    async function fetchTablesMetadataParents() {
        const data = await svc.fetchTablesMetadataParents();
        store.setTablesParents(data);
    }

    async function fetchTablesMetadataChilds() {
        const data = await svc.fetchTablesMetadataChilds();
        store.setTablesChilds(data);
    }

    async function fetchRelations() {
        const data = await svc.fetchRelations();
        store.setRelations(data);
    }

    async function fetchAvailableLanguages() {
        const data = await svc.fetchAvailableLanguages();
        store.setAvailableLanguages(data);
    }

    // ═══════════════════════════════════════════════════════════
    // MUTATIONS & SÉLECTIONS
    // ═══════════════════════════════════════════════════════════
    function handleFrameworkSelect(framework: Framework) {
        store.setFramework(framework);
        // goToNextStep();
    }

    function setSelectedFrontendFramework(framework: FrontendFramework | null) {
        store.setSelectedFrontendFramework(framework);
        // goToNextStep();
    }

    function reset() {
        store.reset();
    }

    // ═══════════════════════════════════════════════════════════
    // RETOUR FINAL
    // ═══════════════════════════════════════════════════════════
    return {
        // État
        currentStep,
        totalSteps,
        isFirstStep,
        isLastStep,
        stepperData,

        // ✅ Getters (maintenant correctement réactifs grâce à storeToRefs)
        getTablesParents,
        getTablesChilds,
        getRelations,
        availableFrontendFrameworks: getAvailableFrontendFrameworks, 
        availableLanguages: getAvailableLanguages,

        tables,
        views,
        availableTables,

        // Navigation
        goToNextStep,
        goToPreviousStep,
        reset,

        // Sélections & Actions Métier
        handleFrameworkSelect,
        setSelectedFrontendFramework,
        setFramework: store.setFramework,
        fetchTablesMetadataParents,
        fetchTablesMetadataChilds,
        fetchRelations,
        fetchAvailableLanguages,

        fetchTablesMetadata,

        // Mutations directes du store
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
    };
}
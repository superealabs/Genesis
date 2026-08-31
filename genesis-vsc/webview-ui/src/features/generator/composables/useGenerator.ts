// webview-ui/src/features/generator/composables/useGenerator.ts
import { storeToRefs } from 'pinia';
import { useGeneratorStore } from '../store/useGenerator.store';
import { generatorService } from '../services/generator.service';
import type { Framework } from '@/features/frameworks/types/framework.types';
import type { ScriptConfig, ComponentType, GeneratorData, FrontendFramework } from '../types/generator.types';

export function useGenerator() {
    const store = useGeneratorStore();
    const { currentStep, totalSteps, stepperData, isFirstStep, isLastStep, getTablesChilds, getTablesParents, getRelations, availableFrontendFrameworks } = storeToRefs(store);

    generatorService.init();

    function validateCurrentStep(): boolean {
        switch (currentStep.value) {
            case 1:
                // if (!stepperData.value.framework) {
                //     console.warn('Veuillez sélectionner un framework');
                //     return false;
                // }
                return true;

            case 2:
                // if (!stepperData.value.config.projectName.trim()) {
                //     console.warn('Le nom du projet est obligatoire');
                //     return false;
                // }
                // if (!stepperData.value.config.languageVersion) {
                //     console.warn('Veuillez sélectionner une version du language');
                //     return false;
                // }
                return true;

            case 3:
                // if (!stepperData.value.database.databaseName.trim()) {
                //     console.warn('Le nom de la base de données est obligatoire');
                //     return false;
                // }
                // if (!stepperData.value.database.username.trim()) {
                //     console.warn('Le nom d\'utilisateur est obligatoire');
                //     return false;
                // }
                return true;

            case 4:
                return true;

            case 5:
                // const { selectedTables, selectedComponents } = stepperData.value.tableSelection;
                // if (selectedTables.length === 0) {
                //     console.warn('Veuillez sélectionner au moins une table.');
                //     return false;
                // }
                // if (selectedComponents.length === 0) {
                //     console.warn('Veuillez sélectionner au moins un type de composant.');
                //     return false;
                // }
                return true;

            case 6:

                return true;

            case 7:

                return true;

            case 8:

                return true;

            case 9:

                return true;

            default:
                return true;
        }
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

    function handleFrameworkSelect(framework: Framework) {
        store.setFramework(framework);
    }

    function updateConfig(key: any, value: any) {
        store.updateConfig(key, value);
    }

    function updateDatabase(key: any, value: any) {
        store.updateDatabase(key, value);
    }

    function updateScript(key: keyof ScriptConfig, value: string) {
        store.updateScript(key, value);
    }

    function selectScriptPath() {
        generatorService.requestScriptPath();
    }

    function setSelectedFrontendFramework(framework: FrontendFramework | null) {
        store.setSelectedFrontendFramework(framework);
    }

    function toggleTable(tableName: string) {
        store.toggleTable(tableName);
    }

    function toggleView(viewName: string) {
        store.toggleView(viewName);
    }

    function toggleComponent(component: ComponentType) {
        store.toggleComponent(component);
    }

    function handleComplete(): GeneratorData {
        console.log('Données finales prêtes pour la génération:', stepperData.value);
        return stepperData.value;
    }

    function reset() {
        store.reset();
    }

    function selectFolderPath() {
        generatorService.requestFolderPath();
    }

    function fetchTablesMetadata() {
        generatorService.fetchTablesMetadata();
    }

    function fetchTablesMetadataParents() {
        generatorService.fetchTablesMetadataParents();
    }

    function fetchTablesMetadataChilds() {
        generatorService.fetchTablesMetadataChilds();
    }

    function fetchRelations() {
        generatorService.fetchRelations();
    }

    return {
        currentStep,
        totalSteps,
        isFirstStep,
        isLastStep,
        stepperData,

        getTablesParents,
        getTablesChilds,
        getRelations,
        addRelation: store.addRelation,
        removeRelation: store.removeRelation,

        availableFrontendFrameworks,
        setSelectedFrontendFramework,

        goToNextStep,
        goToPreviousStep,
        handleFrameworkSelect,
        updateConfig,
        updateDatabase,
        updateScript,
        handleComplete,
        reset,
        selectFolderPath,
        selectScriptPath,
        fetchTablesMetadata,
        fetchRelations,
        fetchTablesMetadataParents,
        fetchTablesMetadataChilds,
        toggleTable,
        toggleView,
        toggleComponent,
    };
}
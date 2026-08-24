import { storeToRefs } from 'pinia';
import { useGeneratorStore } from '../store/useGenerator.store';
import { generatorService } from '../services/generator.service';
import type { Framework } from '@/features/frameworks/types/framework.types';

export function useGenerator() {
    const store = useGeneratorStore();
    const { currentStep, totalSteps, stepperData, isFirstStep, isLastStep } = storeToRefs(store);

    generatorService.init();

    function validateCurrentStep(): boolean {
        console.log(`current step : ${currentStep}`);
        switch (currentStep.value) {
            case 1:
                if (!stepperData.value.framework) {
                    console.error('Veuillez sélectionner un framework');
                    return false;
                }
                return true;

            case 2:
                // Validation de l'étape 2 : Le nom du projet est obligatoire
                if (!stepperData.value.config.projectName.trim()) {
                    console.error('Le nom du projet est obligatoire');
                    return false;
                }
                if (!stepperData.value.config.languageVersion) {
                    console.error('Veuillez sélectionner une version du language');
                    return false;
                }
                return true;

            case 3:
                return true;

            default:
                return true;
        }
    }

    function goToNextStep() {
        console.log("Bonjour 1")
        if (!validateCurrentStep()) return;
        console.log("Bonjour 2")

        if (!isLastStep.value) {
            store.goToNextStep();
        } else {
            handleComplete();
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

    function handleComplete() {
        console.log('Données finales prêtes pour la génération:', stepperData.value);
        // Ici, plus tard, on appellera le generator.service.ts
    }

    function reset() {
        store.reset();
    }

    function selectFolderPath() {
        generatorService.requestFolderPath();
    }

    return {
        currentStep,
        totalSteps,
        isFirstStep,
        isLastStep,
        stepperData,
        goToNextStep,
        goToPreviousStep,
        handleFrameworkSelect,
        updateConfig,
        handleComplete,
        reset,
        selectFolderPath
    };
}
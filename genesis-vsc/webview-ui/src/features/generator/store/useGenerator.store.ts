import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import type { Framework } from '@/features/frameworks/types/framework.types';
import type { GeneratorData, ProjectConfig } from '../types/generator.types';

export const useGeneratorStore = defineStore('generator', () => {
    // ═══ État ═══
    const currentStep = ref(1);
    const totalSteps = 3;
    
    const stepperData = ref<GeneratorData>({
        framework: null,
        config: {
            projectName: '',
            projectLocation: '/home/user/projects/',
            languageVersion: '',
            buildTool: 'maven',
            groupId: 'com.example',
            frameworkVersion: ''
        }
    });

    // ═══ Getters ═══
    const isFirstStep = computed(() => currentStep.value === 1);
    const isLastStep = computed(() => currentStep.value === totalSteps);

    // ═══ Actions ═══
    function setFramework(framework: Framework) {
        stepperData.value.framework = framework;
        // Pré-remplissage intelligent basé sur le framework (mock)
        if (framework.coreFramework === 'Spring') {
            stepperData.value.config.buildTool = 'maven';
            stepperData.value.config.languageVersion = '17';
        } else if (framework.coreFramework === 'Express') {
            stepperData.value.config.buildTool = 'npm';
            stepperData.value.config.languageVersion = '20';
        }
    }

    function updateConfig(key: keyof ProjectConfig, value: any) {
        stepperData.value.config[key] = value;
    }

    function goToNextStep() {
        if (currentStep.value < totalSteps) {
            currentStep.value++;
        }
    }

    function goToPreviousStep() {
        if (currentStep.value > 1) {
            currentStep.value--;
        }
    }

    function reset() {
        currentStep.value = 1;
        stepperData.value = {
            framework: null,
            config: {
                projectName: '',
                projectLocation: '/home/user/projects/',
                languageVersion: '',
                buildTool: 'maven',
                groupId: 'com.example',
                frameworkVersion: ''
            }
        };
    }

    return {
        currentStep,
        totalSteps,
        stepperData,
        isFirstStep,
        isLastStep,
        setFramework,
        updateConfig,
        goToNextStep,
        goToPreviousStep,
        reset
    };
});
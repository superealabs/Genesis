import { ref } from 'vue';
import type { GeneratorForm } from '../types/generator.types';

export function useGenerator() {
    const currentStep = ref(1);
    const totalSteps = 4;

    const form = ref<GeneratorForm>({
        language: null,
        framework: null,
        database: null,
        databaseConfig: {
            host: 'localhost',
            port: '5432',
            name: '',
            username: '',
            password: ''
        },
        frontend: null
    });

    function next() {
        if (currentStep.value < totalSteps) currentStep.value++;
    }

    function previous() {
        if (currentStep.value > 1) currentStep.value--;
    }

    function reset() {
        currentStep.value = 1;
        form.value = {
            language: null,
            framework: null,
            database: null,
            databaseConfig: {
                host: 'localhost',
                port: '5432',
                name: '',
                username: '',
                password: ''
            },
            frontend: null
        };
    }

    return { currentStep, totalSteps, form, next, previous, reset };
}
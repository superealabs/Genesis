import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import type { Framework } from '@/features/frameworks/types/framework.types';
import type { GeneratorData, ProjectConfig, DatabaseConfig, ScriptConfig, ComponentType, TableMetadataDto } from '../types/generator.types';

export const useGeneratorStore = defineStore('generator', () => {
    // ═══ État ═══
    const currentStep = ref(1);
    const totalSteps = 7;
    const availableTables = ref<TableMetadataDto[]>([]);
    const availableViews = ref<TableMetadataDto[]>([]);

    function setAvailableTables(tables: TableMetadataDto[]) {
        availableTables.value = tables;
    }

    function setAvailableViews(views: TableMetadataDto[]) {
        availableViews.value = views;
    }

    
    const stepperData = ref<GeneratorData>({
        framework: null,
        config: {
            projectName: '',
            projectLocation: '/home/user/projects/',
            languageVersion: '',
            buildTool: 'maven',
            groupId: 'com.example',
            frameworkVersion: ''
        },
        database: { // <-- AJOUT : État initial
            engine: 'postgre',
            host: 'localhost',
            port: 5432,
            databaseName: '',
            schema: 'public',
            username: '',
            password: '',
            driverType: 'org.postgresql.Driver',
            driverName: 'PostgreSQL JDBC Driver',
            sid: '',
            trustCertificate: false,
            allowPublicKeyRetrieval: false,
        },
        script: {
            path: '',
            content: '',
        },
        tableSelection: {
            selectedTables: [],
            selectedViews: [],
            selectedComponents: [],
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

    function updateConfig<K extends keyof ProjectConfig>(key: K, value: ProjectConfig[K]) {
        (stepperData.value.config as any)[key] = value;
    }

    function updateDatabase<K extends keyof DatabaseConfig>(key: K, value: DatabaseConfig[K]) {
        (stepperData.value.database as any)[key] = value;
    }

    function updateScript<K extends keyof ScriptConfig>(key: K, value: ScriptConfig[K]) {
        (stepperData.value.script as any)[key] = value;
    }

    function toggleTable(tableName: string) {
        const list = stepperData.value.tableSelection.selectedTables;
        const idx = list.indexOf(tableName);
        idx === -1 ? list.push(tableName) : list.splice(idx, 1);
    }

    function toggleView(viewName: string) {
        const list = stepperData.value.tableSelection.selectedViews;
        const idx = list.indexOf(viewName);
        idx === -1 ? list.push(viewName) : list.splice(idx, 1);
    }

    function toggleComponent(component: ComponentType) {
        const list = stepperData.value.tableSelection.selectedComponents;
        const idx = list.indexOf(component);
        idx === -1 ? list.push(component) : list.splice(idx, 1);
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
            },
            database: {
                engine: 'postgre',
                host: 'localhost',
                port: 5432,
                databaseName: '',
                schema: 'public',
                username: '',
                password: '',
                driverType: 'org.postgresql.Driver',
                driverName: 'PostgreSQL JDBC Driver',
                sid: '',
                trustCertificate: false,
                allowPublicKeyRetrieval: false,                
            },
            script: {
                path: '',
                content: '',
            },
            tableSelection: {
                selectedTables: [],
                selectedViews: [],
                selectedComponents: [],
            }
        };
    }

    return {
        currentStep,
        totalSteps,
        stepperData,
        isFirstStep,
        isLastStep,
        availableTables,
        availableViews,
        setAvailableTables,
        setAvailableViews,
        setFramework,
        updateConfig,
        updateDatabase,
        updateScript,
        toggleTable,
        toggleComponent,
        toggleView,
        goToNextStep,
        goToPreviousStep,
        reset
    };
});
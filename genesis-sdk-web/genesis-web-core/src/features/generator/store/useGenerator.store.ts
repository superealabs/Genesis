import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import type { Framework } from '../../frameworks/types/framework.types';
import type { FrontendFramework } from '../../frontend/types/frontend.types';
import type { 
    GeneratorData, ProjectConfig, DatabaseConfig, ScriptConfig, 
    ComponentType, TableMetadataDto, RelationParameter, LanguageDto, 
    FrontendLayoutConfig, GitConfiguration 
} from '../types/generator.types';

import { INITIAL_STATE } from './generator.initial-state'

export const useGeneratorStore = defineStore('generator', () => {
    // ═══ État ═══
    const currentStep = ref(1);
    const totalSteps = 9;
    const isGenerating = ref(false);
    
    // Données externes
    const availableTables = ref<TableMetadataDto[]>([]);
    const tablesParents = ref<TableMetadataDto[]>([]);
    const tablesChilds = ref<TableMetadataDto[]>([]);
    const relations = ref<RelationParameter[]>([]);
    const availableFrontendFrameworks = ref<FrontendFramework[]>([]);
    const availableLanguages = ref<LanguageDto[]>([]);

    // État principal du wizard
    const stepperData = ref<GeneratorData>(structuredClone(INITIAL_STATE));

    // ═══ Getters (État calculé) ═══
    const isFirstStep = computed(() => currentStep.value === 1);
    const isLastStep = computed(() => currentStep.value === totalSteps);

    // Getters pour les données externes
    const getTablesParents = computed(() => tablesParents.value);
    const getTablesChilds = computed(() => tablesChilds.value);
    const getRelations = computed(() => relations.value);
    const getAvailableFrontendFrameworks = computed(() => availableFrontendFrameworks.value);
    const getAvailableLanguages = computed(() => availableLanguages.value);

    // NOUVEAUX COMPUTED : Séparation logique à partir de la liste unifiée
    const tables = computed(() => availableTables.value.filter(t => !t.isView));
    const views = computed(() => availableTables.value.filter(t => t.isView));
    
    // Alias pour compatibilité (retourne la liste unifiée)
    const getAvailableTables = computed(() => availableTables.value);
    const getAvailableViews = computed(() => views.value); 

    // ═══ Actions de Navigation ═══
    function goToNextStep() { 
        if (currentStep.value < totalSteps) currentStep.value++;
        console.log(`CurrentStep : ${currentStep.value}`)
    }
    
    function goToPreviousStep() { 
        if (currentStep.value > 1) currentStep.value--; 
    }

    // ═══ Actions de Mutation ═══
    function setFramework(framework: Framework) {
        stepperData.value.framework = framework;
        if (framework.coreFramework === 'Spring') {
            stepperData.value.config.buildTool = 'maven';
            stepperData.value.config.languageVersion = '17';
        } else if (framework.coreFramework === 'Express') {
            stepperData.value.config.buildTool = 'npm';
            stepperData.value.config.languageVersion = '20';
        }
    }


    function setSelectedFrontendFramework(framework: FrontendFramework | null) {
        stepperData.value.frontend = framework;
        if (framework) stepperData.value.frontendLayout.port = framework.defaultPort;
    }

    function setAvailableTables(data: TableMetadataDto[]) {
        availableTables.value = data;
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

    function updateFrontendLayout<K extends keyof FrontendLayoutConfig>(key: K, value: FrontendLayoutConfig[K]) {
        (stepperData.value.frontendLayout as any)[key] = value;
    }

    function updateGitConfig<K extends keyof GitConfiguration>(key: K, value: GitConfiguration[K]) {
        (stepperData.value.git as any)[key] = value;
        if (key === 'useGit' && value === false) {
            stepperData.value.git = { 
                useGit: false, separateRepositories: false, useRemoteRepo: false, 
                isNewRemoteRepo: true, repositoryName: '', backendRepositoryName: '',
                frontendRepositoryName: '', githubUsername: '', githubToken: '' 
            };
        }
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

    function toggleLanguage(code: string) {
        const list = stepperData.value.frontendLayout.selectedLanguages;
        const idx = list.indexOf(code);
        idx === -1 ? list.push(code) : list.splice(idx, 1);
    }

    function addRelation(relation: RelationParameter): boolean {
        const exists = relations.value.some(r => 
            r.parentTable === relation.parentTable && 
            r.childTable === relation.childTable
        );
        if (exists) return false;
        relations.value.push(relation);
        return true;
    }

    function removeRelation(index: number) {
        relations.value.splice(index, 1);
    }


    function setTablesParents(data: TableMetadataDto[]) { 
        tablesParents.value = data; 
    }
    
    function setTablesChilds(data: TableMetadataDto[]) { 
        tablesChilds.value = data; 
    }
    
    function setRelations(data: RelationParameter[]) { 
        relations.value = data; 
    }
    
    function setAvailableLanguages(data: LanguageDto[]) { 
        availableLanguages.value = data; 
    }

    function setIsGenerating(value: boolean) {
        isGenerating.value = value;
    }

    function reset() {
        currentStep.value = 1;
        isGenerating.value = false;
        stepperData.value = structuredClone(INITIAL_STATE); // ← pas de mutation accidentelle
    }

    return {
        // État brut
        currentStep, 
        totalSteps, 
        isGenerating, 
        stepperData,
        
        availableTables,

        tables,
        views,

        // Getters calculés
        isFirstStep, 
        isLastStep,
        getTablesParents,
        getTablesChilds,
        getRelations,
        getAvailableTables,
        getAvailableViews,
        getAvailableFrontendFrameworks,
        getAvailableLanguages,
        
        // Navigation
        goToNextStep, 
        goToPreviousStep, 
        
        // Mutations
        setFramework, 
        setSelectedFrontendFramework,
        setAvailableTables,
        updateConfig, 
        updateDatabase, 
        updateScript, 
        updateFrontendLayout, 
        updateGitConfig,
        toggleTable, 
        toggleView, 
        toggleComponent, 
        toggleLanguage,
        addRelation, 
        removeRelation, 
        
        // Setters pour données externes
        setTablesParents, 
        setTablesChilds, 
        setRelations, 
        setAvailableLanguages,
        setIsGenerating,
        
        reset
    };
});
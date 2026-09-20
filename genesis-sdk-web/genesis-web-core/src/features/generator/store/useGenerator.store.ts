// genesis-sdk-web/genesis-web-core/src/features/generator/store/useGenerator.store.ts
import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import type { FrontendFramework, Framework, InterfaceLanguage } from '@genesis-labs/shared-types';
import type { 
    GeneratorData, ProjectConfig, DatabaseConfig, ScriptConfig, 
    ComponentType, TableMetadataDto, RelationParameter,  
    FrontendLayoutConfig, GitConfiguration, 
    DatabaseEngineDto
} from '@genesis-labs/shared-types';

import { INITIAL_STATE } from './generator.initial-state';

export const useGeneratorStore = defineStore('generator', () => {


    const pendingFramework = ref<Framework | null>(null);

    // ═══ État des Données Uniquement ═══
    const isGenerating = ref(false);
    const wizardError = ref<string | null>(null);
    
    const availableTables = ref<TableMetadataDto[]>([]);
    const tablesParents = ref<TableMetadataDto[]>([]);
    const tablesChilds = ref<TableMetadataDto[]>([]);
    const relations = ref<RelationParameter[]>([]);
    const availableFrontendFrameworks = ref<FrontendFramework[]>([]);
    const availableLanguages = ref<InterfaceLanguage[]>([]);

    //  CORRECTION : Types simples (string[])
    const availableLoggingLevels = ref<string[]>([]);
    const availableSecurityTypes = ref<string[]>([]);
    const availableCacheProviders = ref<string[]>([]);
    const availableHibernateDdlAutoOptions = ref<string[]>([]);

    const availableLanguageVersions = ref<string[]>([]);
    const availableFrameworkVersions = ref<string[]>([]);
    const availableBuildTools = ref<string[]>([]);

    const stepperData = ref<GeneratorData>(structuredClone(INITIAL_STATE));

    // ═══ Getters ═══
    const getTablesParents = computed(() => tablesParents.value);
    const getTablesChilds = computed(() => tablesChilds.value);
    const getRelations = computed(() => relations.value);
    const getAvailableFrontendFrameworks = computed(() => availableFrontendFrameworks.value);
    const getAvailableLanguages = computed(() => availableLanguages.value);
    
    const getAvailableLoggingLevels = computed(() => availableLoggingLevels.value);
    const getAvailableSecurityTypes = computed(() => availableSecurityTypes.value);
    const getAvailableCacheProviders = computed(() => availableCacheProviders.value);
    const getAvailableHibernateDdlAutoOptions = computed(() => availableHibernateDdlAutoOptions.value);


    const getAvailableLanguageVersions = computed(() => availableLanguageVersions.value);
    const getAvailableFrameworkVersions = computed(() => availableFrameworkVersions.value);
    const getAvailableBuildTools = computed(() => availableBuildTools.value);

    const tables = computed(() => availableTables.value.filter(t => !t.isView));
    const views = computed(() => availableTables.value.filter(t => t.isView));
    const getAvailableTables = computed(() => availableTables.value);
    const getAvailableViews = computed(() => views.value); 


    function setWizardError(message: string) {
        wizardError.value = message;
    }
    function clearWizardError() {
        wizardError.value = null;
    }

    function setPendingFramework(framework: Framework | null) {
        pendingFramework.value = framework;
    }

    // ═══ Actions de Mutation des Données ═══
    function setFramework(framework: Framework) {
        console.log("💾 [Store] setFramework appelé avec :", framework); // <-- AJOUTEZ CECI
        
        stepperData.value.framework = framework;
        if (framework?.coreFramework === 'Spring') {
            stepperData.value.config.buildTool = 'maven';
            stepperData.value.config.languageVersion = '17';
        } else if (framework?.coreFramework === 'Express') {
            stepperData.value.config.buildTool = 'npm';
            stepperData.value.config.languageVersion = '20';
        }
    }

    function setDatabaseEngine(engine: DatabaseEngineDto) {
        let engineKey = engine.name.toLowerCase().replace(' ', '');
        if (engineKey === 'postgresql') engineKey = 'postgre';
        if (engineKey === 'sqlserver') engineKey = 'sqlserver';

        stepperData.value.database.engine = engineKey as DatabaseConfig['engine'];
        stepperData.value.database.port = Number(engine.port);
        stepperData.value.database.driverName = engine.driverName;
        stepperData.value.database.driverType = engine.driverType || 'jdbc';
        
        if (engine.sid) {
            stepperData.value.database.sid = engine.sid;
        }
    }

    function setSelectedFrontendFramework(framework: FrontendFramework | null) {
        stepperData.value.frontend = framework;
        if (framework) stepperData.value.frontendLayout.port = framework.defaultPort;
    }

    function setAvailableLoggingLevels(data: string[]) { availableLoggingLevels.value = data; }
    function setAvailableSecurityTypes(data: string[]) { availableSecurityTypes.value = data; }
    function setAvailableCacheProviders(data: string[]) { availableCacheProviders.value = data; } // ✅ Ajouté

    function setAvailableTables(data: TableMetadataDto[]) { availableTables.value = data; }
    function updateConfig<K extends keyof ProjectConfig>(key: K, value: ProjectConfig[K]) { 
        (stepperData.value.config as any)[key] = value; 
    }
    function updateDatabase<K extends keyof DatabaseConfig>(key: K, value: DatabaseConfig[K]) { (stepperData.value.database as any)[key] = value; }
    function updateScript<K extends keyof ScriptConfig>(key: K, value: ScriptConfig[K]) { (stepperData.value.script as any)[key] = value; }
    function updateFrontendLayout<K extends keyof FrontendLayoutConfig>(key: K, value: FrontendLayoutConfig[K]) { (stepperData.value.frontendLayout as any)[key] = value; }
    
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
        const list = stepperData.value.frontendLayout.selectedInterfaceLanguages;
        const idx = list.indexOf(code);
        idx === -1 ? list.push(code) : list.splice(idx, 1);
    }
    function addRelation(relation: RelationParameter): boolean {
        const exists = relations.value.some(r => r.parentTable === relation.parentTable && r.childTable === relation.childTable);
        if (exists) return false;
        relations.value.push(relation);
        return true;
    }


    function removeRelation(index: number) { relations.value.splice(index, 1); }
    
    function setTablesParents(data: TableMetadataDto[]) { tablesParents.value = data; }
    function setTablesChilds(data: TableMetadataDto[]) { tablesChilds.value = data; }
    function setRelations(data: RelationParameter[]) { relations.value = data; }
    function setAvailableLanguages(data: InterfaceLanguage[]) { availableLanguages.value = data; }
    function setIsGenerating(value: boolean) { isGenerating.value = value; }

    function setAvailableLanguageVersions(data: string[]) { availableLanguageVersions.value = data; }
    function setAvailableFrameworkVersions(data: string[]) { availableFrameworkVersions.value = data; }
    function setAvailableBuildTools(data: string[]) { availableBuildTools.value = data; }

    function setAvailableHibernateDdlAutoOptions(data: string[]) { 
        availableHibernateDdlAutoOptions.value = data; 
    }

    function reset() {
        isGenerating.value = false;
        stepperData.value = structuredClone(INITIAL_STATE);
    }

    return {
        wizardError,
        isGenerating, stepperData,
        availableTables, tables, views,
        getTablesParents, getTablesChilds, getRelations,
        getAvailableTables, getAvailableViews, getAvailableFrontendFrameworks, getAvailableLanguages,
        getAvailableLoggingLevels, getAvailableSecurityTypes, getAvailableCacheProviders,
        getAvailableLanguageVersions, getAvailableFrameworkVersions, getAvailableBuildTools,
        getAvailableHibernateDdlAutoOptions,
        pendingFramework,
        

        setWizardError,
        clearWizardError,
        setPendingFramework,
        setDatabaseEngine, setFramework, setSelectedFrontendFramework, setAvailableTables,
        updateConfig, updateDatabase, updateScript, updateFrontendLayout, updateGitConfig,
        toggleTable, toggleView, toggleComponent, toggleLanguage, addRelation, removeRelation,
        setTablesParents, setTablesChilds, setRelations, setAvailableLanguages, setIsGenerating,
        setAvailableLoggingLevels, setAvailableSecurityTypes, setAvailableCacheProviders,
        setAvailableLanguageVersions, setAvailableFrameworkVersions, setAvailableBuildTools,
        setAvailableHibernateDdlAutoOptions,
        reset
    };
});
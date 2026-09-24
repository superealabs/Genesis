et ce système, on va l'appliquer pour tous les autres aussi,
afin de rester cohérent, ok ?

on commence par la base de donnée :
genesis-sdk-web\genesis-web-core\src\features\generator\store\useGenerator.store.ts
// genesis-sdk-web/genesis-web-core/src/features/generator/store/useGenerator.store.ts
import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import type { FrontendFramework, Framework } from '@genesis-labs/shared-types';
import type { 
    GeneratorData, ProjectConfig, DatabaseConfig, ScriptConfig, 
    ComponentType, TableMetadataDto, RelationParameter,  
    FrontendLayoutConfig, GitConfiguration, 
    DatabaseEngineDto
} from '@genesis-labs/shared-types';

import { INITIAL_STATE } from './generator.initial-state';

export const useGeneratorStore = defineStore('generator', () => {

    // ═══ ÉTAT GLOBAL & UTILITAIRE ═══
    const isGenerating = ref(false);
    const wizardError = ref<string | null>(null);
    const stepperData = ref<GeneratorData>(structuredClone(INITIAL_STATE));

    // ═══ ÉTAPE 1 : FRAMEWORK ═══
    // hdqshdqsd
    // dsqdsqdqsjbjkbkjb
    const pendingFramework = ref<Framework | null>(null);

    // ═══ ÉTAPE 2 : CONFIGURATION PROJET (Listes dynamiques spécifiques au générateur) ═══
    const availableLoggingLevels = ref<string[]>([]);
    const availableSecurityTypes = ref<string[]>([]);
    const availableCacheProviders = ref<string[]>([]);
    const availableHibernateDdlAutoOptions = ref<string[]>([]);
    const availableLanguageVersions = ref<string[]>([]);
    const availableFrameworkVersions = ref<string[]>([]);
    const availableBuildTools = ref<string[]>([]);

    // ═══ ÉTAPE 6 : TABLES, VUES & COMPOSANTS ═══
    const availableTables = ref<TableMetadataDto[]>([]);
    const tablesParents = ref<TableMetadataDto[]>([]);
    const tablesChilds = ref<TableMetadataDto[]>([]);

    // ═══ ÉTAPE 7 : RELATIONS ═══
    const relations = ref<RelationParameter[]>([]);


    // ═══════════════════════════════════════════════════════════
    // ═══ GETTERS (Computeds) ═══
    // ═══════════════════════════════════════════════════════════

    // Étape 6 : Tables & Vues
    const getAvailableTables = computed(() => availableTables.value);
    const tables = computed(() => availableTables.value.filter(t => !t.isView));
    const views = computed(() => availableTables.value.filter(t => t.isView));
    const getAvailableViews = computed(() => views.value);
    const getTablesParents = computed(() => tablesParents.value);
    const getTablesChilds = computed(() => tablesChilds.value);

    // Étape 7 : Relations
    const getRelations = computed(() => relations.value);

    // Étape 2 : Config Projet
    const getAvailableLoggingLevels = computed(() => availableLoggingLevels.value);
    const getAvailableSecurityTypes = computed(() => availableSecurityTypes.value);
    const getAvailableCacheProviders = computed(() => availableCacheProviders.value);
    const getAvailableHibernateDdlAutoOptions = computed(() => availableHibernateDdlAutoOptions.value);
    const getAvailableLanguageVersions = computed(() => availableLanguageVersions.value);
    const getAvailableFrameworkVersions = computed(() => availableFrameworkVersions.value);
    const getAvailableBuildTools = computed(() => availableBuildTools.value);


    // ═══════════════════════════════════════════════════════════
    // ═══ ACTIONS ═══
    // ═══════════════════════════════════════════════════════════

    // ── Global & Utilitaire ──
    function setWizardError(message: string) { wizardError.value = message; }
    function clearWizardError() { wizardError.value = null; }
    function setIsGenerating(value: boolean) { isGenerating.value = value; }
    function reset() {
        isGenerating.value = false;
        stepperData.value = structuredClone(INITIAL_STATE);
    }

    // ── Étape 1 : Framework ──
    function setPendingFramework(framework: Framework | null) {
        pendingFramework.value = framework;
    }
    function setFramework(framework: Framework) {
        console.log("💾 [Store] setFramework appelé avec :", framework);
        stepperData.value.framework = framework;
        if (framework?.coreFramework === 'Spring') {
            stepperData.value.config.buildTool = 'maven';
            stepperData.value.config.languageVersion = '17';
        } else if (framework?.coreFramework === 'Express') {
            stepperData.value.config.buildTool = 'npm';
            stepperData.value.config.languageVersion = '20';
        }
    }

    // ── Étape 2 : Configuration Projet ──
    function updateConfig<K extends keyof ProjectConfig>(key: K, value: ProjectConfig[K]) { 
        (stepperData.value.config as any)[key] = value; 
    }
    function setAvailableLoggingLevels(data: string[]) { availableLoggingLevels.value = data; }
    function setAvailableSecurityTypes(data: string[]) { availableSecurityTypes.value = data; }
    function setAvailableCacheProviders(data: string[]) { availableCacheProviders.value = data; }
    function setAvailableLanguageVersions(data: string[]) { availableLanguageVersions.value = data; }
    function setAvailableFrameworkVersions(data: string[]) { availableFrameworkVersions.value = data; }
    function setAvailableBuildTools(data: string[]) { availableBuildTools.value = data; }
    function setAvailableHibernateDdlAutoOptions(data: string[]) { availableHibernateDdlAutoOptions.value = data; }

    // ── Étape 3 & 4 : Base de Données ──
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
    function updateDatabase<K extends keyof DatabaseConfig>(key: K, value: DatabaseConfig[K]) { 
        (stepperData.value.database as any)[key] = value; 
    }

    // ── Étape 5 : Script / IA ──
    function updateScript<K extends keyof ScriptConfig>(key: K, value: ScriptConfig[K]) { 
        (stepperData.value.script as any)[key] = value; 
    }

    // ── Étape 6 : Tables, Vues & Composants ──
    function setAvailableTables(data: TableMetadataDto[]) { availableTables.value = data; }
    function setTablesParents(data: TableMetadataDto[]) { tablesParents.value = data; }
    function setTablesChilds(data: TableMetadataDto[]) { tablesChilds.value = data; }
    
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

    // ── Étape 7 : Relations ──
    function setRelations(data: RelationParameter[]) { relations.value = data; }
    function addRelation(relation: RelationParameter): boolean {
        const exists = relations.value.some(r => r.parentTable === relation.parentTable && r.childTable === relation.childTable);
        if (exists) return false;
        relations.value.push(relation);
        return true;
    }
    function removeRelation(index: number) { relations.value.splice(index, 1); }

    // ── Étape 8 & 9 : Frontend (Uniquement les actions modifiant stepperData) ──
    function setSelectedFrontendFramework(framework: FrontendFramework | null) {
        stepperData.value.frontend = framework;
        if (framework) stepperData.value.frontendLayout.port = framework.defaultPort;
    }
    function updateFrontendLayout<K extends keyof FrontendLayoutConfig>(key: K, value: FrontendLayoutConfig[K]) { 
        (stepperData.value.frontendLayout as any)[key] = value; 
    }
    function toggleLanguage(code: string) {
        const list = stepperData.value.frontendLayout.selectedInterfaceLanguages;
        const idx = list.indexOf(code);
        idx === -1 ? list.push(code) : list.splice(idx, 1);
    }

    // ── Étape 10 : Git ──
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


    // ═══════════════════════════════════════════════════════════
    // ═══ RETURN ═══
    // ═══════════════════════════════════════════════════════════
    return {
        // Global
        wizardError, isGenerating, stepperData,
        setWizardError, clearWizardError, setIsGenerating, reset,

        // Étape 1
        pendingFramework, setPendingFramework, setFramework,

        // Étape 2
        getAvailableLoggingLevels, getAvailableSecurityTypes, getAvailableCacheProviders,
        getAvailableHibernateDdlAutoOptions, getAvailableLanguageVersions,
        getAvailableFrameworkVersions, getAvailableBuildTools,
        updateConfig, setAvailableLoggingLevels, setAvailableSecurityTypes,
        setAvailableCacheProviders, setAvailableLanguageVersions, setAvailableFrameworkVersions,
        setAvailableBuildTools, setAvailableHibernateDdlAutoOptions,

        // Étape 3 & 4
        setDatabaseEngine, updateDatabase,

        // Étape 5
        updateScript,

        // Étape 6
        availableTables, tables, views, getAvailableTables, getAvailableViews,
        getTablesParents, getTablesChilds,
        setAvailableTables, setTablesParents, setTablesChilds,
        toggleTable, toggleView, toggleComponent,

        // Étape 7
        relations, getRelations, setRelations, addRelation, removeRelation,

        // Étape 8 & 9
        setSelectedFrontendFramework, updateFrontendLayout, toggleLanguage,

        // Étape 10
        updateGitConfig,
    };
});

genesis-sdk-web\genesis-web-core\src\features\generator\composables\useGenerator.ts
// genesis-sdk-web/genesis-web-core/src/features/generator/composables/useGenerator.ts
import { inject, computed } from 'vue';
import { storeToRefs } from 'pinia';
import { useGeneratorStore } from '@genesis-labs/web-core/features/generator/store/useGenerator.store';
import { useFrontendStore } from '@genesis-labs/web-core/features/frontend/store/useFrontend.store'; // ✅ AJOUTÉ

import { GENERATOR_SERVICE_KEY, type IGeneratorService } from '@genesis-labs/web-core/features/generator/types/generator.service.interface';
import { useGenesisWizard } from '@genesis-labs/web-core/core/composables/ux/useGenesisWizard';

import type { GeneratorData, IDatabaseService, IFrameworkService } from '@genesis-labs/shared-types';
import { FRONTEND_SERVICE_KEY, IFrontendService } from '../../frontend/manifest';
import { DATABASE_SERVICE_KEY } from '../../database/manifest';
import { FRAMEWORK_SERVICE_KEY } from '../../frameworks/manifest';
import { WIZARD_STEP_CONFIG, type WizardServices } from './wizard-step-config';

export function useGenerator() {
    const service = inject(GENERATOR_SERVICE_KEY);
    const frontendService = inject(FRONTEND_SERVICE_KEY);
    const databaseService = inject(DATABASE_SERVICE_KEY);
    const frameworkService = inject(FRAMEWORK_SERVICE_KEY);

    if (!service) throw new Error('[useGenerator] IGeneratorService non fourni.');
    
    const svc = service as IGeneratorService;
    const fdsvc = frontendService as IFrontendService;
    const dbsvc = databaseService as IDatabaseService;
    const fsvc = frameworkService as IFrameworkService;

    const store = useGeneratorStore();
    const frontendStore = useFrontendStore(); // ✅ AJOUTÉ pour les actions frontend

    const SKIPPABLE_CONFIG = { 
        5: [], 
        8: [9], 
        10: [] 
    };

    const wizardServices: WizardServices = { svc, fdsvc, dbsvc, fsvc };

    const wizard = useGenesisWizard({
        totalSteps: 10,
        skippableStepsConfig: SKIPPABLE_CONFIG,
        
        // HOOK ON ENTER : On délègue à la config de l'étape
        onStepEnter: async (currentStep: number) => {
            const stepConfig = WIZARD_STEP_CONFIG[currentStep];
            if (stepConfig?.onEnter) {
                console.log(`[Wizard] Entrée dans l'étape ${currentStep} : Chargement...`);
                await stepConfig.onEnter(store, wizardServices);
            }
        },

        // HOOK AVANT NEXT : On délègue à la config de l'étape   
        onBeforeNext: async (currentStep: number) => {
            store.clearWizardError();
            const stepConfig = WIZARD_STEP_CONFIG[currentStep];
            
            if (stepConfig?.beforeNext) {
                console.log(`[Wizard] Validation de l'étape ${currentStep}...`);
                const canProceed = await stepConfig.beforeNext(store, wizardServices);
                if (!canProceed) {
                    console.warn(`[Wizard] Navigation bloquée à l'étape ${currentStep}.`);
                    return false;
                }
            }
            return true; 
        }
    });

    const { 
        stepperData, 
        getTablesParents, getTablesChilds, getRelations,
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
    
    // ✅ CORRIGÉ : Cible désormais le store Frontend (Source de Vérité)
    async function fetchAvailableLanguages() { 
        const data = await fdsvc.fetchInterfaceLanguages();
        frontendStore.setAvailableInterfaceLanguages(data); 
    }

    async function testDatabaseConnection(): Promise<{ success: boolean; message: string }> {
        try {
            return await dbsvc.testDatabaseConnection(stepperData.value.database);
        } catch (error) {
            return { success: false, message: error instanceof Error ? error.message : 'Erreur inconnue' };
        }
    }

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
        
        // ✅ SUPPRIMÉ : availableFrontendFrameworks et availableLanguages 
        // (Les composants UI doivent désormais utiliser useFrontendStore ou useWizardFrontend pour ces données)
        
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

genesis-sdk-web\genesis-web-core\src\features\generator\composables\wizard-step-config.ts

    3: {
        onEnter: async (_store, { dbsvc }) => {
            console.log("🔄 [Étape 3] Chargement des moteurs de base de données...");
            try {
                const engines = await dbsvc.fetchDatabaseEngines();
                const databaseStore = useDatabaseStore();
                databaseStore.setAvailableEngines(engines);
            } catch (error) {
                console.error("❌ Échec chargement moteurs DB:", error);
                _store.setWizardError("Impossible de charger la liste des bases de données.");
            }
        },
        beforeNext: async (store, { svc }) => {
            const db = store.stepperData.database;
            if (!db.engine) {
                store.setWizardError("Veuillez sélectionner un moteur de base de données.");
                return false;
            }

            try {
                const databaseStore = useDatabaseStore();
                const matchedEngine = databaseStore.availableEngines.find(eng => {
                    const normalizedName = eng.name.toLowerCase().replace(/\s+/g, '');
                    const normalizedConfig = db.engine.toLowerCase().replace(/\s+/g, '');
                    return normalizedName === normalizedConfig || eng.name.toLowerCase() === db.engine.toLowerCase();
                });

                if (!matchedEngine) {
                    console.warn("[Wizard] Moteur non trouvé dans la liste. engine config:", db.engine);
                    store.setWizardError("Moteur de base de données invalide. Veuillez le sélectionner à nouveau.");
                    return false;
                }
                console.log(svc);
                const engineId = matchedEngine.id;
                console.log(`[Wizard] Sélection de la BDD validée. ID récupéré :`, engineId);
                
                // await svc.selectDatabase(engineId);
                return true;
            } catch (error) {
                store.setWizardError(error instanceof Error ? error.message : "Échec de la sélection de la base de données.");
                return false;
            }
        }
    },

packages\webview\src\features\generator\views\GeneratorViewVsc.vue
<script setup lang="ts">
import { useGeneratorVsc } from '@/features/generator/composables/useGeneratorVsc';

import type { DatabaseEngineDto } from '@genesis-labs/shared-types';

import { GeneratorStepper } from '@genesis-labs/web-core/features/generator/manifest';
import { FrontendFramework } from '@genesis-labs/shared-types';

const {
    currentStep,
    totalSteps,
    isCurrentStepSkippable,
    goToPreviousStep,
    goToNextStep,
    setDatabaseEngine,
    setSelectedFrontendFramework,
    reset,
    handleSelectFolderPath,
    handleFileRequest,
    skipCurrentStep,
    stepperData,
    setPendingFramework
} = useGeneratorVsc();

function handleClose() {
    reset();
}

async function handleNextStep() {
    const finalData = await goToNextStep();
    if (finalData) {
        // Déclencher la génération du projet ici si nécessaire
        console.log("Prêt à générer :", finalData);
    }
}

// ═══ ORCHESTRATION UI (Sélection + Avancement) ═══
// Dans packages/webview/src/features/generator/views/GeneratorViewVsc.vue

function onSelectFramework(framework: any) {
    // setFramework(framework);
    setPendingFramework(framework);
    // Vérifier immédiatement après
    console.log('[onSelectFramework] store après setFramework =', stepperData.value.framework);
}

function onSelectDatabase(engine: DatabaseEngineDto) {
    setDatabaseEngine(engine);
}

function onSelectFrontend(framework: FrontendFramework) {
    setSelectedFrontendFramework(framework);
}
</script>

<template>
    <GeneratorStepper
        :currentStep="currentStep"
        :totalSteps="totalSteps"
        :is-skippable="isCurrentStepSkippable" 
        @close="handleClose"
        @previous="goToPreviousStep"
        @next="handleNextStep"
        @skip="skipCurrentStep" 
        @select-framework="onSelectFramework"
        @select-database="onSelectDatabase"
        @select-frontend="onSelectFrontend"
        @request-folder-path="handleSelectFolderPath"
        @request-file-path="handleFileRequest"
    />
</template> 
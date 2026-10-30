// genesis-sdk-web/genesis-web-core/src/features/generator/store/useGenerator.store.ts
import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import type { StepId } from '../composables/wizard-step-config'

// Types partagés
import type { 
  FrontendFramework, 
  Framework, 
  GeneratorData, 
  ProjectConfig, 
  DatabaseConfig, 
  ScriptConfig, 
  TableMetadataDto, 
  RelationParameter,  
  FrontendLayoutConfig, 
  GitConfiguration,
  DatabaseEngineDto,
  GenerationAssociatedToConfig,
  ComponentType,
  RepositoryProviderMetadata,
  RepositoryConfig
} from '@genesis-labs/shared-types';

import { INITIAL_STATE } from './generator.initial-state';

export const useGeneratorStore = defineStore('generator', () => {

  // ==========================================================================
  // 1. ÉTAT (STATE) - Groupé par domaine
  // ==========================================================================
  
  // Domaine : Global & Utilitaire
  const isInitializing = ref(false);
  const isGenerating = ref(false);
  const isGenerationComplete = ref(false);
  const wizardError = ref<string | null>(null);
  const stepperData = ref<GeneratorData>(structuredClone(INITIAL_STATE));

  // Domaine : Sélections en attente (Brouillons avant validation)
  const pendingFramework = ref<Framework | null>(null);
  const pendingDatabaseEngine = ref<DatabaseEngineDto | null>(null);
  const pendingFrontendFramework = ref<FrontendFramework | null>(null);
  const pendingRepositoryProvider = ref<RepositoryProviderMetadata | null>(null);

  // Domaine : Base de données - traçage pour détecter les changements
  const lastCommittedDatabaseConfig = ref<DatabaseConfig | null>(null);

  // Domaine : Étape 2 - Options de configuration projet (Listes dynamiques)
  const availableLoggingLevels = ref<string[]>([]);
  const availableSecurityTypes = ref<string[]>([]);
  const availableCacheProviders = ref<string[]>([]);
  const availableHibernateDdlAutoOptions = ref<string[]>([]);
  const availableLanguageVersions = ref<string[]>([]);
  const availableFrameworkVersions = ref<string[]>([]);
  const availableBuildTools = ref<string[]>([]);

  // Domaine : Étape 6 - Tables, Vues & Composants
  const availableTables = ref<TableMetadataDto[]>([]);
  const tablesParents = ref<TableMetadataDto[]>([]);
  const tablesChilds = ref<TableMetadataDto[]>([]);

  const generationAssociations = ref<GenerationAssociatedToConfig[]>([]);
  const selectedItemsInList = ref<string[]>([]);
  const generationProfiles = ref<{ id: string | number; name: string; components: ComponentType[] }[]>([]);

  // Domaine : Étape 7 - Relations
  const relations = ref<RelationParameter[]>([]);
  const relationsLoaded = ref(false);

  // ==========================================================================
  // 2. GETTERS (COMPUTED) - Dérivés de l'état
  // ==========================================================================
  
  // Étape 6 : Tables & Vues
  const getAvailableTables = computed(() => availableTables.value);
  const tables = computed(() => availableTables.value.filter(t => !t.isView));
  const views = computed(() => availableTables.value.filter(t => t.isView));
  const getAvailableViews = computed(() => views.value);
  const getTablesParents = computed(() => tablesParents.value);
  const getTablesChilds = computed(() => tablesChilds.value);

  // Étape 7 : Relations
  const getRelations = computed(() => relations.value);

  // Étape 2 : Options de configuration projet
  const getAvailableLoggingLevels = computed(() => availableLoggingLevels.value);
  const getAvailableSecurityTypes = computed(() => availableSecurityTypes.value);
  const getAvailableCacheProviders = computed(() => availableCacheProviders.value);
  const getAvailableHibernateDdlAutoOptions = computed(() => availableHibernateDdlAutoOptions.value);
  const getAvailableLanguageVersions = computed(() => availableLanguageVersions.value);
  const getAvailableFrameworkVersions = computed(() => availableFrameworkVersions.value);
  const getAvailableBuildTools = computed(() => availableBuildTools.value);

  const configFramework = ref<Framework | null>(null);

  const lastInvalidation = ref<{ id: number; steps: StepId[] } | null>(null);
  let invalidationCounter = 0;

  // ==========================================================================
  // 3. ACTIONS - Groupées par domaine
  // ==========================================================================
  
  // --- Global & Utilitaire ---
  function setWizardError(message: string) { wizardError.value = message; }
  function clearWizardError() { wizardError.value = null; }
  function setIsGenerating(value: boolean) { isGenerating.value = value; }
  function setConfigFramework(fw: Framework | null) { configFramework.value = fw; }
  function setIsInitializing(value: boolean) { isInitializing.value = value; }
  
  function reset() {
    isGenerating.value = false;
    wizardError.value = null;
    isInitializing.value = false;
    isGenerationComplete.value = false;
    stepperData.value = structuredClone(INITIAL_STATE);
    pendingFramework.value = null;
    pendingDatabaseEngine.value = null;
    pendingFrontendFramework.value = null;
    generationAssociations.value = [];
    relations.value = [];
    relationsLoaded.value = false;
    lastCommittedDatabaseConfig.value = null;  // 👈 reset
  }

  
  function resetFrameworkDependentConfig(next: Framework) {
    const initial = INITIAL_STATE.config;
    const keys = [
      'languageVersion', 'buildTool', 'groupId', 'frameworkVersion',
      'loggingLevel', 'securityType', 'cacheProvider', 'hibernateDdlAuto'
    ] as const;
    keys.forEach(k => updateConfig(k, initial[k]));
    configFramework.value = next;
  }

  /** Remet à zéro le choix du frontend et sa configuration */
  function resetFrontendDependentConfig() {
    pendingFrontendFramework.value = null;
    stepperData.value.frontend = null;
    stepperData.value.frontendLayout = structuredClone(INITIAL_STATE.frontendLayout);
  }

  /** Remet à zéro les associations de génération (tables/vues assignées) */
  function resetGenerationAssociations() {
    generationAssociations.value = [];
    generationProfiles.value = [];
    stepperData.value.generationParameters = [];
    selectedItemsInList.value = [];
  }

  /** Y a-t-il quelque chose à perdre côté frontend ? */
  const hasFrontendData = computed(() =>
    !!pendingFrontendFramework.value || !!stepperData.value.frontend
  );

  // --- Étape 1 : Framework ---
  function setPendingFramework(framework: Framework | null) {
    pendingFramework.value = framework;
  }

  function setPendingRepositoryProvider(provider: RepositoryProviderMetadata | null) {
    pendingRepositoryProvider.value = provider;
  }

  function setFramework(framework: Framework) {
    stepperData.value.framework = framework;
  }

  function setIsGenerationComplete(value: boolean) {
    isGenerationComplete.value = value;
  }

  // --- Étape 2 : Configuration Projet ---
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

  /**
   * Annule un changement de framework refusé par l'utilisateur.
   * Réaligne le pending sur le framework réellement commité dans stepperData.
   */
  function cancelPendingFrameworkChange() {
    pendingFramework.value = stepperData.value.framework;
  }

  // --- Étape 3 & 4 : Base de Données ---
  function setPendingDatabaseEngine(engine: DatabaseEngineDto | null) {
    pendingDatabaseEngine.value = engine;
    // ⚠️ NE PAS toucher stepperData ici. Le commit se fait via setDatabaseEngine.
  }

  function cancelPendingDatabaseChange() {
    pendingDatabaseEngine.value = null;
  }

  /**
   * Officialise le choix du moteur de base de données dans stepperData.
   */
  function setDatabaseEngine(engine: DatabaseEngineDto) {
    stepperData.value.database.engine = engine.name;
    stepperData.value.database.port = Number(engine.port) || 0;
    stepperData.value.database.driverName = engine.driverName || '';
    stepperData.value.database.driverType = engine.driverType || 'jdbc';

    if (engine.sid) {
      stepperData.value.database.sid = engine.sid;
    }
  }

  function updateDatabase<K extends keyof DatabaseConfig>(key: K, value: DatabaseConfig[K]) { 
    (stepperData.value.database as any)[key] = value; 
  }

  /**
   * Mémorise la configuration DB qui vient d'être commitée côté backend.
   * Sert de référence pour détecter un changement de config lors d'un prochain passage.
   */
  function setLastCommittedDatabaseConfig(config: DatabaseConfig) {
    lastCommittedDatabaseConfig.value = structuredClone(config);
  }

  /**
   * Détecte si la configuration actuelle diffère de la dernière config commitée.
   * 
   * ⚠️ On ne compare QUE les champs qui impactent les tables (données métier) :
   * - engine, host, port, databaseName, schema, sid, username
   * 
   * Les champs "non-métier" (password, driverType, driverName, trustCertificate,
   * allowPublicKeyRetrieval) sont IGNORÉS : les modifier ne change pas les tables,
   * donc ne doit pas déclencher d'alerte ni de reset de l'étape Génération.
   */
  function hasDatabaseConfigChanged(current: DatabaseConfig): boolean {
    const last = lastCommittedDatabaseConfig.value;
    if (!last) return true;  // jamais commité → considéré comme changé

    return last.engine !== current.engine
      || last.host !== current.host
      || String(last.port) !== String(current.port)
      || last.databaseName !== current.databaseName
      || last.schema !== current.schema
      || last.sid !== current.sid
      || last.username !== current.username;
  }

  // --- Étape 5 : Script / IA ---
  function updateScript<K extends keyof ScriptConfig>(key: K, value: ScriptConfig[K]) { 
    (stepperData.value.script as any)[key] = value; 
  }

  // --- Étape 6 : Tables, Vues & Composants ---
  function setAvailableTables(data: TableMetadataDto[]) { availableTables.value = data; }
  function setTablesParents(data: TableMetadataDto[]) { tablesParents.value = data; }
  function setTablesChilds(data: TableMetadataDto[]) { tablesChilds.value = data; }
  function setGenerationProfiles(profiles: { id: string | number; name: string; components: ComponentType[] }[]) {
    generationProfiles.value = profiles;
  }

  function assignTableToConfig(configId: string | number, tableName: string, isView: boolean) {
    generationAssociations.value.forEach(assoc => {
      if (isView) {
        const viewIndex = assoc.associatedViews.indexOf(tableName);
        if (viewIndex !== -1) {
          assoc.associatedViews.splice(viewIndex, 1);
        }
      } else {
        const tableIndex = assoc.associatedTables.indexOf(tableName);
        if (tableIndex !== -1) {
          assoc.associatedTables.splice(tableIndex, 1);
        }
      }
    });

    generationAssociations.value = generationAssociations.value.filter(
      assoc => assoc.associatedTables.length > 0 || assoc.associatedViews.length > 0
    );

    let targetAssoc = generationAssociations.value.find(a => a.configId === configId);
    if (!targetAssoc) {
      targetAssoc = { configId, associatedTables: [], associatedViews: [] };
      generationAssociations.value.push(targetAssoc);
    }

    if (isView) {
      if (!targetAssoc.associatedViews.includes(tableName)) {
        targetAssoc.associatedViews.push(tableName);
      }
    } else {
      if (!targetAssoc.associatedTables.includes(tableName)) {
        targetAssoc.associatedTables.push(tableName);
      }
    }
  }

  function removeTableFromConfig(_configId: string | number, tableName: string, isView: boolean) {
    generationAssociations.value.forEach(assoc => {
      if (isView) {
        assoc.associatedViews = assoc.associatedViews.filter(t => t !== tableName);
      } else {
        assoc.associatedTables = assoc.associatedTables.filter(t => t !== tableName);
      }
    });

    generationAssociations.value = generationAssociations.value.filter(
      assoc => assoc.associatedTables.length > 0 || assoc.associatedViews.length > 0
    );
  }

  function isTableAssignedToConfig(configId: string | number, tableName: string, isView: boolean): boolean {
    const assoc = generationAssociations.value.find(a => a.configId === configId);
    if (!assoc) return false;
    return isView ? assoc.associatedViews.includes(tableName) : assoc.associatedTables.includes(tableName);
  }

  function toggleItemInList(tableName: string) {
    const idx = selectedItemsInList.value.indexOf(tableName);
    if (idx === -1) selectedItemsInList.value.push(tableName);
    else selectedItemsInList.value.splice(idx, 1);
  }
  
  function clearSelectedItemsInList() {
    selectedItemsInList.value = [];
  }

  // --- Étape 7 : Relations ---
  function setRelations(data: RelationParameter[]) {
    relations.value = data;
    relationsLoaded.value = true;
  }
  
  function addRelation(relation: RelationParameter): boolean {
    const exists = relations.value.some(
      r => r.parentTable === relation.parentTable && r.childTable === relation.childTable
    );
    
    if (exists) {
      setWizardError(`La relation entre la table "${relation.parentTable}" et la table "${relation.childTable}" existe déjà.`);
      return false;
    }
    
    clearWizardError();
    relations.value.push(relation);
    return true;
  }
  
  function removeRelation(index: number) { 
    relations.value.splice(index, 1); 
  }

  // --- Étape 8 & 9 : Frontend ---
  function setPendingFrontendFramework(frontendFramework: FrontendFramework | null) {
    const changed = pendingFrontendFramework.value?.id !== frontendFramework?.id;
    pendingFrontendFramework.value = frontendFramework;
    if (frontendFramework && changed) {
      stepperData.value.frontendLayout.port = frontendFramework.defaultPort;
    }
  }

  function setSelectedFrontendFramework(framework: FrontendFramework | null) {
    stepperData.value.frontend = framework;
  }
  
  function updateFrontendLayout<K extends keyof FrontendLayoutConfig>(key: K, value: FrontendLayoutConfig[K]) { 
    (stepperData.value.frontendLayout as any)[key] = value; 
  }
  
  function toggleLanguage(code: string) {
    const list = stepperData.value.frontendLayout.selectedInterfaceLanguages;
    const idx = list.indexOf(code);
    idx === -1 ? list.push(code) : list.splice(idx, 1);
  }

  // --- Étape 10 : Git ---
  function updateGitConfig<K extends keyof GitConfiguration>(key: K, value: GitConfiguration[K]) {
    (stepperData.value.git as any)[key] = value;
    
    if (key === 'useGit' && value === false) {
      stepperData.value.git = { 
        useGit: false, 
        separateRepositories: false, 
        useRemoteRepo: false, 
        isNewRemoteRepo: true, 
        repositoryName: '', 
        backendRepositoryName: '',
        frontendRepositoryName: '', 
        githubUsername: '', 
        githubToken: '' 
      };
    }
  }

  function updateRepositoryConfig<K extends keyof RepositoryConfig>(key: K, value: RepositoryConfig[K]) { 
    if (!stepperData.value.repositoryConfig) {
      stepperData.value.repositoryConfig = {
        provider: 'github', useRemote: false, isNewRemote: true, scope: 'monorepo', authMethod: 'personal_access_token'
      } as RepositoryConfig;
    }
    (stepperData.value.repositoryConfig as any)[key] = value; 
  }

  function skipGitConfig() {
    stepperData.value.git = {
      useGit: false,
      useRemoteRepo: false,
      isNewRemoteRepo: true,
      separateRepositories: false,
      repositoryName: '',
      backendRepositoryName: '',
      frontendRepositoryName: '',
      githubUsername: '',
      githubToken: ''
    };
  }

  function invalidateSteps(steps: StepId[]) {
    lastInvalidation.value = { id: ++invalidationCounter, steps };
  }

  // ==========================================================================
  // 4. RETURN - Ordre identique à la déclaration
  // ==========================================================================
  return {
    // Global
    wizardError, isGenerating, stepperData,
    setWizardError, clearWizardError, setIsGenerating, reset,
    isGenerationComplete, setIsGenerationComplete,
    isInitializing, setIsInitializing,

    // Étape 1 : Framework
    pendingFramework, setPendingFramework, setFramework,
    pendingRepositoryProvider, setPendingRepositoryProvider,
    cancelPendingFrameworkChange,

    // Étape 2 : Configuration Projet
    getAvailableLoggingLevels, getAvailableSecurityTypes, getAvailableCacheProviders,
    getAvailableHibernateDdlAutoOptions, getAvailableLanguageVersions,
    getAvailableFrameworkVersions, getAvailableBuildTools,
    updateConfig, setAvailableLoggingLevels, setAvailableSecurityTypes,
    setAvailableCacheProviders, setAvailableLanguageVersions, setAvailableFrameworkVersions,
    setAvailableBuildTools, setAvailableHibernateDdlAutoOptions,

    // Étape 3 & 4 : Base de Données
    pendingDatabaseEngine, setPendingDatabaseEngine, setDatabaseEngine, updateDatabase,
    lastCommittedDatabaseConfig, setLastCommittedDatabaseConfig, hasDatabaseConfigChanged,
    cancelPendingDatabaseChange,

    // Étape 5 : Script / IA
    updateScript,

    // Étape 6 : Tables, Vues & Composants
    generationProfiles,
    setGenerationProfiles,
    availableTables, tables, views, getAvailableTables, getAvailableViews,
    getTablesParents, getTablesChilds,
    setAvailableTables, setTablesParents, setTablesChilds,
    assignTableToConfig,
    removeTableFromConfig,
    isTableAssignedToConfig,
    selectedItemsInList,
    toggleItemInList,
    clearSelectedItemsInList,
    generationAssociations,
    resetGenerationAssociations,

    // Étape 7 : Relations
    relations, relationsLoaded, getRelations, setRelations, addRelation, removeRelation,

    // Étape 8 & 9 : Frontend
    pendingFrontendFramework, setPendingFrontendFramework, setSelectedFrontendFramework, 
    updateFrontendLayout, toggleLanguage,

    // Étape 10 : Git
    updateGitConfig,
    skipGitConfig,
    updateRepositoryConfig,

    setConfigFramework,
    resetFrameworkDependentConfig,
    configFramework,
    resetFrontendDependentConfig,
    hasFrontendData,
    invalidateSteps,
    lastInvalidation,
  };
});
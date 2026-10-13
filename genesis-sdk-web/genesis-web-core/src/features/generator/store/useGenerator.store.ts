// genesis-sdk-web/genesis-web-core/src/features/generator/store/useGenerator.store.ts
import { defineStore } from 'pinia';
import { ref, computed } from 'vue';

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
  ComponentType
} from '@genesis-labs/shared-types';

import { INITIAL_STATE } from './generator.initial-state';

export const useGeneratorStore = defineStore('generator', () => {

  // ==========================================================================
  // 1. ÉTAT (STATE) - Groupé par domaine
  // ==========================================================================
  
  // Domaine : Global & Utilitaire
  const isGenerating = ref(false);
  const wizardError = ref<string | null>(null);
  const stepperData = ref<GeneratorData>(structuredClone(INITIAL_STATE));

  // Domaine : Sélections en attente (Brouillons avant validation)
  const pendingFramework = ref<Framework | null>(null);
  const pendingDatabaseEngine = ref<DatabaseEngineDto | null>(null);
  const pendingFrontendFramework = ref<FrontendFramework | null>(null);

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


  // ==========================================================================
  // 3. ACTIONS - Groupées par domaine
  // ==========================================================================
  
  // --- Global & Utilitaire ---
  function setWizardError(message: string) { wizardError.value = message; }
  function clearWizardError() { wizardError.value = null; }
  function setIsGenerating(value: boolean) { isGenerating.value = value; }
  
  function reset() {
    isGenerating.value = false;
    wizardError.value = null;
    stepperData.value = structuredClone(INITIAL_STATE);
    pendingFramework.value = null;
    pendingDatabaseEngine.value = null;
    pendingFrontendFramework.value = null;
    generationAssociations.value = [];
  }

  // --- Étape 1 : Framework ---
  function setPendingFramework(framework: Framework | null) {
    pendingFramework.value = framework;
  }

  function setFramework(framework: Framework) {
    stepperData.value.framework = framework;
  }

  // --- Étape 2 : Configuration Projet ---
  function updateConfig<K extends keyof ProjectConfig>(key: K, value: ProjectConfig[K]) { 
    // Note: L'assertion 'as any' est nécessaire ici pour permettre l'assignation dynamique 
    // de clés tout en conservant la flexibilité du type générique K.
    (stepperData.value.config as any)[key] = value; 
  }
  
  function setAvailableLoggingLevels(data: string[]) { availableLoggingLevels.value = data; }
  function setAvailableSecurityTypes(data: string[]) { availableSecurityTypes.value = data; }
  function setAvailableCacheProviders(data: string[]) { availableCacheProviders.value = data; }
  function setAvailableLanguageVersions(data: string[]) { availableLanguageVersions.value = data; }
  function setAvailableFrameworkVersions(data: string[]) { availableFrameworkVersions.value = data; }
  function setAvailableBuildTools(data: string[]) { availableBuildTools.value = data; }
  function setAvailableHibernateDdlAutoOptions(data: string[]) { availableHibernateDdlAutoOptions.value = data; }

  // --- Étape 3 & 4 : Base de Données ---
  function setPendingDatabaseEngine(engine: DatabaseEngineDto | null) {
    pendingDatabaseEngine.value = engine;
  }

  /**
   * Officialise le choix du moteur de base de données dans stepperData.
   * Applique une normalisation des noms de moteurs (ex: 'postgresql' -> 'postgre') 
   * pour garantir la compatibilité avec les attentes du backend.
   */
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
    // 1. RETIRER la table/vue de TOUTES les configurations existantes (garantit l'unicité)
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

    // (Optionnel mais recommandé) Nettoyer les objets de configuration devenus vides
    generationAssociations.value = generationAssociations.value.filter(
      assoc => assoc.associatedTables.length > 0 || assoc.associatedViews.length > 0
    );

    // 2. AJOUTER la table/vue à la NOUVELLE configuration cible
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
    // 1. Retirer la table/vue de TOUTES les associations existantes
    generationAssociations.value.forEach(assoc => {
      if (isView) {
        assoc.associatedViews = assoc.associatedViews.filter(t => t !== tableName);
      } else {
        assoc.associatedTables = assoc.associatedTables.filter(t => t !== tableName);
      }
    });

    // 2. Nettoyer les objets de configuration devenus vides (bonne pratique pour garder un état propre)
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
  function setRelations(data: RelationParameter[]) { relations.value = data; }
  
  /**
   * Ajoute une relation après avoir vérifié qu'elle n'existe pas déjà.
   * @returns true si l'ajout a réussi, false si c'était un doublon.
   */
  function addRelation(relation: RelationParameter): boolean {
    const exists = relations.value.some(
      r => r.parentTable === relation.parentTable && r.childTable === relation.childTable
    );
    if (exists) return false;
    
    relations.value.push(relation);
    return true;
  }
  
  function removeRelation(index: number) { 
    relations.value.splice(index, 1); 
  }

  // --- Étape 8 & 9 : Frontend ---
  function setPendingFrontendFramework(frontendFramework: FrontendFramework | null) {
    pendingFrontendFramework.value = frontendFramework;
  }

  function setSelectedFrontendFramework(framework: FrontendFramework | null) {
    stepperData.value.frontend = framework;
    if (framework) {
      stepperData.value.frontendLayout.port = framework.defaultPort;
    }
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
  /**
   * Met à jour la configuration Git.
   * Si l'utilisation de Git est désactivée, réinitialise complètement l'objet 
   * pour éviter de conserver des données obsolètes (comme des noms de repo).
   */
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

  function skipGitConfig() {
    stepperData.value.git = {
      useGit: false, // C'est ici qu'on désactive Git globalement
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


  // ==========================================================================
  // 4. RETURN - Ordre identique à la déclaration
  // ==========================================================================
  return {
    // Global
    wizardError, isGenerating, stepperData,
    setWizardError, clearWizardError, setIsGenerating, reset,

    // Étape 1 : Framework
    pendingFramework, setPendingFramework, setFramework,

    // Étape 2 : Configuration Projet
    getAvailableLoggingLevels, getAvailableSecurityTypes, getAvailableCacheProviders,
    getAvailableHibernateDdlAutoOptions, getAvailableLanguageVersions,
    getAvailableFrameworkVersions, getAvailableBuildTools,
    updateConfig, setAvailableLoggingLevels, setAvailableSecurityTypes,
    setAvailableCacheProviders, setAvailableLanguageVersions, setAvailableFrameworkVersions,
    setAvailableBuildTools, setAvailableHibernateDdlAutoOptions,

    // Étape 3 & 4 : Base de Données
    pendingDatabaseEngine, setPendingDatabaseEngine, setDatabaseEngine, updateDatabase,

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

    // Étape 7 : Relations
    relations, getRelations, setRelations, addRelation, removeRelation,

    // Étape 8 & 9 : Frontend
    pendingFrontendFramework, setPendingFrontendFramework, setSelectedFrontendFramework, 
    updateFrontendLayout, toggleLanguage,

    // Étape 10 : Git
    updateGitConfig,
    skipGitConfig
  };
});
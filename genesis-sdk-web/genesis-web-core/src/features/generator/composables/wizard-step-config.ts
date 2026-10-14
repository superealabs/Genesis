// genesis-sdk-web/genesis-web-core/src/features/generator/composables/wizard-step-config.ts

// Types & Interfaces
import type { IGeneratorService } from '../types/generator.service.interface';
import type { IFrontendService } from '../../frontend/types/frontend.service.interface';
import type { IDatabaseService } from '../../database/types/database.service.interface';
import type { IFrameworkService, GenerationParameter, ComponentType } from '@genesis-labs/shared-types';

// Stores
import { useGeneratorStore } from '../store/useGenerator.store';
import { useFrameworkStore } from '../../frameworks/store/useFramework.store';
import { useDatabaseStore } from '../../database/store/useDatabase.store';
import { useFrontendStore } from '../../frontend/store/useFrontend.store';

// ============================================================================
// INTERFACES & TYPES
// ============================================================================

export interface WizardServices {
  svc: IGeneratorService;
  fdsvc: IFrontendService;
  dbsvc: IDatabaseService;
  fsvc: IFrameworkService;
}

export interface StepConfig {
  onEnter?: (store: ReturnType<typeof useGeneratorStore>, services: WizardServices) => Promise<void> | void;
  beforeNext?: (store: ReturnType<typeof useGeneratorStore>, services: WizardServices) => Promise<boolean> | boolean;
  onSkip?: (store: ReturnType<typeof useGeneratorStore>, services: WizardServices) => Promise<void> | void;

  /**
   * liste des éléments skippés après ce dernier
   */
  skipCascade?: StepId[];
}

// Identifiants sémantiques des étapes (Le contrat TypeScript)
export type StepId = 
  | 'framework'
  | 'project_config'
  | 'database_select'
  | 'database_config'
  | 'script'
  | 'tables'
  | 'relations'
  | 'frontend_select'
  | 'frontend_layout'
  | 'git_config'
  | 'summary';

// ============================================================================
// SOURCE DE VÉRITÉ : ORDRE DES ÉTAPES
// ============================================================================

/**
 * L'ordre de ce tableau définit l'ordre d'exécution du wizard.
 * Pour réorganiser le wizard, il suffit de réorganiser ce tableau.
 */
export const WIZARD_STEP_ORDER: StepId[] = [
  'framework',
  'project_config',
  'database_select',
  'database_config',
  'script',
  'tables',
  'relations',
  'frontend_select',
  'frontend_layout',
  'git_config',
  'summary'
];

// ============================================================================
// CONFIGURATION MÉTIER PAR ÉTAPE
// ============================================================================

export const WIZARD_STEP_CONFIG: Record<StepId, StepConfig> = {
  
  framework: {
    onEnter: async (_store, { fsvc }) => {
      console.log("[Wizard: framework] Chargement des frameworks et langues...");
      const frameworkStore = useFrameworkStore();
      
      try { 
        const [frameworksData, languagesData] = await Promise.all([
          fsvc.fetchFrameworks(),
          fsvc.fetchLanguages()
        ]);
        
        frameworkStore.setFrameworks(frameworksData);
        frameworkStore.setLanguages(languagesData);
        
        console.log(`[Wizard: framework] Chargement réussi : ${frameworksData.length} frameworks, ${languagesData.length} langues`);
      } catch (error) {
        console.error("[Wizard: framework] Échec du chargement:", error);
        _store.setWizardError("Impossible de charger la liste des frameworks.");
      }
    },
    
    beforeNext: async (store) => {
      const framework = store.pendingFramework;
      
      if (!framework) {
        store.setWizardError("Veuillez sélectionner un framework avant de continuer.");
        return false;
      }

      try {
        store.setFramework(framework);
        console.log(`[Wizard: framework] Framework sélectionné :`, framework.name);
        return true;
      } catch (error) {
        store.setWizardError(error instanceof Error ? error.message : "Échec de la sélection du framework.");
        return false;
      }
    }
  },

  project_config: {
    onEnter: async (store, { fsvc }) => {
      const framework = store.stepperData.framework;
      if (!framework) return;
      
      console.log("[Wizard: project_config] Chargement des options de configuration...");
      
      try {
        await Promise.all([
          fsvc.fetchLoggingLevels(framework.id).then(data => store.setAvailableLoggingLevels(data)),
          fsvc.fetchSecurityTypes(framework.id).then(data => store.setAvailableSecurityTypes(data)),
          fsvc.fetchCacheProviders(framework.id).then(data => store.setAvailableCacheProviders(data)),
          fsvc.fetchLanguageVersions(framework.languageId).then(data => store.setAvailableLanguageVersions(data)),
          fsvc.fetchFrameworkVersions(framework.id).then(data => store.setAvailableFrameworkVersions(data)),
          fsvc.fetchBuildTools(framework.id).then(data => store.setAvailableBuildTools(data)),
        ]);
        console.log("[Wizard: project_config] Options de configuration chargées");
      } catch (error) {
        console.error("[Wizard: project_config] Échec du chargement des options:", error);
      }
    },
    
    beforeNext: async (store) => {
      const config = store.stepperData.config;
      
      if (!config.projectName || !config.projectLocation) {
        store.setWizardError("Le nom du projet et la localisation sont obligatoires.");
        return false;
      }

      try {
        console.log("[Wizard: project_config] Configuration du projet validée");
        return true;
      } catch (error) {
        store.setWizardError(error instanceof Error ? error.message : "Échec de la sauvegarde de la configuration.");
        return false;
      }
    }
  },

  database_select: {
    onEnter: async (_store, { dbsvc }) => {
      console.log("[Wizard: database_select] Chargement des moteurs de base de données...");
      const databaseStore = useDatabaseStore();
      
      try {
        const engines = await dbsvc.fetchDatabaseEngines();
        databaseStore.setAvailableEngines(engines);
        console.log(`[Wizard: database_select] ${engines.length} moteurs chargés`);
      } catch (error) {
        console.error("[Wizard: database_select] Échec du chargement des moteurs DB:", error);
        _store.setWizardError("Impossible de charger la liste des bases de données.");
      }
    },
    
    beforeNext: async (store) => {
      if (!store.pendingDatabaseEngine) {
        store.setWizardError("Veuillez sélectionner un moteur de base de données.");
        return false;
      }

      try {
        store.setDatabaseEngine(store.pendingDatabaseEngine);

        const databaseStore = useDatabaseStore();
        const matchedEngine = databaseStore.availableEngines.find(eng => {
          const normalizedName = eng.name.toLowerCase().replace(/\s+/g, '');
          const normalizedConfig = store.pendingDatabaseEngine!.name.toLowerCase().replace(/\s+/g, '');
          return normalizedName === normalizedConfig || eng.name.toLowerCase() === store.pendingDatabaseEngine!.name.toLowerCase();
        });

        if (!matchedEngine) {
          store.setWizardError("Moteur de base de données invalide. Veuillez le sélectionner à nouveau.");
          return false;
        }

        console.log(`[Wizard: database_select] Sélection BDD validée. ID : ${matchedEngine.id}`);
        return true;
      } catch (error) {
        store.setWizardError(error instanceof Error ? error.message : "Échec de la sélection de la base de données.");
        return false;
      }
    }
  },

  database_config: {
    beforeNext: async (store) => {
      try {
        console.log("[Wizard: database_config] Configuration BDD validée");
        return true;
      } catch (error) {
        store.setWizardError(error instanceof Error ? error.message : "Échec de la sauvegarde des credentials.");
        return false;
      }
    }
  },

  script: {
    beforeNext: async (store, { svc }) => {
      const script = store.stepperData.script;
      
      if (script.path || script.content) {
        try { 
          await svc.saveScriptConfig(script); 
        } catch (error) { 
          console.warn("[Wizard: script] Échec sauvegarde script (non bloquant):", error); 
        }
      }
      
      return true;
    },
    skipCascade: [] // Peut être sauté sans conséquence sur les autres
  },

  tables: {
    onEnter: async (store, { svc }) => {
      console.log("[Wizard: tables] Chargement des tables et vues...");
      
      try {
        const tables = await svc.fetchTablesMetadata();
        store.setAvailableTables(tables);
        console.log(`[Wizard: tables] ${tables.length} tables/vues chargées`);
      } catch (error) {
        console.error("[Wizard: tables] Échec du chargement des tables:", error);
      }
    },
    
    beforeNext: async (store) => {
      try {
        // 1. Récupérer les données sources
        const profiles = store.generationProfiles; // Type 1 : Les profils avec composants
        const associations = store.generationAssociations; // Type 2 : Les liens tables/vues ↔ profils
        
        // 2. Construire le tableau final (Type 3)
        const generationParameters: GenerationParameter[] = [];
        
        for (const profile of profiles) {
          // Trouver l'association correspondante
          const assoc = associations.find(a => a.configId === profile.id);
          
          const assignedTables = assoc?.associatedTables || [];
          const assignedViews = assoc?.associatedViews || [];
          
          // On ignore totalement les profils qui n'ont 
          // aucune table ou vue assignée. Cela garantit que seules les entités 
          // explicitement liées à une configuration sont envoyées au backend.
          if (assignedTables.length === 0 && assignedViews.length === 0) {
            console.log(`[Wizard: tables] Profil "${profile.name}" ignoré : aucune entité assignée.`);
            continue; // On passe au profil suivant sans l'ajouter au tableau
          }
          
          // Construire le GenerationParameter uniquement avec les données assignées
          const param: GenerationParameter = {
            profileName: profile.name,
            selectedTables: assignedTables,
            selectedViews: assignedViews,
            selectedComponents: profile.components as ComponentType[]
          };
          
          generationParameters.push(param);
        }
        
        // 3. Stocker dans stepperData pour l'envoi au backend
        store.stepperData.generationParameters = generationParameters;
        
        console.log("[Wizard: tables] Sélection validée. Seules les entités assignées sont incluses :", generationParameters);
        return true;
      } catch (error) {
        store.setWizardError(error instanceof Error ? error.message : "Échec de la sauvegarde des tables.");
        return false;
      }
    }
  },

  relations: {
    beforeNext: async (store) => {
      try {
        console.log("[Wizard: relations] Configuration des relations validée");
        return true;
      } catch (error) {
        store.setWizardError(error instanceof Error ? error.message : "Échec de la sauvegarde des relations.");
        return false;
      }
    }
  },

  frontend_select: {
    onEnter: async (_store, { fdsvc }) => {
      console.log("[Wizard: frontend_select] Chargement des options Frontend...");
      const frontendStore = useFrontendStore();
      
      try {
        await Promise.all([
          fdsvc.fetchFrontendFrameworks().then(data => frontendStore.setAvailableFrontendFrameworks(data)),
          fdsvc.fetchInterfaceLanguages().then(data => frontendStore.setAvailableInterfaceLanguages(data)),
        ]);
        console.log("[Wizard: frontend_select] Options Frontend chargées");
      } catch (error) {
        console.error("[Wizard: frontend_select] Échec du chargement des options Frontend:", error);
        _store.setWizardError("Impossible de charger les options frontend.");
      }
    },
    
    beforeNext: async (store) => {
      if (!store.pendingFrontendFramework) {
        console.log("[Wizard: frontend_select] Frontend ignoré (skippable)");
        return true;
      }

      try {
        store.setSelectedFrontendFramework(store.pendingFrontendFramework);

        const frontendId = store.stepperData.frontend?.id;
        if (frontendId) {
          console.log(`[Wizard: frontend_select] Sélection Frontend validée. ID : ${frontendId}`);
        }
        
        return true;
      } catch (error) {
        store.setWizardError(error instanceof Error ? error.message : "Échec de la sélection du frontend.");
        return false;
      }
    },
    // ✅ CORRECTION MAJEURE : Si on saute le choix du frontend, on saute automatiquement sa configuration
    skipCascade: ['frontend_layout'] 
  },

  frontend_layout: {
    beforeNext: async (store) => {
      try {
        console.log("[Wizard: frontend_layout] Configuration layout Frontend validée");
        return true;
      } catch (error) {
        store.setWizardError(error instanceof Error ? error.message : "Échec de la sauvegarde du layout frontend.");
        return false;
      }
    }
  },

  git_config: {
    onEnter: (store) => {
      // Par défaut, on considère que Git local est voulu, mais pas de remote
      // Cela garde l'interface propre tout en préparant le terrain pour "Suivant"
      store.stepperData.git.useGit = true;
      // On s'assure que les valeurs par défaut sont propres
      if (!store.stepperData.git.useRemoteRepo) {
        store.stepperData.git.isNewRemoteRepo = true;
        store.stepperData.git.separateRepositories = false;
      }
    },
    
    onSkip: (store) => {
      // L'utilisateur a cliqué sur "Passer" : on désactive Git et on nettoie
      store.skipGitConfig();
    },

    beforeNext: (store) => {
      // L'utilisateur clique sur "Suivant" : on valide que Git est actif
      store.stepperData.git.useGit = true;
      
      const git = store.stepperData.git;

      // Si l'utilisateur a activé le remote, on valide les champs obligatoires
      if (git.useRemoteRepo) {
        if (!git.githubUsername) {
          store.setWizardError("Le nom d'utilisateur GitHub est obligatoire pour un dépôt distant.");
          return false;
        }
        if (git.isNewRemoteRepo && !git.githubToken) {
          store.setWizardError("Le Personal Access Token est obligatoire pour créer un nouveau dépôt.");
          return false;
        }
        if (git.separateRepositories) {
          if (!git.backendRepositoryName || !git.frontendRepositoryName) {
            store.setWizardError("Les noms des dépôts Backend et Frontend sont obligatoires.");
            return false;
          }
        } else {
          if (!git.repositoryName) {
            store.setWizardError("Le nom du dépôt est obligatoire.");
            return false;
          }
        }
      }
      
      console.log("[Wizard: git_config] Configuration Git validée");
      return true;
    },
    skipCascade: []
  },

  summary: {
    onEnter: (/** store */) => {
      console.log("[Wizard: summary] Affichage du résumé final de la configuration.");
      // Aucune action de chargement requise ici, toutes les données sont déjà dans le store.
      // Le composant SummaryView se chargera de les afficher via les composantes enfants.
    },
    
    beforeNext: async (store) => {
      try {
        console.log("[Wizard: summary] Résumé validé. Prêt pour la génération finale.");
        
        // C'est ici que tu pourrais déclencher la génération si le wizard le gère via beforeNext,
        // ou simplement retourner true pour permettre au composant parent de gérer le clic sur "Générer".
        return true;
      } catch (error) {
        store.setWizardError(error instanceof Error ? error.message : "Échec de la validation du résumé.");
        return false;
      }
    }
  }
};

// ============================================================================
// UTILITAIRES D'ADAPTATION (Pont entre Noms et Numéros)
// ============================================================================

/**
 * Retourne le nombre total d'étapes (dynamique, basé sur le tableau d'ordre)
 */
export const getTotalSteps = (): number => WIZARD_STEP_ORDER.length;

/**
 * Convertit un numéro d'étape (1-based) en identifiant sémantique
 */
export const getStepIdByNumber = (num: number): StepId | undefined => {
  return WIZARD_STEP_ORDER[num - 1];
};

/**
 * Convertit un identifiant sémantique en numéro d'étape (1-based)
 */
export const getStepNumberById = (id: StepId): number => {
  return WIZARD_STEP_ORDER.indexOf(id) + 1;
};

/**
 * Génère le format Record<number, number[]> attendu par useGenesisWizard
 * en se basant sur les dépendances (skipCascade) définies dans la config.
 */
export const getSkippableStepsConfig = (): Record<number, number[]> => {
  const config: Record<number, number[]> = {};
  
  WIZARD_STEP_ORDER.forEach((stepId, index) => {
    const stepConfig = WIZARD_STEP_CONFIG[stepId];
    const stepNum = index + 1;
    
    // On vérifie si la propriété 'skipCascade' existe explicitement dans la config de l'étape
    if (stepConfig && 'skipCascade' in stepConfig) {
      config[stepNum] = (stepConfig.skipCascade || []).map(cascadeId => getStepNumberById(cascadeId));
    }
  });
  
  return config;
};
// genesis-sdk-web/genesis-web-core/src/features/generator/composables/wizard-step-config.ts

// Types & Interfaces
import type { IGeneratorService } from '../types/generator.service.interface';
import type { IFrontendService } from '../../frontend/types/frontend.service.interface';
import type { IDatabaseService } from '../../database/types/database.service.interface';
import type { IFrameworkService } from '@genesis-labs/shared-types';

// Stores
import { useGeneratorStore } from '../store/useGenerator.store';
import { useFrameworkStore } from '../../frameworks/store/useFramework.store';
import { useDatabaseStore } from '../../database/store/useDatabase.store';
import { useFrontendStore } from '../../frontend/store/useFrontend.store';

// ============================================================================
// INTERFACES
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
}

// ============================================================================
// CONFIGURATION DES ÉTAPES DU WIZARD
// ============================================================================

export const WIZARD_STEP_CONFIG: Record<number, StepConfig> = {
  
  // ==========================================================================
  // ÉTAPE 1 : SÉLECTION DU FRAMEWORK
  // ==========================================================================
  1: {
    onEnter: async (_store, { fsvc }) => {
      console.log("[Étape 1] Chargement des frameworks et langues...");
      const frameworkStore = useFrameworkStore();
      
      try { 
        const [frameworksData, languagesData] = await Promise.all([
          fsvc.fetchFrameworks(),
          fsvc.fetchLanguages()
        ]);
        
        frameworkStore.setFrameworks(frameworksData);
        frameworkStore.setLanguages(languagesData);
        
        console.log(`[Étape 1] Chargement réussi : ${frameworksData.length} frameworks, ${languagesData.length} langues`);
      } catch (error) {
        console.error("[Étape 1] Échec du chargement:", error);
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
        console.log(`[Étape 1] Framework sélectionné :`, framework.name);
        return true;
      } catch (error) {
        store.setWizardError(error instanceof Error ? error.message : "Échec de la sélection du framework.");
        return false;
      }
    }
  },

  // ==========================================================================
  // ÉTAPE 2 : CONFIGURATION DU PROJET
  // ==========================================================================
  2: {
    onEnter: async (store, { fsvc }) => {
      const framework = store.stepperData.framework;
      if (!framework) return;
      
      console.log("[Étape 2] Chargement des options de configuration...");
      
      try {
        await Promise.all([
          fsvc.fetchLoggingLevels(framework.id).then(data => store.setAvailableLoggingLevels(data)),
          fsvc.fetchSecurityTypes(framework.id).then(data => store.setAvailableSecurityTypes(data)),
          fsvc.fetchCacheProviders(framework.id).then(data => store.setAvailableCacheProviders(data)),
          fsvc.fetchLanguageVersions(framework.languageId).then(data => store.setAvailableLanguageVersions(data)),
          fsvc.fetchFrameworkVersions(framework.id).then(data => store.setAvailableFrameworkVersions(data)),
          fsvc.fetchBuildTools(framework.id).then(data => store.setAvailableBuildTools(data)),
        ]);
        console.log("[Étape 2] Options de configuration chargées");
      } catch (error) {
        console.error("[Étape 2] Échec du chargement des options:", error);
      }
    },
    
    beforeNext: async (store) => {
      const config = store.stepperData.config;
      
      if (!config.projectName || !config.projectLocation) {
        store.setWizardError("Le nom du projet et la localisation sont obligatoires.");
        return false;
      }

      try {
        console.log("[Étape 2] Configuration du projet validée");
        return true;
      } catch (error) {
        store.setWizardError(error instanceof Error ? error.message : "Échec de la sauvegarde de la configuration.");
        return false;
      }
    }
  },

  // ==========================================================================
  // ÉTAPE 3 : SÉLECTION DE LA BASE DE DONNÉES
  // ==========================================================================
  3: {
    onEnter: async (_store, { dbsvc }) => {
      console.log("[Étape 3] Chargement des moteurs de base de données...");
      const databaseStore = useDatabaseStore();
      
      try {
        const engines = await dbsvc.fetchDatabaseEngines();
        databaseStore.setAvailableEngines(engines);
        console.log(`[Étape 3] ${engines.length} moteurs chargés`);
      } catch (error) {
        console.error("[Étape 3] Échec du chargement des moteurs DB:", error);
        _store.setWizardError("Impossible de charger la liste des bases de données.");
      }
    },
    
    beforeNext: async (store) => {
      if (!store.pendingDatabaseEngine) {
        store.setWizardError("Veuillez sélectionner un moteur de base de données.");
        return false;
      }

      try {
        // Engagement du choix dans stepperData
        store.setDatabaseEngine(store.pendingDatabaseEngine);

        // Recherche de l'ID du moteur pour l'appel API
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

        console.log(`[Étape 3] Sélection BDD validée. ID : ${matchedEngine.id}`);
        return true;
      } catch (error) {
        store.setWizardError(error instanceof Error ? error.message : "Échec de la sélection de la base de données.");
        return false;
      }
    }
  },

  // ==========================================================================
  // ÉTAPE 4 : CONFIGURATION DE LA BASE DE DONNÉES
  // ==========================================================================
  4: {
    beforeNext: async (store) => {
      try {
        console.log("[Étape 4] Configuration BDD validée");
        return true;
      } catch (error) {
        store.setWizardError(error instanceof Error ? error.message : "Échec de la sauvegarde des credentials.");
        return false;
      }
    }
  },

  // ==========================================================================
  // ÉTAPE 5 : SCRIPT / IA (Skippable)
  // ==========================================================================
  5: {
    beforeNext: async (store, { svc }) => {
      const script = store.stepperData.script;
      
      if (script.path || script.content) {
        try { 
          await svc.saveScriptConfig(script); 
        } catch (error) { 
          console.warn("[Étape 5] Échec sauvegarde script (non bloquant):", error); 
        }
      }
      
      return true;
    }
  },

  // ==========================================================================
  // ÉTAPE 6 : SÉLECTION DES TABLES ET VUES
  // ==========================================================================
  6: {
    onEnter: async (store, { svc }) => {
      console.log("[Étape 6] Chargement des tables et vues...");
      
      try {
        const tables = await svc.fetchTablesMetadata();
        store.setAvailableTables(tables);
        console.log(`[Étape 6] ${tables.length} tables/vues chargées`);
      } catch (error) {
        console.error("[Étape 6] Échec du chargement des tables:", error);
      }
    },
    
    beforeNext: async (store) => {
      try {
        console.log("[Étape 6] Sélection des tables validée");
        return true;
      } catch (error) {
        store.setWizardError(error instanceof Error ? error.message : "Échec de la sauvegarde des tables.");
        return false;
      }
    }
  },

  // ==========================================================================
  // ÉTAPE 7 : RELATIONS
  // ==========================================================================
  7: {
    beforeNext: async (store) => {
      try {
        console.log("[Étape 7] Configuration des relations validée");
        return true;
      } catch (error) {
        store.setWizardError(error instanceof Error ? error.message : "Échec de la sauvegarde des relations.");
        return false;
      }
    }
  },

  // ==========================================================================
  // ÉTAPE 8 : SÉLECTION DU FRONTEND (Skippable)
  // ==========================================================================
  8: {
    onEnter: async (_store, { fdsvc }) => {
      console.log("[Étape 8] Chargement des options Frontend...");
      const frontendStore = useFrontendStore();
      
      try {
        await Promise.all([
          fdsvc.fetchFrontendFrameworks().then(data => frontendStore.setAvailableFrontendFrameworks(data)),
          fdsvc.fetchInterfaceLanguages().then(data => frontendStore.setAvailableInterfaceLanguages(data)),
        ]);
        console.log("[Étape 8] Options Frontend chargées");
      } catch (error) {
        console.error("[Étape 8] Échec du chargement des options Frontend:", error);
        _store.setWizardError("Impossible de charger les options frontend.");
      }
    },
    
    beforeNext: async (store) => {
      // Si aucun framework n'est sélectionné, l'étape est considérée comme skippée
      if (!store.pendingFrontendFramework) {
        console.log("[Étape 8] Frontend ignoré (skippable)");
        return true;
      }

      try {
        // Engagement du choix dans stepperData
        store.setSelectedFrontendFramework(store.pendingFrontendFramework);

        const frontendId = store.stepperData.frontend?.id;
        if (frontendId) {
          console.log(`[Étape 8] Sélection Frontend validée. ID : ${frontendId}`);
        }
        
        return true;
      } catch (error) {
        store.setWizardError(error instanceof Error ? error.message : "Échec de la sélection du frontend.");
        return false;
      }
    }
  },

  // ==========================================================================
  // ÉTAPE 9 : CONFIGURATION DU LAYOUT FRONTEND
  // ==========================================================================
  9: {
    beforeNext: async (store) => {
      try {
        console.log("[Étape 9] Configuration layout Frontend validée");
        return true;
      } catch (error) {
        store.setWizardError(error instanceof Error ? error.message : "Échec de la sauvegarde du layout frontend.");
        return false;
      }
    }
  },

  // ==========================================================================
  // ÉTAPE 10 : CONFIGURATION GIT
  // ==========================================================================
  10: {
    beforeNext: async (store) => {
      try {
        console.log("[Étape 10] Configuration Git validée");
        return true;
      } catch (error) {
        store.setWizardError(error instanceof Error ? error.message : "Échec de la sauvegarde Git.");
        return false;
      }
    }
  }
};
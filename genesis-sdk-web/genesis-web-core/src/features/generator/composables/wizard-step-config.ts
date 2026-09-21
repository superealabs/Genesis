import { useGeneratorStore } from '../store/useGenerator.store';
import type { IGeneratorService } from '../types/generator.service.interface';
import type { IFrontendService } from '../../frontend/types/frontend.service.interface';
import type { IDatabaseService } from '../../database/types/database.service.interface';
import type { IFrameworkService } from '@genesis-labs/shared-types'; // Adapte le chemin si nécessaire
import { useFrameworkStore } from '../../frameworks/manifest';

export interface WizardServices {
    svc: IGeneratorService;
    fdsvc: IFrontendService;
    dbsvc: IDatabaseService;
    fsvc: IFrameworkService;
}

/**
 * Configuration d'une étape du Wizard.
 * Les deux propriétés sont optionnelles : une étape peut n'avoir que du chargement, 
 * que de la validation, ou les deux.
 */
export interface StepConfig {
    onEnter?: (store: ReturnType<typeof useGeneratorStore>, services: WizardServices) => Promise<void> | void;
    beforeNext?: (store: ReturnType<typeof useGeneratorStore>, services: WizardServices) => Promise<boolean> | boolean;
}

/**
 * Registre central de la logique de chaque étape.
 * C'est la "Single Source of Truth" pour le comportement du Wizard.
 */
export const WIZARD_STEP_CONFIG: Record<number, StepConfig> = {
    
    // ═══ ÉTAPE 1 : FRAMEWORK ═══
    1: {
        onEnter: async (_store, { fsvc }) => {
            console.log("🔄 [Étape 1] Chargement des frameworks...");
            try { 
                // ✅ 2. On récupère les données
                const data = await fsvc.fetchFrameworks();
                console.log("✅ Récupération réussie :", data.length, "frameworks");
                
                // on injecte dans le store ou via le composable
                // ✅ 3. On les injecte dans le store dédié
                const frameworkStore = useFrameworkStore();
                frameworkStore.setFrameworks(data);
                console.log(`contenu du framework store : ${frameworkStore.frameworks}`)
            } catch (error) {
                console.error("❌ Échec chargement frameworks:", error);
                // Optionnel : tu peux aussi prévenir l'utilisateur via le store du wizard
                _store.setWizardError("Impossible de charger la liste des frameworks.");
            }
        },
        beforeNext: async (store, { svc }) => {
            const fw = store.pendingFramework;
            if (!fw) {
                store.setWizardError("Veuillez sélectionner un framework avant de continuer.");
                return false;
            }
            try {
                store.setFramework(fw);
                console.log(svc)
                // await svc.selectFramework(fw.id); 
                return true;
            } catch (error) {
                store.setWizardError(error instanceof Error ? error.message : "Échec de la sélection du framework.");
                return false;
            }
        }
    },


    // ═══ ÉTAPE 2 : CONFIGURATION PROJET ═══
    2: {
        onEnter: async (store, { svc }) => {
            const fw = store.stepperData.framework;
            if (!fw) return;
            console.log("🔄 [Étape 2] Chargement des options de configuration...");
            try {
                await Promise.all([
                    svc.fetchLoggingLevels(fw.id).then(data => store.setAvailableLoggingLevels(data)),
                    svc.fetchSecurityTypes(fw.id).then(data => store.setAvailableSecurityTypes(data)),
                    svc.fetchCacheProviders(fw.id).then(data => store.setAvailableCacheProviders(data)),
                    svc.fetchLanguageVersions(fw.languageId).then(data => store.setAvailableLanguageVersions(data)),
                    svc.fetchFrameworkVersions(fw.id).then(data => store.setAvailableFrameworkVersions(data)),
                    svc.fetchBuildTools(fw.id).then(data => store.setAvailableBuildTools(data)),
                ]);
            } catch (error) {
                console.error("❌ Échec chargement options config:", error);
            }
 },
        beforeNext: async (store, { svc }) => {
            const config = store.stepperData.config;
            if (!config.projectName || !config.projectLocation) {
                store.setWizardError("Le nom du projet et la localisation sont obligatoires.");
                return false;
            }
            try {
                await svc.saveProjectConfig(config);
                return true;
            } catch (error) {
                store.setWizardError(error instanceof Error ? error.message : "Échec de la sauvegarde de la configuration.");
                return false;
            }
        }
    },

    // ═══ ÉTAPE 3 : SÉLECTION BASE DE DONNÉES ═══
    3: {
        onEnter: async (store, { dbsvc }) => {
            console.log("🔄 [Étape 3] Chargement des moteurs de base de données...");
            try {
                const engines = await dbsvc.fetchDatabaseEngines();
                store.setAvailableDatabaseEngines(engines);
            } catch (error) {
                console.error("❌ Échec chargement moteurs DB:", error);
            }
        },
        beforeNext: async (store, { svc }) => {
            const db = store.stepperData.database;
            if (!db.engine) {
                store.setWizardError("Veuillez sélectionner un moteur de base de données.");
                return false;
            }
            try {
                // TODO: Récupérer dynamiquement l'ID du moteur sélectionné depuis le store
                const engineId = 1; 
                await svc.selectDatabase(engineId);
                return true;
            } catch (error) {
                store.setWizardError(error instanceof Error ? error.message : "Échec de la sélection de la base de données.");
                return false;
            }
        }
    },

    // ═══ ÉTAPE 4 : CONFIGURATION BASE DE DONNÉES ═══
    4: {
        beforeNext: async (store, { svc }) => {
            try {
                await svc.saveDatabaseConfig(store.stepperData.database);
                return true;
            } catch (error) {
                store.setWizardError(error instanceof Error ? error.message : "Échec de la sauvegarde des credentials.");
                return false;
            }
        }
    },

    // ═══ ÉTAPE 5 : SCRIPT / IA (Skippable) ═══
    5: {
        beforeNext: async (store, { svc }) => {
            const script = store.stepperData.script;
            if (script.path || script.content) {
                try { await svc.saveScriptConfig(script); } 
                catch (error) { console.warn("Échec sauvegarde script (non bloquant):", error); }
            }
            return true; // Toujours true car skippable
        }
    },

    // ═══ ÉTAPE 6 : SÉLECTION TABLES/VUES ═══
    6: {
        onEnter: async (store, { svc }) => {
            console.log("🔄 [Étape 6] Chargement des tables et vues...");
            try {
                const tables = await svc.fetchTablesMetadata();
                store.setAvailableTables(tables);
            } catch (error) {
                console.error("❌ Échec chargement tables:", error);
            }
        },
        beforeNext: async (store, { svc }) => {
            try {
                await svc.saveTableSelection(store.stepperData.tableSelection);
                return true;
            } catch (error) {
                store.setWizardError(error instanceof Error ? error.message : "Échec de la sauvegarde des tables.");
                return false;
            }
        }
    },

    // ═══ ÉTAPE 7 : RELATIONS ═══
    7: {
        beforeNext: async (store, { svc }) => {
            try {
                await svc.saveRelationParameters(store.relations); // Correction: store.relations au lieu de store.getRelations
                return true;
            } catch (error) {
                store.setWizardError(error instanceof Error ? error.message : "Échec de la sauvegarde des relations.");
                return false;
            }
        }
    },

    // ═══ ÉTAPE 8 : SÉLECTION FRONTEND (Skippable) ═══
    8: {
        onEnter: async (store, { fdsvc }) => {
            console.log("🔄 [Étape 8] Chargement des options Frontend...");
            try {
                await Promise.all([
                    fdsvc.fetchFrontendFrameworks().then(data => store.setAvailableFrontendFrameworks(data)),
                    fdsvc.fetchInterfaceLanguages().then(data => store.setAvailableLanguages(data)),
                ]);
            } catch (error) {
                console.error("❌ Échec chargement options Frontend:", error);
            }
        },
        beforeNext: async (store, { svc }) => {
            const frontend = store.stepperData.frontend;
            if (frontend) {
                try {
                    await svc.selectFrontendFramework(frontend.id);
                    return true;
                } catch (error) {
                    store.setWizardError(error instanceof Error ? error.message : "Échec de la sélection du frontend.");
                    return false;
                }
            }
            return true; // Skippable
        }
    },

    // ═══ ÉTAPE 9 : CONFIGURATION LAYOUT FRONTEND ═══
    9: {
        beforeNext: async (store, { svc }) => {
            try {
                await svc.saveFrontendLayoutConfig(store.stepperData.frontendLayout);
                return true;
            } catch (error) {
                store.setWizardError(error instanceof Error ? error.message : "Échec de la sauvegarde du layout frontend.");
                return false;
            }
        }
    },

    // ═══ ÉTAPE 10 : CONFIGURATION GIT ═══
    10: {
        beforeNext: async (store, { svc }) => {
            try {
                await svc.saveGitConfiguration(store.stepperData.git);
                return true;
            } catch (error) {
                store.setWizardError(error instanceof Error ? error.message : "Échec de la sauvegarde Git.");
                return false;
            }
        }
    }
};
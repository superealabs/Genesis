// genesis-sdk-web/genesis-web-core/src/features/generator/composables/wizard-step-config.ts
import { useGeneratorStore } from '../store/useGenerator.store';
import type { IGeneratorService } from '../types/generator.service.interface';
import type { IFrontendService } from '../../frontend/types/frontend.service.interface';
import type { IDatabaseService } from '../../database/types/database.service.interface';
import type { IFrameworkService } from '@genesis-labs/shared-types';

// ✅ IMPORTS DES STORES DE FEATURE (Sources de Vérité)
import { useFrameworkStore } from '../../frameworks/store/useFramework.store';
import { useDatabaseStore } from '../../database/store/useDatabase.store';
import { useFrontendStore } from '../../frontend/store/useFrontend.store'; // ✅ AJOUTÉ

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

export const WIZARD_STEP_CONFIG: Record<number, StepConfig> = {
    
    // ═══ ÉTAPE 1 : FRAMEWORK ═══
    1: {
        onEnter: async (_store, { fsvc }) => {
            console.log("🔄 [Étape 1] Chargement des frameworks...");
            try { 
                const data = await fsvc.fetchFrameworks();
                console.log("✅ Récupération réussie :", data.length, "frameworks");
                
                // ✅ Source de vérité : useFrameworkStore
                const frameworkStore = useFrameworkStore();
                frameworkStore.setFrameworks(data);

                const languagesData = await fsvc.fetchLanguages();
                frameworkStore.setLanguages(languagesData);
                console.log("✅ Récupération réussie :", languagesData.length, "langues");

            } catch (error) {
                console.error("❌ Échec chargement frameworks:", error);
                _store.setWizardError("Impossible de charger la liste des frameworks.");
            }
        },
        beforeNext: async (store, { }) => {
            const fw = store.pendingFramework;
            if (!fw) {
                store.setWizardError("Veuillez sélectionner un framework avant de continuer.");
                return false;
            }

            // console.log(svc);
            try {
                store.setFramework(fw);
                console.log(`Framework séléctionné :`, JSON.stringify(fw, null, 2));
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
        onEnter: async (store, { fsvc }) => {
            const fw = store.stepperData.framework;
            if (!fw) return;
            console.log("🔄 [Étape 2] Chargement des options de configuration...");
            try {
                await Promise.all([
                    fsvc.fetchLoggingLevels(fw.id).then(data => store.setAvailableLoggingLevels(data)),
                    fsvc.fetchSecurityTypes(fw.id).then(data => store.setAvailableSecurityTypes(data)),
                    fsvc.fetchCacheProviders(fw.id).then(data => store.setAvailableCacheProviders(data)),
                    fsvc.fetchLanguageVersions(fw.languageId).then(data => store.setAvailableLanguageVersions(data)),
                    fsvc.fetchFrameworkVersions(fw.id).then(data => store.setAvailableFrameworkVersions(data)),
                    fsvc.fetchBuildTools(fw.id).then(data => store.setAvailableBuildTools(data)),
                ]);
            } catch (error) {
                console.error("❌ Échec chargement options config:", error);
            }
        },
        beforeNext: async (store, { svc }) => {
            const config = store.stepperData.config;
            // if (!config.projectName || !config.projectLocation) {
            //     store.setWizardError("Le nom du projet et la localisation sont obligatoires.");
            //     return false;
            // }

            // BON (Affiche l'objet formaté avec des sauts de ligne)
            console.log("configuration du projet :", JSON.stringify(config, null, 2));
            try {
                console.log(svc);
                // jjsdkjqshdkqsjhd
                // dqsdsqd
                // await svc.saveProjectConfig(config);
                return true;
            } catch (error) {
                store.setWizardError(error instanceof Error ? error.message : "Échec de la sauvegarde de la configuration.");
                return false;
            }
        }
    },

    // ═══ ÉTAPE 3 : SÉLECTION BASE DE DONNÉES ═══
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
            // 1. On vérifie le brouillon (pending), pas encore stepperData
            if (!store.pendingDatabaseEngine) {
                store.setWizardError("Veuillez sélectionner un moteur de base de données.");
                return false;
            }

            try {
                // 2. ENGAGEMENT : On officialise le choix dans stepperData
                store.setDatabaseEngine(store.pendingDatabaseEngine);

                // 3. RECHERCHE DYNAMIQUE DE L'ID pour l'API
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

                const engineId = matchedEngine.id;
                console.log(`[Wizard] Sélection de la BDD validée. ID récupéré :`, engineId);
                
                console.log(svc);
                // await svc.selectDatabase(engineId);
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
                console.log(svc);
                console.log(`configuration de la base de donnée : `, JSON.stringify(store.stepperData.database, null, 2));
                // await svc.saveDatabaseConfig(store.stepperData.database);
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
                try { 
                    await svc.saveScriptConfig(script); 
                } 
                catch (error) { console.warn("Échec sauvegarde script (non bloquant):", error); }
            }
            return true;
        }
    },

    // ═══ ÉTAPE 6 : SÉLECTION TABLES/VUES ═══
    6: {
        onEnter: async (store, { svc }) => {
            console.log("🔄 [Étape 6] Chargement des tables et vues...");
            try {
                const tables = await svc.fetchTablesMetadata();
                store.setAvailableTables(tables); // ✅ Reste dans GeneratorStore (contexte spécifique)
            } catch (error) {
                console.error("❌ Échec chargement tables:", error);
            }
        },
        beforeNext: async (store, { svc }) => {
            try {
                // await svc.saveTableSelection(store.stepperData.tableSelection);
                                console.log(svc);

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
                // store.relations est un Ref unwrap par Pinia, c'est correct
                // await svc.saveRelationParameters(store.relations); 
                                console.log(svc);

                return true;
            } catch (error) {
                store.setWizardError(error instanceof Error ? error.message : "Échec de la sauvegarde des relations.");
                return false;
            }
        }
    },

    // ═══ ÉTAPE 8 : SÉLECTION FRONTEND (Skippable) ═══
    8: {
        onEnter: async (_store, { fdsvc }) => {
            console.log("🔄 [Étape 8] Chargement des options Frontend...");
            // ✅ Source de vérité : useFrontendStore
            const frontendStore = useFrontendStore();
            try {
                await Promise.all([
                    fdsvc.fetchFrontendFrameworks().then(data => frontendStore.setAvailableFrontendFrameworks(data)),
                    fdsvc.fetchInterfaceLanguages().then(data => frontendStore.setAvailableInterfaceLanguages(data)), // ✅ Méthode renommée
                ]);
            } catch (error) {
                console.error("❌ Échec chargement options Frontend:", error);
                _store.setWizardError("Impossible de charger les options frontend.");
            }
        },
        beforeNext: async (store, { svc }) => {
            // 1. On vérifie le brouillon (pending). 
            // Si c'est null, c'est que l'étape est skippée, donc on autorise le passage.
            console.log(svc);
            if (!store.pendingFrontendFramework) {
                return true; // Skippable
            }

            try {
                // 2. ENGAGEMENT : On officialise le choix dans stepperData
                store.setSelectedFrontendFramework(store.pendingFrontendFramework);

                // 3. Appel API avec l'ID du framework maintenant engagé
                const frontendId = store.stepperData.frontend?.id;
                if (frontendId) {
                    console.log(`[Wizard] Sélection du Frontend validée. ID :`, frontendId);
                    // await svc.selectFrontendFramework(frontendId);
                }
                
                return true;
            } catch (error) {
                store.setWizardError(error instanceof Error ? error.message : "Échec de la sélection du frontend.");
                return false;
            }
        }
    },

    // ═══ ÉTAPE 9 : CONFIGURATION LAYOUT FRONTEND ═══
    9: {
        beforeNext: async (store, { svc }) => {
            try {
                // await svc.saveFrontendLayoutConfig(store.stepperData.frontendLayout);
                                console.log(svc);

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
                // await svc.saveGitConfiguration(store.stepperData.git);
                console.log(svc);
                return true;
            } catch (error) {
                store.setWizardError(error instanceof Error ? error.message : "Échec de la sauvegarde Git.");
                return false;
            }
        }
    }
};
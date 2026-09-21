import { useGeneratorStore } from '../store/useGenerator.store';
import type { WizardServices } from './wizard-step-handlers-beforeNext';

/**
 * Dictionnaire des actions à exécuter À L'ARRIVÉE sur une étape.
 * Idéal pour le chargement des données (Lazy Loading).
 */
export const ON_ENTER_HANDLERS: Record<
    number, 
    (store: ReturnType<typeof useGeneratorStore>, services: WizardServices) => Promise<void> | void
> = {
    // ═══ ÉTAPE 1 : FRAMEWORK ═══
    1: async (_store, { fsvc }) => { // ✅ _store préfixé car non utilisé ici (géré par useFrameworks)
        console.log("🔄 [OnEnter] Chargement des frameworks...");
        try {
            await fsvc.fetchFrameworks(); 
            // Note: Le composable useFrameworks intercepte et met à jour son propre store.
        } catch (error) {
            console.error("❌ [OnEnter] Échec du chargement des frameworks:", error);
        }
    },

    // ═══ ÉTAPE 2 : CONFIGURATION PROJET ═══
    2: async (store, { svc }) => {
        const fw = store.stepperData.framework;
        if (!fw) return;

        console.log("🔄 [OnEnter] Chargement des options de configuration (Étape 2)...");
        try {
            await Promise.all([
                svc.fetchLoggingLevels(fw.id).then(data => store.setAvailableLoggingLevels(data)),
                svc.fetchSecurityTypes(fw.id).then(data => store.setAvailableSecurityTypes(data)),
                svc.fetchCacheProviders(fw.id).then(data => store.setAvailableCacheProviders(data)),
                svc.fetchLanguageVersions(fw.languageId).then(data => store.setAvailableLanguageVersions(data)), // ✅ CORRECTION
                svc.fetchFrameworkVersions(fw.id).then(data => store.setAvailableFrameworkVersions(data)),
                svc.fetchBuildTools(fw.id).then(data => store.setAvailableBuildTools(data)),
            ]);
        } catch (error) {
            console.error("❌ [OnEnter] Échec du chargement des options de configuration:", error);
        }
    },

    // ═══ ÉTAPE 3 : SÉLECTION BASE DE DONNÉES ═══
    3: async (store, { dbsvc }) => {
        console.log("🔄 [OnEnter] Chargement des moteurs de base de données...");
        try {
            const engines = await dbsvc.fetchDatabaseEngines();
            store.setAvailableDatabaseEngines(engines); // ✅ CORRECTION : Utilise la nouvelle action
        } catch (error) {
            console.error("❌ [OnEnter] Échec du chargement des moteurs DB:", error);
        }
    },

    // ═══ ÉTAPE 6 : SÉLECTION TABLES/VUES ═══
    6: async (store, { svc }) => {
        console.log("🔄 [OnEnter] Chargement des tables et vues...");
        try {
            const tables = await svc.fetchTablesMetadata();
            store.setAvailableTables(tables);
        } catch (error) {
            console.error("❌ [OnEnter] Échec du chargement des tables:", error);
        }
    },

    // ═══ ÉTAPE 8 : SÉLECTION FRONTEND ═══
    8: async (store, { fdsvc }) => {
        console.log("🔄 [OnEnter] Chargement des options Frontend...");
        try {
            await Promise.all([
                fdsvc.fetchFrontendFrameworks().then(data => store.setAvailableFrontendFrameworks(data)), // ✅ CORRECTION
                fdsvc.fetchInterfaceLanguages().then(data => store.setAvailableLanguages(data)),
            ]);
        } catch (error) {
            console.error("❌ [OnEnter] Échec du chargement des options Frontend:", error);
        }
    }
};
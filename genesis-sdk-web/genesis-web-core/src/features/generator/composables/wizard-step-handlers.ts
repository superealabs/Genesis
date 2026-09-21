import { useGeneratorStore } from '../store/useGenerator.store';
import type { IGeneratorService } from '../types/generator.service.interface';
import type { IFrontendService } from '../../frontend/types/frontend.service.interface';
import type { IDatabaseService } from '../../database/types/database.service.interface';

export interface WizardServices {
    svc: IGeneratorService;
    fdsvc: IFrontendService;
    dbsvc: IDatabaseService;
}

/**
 * Dictionnaire des actions à exécuter avant de passer à l'étape suivante.
 * Retourne `true` pour autoriser la navigation, `false` pour la bloquer.
 */
export const STEP_HANDLERS: Record<
    number, 
    (store: ReturnType<typeof useGeneratorStore>, services: WizardServices) => Promise<boolean>
> = {
    // ═══ ÉTAPE 1 : FRAMEWORK ═══
    1: async (store, { svc }) => {
        const fw = store.pendingFramework;
        if (!fw) {
            store.setWizardError("Veuillez sélectionner un framework avant de continuer.");
            return false;
        }
        try {
            store.setFramework(fw);
            console.log(svc);
            // await svc.selectFramework(fw.id);
            return true;
        } catch (error) {
            store.setWizardError(error instanceof Error ? error.message : "Échec de la sélection du framework.");
            return false;
        }
    },

    // ═══ ÉTAPE 2 : CONFIGURATION PROJET ═══
    2: async (store, { svc }) => {
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
    },

    // ═══ ÉTAPE 3 : SÉLECTION BASE DE DONNÉES ═══
    3: async (store, { svc }) => {
        const db = store.stepperData.database;
        if (!db.engine) {
            store.setWizardError("Veuillez sélectionner un moteur de base de données.");
            return false;
        }
        try {
            // On suppose que tu as un ID quelque part, sinon adapte selon ton store
            const engineId = 1; // À récupérer dynamiquement selon ton store
            await svc.selectDatabase(engineId);
            return true;
        } catch (error) {
            store.setWizardError(error instanceof Error ? error.message : "Échec de la sélection de la base de données.");
            return false;
        }
    },

    // ═══ ÉTAPE 4 : CONFIGURATION BASE DE DONNÉES ═══
    4: async (store, { svc }) => {
        try {
            await svc.saveDatabaseConfig(store.stepperData.database);
            return true;
        } catch (error) {
            store.setWizardError(error instanceof Error ? error.message : "Échec de la sauvegarde des credentials.");
            return false;
        }
    },

    // ═══ ÉTAPE 5 : SCRIPT / IA (Skippable) ═══
    5: async (store, { svc }) => {
        const script = store.stepperData.script;
        // On ne sauvegarde que si un chemin ou un contenu existe
        if (script.path || script.content) {
            try {
                await svc.saveScriptConfig(script);
            } catch (error) {
                console.warn("Échec de la sauvegarde du script (non bloquant) :", error);
            }
        }
        return true; // Toujours true car skippable
    },

    // ═══ ÉTAPE 6 : SÉLECTION TABLES/VUES ═══
    6: async (store, { svc }) => {
        try {
            await svc.saveTableSelection(store.stepperData.tableSelection);
            return true;
        } catch (error) {
            store.setWizardError(error instanceof Error ? error.message : "Échec de la sauvegarde des tables.");
            return false;
        }
    },

    // ═══ ÉTAPE 7 : RELATIONS ═══
    7: async (store, { svc }) => {
        try {
            await svc.saveRelationParameters(store.getRelations); // ou store.stepperData.relations selon ton store
            return true;
        } catch (error) {
            store.setWizardError(error instanceof Error ? error.message : "Échec de la sauvegarde des relations.");
            return false;
        }
    },

    // ═══ ÉTAPE 8 : SÉLECTION FRONTEND (Skippable) ═══
    8: async (store, { svc }) => {
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
        return true; // Skippable si aucun frontend choisi
    },

    // ═══ ÉTAPE 9 : CONFIGURATION LAYOUT FRONTEND ═══
    9: async (store, { svc }) => {
        try {
            await svc.saveFrontendLayoutConfig(store.stepperData.frontendLayout);
            return true;
        } catch (error) {
            store.setWizardError(error instanceof Error ? error.message : "Échec de la sauvegarde du layout frontend.");
            return false;
        }
    },

    // ═══ ÉTAPE 10 : CONFIGURATION GIT ═══
    10: async (store, { svc }) => {
        try {
            await svc.saveGitConfiguration(store.stepperData.git);
            return true;
        } catch (error) {
            store.setWizardError(error instanceof Error ? error.message : "Échec de la sauvegarde Git.");
            return false;
        }
    }
};
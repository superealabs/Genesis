// genesis-sdk-web/genesis-web-core/src/features/generator/composables/wizard-step-config.ts

// Types & Interfaces
import type { IGeneratorService } from '../types/generator.service.interface';
import type { IFrontendService } from '../../frontend/types/frontend.service.interface';
import type { IDatabaseService } from '../../database/types/database.service.interface';
import type { IFrameworkService, GenerationParameter, ComponentType, IRepositoryService, ProjectConfig } from '@genesis-labs/shared-types';

// Stores
import { useGeneratorStore } from '../store/useGenerator.store';
import { useFrameworkStore } from '../../frameworks/store/useFramework.store';
import { useDatabaseStore } from '../../database/store/useDatabase.store';
import { useFrontendStore } from '../../frontend/store/useFrontend.store';
import { useRepositoryStore } from '../../repository/manifest';

import { useRecentStore } from '@genesis-labs/web-core/core/features/recent/store/useRecent.store';
import { RecentItem } from '@genesis-labs/shared-types';
import type { IRecentService } from '@genesis-labs/web-core/core/features/recent/types/recent.service.interface';
import { useGenesisWarning } from '@genesis-labs/web-core/core/composables/ux/feedback/useGenesisWarning';

import { toRaw } from 'vue';
import { Framework } from '@genesis-labs/shared-types';
import { buildProjectConfigPayload } from '../utils/projectConfigHelpers';
import {
  validateStep,
  validateBeforeGeneration,
  validateDatabaseFields,
  validateGenerationParameters,
  isRestFramework,
} from '../utils/validators/stepValidators';


// ============================================================================
// INTERFACES & TYPES
// ============================================================================

export interface WizardServices {
  svc: IGeneratorService;
  fdsvc: IFrontendService;
  dbsvc: IDatabaseService;
  fsvc: IFrameworkService;
  repoSvc: IRepositoryService;
  recentSvc: IRecentService;
}

type GeneratorStore = ReturnType<typeof useGeneratorStore>;

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
  | 'framework_select'
  | 'project_config'
  | 'database_select'
  | 'database_config'
  | 'script'
  | 'generation'
  | 'relations'
  | 'frontend_select'
  | 'frontend_config'
  | 'repository_select'
  | 'repository_config'
  | 'summary';

export const STEP_LABELS: string[] = [
  'Framework',
  'Configuration Projet',
  'Sélection BDD',
  'Configuration BDD',
  'Script / IA',
  'Génération',
  'Relations',
  'Sélection Frontend',
  'Configuration Frontend',
  'Sélection Dépôt',
  'Configuration Dépôt',
  'Résumé'
];

// ============================================================================
// SOURCE DE VÉRITÉ : ORDRE DES ÉTAPES
// ============================================================================

/**
 * L'ordre de ce tableau définit l'ordre d'exécution du wizard.
 * Pour réorganiser le wizard, il suffit de réorganiser ce tableau.
 */
export const WIZARD_STEP_ORDER: StepId[] = [
  'framework_select',
  'project_config',
  'database_select',
  'database_config',
  'script',
  'generation',
  'relations',
  'frontend_select',
  'frontend_config',
  'repository_select',
  'repository_config',
  'summary'
];

// ============================================================================
// MAPPING WIZARD <-> SUMMARY
// ============================================================================
// Index du résumé : 0:Project, 1:Database, 2:Generation, 3:Relations, 4:Frontend, 5:Repository
export const STEP_TO_SUMMARY_MAP: Record<StepId, number | null> = {
  'framework_select': 0,
  'project_config': 0,
  'database_select': 1,
  'database_config': 1,
  'script': null, // Intentionnel : pas de résumé
  'generation': 2,
  'relations': 3,
  'frontend_select': 4,
  'frontend_config': 4,
  'repository_select': 5,
  'repository_config': 5,
  'summary': null
};

// Mapping inverse : quel est le premier step de config associé à un index de résumé ?
export const SUMMARY_TO_STEP_MAP: Record<number, StepId> = {
  0: 'project_config',
  1: 'database_config',
  2: 'generation',
  3: 'relations',
  4: 'frontend_config',
  5: 'repository_config'
};

// ==========================================
// HELPER
// =========================================


const frontendImpacted = (from: Framework, to: Framework) => !(isRestFramework(from) && isRestFramework(to));

/**
 * Affiche les problèmes d'un validator dans le wizard.
 * Renvoie true s'il y en a (→ `return false` dans le beforeNext).
 */
function reject(store: GeneratorStore, issues: string[]): boolean {
  if (issues.length === 0) return false;
  store.setWizardError(issues.join('\n'));
  return true;
}

export async function confirmFrameworkChange(store: GeneratorStore, next: Framework): Promise<boolean> {
  const owner = store.configFramework;
  if (!owner || owner.id === next.id) return true;

  const resetFrontend = frontendImpacted(owner, next) && store.hasFrontendData;

  const details = [
    'Versions du langage et du framework, build tool',
    'Configuration avancée (logging, sécurité, cache, Hibernate)'
  ];
  if (resetFrontend) {
    details.push('Frontend sélectionné et sa configuration (chaque framework MVC a sa propre liste de frontends)');
  }

  const confirmed = await useGenesisWarning().confirm({
    title: 'Changer de framework ?',
    message: `Vous passez de ${owner.name} à ${next.name}. Certaines données de la configuration seront réinitialisées.`,
    details,
    confirmLabel: 'Changer de framework',
    variant: 'danger'
  });
  if (!confirmed) return false;

  store.resetFrameworkRelatedProjectConfig();
  store.resetFrameworkDependentConfig(next);
  
  if (frontendImpacted(owner, next)) store.resetFrontendDependentConfig();

  const invalidated: StepId[] = ['project_config'];
  if (resetFrontend) invalidated.push('frontend_select', 'frontend_config');
  store.invalidateSteps(invalidated);

  return true;
}

// ============================================================================
// CONFIGURATION MÉTIER PAR ÉTAPE
// ============================================================================

export const WIZARD_STEP_CONFIG: Record<StepId, StepConfig> = {
  
  framework_select: {
    onEnter: async (_store, { fsvc, recentSvc }) => {
      console.log("[Wizard: framework] Chargement des frameworks, langues et données récentes...");
      
      const frameworkStore = useFrameworkStore();
      const recentStore = useRecentStore();

      console.log('[framework_select.onEnter] pendingFramework:', _store.pendingFramework?.name);
      console.log('[framework_select.onEnter] stepperData.framework:', _store.stepperData.framework?.name);
      console.log('[framework_select.onEnter] configFramework:', _store.configFramework?.name);
      
      

      try { 
        const [frameworksData, languagesData, recencyData] = await Promise.all([
          fsvc.fetchFrameworks(),
          fsvc.fetchLanguages(),
          recentSvc.getRecencyData() 
        ]);
        
        frameworkStore.setFrameworks(frameworksData);
        frameworkStore.setLanguages(languagesData);
        recentStore.setRecencyData(recencyData);
        
        console.log(`[Wizard: framework] Chargement réussi : ${frameworksData.length} frameworks, ${languagesData.length} langues, ${recencyData.frameworks.length} récents.`);
      } catch (error) {
        console.error("[Wizard: framework] Échec du chargement:", error);
        _store.setWizardError("Impossible de charger la liste des frameworks.");
      }
    },
    
    beforeNext: async (store, { svc }) => {
      const framework = store.pendingFramework;
      
      if (!framework) {
        store.setWizardError("Veuillez sélectionner un framework avant de continuer.");
        return false;
      }

      console.log('[beforeNext] AVANT — buildTool =', store.stepperData.config.buildTool, '| configFramework =', store.configFramework?.name);
      // Confirmation du changement (rollback si refus)
      if (!(await confirmFrameworkChange(store, framework))) {
        store.cancelPendingFrameworkChange();
        store.resetFrameworkRelatedProjectConfig();
        return false;
      }
      console.log('[framework_select.beforeNext] Après confirm, buildTool =', store.stepperData.config.buildTool);

      // Commit local
      store.setFramework(framework);
      console.log(`[Wizard: framework_select] Framework commit local :`, framework.name);

      // Commit backend (attendu + gestion d'erreur bloquante)
      const previous = store.stepperData.framework;
      store.setFramework(framework);

      try {
        console.log("FRAMEWORK SELECTED IN API : ", toRaw(framework));
        await svc.selectFramework(framework.id);
        return true;
      } catch (error) {
        // 👇 Rollback local
        if (previous) {
          store.setFramework(previous);
        } else {
          store.setPendingFramework(null);
        }
        store.setWizardError(`Erreur à la sélection du framework : ${(error as Error).message}`);
        return false;
      }

    }
  },

  project_config: {
    onEnter: async (store, { fsvc }) => {
      // On lit le pending en priorité pour charger les bonnes options
      const framework = store.pendingFramework || store.stepperData.framework;
      if (!framework) return;
      if (!store.configFramework) store.setConfigFramework(framework);
      
      console.log("[Wizard: project_config] Chargement des options de configuration...");
      
      try {
        await Promise.all([
          fsvc.fetchLoggingLevels(framework.id).then(data => store.setAvailableLoggingLevels(data)),
          fsvc.fetchSecurityTypes(framework.id).then(data => store.setAvailableSecurityTypes(data)),
          fsvc.fetchCacheProviders(framework.id).then(data => store.setAvailableCacheProviders(data)),
          fsvc.fetchLanguageVersions(framework.languageId).then(data => store.setAvailableLanguageVersions(data)),
          fsvc.fetchFrameworkVersions(framework.id).then(data => store.setAvailableFrameworkVersions(data)),
          fsvc.fetchBuildTools(framework.id).then(data => store.setAvailableBuildTools(data)),
          fsvc.fetchHibernateDdlAutoOptions(framework.id).then(data => store.setAvailableHibernateDdlAutoOptions(data))
        ]);
        console.log("[Wizard: project_config] Options de configuration chargées");
      } catch (error) {
        console.error("[Wizard: project_config] Échec du chargement des options:", error);
      }
    },
    
    beforeNext: async (store, { svc }) => {
      const config = store.stepperData.config;
      
      if (reject(store, validateStep('project_config', store))) return false;

      const payload = buildProjectConfigPayload(
        toRaw(config),
        {
          languageVersionOptions: store.getAvailableLanguageVersions.map(v => ({ label: v, value: v })),
          buildToolOptions:       store.getAvailableBuildTools.map(v => ({ label: v, value: v })),
          frameworkVersionOptions: store.getAvailableFrameworkVersions.map(v => ({ label: v, value: v })),
          loggingLevelOptions:    store.getAvailableLoggingLevels.map(v => ({ label: v, value: v })),
          securityTypeOptions:    store.getAvailableSecurityTypes.map(v => ({ label: v, value: v })),
          cacheProviderOptions:   store.getAvailableCacheProviders.map(v => ({ label: v, value: v })),
          hibernateDdlAutoOptions: store.getAvailableHibernateDdlAutoOptions.map(v => ({ label: v, value: v })),
        }
      ) as unknown as ProjectConfig;   // cast si nécessaire

      try {

        console.log("PROJECT CONFIG IN API : ", payload)
        await svc.saveProjectConfig(payload);
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

        // 👇 SYNCHRONISATION : aligner pendingDatabaseEngine sur le moteur commité
        const committedEngine = _store.stepperData.database.engine;

        if (committedEngine) {
          const matched = engines.find(e => {
            const n = e.name.toLowerCase().replace(/\s+/g, '');
            const c = committedEngine.toLowerCase().replace(/\s+/g, '');
            return n === c;  // 👈 plus de conditions postgre/postgresql
          });
          _store.setPendingDatabaseEngine(matched ?? null);
          console.log(`[Wizard: database_select] Pending sync sur moteur commité :`, matched?.name);
        } else {
          _store.setPendingDatabaseEngine(null);
        }

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

      store.setDatabaseEngine(store.pendingDatabaseEngine);
      console.log(`[Wizard: database_select] Moteur BDD commit :`, store.pendingDatabaseEngine.name);

      return true;
    }
  },

  database_config: {
    beforeNext: async (store, { dbsvc, svc }) => {
      const db = toRaw(store.stepperData.database);

      console.log("BEFORE NEXT : DATABASE, CONFIG : ", db);

      // 1. Validation des champs
      if (reject(store, validateDatabaseFields(store))) return false;

      // 2. Détection du changement (uniquement les champs métier)
      const configChanged = store.hasDatabaseConfigChanged(db);
      const isFirstCommit = !store.lastCommittedDatabaseConfig;

      // 3. Confirmation utilisateur si changement (et pas premier commit)
      if (configChanged && !isFirstCommit) {
        const confirmed = await useGenesisWarning().confirm({
          title: 'Changer de base de données ?',
          message: 'La configuration de la base de données a changé. Les associations tables/profils de l\'étape Génération seront réinitialisées.',
          details: [
            'Les tables et vues assignées seront perdues.',
            'Vous devrez reconfigurer l\'étape Génération.',
            'Les relations définies seront également réinitialisées.'
          ],
          confirmLabel: 'Continuer',
          variant: 'danger'
        });

        if (!confirmed) {
          return false;
        }
      }

      // 4. Test de connexion (avec toRaw)
      try {
        const result = await dbsvc.testDatabaseConnection(db);
        if (!result.success) {
          store.setWizardError(result.message);
          return false;
        }
      } catch (error) {
        store.setWizardError(
          error instanceof Error ? error.message : "Échec du test de connexion."
        );
        return false;
      }

      // 5. Sauvegarde backend (await !)
      try {
        await svc.saveDatabaseConfig(db);
        console.log("[Wizard: database_config] Configuration DB sauvegardée côté backend.");
      } catch (error) {
        store.setWizardError(
          error instanceof Error ? error.message : "Échec de la sauvegarde de la configuration."
        );
        return false;
      }

      // 6. Reset des associations SI changement (même premier commit : no-op si vide)
      if (configChanged) {
        console.log("[Wizard: database_config] Config DB changée → reset des associations de génération.");
        store.resetGenerationAssociations();
        store.invalidateSteps(['generation', 'relations', 'summary']);
      }

      // 7. Mémorise la nouvelle config commitée
      store.setLastCommittedDatabaseConfig(db);

      console.log("[Wizard: database_config] Configuration BDD validée (test + save OK).");
      return true;
    }
  },

  script: {
    beforeNext: async (/* store, { svc } */) => {
      // const script = store.stepperData.script;
      
      // if (script.path || script.content) {
      //   try { 
      //     await svc.saveScriptConfig(script); 
      //   } catch (error) { 
      //     console.warn("[Wizard: script] Échec sauvegarde script (non bloquant):", error); 
      //   }
      // }
      
      return true;
    },
    skipCascade: [] // Peut être sauté sans conséquence sur les autres


  },

  generation: {
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
    
    beforeNext: async (store, { svc }) => {
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
        
        // Au moins un profil avec entités + composants (sinon rien à générer)
        if (reject(store, validateGenerationParameters(generationParameters))) return false;

        const rawParams = generationParameters.map(p => ({
            profileName: p.profileName,
            selectedTables: [...p.selectedTables],
            selectedViews: [...p.selectedViews],
            selectedComponents: [...p.selectedComponents]
        }));

        console.log("BEFORE NEXT GENERATION SENDED : ", rawParams);
        await svc.saveGenerationParameters(rawParams);

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
    onEnter: async (store, { svc }) => {
      try {
        const tableParents = await svc.fetchTablesMetadataParents();
        const tableChilds = await svc.fetchTablesMetadataChilds();
        // const relations = await svc.fetchRelations();

        store.setTablesParents(tableParents);
        store.setTablesChilds(tableChilds);
        // store.setRelations(relations);
        store.pruneRelations();
        // store
      } catch (error) {
        store.setWizardError(error instanceof Error ? error.message : "Échec du chargement des relations.");
      }
    },
    beforeNext: async (store, { svc }) => {
      try {
        console.log("[Wizard: relations] Configuration des relations validée");

        const relations = store.relations;
        const relationRaw = relations.map(r => ({
            parentTable: r.parentTable,
            childTable: r.childTable,
            mandatory: r.mandatory,
            hasForm: r.hasForm
        }));

        console.log("BEFORE NEXT RELATION : ", relationRaw);

        await svc.saveRelationParameters(relationRaw);

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
        const frameworkId = _store.pendingFramework?.id ?? _store.stepperData.framework?.id ?? 0;

        console.log("ID FRAMEWORK sended TO API : ", frameworkId);

        await Promise.all([
          fdsvc.fetchFrontendFrameworks(frameworkId).then(data => frontendStore.setAvailableFrontendFrameworks(data)),
          fdsvc.fetchInterfaceLanguages().then(data => frontendStore.setAvailableInterfaceLanguages(data)),
          fdsvc.fetchFrontendProgrammingLanguages().then(data => frontendStore.setAvailableProgrammingLanguages(data))
        ]);
        console.log("[Wizard: frontend_select] Options Frontend chargées");
      } catch (error) {
        console.error("[Wizard: frontend_select] Échec du chargement des options Frontend:", error);
        _store.setWizardError("Impossible de charger les options frontend.");
      }
    },
    
    beforeNext: async (store) => {
      if (!store.pendingFrontendFramework) {
        store.setWizardError("Veuillez sélectionner un frontend");
        return false;
      }
      
      if (store.pendingFrontendFramework) {
        store.setSelectedFrontendFramework(store.pendingFrontendFramework);
        store.unmarkStepAsSkipped('frontend_select');
        store.unmarkStepAsSkipped('frontend_config');
        console.log(`[Wizard: frontend_select] Framework Frontend validé et commit :`, store.pendingFrontendFramework.name);
      }

      // Sans sélection : autorisé seulement pour un framework REST (frontend skippable)
      return !reject(store, validateStep('frontend_select', store));
    },

    onSkip: async (store) => {
      store.markStepAsSkipped('frontend_select');
      store.markStepAsSkipped('frontend_config');


      const invalidated: StepId[] = ['frontend_select', 'frontend_config'];
      store.resetFrontendDependentConfig();
      store.invalidateSteps(invalidated);
    },
    // CORRECTION MAJEURE : Si on saute le choix du frontend, on saute automatiquement sa configuration
    
    skipCascade: [
      'frontend_config'
    ] 
  },

  frontend_config: {
    beforeNext: async (store, { svc }) => {
      try {

        const frontend = store.stepperData.frontend;
        if (!frontend) {
          store.setWizardError("Aucun framework frontend sélectionné.");
          return false;
        }

        if (reject(store, validateStep('frontend_config', store))) return false;

        console.log("BEFORE_NEXT FRONTEND SAVED : ", toRaw(frontend));

        // 1. Config frontend (frameworkId + languageId + port)
        await svc.saveFrontendConfig(
          frontend.id,
          frontend.languageId ?? null,
          frontend.defaultPort
        );

        const config = store.stepperData.frontendLayout;
        const assets = store.frontendAssetContents;

        console.log("[frontend_config] Début beforeNext", {
          logoContent: assets.logoContent ? `${assets.logoContent.length} chars` : 'null',
          logoMimeType: assets.logoMimeType,
          logoAssetId: config.logoAssetId,
          faviconContent: assets.faviconContent ? `${assets.faviconContent.length} chars` : 'null',
          faviconMimeType: assets.faviconMimeType,
          faviconAssetId: config.faviconAssetId,
        });

        // 1. Upload logo
        if (assets.logoContent && !config.logoAssetId) {
          console.log("[frontend_config] Upload logo — START");
          try {
            const result = await svc.uploadFrontendAsset(
              'logo',
              assets.logoContent,
              assets.logoMimeType || 'image/png'
            );
            console.log("[frontend_config] Upload logo — SUCCESS", result);
            store.updateFrontendLayout('logoAssetId', result.assetId);
          } catch (error) {
            console.error("[frontend_config] Upload logo — FAIL", error);
            store.setWizardError(
              error instanceof Error ? `Échec de l'upload du logo : ${error.message}` : "Échec de l'upload du logo."
            );
            return false;
          }
        } else {
          console.log("[frontend_config] Upload logo — SKIP", {
            hasContent: !!assets.logoContent,
            hasAssetId: !!config.logoAssetId,
          });
        }

        // 2. Upload favicon
        if (assets.faviconContent && !config.faviconAssetId) {
          console.log("[frontend_config] Upload favicon — START");
          try {
            const result = await svc.uploadFrontendAsset(
              'favicon',
              assets.faviconContent,
              assets.faviconMimeType || 'image/x-icon'
            );
            console.log("[frontend_config] Upload favicon — SUCCESS", result);
            store.updateFrontendLayout('faviconAssetId', result.assetId);
          } catch (error) {
            console.error("[frontend_config] Upload favicon — FAIL", error);
            store.setWizardError(
              error instanceof Error ? `Échec de l'upload du favicon : ${error.message}` : "Échec de l'upload du favicon."
            );
            return false;
          }
        } else {
          console.log("[frontend_config] Upload favicon — SKIP", {
            hasContent: !!assets.faviconContent,
            hasAssetId: !!config.faviconAssetId,
          });
        }

        // 3. Save config
        const configToSend = {
          selectedInterfaceLanguages: [...config.selectedInterfaceLanguages],
          navbarType: config.navbarType,
          primaryColor: config.primaryColor,
          secondaryColor: config.secondaryColor,
          
          //  IMPORTANT : logoPath/faviconPath = '' quand assetId est présent
          //    (le backend refuse URL + assetId simultanément)
          logoPath: config.logoAssetId ? '' : (config.logoPath || ''),
          faviconPath: config.faviconAssetId ? '' : (config.faviconPath || ''),
          
          port: config.port,
          logoAssetId: config.logoAssetId ?? '',
          faviconAssetId: config.faviconAssetId ?? '',
        };

        console.log("[frontend_config] Save config — START", configToSend);
        const saveResult = await svc.saveFrontendLayoutConfig(configToSend);
        console.log("[frontend_config] Save config — SUCCESS", saveResult);

        return true;
      } catch (error) {
        console.error("[frontend_config] ERREUR GLOBALE", error);
        store.setWizardError(
          error instanceof Error ? error.message : "Échec de la sauvegarde du layout frontend."
        );
        return false;
      }
    }
  },

  repository_select: {
    onEnter: async (_store, { repoSvc }) => {
      console.log("[Wizard: repository_select] Chargement des fournisseurs de dépôt...");
      const repositoryStore = useRepositoryStore();
      
      try {
        const providersData = await repoSvc.getSupportedProviders();
        repositoryStore.setProviders(providersData);
        console.log(`[Wizard: repository_select] ${providersData.length} fournisseur(s) chargé(s)`);
      } catch (error) {
        console.error("[Wizard: repository_select] Échec du chargement des fournisseurs:", error);
        _store.setWizardError("Impossible de charger la liste des fournisseurs de dépôt.");
      }
    },
    
    beforeNext: async (store) => {
      if (!store.pendingRepositoryProvider) {
        store.setWizardError("Veuillez sélectionner un fournisseur de dépôt avant de continuer.");
        return false;
      }

      store.unmarkStepAsSkipped('repository_select');
      store.unmarkStepAsSkipped('repository_config');

      store.updateRepositoryConfig('provider', store.pendingRepositoryProvider.id);
      
      console.log(`[Wizard: repository_select] Fournisseur sélectionné et persisté :`, store.pendingRepositoryProvider.name);
      return true;
    },

    onSkip: async (store, { svc }) => {
      // 1. Reset local
      store.skipGitConfig();

      store.markStepAsSkipped('repository_select');
      store.markStepAsSkipped('repository_config');

      const currentGitConfig = store.stepperData.git;
      console.log("ON SKIP GIT CONFIGURATION : ", toRaw(currentGitConfig));
      
      // 2. Reset backend (non bloquant)
      try {
        await svc.resetGit();
        console.log("[Wizard: repository_select] Reset Git backend OK.");
      } catch (error) {
        console.warn("[Wizard: repository_select] Échec du reset Git backend:", error);
        // Non bloquant : on continue
      }
      
      // 3. Invalider les étapes
      const invalidated: StepId[] = ['repository_select', 'repository_config'];
      store.invalidateSteps(invalidated);
    },
    
    skipCascade: [
      'repository_config'
    ]
  },

  repository_config: {
    onEnter: (store) => {
      // Initialisation par défaut si l'objet n'existe pas encore
      if (!store.stepperData.repositoryConfig) {
        store.updateRepositoryConfig('provider', store.pendingRepositoryProvider?.id || 'github');
        store.updateRepositoryConfig('useRemote', false);
        store.updateRepositoryConfig('isNewRemote', true);
        store.updateRepositoryConfig('scope', 'monorepo');
        store.updateRepositoryConfig('authMethod', 'personal_access_token');
      }
      
      // On s'assure que les valeurs par défaut Git sont propres
      store.stepperData.git.useGit = true;
      if (!store.stepperData.git.useRemoteRepo) {
        store.stepperData.git.isNewRemoteRepo = true;
        store.stepperData.git.separateRepositories = false;
      }
    },
    
    beforeNext: async (store, { svc }) => {
      const repo = store.stepperData.repositoryConfig;
      
      // On active Git par défaut
      store.updateGitConfig('useGit', true);

      if (repo?.useRemote) {
        // 🔄 MAPPING (FORCING) : Conversion de RepositoryConfig vers GitConfiguration
        // Le backend ne supporte que GitHub pour l'instant, on mappe donc les données génériques 
        // vers les champs spécifiques GitHub attendus par l'interface GitConfiguration.
        store.updateGitConfig('separateRepositories', repo.scope === 'separated');
        store.updateGitConfig('useRemoteRepo', true);
        store.updateGitConfig('isNewRemoteRepo', repo.isNewRemote);
        store.updateGitConfig('repositoryName', repo.monorepoName || '');
        store.updateGitConfig('backendRepositoryName', repo.backendRepoName || '');
        store.updateGitConfig('frontendRepositoryName', repo.frontendRepoName || '');
        store.updateGitConfig('githubUsername', repo.username || '');
        store.updateGitConfig('githubToken', repo.token || '');
        
      } else {
        // 3. Si pas de remote, on s'assure que Git est configuré en local uniquement
        store.updateGitConfig('useRemoteRepo', false);
        store.updateGitConfig('isNewRemoteRepo', true);
        store.updateGitConfig('separateRepositories', false);
        store.updateGitConfig('repositoryName', '');
        store.updateGitConfig('backendRepositoryName', '');
        store.updateGitConfig('frontendRepositoryName', '');
        store.updateGitConfig('githubUsername', '');
        store.updateGitConfig('githubToken', '');
      }


      // Validation sur les données mappées (git) : champs du dépôt distant
      if (reject(store, validateStep('repository_config', store))) return false;

      try {
        const gitConfig = toRaw(store.stepperData.git);
        console.log("[Wizard: git_config] Envoi de la config Git :", gitConfig);
        await svc.saveGitConfiguration(gitConfig);
        console.log("[Wizard: git_config] Configuration Git sauvegardée.");
      } catch (error) {
        store.setWizardError(
          error instanceof Error ? error.message : "Échec de la sauvegarde de la configuration Git."
        );
        return false;
      }
      
      console.log("[Wizard: git_config] Configuration Repository validée et mappée pour le backend.");
      return true;
    }
  },

  // genesis-sdk-web/genesis-web-core/src/features/generator/composables/wizard-step-config.ts
  summary: {
    onEnter: (store) => {
      store.setIsGenerationComplete(false);
    },
    
    beforeNext: async (store, { recentSvc, svc }) => {
      try {
        console.log("[Wizard: summary] Résumé validé. Prêt pour la génération finale.");
        // Toutes les étapes obligatoires doivent être valides avant de lancer la génération
        const reports = validateBeforeGeneration(store);
        if (reports.length > 0) {
          store.setWizardError(
            reports
              .map(r => `${STEP_LABELS[getStepNumberById(r.step) - 1]} : ${r.issues.join(' ')}`)
              .join('\n')
          );
          return false;
        }

        store.setIsGenerating(true);

        const startTime = Date.now();
        const data = toRaw(store.stepperData);

        // 1. Génération (bloquant — si throw, on sort)
        await svc.launchGeneration();

        // // 2. Init Git (NON BLOQUANT + avertissement UI si échec)
        
        // if (data.git?.useGit && data.git?.UseRemote) {
        //   try {
        //     const gitResult = await svc.initGitRepository();
        //     console.log("[Wizard: summary] Dépôt Git initialisé :", gitResult);
        //   } catch (gitError) {
        //     // 👇 Avertissement UI non bloquant
        //     const gitMessage = gitError instanceof Error ? gitError.message : 'Erreur inconnue';
        //     console.warn("[Wizard: summary] Échec init Git (non bloquant):", gitError);
            
        //     useGenesisWarning().confirm({
        //       title: 'Dépôt Git non initialisé',
        //       message: "Le projet a bien été généré, mais l'initialisation du dépôt Git a échoué.",
        //       details: [
        //         gitMessage,
        //         "Vérifiez votre connexion et la configuration Git (token, remote).",
        //         "Vous pourrez réinitialiser le dépôt manuellement plus tard."
        //       ],
        //       confirmLabel: 'Compris',
        //       variant: 'danger'  // ou 'warning' si dispo
        //     });
        //   }
        // } else {
        //   console.log("[Wizard: summary] Git désactivé, pas d'init.");
        // }


        // 2. Reset backend (bloquant aussi ? ou optionnel ?)
        await svc.resetAll();

        // 3. Récence (non bloquant)
        try {
          if (data.framework) {
            await recentSvc.addRecentItem('frameworks', {
              id: data.framework.id,
              name: data.framework.name,
              lastUsed: Date.now()
            } as RecentItem);
          }
          if (data.database?.engine) {
            const databaseStore = useDatabaseStore();
            const matchedEngine = databaseStore.availableEngines.find(eng => {
              const n = eng.name.toLowerCase().replace(/\s+/g, '');
              const c = data.database!.engine.toLowerCase().replace(/\s+/g, '');
              return n === c;
            });
            if (matchedEngine) {
              await recentSvc.addRecentItem('databases', {
                id: matchedEngine.id,
                name: matchedEngine.name,
                lastUsed: Date.now()
              } as RecentItem);
            }
          }
          if (data.frontend) {
            await recentSvc.addRecentItem('frontends', {
              id: data.frontend.id,
              name: data.frontend.name,
              lastUsed: Date.now()
            } as RecentItem);
          }
        } catch (recentError) {
          console.warn("[Wizard: summary] Échec récence (non bloquant):", recentError);
        }

        // 4. Attente minimum (UX)
        const elapsedTime = Date.now() - startTime;
        const remainingTime = Math.max(0, 3000 - elapsedTime);
        if (remainingTime > 0) {
          await new Promise(resolve => setTimeout(resolve, remainingTime));
        }

        // 5. SUCCÈS uniquement si tout s'est bien passé
        store.setIsGenerating(false);
        store.setIsGenerationComplete(true);
        return true;

      } catch (error) {
        // ❌ ÉCHEC : on reset le flag, on propage l'erreur
        console.error("[Wizard: summary] Échec de la génération:", error);
        store.setIsGenerating(false);
        store.setIsGenerationComplete(false);   // 👈 forcer false
        store.setWizardError(
          error instanceof Error ? error.message : "Échec de la génération."
        );
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
// genesis-sdk-web/genesis-web-core/src/features/generator/utils/validators/stepValidators.ts

import { toRaw } from 'vue';
import type { Framework, GenerationParameter } from '@genesis-labs/shared-types';
import type { useGeneratorStore } from '../../store/useGenerator.store';
import type { StepId } from '../../composables/wizard-step-config';

// ============================================================================
// TYPES
// ============================================================================

type GeneratorStore = ReturnType<typeof useGeneratorStore>;

/** Un validator lit le store et renvoie la liste des problèmes (vide = étape valide). */
export type StepValidator = (store: GeneratorStore) => string[];

export interface StepValidationReport {
  step: StepId;
  issues: string[];
}

// ============================================================================
// HELPERS
// ============================================================================

/** Framework de type REST : le frontend est alors optionnel (skippable). */
export const isRestFramework = (fw: Framework) => fw.type === 'REST API';

const isBlank = (value: unknown): boolean =>
  value === undefined || value === null || (typeof value === 'string' && value.trim() === '');

const isValidPort = (value: unknown): boolean => {
  const n = Number(value);
  return Number.isInteger(n) && n >= 1 && n <= 65535;
};

type Field = readonly [label: string, value: unknown];

/**
 * Vérifie une liste de champs obligatoires.
 * Renvoie un seul message qui liste tous les champs manquants (ou [] si tout est renseigné).
 */
function requireFields(fields: Array<Field | null>): string[] {
  const missing = fields
    .filter((f): f is Field => f !== null && isBlank(f[1]))
    .map(([label]) => label);
  return missing.length > 0 ? [`Champs obligatoires manquants : ${missing.join(', ')}.`] : [];
}

/** Champ obligatoire seulement si le backend propose des options pour lui (sinon rien à choisir). */
const requiredIfOptions = (label: string, value: unknown, options: string[]): Field | null =>
  options.length > 0 ? [label, value] : null;

const portIssue = (label: string, value: unknown): string[] =>
  isValidPort(value) ? [] : [`${label} : le port doit être un entier entre 1 et 65535.`];

// ============================================================================
// VALIDATORS (un par étape ; l'ordre de déclaration = ordre du wizard)
// ============================================================================

const validateFramework: StepValidator = ({ stepperData: d }) =>
  d.framework ? [] : ['Aucun framework sélectionné.'];

const validateProjectConfig: StepValidator = (store) => {
  const c = store.stepperData.config;
  const frameworkWithGroupID = store.stepperData.framework?.withGroupId;
  return [
    ...requireFields([
      ['Nom du projet', c.projectName],
      ['Localisation', c.projectLocation],
      // Obligatoire uniquement si le framework l'exige
      frameworkWithGroupID ? ['Group ID', c.groupId] : null, 
      requiredIfOptions('Version du langage', c.languageVersion, store.getAvailableLanguageVersions),
      requiredIfOptions('Build tool', c.buildTool, store.getAvailableBuildTools),
      requiredIfOptions('Version du framework', c.frameworkVersion, store.getAvailableFrameworkVersions),
      requiredIfOptions('Niveau de logging', c.loggingLevel, store.getAvailableLoggingLevels),
      requiredIfOptions('Sécurité', c.securityType, store.getAvailableSecurityTypes),
      requiredIfOptions('Cache', c.cacheProvider, store.getAvailableCacheProviders),
      requiredIfOptions('Hibernate DDL auto', c.hibernateDdlAuto, store.getAvailableHibernateDdlAutoOptions),
    ]),
    ...portIssue('Configuration projet', c.projectPort),
  ];
};

const validateDatabaseSelect: StepValidator = ({ stepperData: d }) =>
  isBlank(d.database.engine) ? ['Aucun moteur de base de données sélectionné.'] : [];

/**
 * Champs de la config BDD (sans le contrôle de commit).
 * Utilisé tel quel par `database_config.beforeNext`, avant le test de connexion et la sauvegarde.
 */
export const validateDatabaseFields: StepValidator = (store) => {
  const db = store.stepperData.database;
  return [
    ...requireFields([
      ['Hôte', db.host],
      ['Nom de la base', db.databaseName],
      ['Utilisateur', db.username],
      // SID : propre à Oracle
      /oracle/i.test(String(db.engine)) ? ['SID', db.sid] : null,
    ]),
    ...portIssue('Base de données', db.port),
  ];
};

/** Champs valides ET config testée/sauvegardée (aucune modification depuis le dernier commit réussi). */
const validateDatabaseConfig: StepValidator = (store) => [
  ...validateDatabaseFields(store),
  ...(store.hasDatabaseConfigChanged(toRaw(store.stepperData.database))
    ? ['La configuration de la base de données n\'a pas été testée et sauvegardée.']
    : []),
];

/**
 * Contrôle les paramètres de génération.
 * Prend le tableau en argument : `generation.beforeNext` l'utilise avant de le sauvegarder.
 */
export function validateGenerationParameters(params: GenerationParameter[]): string[] {
  if (params.length === 0) {
    return ['Assignez au moins une table ou une vue à un profil de génération.'];
  }
  return params.flatMap(p => {
    const issues: string[] = [];
    if (p.selectedTables.length + p.selectedViews.length === 0) {
      issues.push(`Le profil « ${p.profileName} » n'a aucune table ni vue assignée.`);
    }
    if (p.selectedComponents.length === 0) {
      issues.push(`Le profil « ${p.profileName} » n'a aucun composant sélectionné.`);
    }
    return issues;
  });
}

const validateGeneration: StepValidator = ({ stepperData: d }) =>
  validateGenerationParameters(d.generationParameters);

/** Les relations peuvent être vides, mais n'ont de sens qu'avec une génération valide. */
const validateRelations: StepValidator = (store) => {
  const relations = store.relations;
  if (relations.length === 0) return [];

  const issues: string[] = [];
  if (validateGeneration(store).length > 0) {
    issues.push('Les relations nécessitent une configuration de génération valide.');
  }
  if (relations.some(r => isBlank(r.parentTable) || isBlank(r.childTable))) {
    issues.push('Une ou plusieurs relations n\'ont pas de table parent ou enfant.');
  }
  return issues;
};

/** Frontend obligatoire, sauf pour un framework REST. */
const validateFrontendSelect: StepValidator = ({ stepperData: d }) =>
  !d.framework || isRestFramework(d.framework) || d.frontend
    ? []
    : ['Un frontend est obligatoire pour un framework MVC.'];

/** Configuration frontend : contrôlée seulement si un frontend est sélectionné. */
const validateFrontendConfig: StepValidator = ({ stepperData: d }) => {
  if (!d.frontend) return [];
  const layout = d.frontendLayout;
  const isREST = d.framework?.type === 'REST API';
  const issues = [
    ...requireFields([
      ['Type de navbar', layout.navbarType],
      ['Couleur primaire', layout.primaryColor],
      ['Couleur secondaire', layout.secondaryColor],
    ]),
    ...(isREST ? portIssue('Frontend', layout.port) : [])
  ];
  if (layout.selectedInterfaceLanguages.length === 0) {
    issues.push('Sélectionnez au moins une langue d\'interface.');
  }
  return issues;
};

/** Dépôt : étape skippable, mais si un dépôt distant est activé il doit être complet. */
const validateRepositoryConfig: StepValidator = ({ stepperData: d }) => {
  const git = d.git;
  if (!git.useGit || !git.useRemoteRepo) return [];
  return requireFields([
    ['Utilisateur du dépôt', git.githubUsername],
    git.isNewRemoteRepo ? ['Personal Access Token', git.githubToken] : null,
    ...(git.separateRepositories
      ? [['Dépôt backend', git.backendRepositoryName] as Field,
         ['Dépôt frontend', git.frontendRepositoryName] as Field]
      : [['Dépôt unique', git.repositoryName] as Field]),
  ]);
};

// ============================================================================
// REGISTRE : étapes contrôlées avant la génération
// (script : skippable, sans donnée à valider)
// ============================================================================

export const STEP_VALIDATORS: Partial<Record<StepId, StepValidator>> = {
  framework_select: validateFramework,
  project_config: validateProjectConfig,
  database_select: validateDatabaseSelect,
  database_config: validateDatabaseConfig,
  generation: validateGeneration,
  relations: validateRelations,
  frontend_select: validateFrontendSelect,
  frontend_config: validateFrontendConfig,
  repository_config: validateRepositoryConfig,
};

// ============================================================================
// API
// ============================================================================

/** Problèmes d'une étape (vide = valide ou étape sans validator). */
export function validateStep(step: StepId, store: GeneratorStore): string[] {
  return STEP_VALIDATORS[step]?.(store) ?? [];
}

/** Toutes les étapes en erreur, dans l'ordre du wizard (vide = génération autorisée). */
export function validateBeforeGeneration(store: GeneratorStore): StepValidationReport[] {
  return (Object.keys(STEP_VALIDATORS) as StepId[])
    .map(step => ({ step, issues: validateStep(step, store) }))
    .filter(report => report.issues.length > 0);
}
/**
 * Helpers pour la gestion des champs dont les options ne contiennent
 * qu'une seule valeur 'NONE' (champs "masquables").
 * 
 * Règle métier :
 * - Si un select n'a qu'une option et que cette option est 'NONE' (insensible casse/espaces),
 *   il est masqué dans l'UI ET sa valeur est envoyée à `null` au backend.
 */

/**
 * Détecte si un select doit être masqué (une seule option = 'NONE').
 */
export function isNoneOnly(options: { label: string; value: any }[] | undefined): boolean {
  if (!options || options.length !== 1) return false;
  const v = options[0].value;
  if (v === null || v === undefined) return true;
  return String(v).trim().toUpperCase() === 'NONE';
}

/**
 * Détecte si un champ "string" doit être nullifié.
 * Un champ est nullifié si :
 * - Il est vide/null/undefined, OU
 * - Sa valeur correspond à la seule option disponible d'un select 'NONE'.
 * 
 * @param value Valeur actuelle du champ
 * @param options Options du select associé (vide si le champ n'est pas un select)
 */
export function sanitizeStringField(
  value: string | undefined | null,
  options: { label: string; value: any }[] | undefined
): string | null {
  if (!value) return null;
  if (isNoneOnly(options)) return null;
  return value;
}

/**
 * Construit le payload "safe" de ProjectConfig :
 * - Tous les champs optionnels dont les options ne contiennent qu'un 'NONE' → `null`.
 * - Les champs utilisateur (projectName, projectLocation, etc.) → inchangés.
 */
export function buildProjectConfigPayload(
  config: {
    projectName: string;
    projectLocation: string;
    languageVersion: string;
    buildTool: string;
    groupId: string;
    frameworkVersion: string;
    projectDescription: string;
    projectPort: string;
    loggingLevel: string;
    securityType: string;
    cacheProvider: string;
    hibernateDdlAuto?: string;
  },
  options: {
    languageVersionOptions: { label: string; value: any }[];
    buildToolOptions: { label: string; value: any }[];
    frameworkVersionOptions: { label: string; value: any }[];
    loggingLevelOptions: { label: string; value: any }[];
    securityTypeOptions: { label: string; value: any }[];
    cacheProviderOptions: { label: string; value: any }[];
    hibernateDdlAutoOptions: { label: string; value: any }[];
  }
) {
  return {
    // Champs "utilisateur" toujours présents
    projectName: config.projectName,
    projectLocation: config.projectLocation,
    projectDescription: config.projectDescription,
    projectPort: config.projectPort,

    // Champs "techniques" : nullifiés si `NONE-only`
    languageVersion: sanitizeStringField(config.languageVersion, options.languageVersionOptions),
    buildTool:       sanitizeStringField(config.buildTool,       options.buildToolOptions),
    groupId:         config.groupId || null,  // groupId : pas de notion 'NONE', nullifié si vide
    frameworkVersion: sanitizeStringField(config.frameworkVersion, options.frameworkVersionOptions),
    loggingLevel:    sanitizeStringField(config.loggingLevel,    options.loggingLevelOptions),
    securityType:    sanitizeStringField(config.securityType,    options.securityTypeOptions),
    cacheProvider:   sanitizeStringField(config.cacheProvider,   options.cacheProviderOptions),
    hibernateDdlAuto: sanitizeStringField(config.hibernateDdlAuto, options.hibernateDdlAutoOptions),
  };
}
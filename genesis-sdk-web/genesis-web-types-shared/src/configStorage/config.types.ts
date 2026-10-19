// genesis-sdk-web/genesis-web-types-shared/src/config.types.ts

/**
 * Types de configurations supportés par le système.
 * Extensible selon les futures fonctionnalités du générateur.
 */
export type ConfigType = 
  | 'framework_technical-stack' 
  | 'framework_advanced-configuration'
  | 'generator_generation-options'
  | 'frontend_languages-configuration';

export interface FrameworkAdvancedConfigurationPayload {
  frameworkId: number;        // NOUVEAU : Lien vers le framework
  frameworkName: string;      // NOUVEAU : Nom du framework (sécurité en cas de changement d'ID)
  loggingLevel: string;
  securityType: string;
  cacheProvider: string;
  hibernateDdlAuto: string;
  [key: string]: unknown;
}

export interface FrameworkTechnicalStackPayload {
  frameworkId: number;
  frameworkName: string;
  languageVersion: string;
  buildTool: string;
  groupId: string;
  frameworkVersion: string;
  [key: string]: unknown;
}

export interface GeneratorGenerationOptionsPayload {
  components: string[]; // Ex: ['model', 'dao', 'service', 'controller']
  [key: string]: unknown;
}

// genesis-sdk-web/genesis-web-types-shared/src/configStorage/config.types.ts
export interface FrontendLanguagesConfigurationPayload {
  languages: string[]; // Tableau des codes de langue (ex: ['fr', 'en'])
  [key: string]: unknown;
}
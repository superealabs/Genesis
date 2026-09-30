// genesis-sdk-web/genesis-web-types-shared/src/config.types.ts

/**
 * Types de configurations supportés par le système.
 * Extensible selon les futures fonctionnalités du générateur.
 */
export type ConfigType = 
  | 'framework_technical-stack' 
  | 'framework_advanced-configuration' // <-- Ajouté pour cet exemple
  | 'generator_generation-options'     // <-- Ajouté pour le futur

export interface FrameworkAdvancedConfigurationPayload {
  loggingLevel: string;
  securityType: string;
  cacheProvider: string;
  hibernateDdlAuto: string;
  [key: string]: unknown; // ✅ AJOUT : Rend l'interface compatible avec Record<string, unknown>
}
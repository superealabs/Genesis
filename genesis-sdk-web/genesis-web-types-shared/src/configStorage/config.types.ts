// genesis-sdk-web/genesis-web-types-shared/src/config.types.ts

/**
 * Types de configurations supportés par le système.
 * Extensible selon les futures fonctionnalités du générateur.
 */
export type ConfigType = 
  | 'technical_stack' 
  | 'advanced_configuration' // <-- Ajouté pour cet exemple
  | 'generation_options'     // <-- Ajouté pour le futur
  | 'database' 
  | 'frontend' 
  | 'relations' 
  | 'generator';


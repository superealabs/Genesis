// genesis-sdk-web/genesis-web-core/src/core/config/ui.config.ts

// ============================================================================
// CONFIGURATION DE L'INTERFACE UTILISATEUR (UI)
// ============================================================================

/**
 * Conventions de tailles pour les menus déroulants (Dropdowns).
 * Utilise des classes Tailwind CSS prédéfinies pour assurer la cohérence visuelle 
 * et la maintenabilité à travers tous les composants.
 */
export const MENU_SIZES = {
  sm: 'w-40',
  md: 'w-56',
  lg: 'w-64',
  xl: 'w-72',
  '2xl': 'w-80',
  '3xl': 'w-96',
} as const;

/**
 * Type dérivé automatiquement des clés de MENU_SIZES.
 * Garantit un typage strict (ex: 'sm' | 'md' | 'lg'...) et l'autocomplétion 
 * lors de l'utilisation des tailles de menu dans les props des composants.
 */
export type MenuSize = keyof typeof MENU_SIZES;

// ============================================================================
// AUTRES CONFIGURATIONS UI (Structure recommandée)
// ============================================================================

// Exemple : Si tu as d'autres constantes, groupe-les par domaine de la même manière.
// export const Z_INDICES = {
//   dropdown: 50,
//   modal: 100,
//   tooltip: 200,
// } as const;
// export type ZIndex = keyof typeof Z_INDICES;
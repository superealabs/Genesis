// genesis-sdk-web/genesis-web-core/src/core/config/ui.config.ts

// ============================================================================
// CONFIGURATION DE L'INTERFACE UTILISATEUR (UI)
// ============================================================================

/**
 * Unités de taille standardisées pour tous les composants d'interface.
 */
export const UI_SIZE_UNITS = ['xs', 'sm', 'md', 'lg', 'xl', '2xl'] as const;
export type UI_Size_Unit = typeof UI_SIZE_UNITS[number];

/**
 * Variantes de style standardisées pour les composants d'action (Boutons, etc.).
 */
export const UI_VARIANTS = ['accent', 'primary', 'secondary', 'tertiary'] as const;
export type UI_Variant = typeof UI_VARIANTS[number];

/**
 * Conventions de tailles pour les menus déroulants (Dropdowns).
 */
export const MENU_SIZES = {
  sm: 'w-40',
  md: 'w-56',
  lg: 'w-64',
  xl: 'w-72',
  '2xl': 'w-80',
  '3xl': 'w-96',
} as const;
export type MenuSize = keyof typeof MENU_SIZES;

// ============================================================================
// CONFIGURATION DES BOUTONS (GenesisButton)
// ============================================================================

export const BUTTON_SIZES = {
  rectangle: {
    xs: 'text-[10px] h-fit [&_svg]:!w-3.5 [&_svg]:!h-3.5 [&_.right-part_svg]:!w-3 [&_.right-part_svg]:!h-3',
    sm: 'text-xs h-fit [&_svg]:!w-4 [&_svg]:!h-4 [&_.right-part_svg]:!w-3.5 [&_.right-part_svg]:!h-3.5',
    md: 'text-sm h-fit [&_svg]:!w-5 [&_svg]:!h-5 [&_.right-part_svg]:!w-4 [&_.right-part_svg]:!h-4',
    lg: 'text-base h-fit [&_svg]:!w-6 [&_svg]:!h-6 [&_.right-part_svg]:!w-5 [&_.right-part_svg]:!h-5',
    xl: 'text-lg h-fit [&_svg]:!w-7 [&_svg]:!h-7 [&_.right-part_svg]:!w-6 [&_.right-part_svg]:!h-6',
    '2xl': 'text-xl h-fit [&_svg]:!w-8 [&_svg]:!h-8 [&_.right-part_svg]:!w-7 [&_.right-part_svg]:!h-7',
  },
  square: {
    xs: 'w-6 h-6 [&_svg]:!w-3.5 [&_svg]:!h-3.5',
    sm: 'w-7 h-7 [&_svg]:!w-4 [&_svg]:!h-4',
    md: 'w-8 h-8 [&_svg]:!w-5 [&_svg]:!h-5',
    lg: 'w-9 h-9 [&_svg]:!w-6 [&_svg]:!h-6',
    xl: 'w-10 h-10 [&_svg]:!w-7 [&_svg]:!h-7',
    '2xl': 'w-11 h-11 [&_svg]:!w-8 [&_svg]:!h-8',
  },
} as const;

export const BUTTON_PADDINGS = {
  withRightIcon: {
    xs: 'px-2 py-0.5',
    sm: 'px-2.5 py-1',
    md: 'px-3 py-1.5',
    lg: 'px-4 py-2',
    xl: 'px-5 py-2.5',
    '2xl': 'px-6 py-3',
  },
  withoutRightIcon: {
    xs: 'px-2.5 py-0.5',
    sm: 'px-3.5 py-1',
    md: 'px-4 py-1.5',
    lg: 'px-5 py-2',
    xl: 'px-6 py-2.5',
    '2xl': 'px-7 py-3',
  },
} as const;

export const BUTTON_GAPS = {
  xs: 'gap-1.5',
  sm: 'gap-2',
  md: 'gap-2',
  lg: 'gap-2.5',
  xl: 'gap-3',
  '2xl': 'gap-3',
} as const;

export const BUTTON_RIGHT_ICON_POSITIONS = {
  xs: 'absolute right-2',
  sm: 'absolute right-2.5',
  md: 'absolute right-3',
  lg: 'absolute right-4',
  xl: 'absolute right-5',
  '2xl': 'absolute right-6',
} as const;

/**
 * Styles des variantes de boutons.
 * Sépare les états 'hover' (survol activé) et 'default' (survol désactivé).
 */
export const BUTTON_VARIANTS = {
  accent: {
    hover: 'bg-accent text-accent-900 font-medium hover:bg-accent/80 disabled:hover:bg-accent disabled:hover:shadow-none',
    default: 'bg-accent text-accent-900 font-medium',
  },
  primary: {
    hover: 'bg-primary text-text font-medium hover:bg-primary/80 disabled:hover:bg-primary disabled:hover:shadow-none',
    default: 'bg-primary text-text font-medium',
  },
  secondary: {
    hover: 'bg-bg-secondary text-secondary font-medium disabled:hover:bg-transparent',
    default: 'bg-transparent text-secondary font-medium',
  },
  tertiary: {
    hover: 'bg-transparent text-text hover:bg-white hover:text-secondary',
    default: 'bg-transparent text-text',
  },
} as const;
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
export const UI_VARIANTS = ['accent', 'primary', 'secondary', 'tertiary', 'neutral'] as const;
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
// ÉCHELLE UNIQUE DES CONTRÔLES (GenesisButton, GenesisButtonIcon, GenesisInput…)
// ============================================================================
//
// RÈGLE : une taille = une hauteur, quel que soit le composant.
// La hauteur est IMPOSÉE (et non déduite du contenu). Avec `box-sizing: border-box`
// (défaut de Tailwind), la bordure est comprise dedans : ajouter ou retirer une
// bordure ne change donc jamais la hauteur.
//
//   xs 24px · sm 28px · md 32px · lg 40px · xl 48px · 2xl 56px
//
// Tous les composants lisent cette table : pour changer une taille, un seul endroit.
// ============================================================================

export interface ControlSizeTokens {
  /** Hauteur imposée (rectangle : bouton texte, input, select) */
  box: string;
  /** Largeur + hauteur imposées (bouton carré / bouton icône) */
  square: string;
  /** Taille du texte */
  text: string;
  /** Taille des icônes (tous les <svg> descendants) */
  icon: string;
  /** Taille des icônes de la partie droite d'un bouton (chevron) */
  rightIcon: string;
  /** Padding horizontal d'un bouton sans icône à droite */
  px: string;
  /** Padding horizontal d'un bouton avec icône à droite */
  pxWithRight: string;
  /** Padding gauche / droit de l'input natif quand aucun slot n'occupe le côté */
  pl: string;
  pr: string;
  /** Padding horizontal des slots d'un input (left / right) */
  slotPx: string;
  /** Espacement entre les éléments */
  gap: string;
}

export const CONTROL_SIZES: Record<UI_Size_Unit, ControlSizeTokens> = {
  xs: {
    box: 'h-6',
    square: 'w-6 h-6',
    text: 'text-[10px]',
    icon: '[&_svg]:!w-3.5 [&_svg]:!h-3.5',
    rightIcon: '[&_.right-part_svg]:!w-3 [&_.right-part_svg]:!h-3',
    px: 'px-2.5',
    pxWithRight: 'px-2',
    pl: 'pl-2.5',
    pr: 'pr-2.5',
    slotPx: 'px-1.5',
    gap: 'gap-1.5',
  },
  sm: {
    box: 'h-7',
    square: 'w-7 h-7',
    text: 'text-xs',
    icon: '[&_svg]:!w-4 [&_svg]:!h-4',
    rightIcon: '[&_.right-part_svg]:!w-3.5 [&_.right-part_svg]:!h-3.5',
    px: 'px-3.5',
    pxWithRight: 'px-2.5',
    pl: 'pl-3.5',
    pr: 'pr-3.5',
    slotPx: 'px-2',
    gap: 'gap-2',
  },
  md: {
    box: 'h-8',
    square: 'w-8 h-8',
    text: 'text-sm',
    icon: '[&_svg]:!w-5 [&_svg]:!h-5',
    rightIcon: '[&_.right-part_svg]:!w-4 [&_.right-part_svg]:!h-4',
    px: 'px-4',
    pxWithRight: 'px-3',
    pl: 'pl-4',
    pr: 'pr-4',
    slotPx: 'px-2.5',
    gap: 'gap-2',
  },
  lg: {
    box: 'h-10',
    square: 'w-10 h-10',
    text: 'text-base',
    icon: '[&_svg]:!w-6 [&_svg]:!h-6',
    rightIcon: '[&_.right-part_svg]:!w-5 [&_.right-part_svg]:!h-5',
    px: 'px-5',
    pxWithRight: 'px-4',
    pl: 'pl-5',
    pr: 'pr-5',
    slotPx: 'px-3',
    gap: 'gap-2.5',
  },
  xl: {
    box: 'h-12',
    square: 'w-12 h-12',
    text: 'text-lg',
    icon: '[&_svg]:!w-7 [&_svg]:!h-7',
    rightIcon: '[&_.right-part_svg]:!w-6 [&_.right-part_svg]:!h-6',
    px: 'px-6',
    pxWithRight: 'px-5',
    pl: 'pl-6',
    pr: 'pr-6',
    slotPx: 'px-3.5',
    gap: 'gap-3',
  },
  '2xl': {
    box: 'h-14',
    square: 'w-14 h-14',
    text: 'text-xl',
    icon: '[&_svg]:!w-8 [&_svg]:!h-8',
    rightIcon: '[&_.right-part_svg]:!w-7 [&_.right-part_svg]:!h-7',
    px: 'px-7',
    pxWithRight: 'px-6',
    pl: 'pl-7',
    pr: 'pr-7',
    slotPx: 'px-4',
    gap: 'gap-3',
  },
};

/**
 * Taille du bouton d'action placé À L'INTÉRIEUR d'un input (dossier, ajout…).
 * Elle suit la taille de l'input, pour ne jamais imposer sa propre hauteur.
 * (xs : le bouton xs de 24px dépasse de 2px dans un input de 24px ; GenesisInput
 * applique alors une surcharge de 20px.)
 */
export const INPUT_ACTION_SIZES: Record<UI_Size_Unit, UI_Size_Unit> = {
  xs: 'xs',
  sm: 'xs',
  md: 'xs',
  lg: 'sm',
  xl: 'md',
  '2xl': 'lg',
};

// ============================================================================
// CONFIGURATION DES BOUTONS (GenesisButton)
// ============================================================================

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
    hover: 'bg-accent font-medium text-accent-900 hover:bg-accent/80 disabled:hover:bg-accent disabled:hover:shadow-none',
    default: 'bg-accent text-accent-900',
  },
  primary: {
    hover: 'bg-primary font-medium text-text hover:bg-primary/80 disabled:hover:bg-primary disabled:hover:shadow-none',
    default: 'bg-primary text-text',
  },
  secondary: {
    hover: 'bg-bg-secondary font-medium text-secondary disabled:hover:bg-transparent',
    default: 'bg-transparent text-secondary',
  },
  tertiary: {
    hover: 'bg-transparent font-medium text-muted hover:bg-hover-ghost hover:text-secondary',
    default: 'bg-transparent text-text',
  },
  neutral: {
    hover: 'bg-bg-neutral-genesis font-medium text-gray-800 disabled:hover:bg-transparent',
    default: 'neutral-light-genesis text-gray-800',
  }
} as const;
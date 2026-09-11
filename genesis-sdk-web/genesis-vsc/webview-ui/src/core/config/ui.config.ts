// ... tes autres variables existantes ...

/**
 * Conventions de tailles pour les menus déroulants (Dropdowns)
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
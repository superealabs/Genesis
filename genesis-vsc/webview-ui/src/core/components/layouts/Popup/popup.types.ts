/**
 * Positions disponibles pour les popups dans le viewport
 */
export type PopupPosition =
    | 'center'
    | 'top-left' | 'top' | 'top-right'
    | 'left' | 'right'
    | 'bottom-left' | 'bottom' | 'bottom-right';

/**
 * Tailles disponibles pour les popups
 */
export type PopupSize = 'sm' | 'md' | 'lg' | 'xl' | '2xl' | '3xl' | 'full';



/**
 * Hiérarchie standardisée des z-index pour les overlays et popups.
 * Plus la valeur est élevée, plus l'élément apparaît au-dessus.
 */
export const Z_INDEX = {
    /** Éléments de base (headers, sidebars) */
    base: 10,
    /** Dropdowns, menus contextuels */
    dropdown: 100,
    /** Popups, modales standards */
    modal: 200,
    /** Popups empilés (popup dans un popup) */
    modalStacked: 300,
    /** Notifications toast */
    toast: 400,
    /** Tooltips */
    tooltip: 500
} as const;

export type ZIndexLevel = typeof Z_INDEX[keyof typeof Z_INDEX];



/**
 * Niveaux de padding standardisés pour l'intérieur des popups.
 * Assure un alignement parfait entre le header et le contenu.
 */
export type PopupPadding = 'none' | 'sm' | 'md' | 'lg' | 'xl';

/**
 * Mapping des classes Tailwind pour le padding.
 * Le padding horizontal (px) du header correspond toujours à celui du contenu (p).
 */
export const PADDING_CLASSES: Record<PopupPadding, { header: string; content: string }> = {
    'none': { header: 'px-0 py-2', content: 'p-0' },
    'sm':   { header: 'px-3 py-2', content: 'p-3' },
    'md':   { header: 'px-4 py-3', content: 'p-4' },   // Défaut actuel
    'lg':   { header: 'px-6 py-4', content: 'p-6' },
    'xl':   { header: 'px-8 py-6', content: 'p-8' }
};
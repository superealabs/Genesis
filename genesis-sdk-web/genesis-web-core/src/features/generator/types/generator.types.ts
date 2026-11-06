// ═══ TYPES SPÉCIFIQUES À L'UI ═══
// Réexporte tout le shared pour que les composants Vue existants continuent de fonctionner
// sans avoir à changer tous leurs imports d'un coup.

// ═══ Types d'erreur du wizard ═══

/**
 * Erreur structurée du wizard.
 * - `title`   : titre court (ex. « Import échoué »)
 * - `summary` : résumé d'une ligne (ex. « 3 importée(s), 1 ignorée(s) — fichier.json »)
 * - `details` : liste de points (ex. « Type incompatible : … »)
 */
export interface WizardErrorPayload {
    title: string;
    summary?: string;
    details?: string[];
}

/**
 * Payload accepté par `setWizardError`.
 * Une string simple est aussi acceptée (compat. avec les appels existants).
 */
export type WizardError = string | WizardErrorPayload;


export * from '@genesis-labs/shared-types';

// Tu pourras ajouter ici uniquement les types qui ont ABSOLUMENT besoin 
// d'imports avec '@/...' (ex: types liés à des composants Pinia/Vue spécifiques)

// genesis-sdk-web/genesis-web-core/src/core/composables/ux/useGenesisWarning.ts
import { computed, shallowRef } from 'vue';

// ============================================================================
// TYPES
// ============================================================================

/** Ce que l'appelant fournit : générique, aucune notion métier (wizard, framework...) */
export interface WarningRequest {
  title: string;
  message: string;
  /** Conséquences listées sous le message (ex. données qui seront réinitialisées) */
  details?: string[];
  /** Défaut : « Continuer » */
  confirmLabel?: string;
  /** Défaut : « Annuler » */
  cancelLabel?: string;
  /** 'danger' = action destructive. Défaut : 'warning' */
  variant?: 'warning' | 'danger';
}

/** Ce que l'interface lira : tous les champs sont renseignés */
export type ActiveWarning = Required<WarningRequest>;

interface PendingWarning {
  warning: ActiveWarning;
  resolve: (confirmed: boolean) => void;
}

// ============================================================================
// ÉTAT PARTAGÉ (singleton de module)
// ============================================================================
// Module-level : utilisable depuis un composant ET depuis un fichier non-composant
// (ex. wizard-step-config.ts), exactement comme `wizardError` dans le store.
// File d'attente : si plusieurs avertissements sont demandés, ils s'affichent un par un.
const queue = shallowRef<PendingWarning[]>([]);

const current = computed<ActiveWarning | null>(() => queue.value[0]?.warning ?? null);

const normalize = (request: WarningRequest): ActiveWarning => ({
  details: [],
  confirmLabel: 'Continuer',
  cancelLabel: 'Annuler',
  variant: 'warning',
  ...request
});

/** Termine l'avertissement affiché avec la décision de l'utilisateur */
function settle(confirmed: boolean) {
  const [head, ...rest] = queue.value;
  if (!head) return;
  queue.value = rest;
  head.resolve(confirmed);
}

// ============================================================================
// COMPOSABLE
// ============================================================================
export function useGenesisWarning() {
  /**
   * Demande une confirmation à l'utilisateur.
   * @returns une Promise : true = « Continuer », false = « Annuler »
   */
  function confirm(request: WarningRequest): Promise<boolean> {
    return new Promise<boolean>((resolve) => {
      queue.value = [...queue.value, { warning: normalize(request), resolve }];
    });
  }

  /**
   * Comme `confirm`, mais seulement si `needed` est vrai.
   * Sinon renvoie directement true : évite un `if` à chaque appelant.
   */
  async function confirmIf(needed: boolean, request: WarningRequest): Promise<boolean> {
    return needed ? confirm(request) : true;
  }

  /** Appelé par l'interface : bouton « Continuer » */
  function accept() { settle(true); }

  /** Appelé par l'interface : bouton « Annuler » (ou fermeture) */
  function cancel() { settle(false); }

  /** Annule tous les avertissements en attente (ex. réinitialisation du wizard) */
  function clearAll() {
    const pending = queue.value;
    queue.value = [];
    pending.forEach(p => p.resolve(false));
  }

  return {
    /** Avertissement actuellement affiché (null si aucun) : à lier au popup */
    warning: current,
    hasWarning: computed(() => current.value !== null),
    confirm,
    confirmIf,
    accept,
    cancel,
    clearAll
  };
}
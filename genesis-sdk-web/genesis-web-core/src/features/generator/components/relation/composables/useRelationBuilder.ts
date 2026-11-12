// useRelationBuilder.ts
import { reactive, toRefs } from 'vue';
import { useGenerator } from '@genesis-labs/web-core/features/generator/composables/useGenerator';

/**
 * État PARTAGÉ entre RelationConfigSimpleView et RelationConfigAdvancedView.
 *
 * - `isChildFirst` : false = ParentFirst, true = ChildFirst
 * - `anchor`       : la table choisie en premier (parent si ParentFirst, enfant si ChildFirst)
 * - `targets`      : les tables choisies de l'autre côté (1 seule en Simple, N en Advanced)
 *
 * Module-level singleton : même instance quelle que soit la vue montée.
 */
const state = reactive({
  isChildFirst: false,
  anchor: '',
  targets: [] as string[],
});

// ============================================================================
// 1. ÉTAT PUR (sans dépendance injectée)
// ============================================================================
/**
 * ✅ Version SANS `useGenerator` — utilisable HORS composant (ex: wizard-step-config.ts).
 * Expose uniquement les mutations SYNCHRONES (pas d'appel API).
 */
export function useRelationBuilderState() {
  /** Setter pur pour `targets`. */
  function setTargets(names: string[]) {
    state.targets = names;
  }

  /**
   * Remise à zéro SYNCHRONE, sans appel API.
   * Utilisable partout (y compris dans `relations.onEnter` qui recharge déjà les listes).
   */
  function resetState() {
    state.isChildFirst = false;
    state.anchor = '';
    state.targets = [];
  }

  /**
   * Reset "ciblé" : vide `anchor` + `targets`, sans toucher à `isChildFirst`.
   * Pas d'appel API (l'appelant gère le rechargement s'il en a besoin).
   */
  function clearSelection() {
    state.anchor = '';
    state.targets = [];
  }

  return {
    ...toRefs(state),
    setTargets,
    resetState,
    clearSelection,
  };
}

// ============================================================================
// 2. COMPOSABLE COMPLET (avec `useGenerator`)
// ============================================================================
/**
 * ⚠️ Utilisable UNIQUEMENT dans un composant Vue (utilise `inject` via `useGenerator`).
 */
export function useRelationBuilder() {
  const { fetchTablesMetadataParents, fetchTablesMetadataChilds } = useGenerator();

  // ── Chargements ────────────────────────────────────────────────────────
  async function safe(label: string, fn: () => Promise<unknown>) {
    try {
      await fn();
    } catch (error) {
      console.error(`[useRelationBuilder] Échec chargement ${label}:`, error);
    }
  }

  /** Charge les deux listes complètes (sans filtre). */
  function loadAll() {
    return Promise.all([
      safe('parents', () => fetchTablesMetadataParents()),
      safe('enfants', () => fetchTablesMetadataChilds()),
    ]);
  }

  /** Charge le côté "cible" : enfants du parent (ParentFirst) ou parents de l'enfant (ChildFirst). */
  function loadTargetSide(anchor: string) {
    const name = anchor || undefined; // vide = tous
    return state.isChildFirst
      ? safe('parents', () => fetchTablesMetadataParents(name))
      : safe('enfants', () => fetchTablesMetadataChilds(name));
  }

  // ── Actions ────────────────────────────────────────────────────────────

  /** Change le sens (ParentFirst ↔ ChildFirst) : reset des sélections + rechargement complet. */
  async function setDirection(childFirst: boolean) {
    if (state.isChildFirst === childFirst) return;
    state.isChildFirst = childFirst;
    state.anchor = '';
    state.targets = [];
    await loadAll();
  }

  /** Choisit la table "ancre" : reset des cibles + chargement des tables liées. */
  async function setAnchor(name: string) {
    if (state.anchor === name) return;
    state.anchor = name;
    state.targets = [];
    await loadTargetSide(name);
  }

  /**
   * Vide la sélection (anchor + targets).
   * ⚠️ SYNCHRONE : ne recharge PAS les listes.
   * Si l'appelant a besoin de recharger les listes complètes, il doit appeler `loadAll()` ou
   * `loadTargetSide('')` lui-même.
   */
  function resetSelection() {
    state.anchor = '';
    state.targets = [];
  }

  // ✅ Réutilise les fonctions pures
  const { setTargets, resetState } = useRelationBuilderState();

  return {
    ...toRefs(state),
    setDirection,
    setAnchor,
    setTargets,
    resetSelection,
    resetState,
  };
}
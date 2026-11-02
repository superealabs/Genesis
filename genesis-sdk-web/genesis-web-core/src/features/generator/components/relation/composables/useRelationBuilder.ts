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
 * Les chargements API (parents/enfants liés) sont centralisés ici (plus de watchers dans les vues).
 */
const state = reactive({
  isChildFirst: false,
  anchor: '',
  targets: [] as string[],
});

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

  function setTargets(names: string[]) {
    state.targets = names;
  }

  /** Vide la sélection (ancre + cibles) et recharge les listes complètes si besoin. */
  async function resetSelection() {
    const hadAnchor = !!state.anchor;
    state.anchor = '';
    state.targets = [];
    if (hadAnchor) await loadTargetSide('');
  }

  /**
   * Remise à zéro SYNCHRONE, sans appel API.
   * À utiliser dans `relations.onEnter` (qui charge déjà les listes complètes).
   */
  function resetState() {
    state.isChildFirst = false;
    state.anchor = '';
    state.targets = [];
  }

  return {
    ...toRefs(state), // isChildFirst, anchor, targets (refs en lecture)
    setDirection,
    setAnchor,
    setTargets,
    resetSelection,
    resetState,
  };
}
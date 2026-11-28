// useRelationBuilder.ts
import { reactive, toRefs } from 'vue';
import type { TableMetadataDto } from '@genesis-labs/shared-types';
import { useGenerator } from '@genesis-labs/web-core/features/generator/composables/useGenerator';
import { useGeneratorStore } from '@genesis-labs/web-core/features/generator/store/useGenerator.store';

/**
 * Jeton de la dernière demande de chargement (module-level, partagé entre les vues).
 * Seule la demande la plus récente peut écrire dans le store : une nouvelle ancre
 * ou une annulation rend obsolètes les réponses encore en vol.
 */
let loadToken = 0;

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
  const { getParentsOf, getChildrenOf } = useGenerator();
  const store = useGeneratorStore();

  // ── Chargements ────────────────────────────────────────────────────────
  /**
   * Chargement « dernier arrivé gagne ».
   * Chaque appel prend un nouveau jeton ; si un autre chargement (ou une annulation)
   * démarre entre-temps, la réponse de celui-ci est ignorée.
   */
  async function runLoad(
    label: string,
    fetchers: {
      parents?: () => Promise<TableMetadataDto[]>;
      childs?: () => Promise<TableMetadataDto[]>;
    }
  ) {
    const token = ++loadToken;
    const isCurrent = () => token === loadToken;

    store.setIsLoadingRelationTables(true);
    try {
      if (fetchers.parents) {
        const parents = await fetchers.parents();
        if (!isCurrent()) return;
        store.setTablesParents(parents);
      }
      if (fetchers.childs) {
        const childs = await fetchers.childs();
        if (!isCurrent()) return;
        store.setTablesChilds(childs);
      }
    } catch (error) {
      if (isCurrent()) console.error(`[useRelationBuilder] Échec chargement ${label}:`, error);
    } finally {
      // Un chargement plus récent possède le flag : on n'y touche pas.
      if (isCurrent()) store.setIsLoadingRelationTables(false);
    }
  }

  /** Charge les deux listes complètes (sans filtre). */
  function loadAll() {
    return runLoad('parents + enfants', {
      parents: () => getParentsOf(),
      childs: () => getChildrenOf(),
    });
  }

  /** Charge le côté "cible" : enfants du parent (ParentFirst) ou parents de l'enfant (ChildFirst). */
  function loadTargetSide(anchor: string) {
    const name = anchor || undefined; // vide = tous
    return state.isChildFirst
      ? runLoad('parents', { parents: () => getParentsOf(name) })
      : runLoad('enfants', { childs: () => getChildrenOf(name) });
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

  /**
   * Annule le choix courant : vide anchor + targets et recharge la liste complète du côté cible.
   * Le nouveau chargement prend un nouveau jeton : la réponse en vol est ignorée.
   */
  async function cancelSelection() {
    resetSelection();
    await loadTargetSide('');
  }

  // ✅ Réutilise les fonctions pures
  const { setTargets, resetState } = useRelationBuilderState();

  return {
    ...toRefs(state),
    setDirection,
    setAnchor,
    setTargets,
    resetSelection,
    cancelSelection,
    resetState,
  };
}
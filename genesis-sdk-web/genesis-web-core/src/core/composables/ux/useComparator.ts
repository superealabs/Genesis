// genesis-sdk-web/genesis-web-core/src/core/composables/ux/useComparator.ts
//
// Responsabilité unique : l'état de la vue de comparaison (ouverte ? possible ?) et la liste d'éditeurs
// qu'elle affiche. Il lit les slots de useCompareSlots mais ne les modifie jamais.
// Générique : Framework, Database et Frontend l'appellent chacun avec leur composant de détails.

import { computed, ref, toValue, watch, type Component, type MaybeRefOrGetter } from 'vue';
import type { PanelEditorDefinition } from './UseGenesisPanel';
import type { UseCompareSlotsReturn } from './useCompareSlots';

export interface ComparatorConfig<T> {
  /** Seulement ce qu'on lit : le composable de slots reste propriétaire de son état */
  compare: Pick<UseCompareSlotsReturn<T>, 'mode' | 'compareCount' | 'selectedItems'>;
  /** Tous les éléments comparables : c'est la liste du sélecteur de chaque colonne */
  items: MaybeRefOrGetter<T[]>;
  getId: (item: T) => string | number;
  getLabel: (item: T) => string;
  /** Composant de détails de la feature (ex. FrameworkDetails) */
  component: Component;
  /** Nom de la prop qui reçoit l'élément (ex. 'framework') */
  propName: string;
}

export function useComparator<T>(config: ComparatorConfig<T>) {
  const { compare, items, getId, getLabel, component, propName } = config;

  const isOpen = ref(false);
  /** Ids des éditeurs affichés à l'ouverture, un par colonne, dans l'ordre des slots */
  const initialEditorIds = ref<string[]>([]);

  /** Déclenchable : mode comparaison et au moins deux slots remplis */
  const canOpen = computed(() => compare.mode.value === 'compare' && compare.compareCount.value >= 2);

  const editors = computed<PanelEditorDefinition[]>(() =>
    toValue(items).map(item => ({
      id: String(getId(item)),
      label: getLabel(item),
      component,
      props: { [propName]: item }
    }))
  );

  function open() {
    if (!canOpen.value) return;
    initialEditorIds.value = compare.selectedItems.value.map(item => String(getId(item)));
    isOpen.value = true;
  }

  function close() {
    isOpen.value = false;
  }

  // Passer en mode sélection ou vider des slots pendant l'affichage ferme la vue
  watch(canOpen, (ok) => { if (!ok) close(); });

  return { isOpen, canOpen, editors, initialEditorIds, open, close };
}
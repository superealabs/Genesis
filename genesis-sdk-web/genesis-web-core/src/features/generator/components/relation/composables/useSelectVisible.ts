import { computed, toValue, type MaybeRefOrGetter } from 'vue';

/**
 * "Select All" limité aux éléments VISIBLES (filtrés / recherchés).
 * - Tout sélectionner : ajoute les visibles à la sélection existante (le reste est conservé).
 * - Tout désélectionner : retire uniquement les visibles (le reste est conservé).
 */
export function useSelectVisible(
  visibleIds: MaybeRefOrGetter<string[]>,
  selectedIds: MaybeRefOrGetter<string[]>,
  update: (ids: string[]) => void,
) {
  const visibleCount = computed(() => toValue(visibleIds).length);

  const selectedCount = computed(() => {
    const selected = new Set(toValue(selectedIds));
    return toValue(visibleIds).filter(id => selected.has(id)).length;
  });

  const allSelected = computed(() => visibleCount.value > 0 && selectedCount.value === visibleCount.value);

  function toggleAll() {
    const visible = toValue(visibleIds);
    const current = toValue(selectedIds);
    if (allSelected.value) {
      const toRemove = new Set(visible);
      update(current.filter(id => !toRemove.has(id)));
    } else {
      const already = new Set(current);
      update([...current, ...visible.filter(id => !already.has(id))]);
    }
  }

  return { visibleCount, selectedCount, allSelected, toggleAll };
}
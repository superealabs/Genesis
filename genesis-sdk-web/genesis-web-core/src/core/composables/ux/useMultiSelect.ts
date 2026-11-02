import { ref, type Ref, watch } from 'vue';

export type SelectMode = 'Select' | 'UnSelect' | 'Invert';

export interface UseMultiSelectOptions<T> {
  /** Éléments visibles (filtrés, dans l'ordre d'affichage) */
  items: Ref<T[]>;
  /** Extracteur d'identifiant unique */
  getId: (item: T) => string | number;
  /** Ref partagée contenant les identifiants sélectionnés (v-model) */
  selectedIds: Ref<Array<string | number>>;
  /** Mode par défaut (réactif) — 'Select' par défaut */
  selectMode?: Ref<SelectMode>;
}

export function useMultiSelect<T>({
  items,
  getId,
  selectedIds,
  selectMode = ref<SelectMode>('Select') as Ref<SelectMode>,
}: UseMultiSelectOptions<T>) {
  // Ancre : dernier élément cliqué sans Shift
  const anchorId = ref<string | number | null>(null);

  /** Retire une valeur du tableau (par identifiant) */
  function removeId(id: string | number) {
    const idx = selectedIds.value.indexOf(id);
    if (idx !== -1) selectedIds.value.splice(idx, 1);
  }

  /** Ajoute une valeur (si absente) */
  function addId(id: string | number) {
    if (!selectedIds.value.includes(id)) {
      selectedIds.value.push(id);
    }
  }

  /** Toggle une valeur */
  function toggleId(id: string | number) {
    if (selectedIds.value.includes(id)) removeId(id);
    else addId(id);
  }

  /** Applique un mode sur une plage d'identifiants */
  function applyMode(ids: Array<string | number>, mode: SelectMode) {
    if (mode === 'Invert') {
      ids.forEach(toggleId);
      return;
    }

    // Détection "toute la plage" pour Select/UnSelect
    const allSelected = ids.every(id => selectedIds.value.includes(id));
    const allUnselected = ids.every(id => !selectedIds.value.includes(id));

    let effectiveMode: SelectMode = mode;
    if (mode === 'Select' && allSelected) effectiveMode = 'UnSelect';
    if (mode === 'UnSelect' && allUnselected) effectiveMode = 'Select';

    if (effectiveMode === 'Select') ids.forEach(addId);
    else ids.forEach(removeId);
  }

  /**
   * Handler principal à brancher sur le clic d'une ligne.
   * - Sans Shift : toggle simple + ancre = item
   * - Avec Shift :
   *   - si pas d'ancre (ou ancre absente des items) → toggle simple
   *   - sinon → applique selectMode sur la plage [ancre..cible]
   */
  function handleClick(item: T, event: MouseEvent) {
    event.preventDefault();

    const id = getId(item);

    // Shift+clic
    if (event.shiftKey) {
      const anchor = anchorId.value;
      const anchorIndex = anchor !== null
        ? items.value.findIndex(i => getId(i) === anchor)
        : -1;

      // Ancre absente (pas encore de clic, ou ancre sortie du filtre) → clic simple
      if (anchorIndex === -1) {
        toggleId(id);
        anchorId.value = id;
        return;
      }

      const targetIndex = items.value.findIndex(i => getId(i) === id);
      if (targetIndex === -1) return;

      const start = Math.min(anchorIndex, targetIndex);
      const end = Math.max(anchorIndex, targetIndex);
      const rangeIds = items.value.slice(start, end + 1).map(getId);

      applyMode(rangeIds, selectMode.value);
      // L'ancre reste inchangée
      return;
    }

    // Clic simple : toggle + ancre
    toggleId(id);
    anchorId.value = id;
  }

  /** Reset l'ancre */
  function clearAnchor() {
    anchorId.value = null;
  }

  // Reset auto si l'ancre n'est plus dans les items visibles (changement de filtre)
  watch(
    items,
    (newItems) => {
      if (anchorId.value === null) return;
      const stillPresent = newItems.some(i => getId(i) === anchorId.value);
      if (!stillPresent) anchorId.value = null;
    },
    { deep: false } // watch sur la référence (les computed renvoient une nouvelle ref à chaque filtre)
  );

  return { handleClick, selectMode, clearAnchor };
}
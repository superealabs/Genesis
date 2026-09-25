<template>
  <DatabaseLayout
    v-bind="layoutProps"
    @back="$emit('back')"
    @update:displayMode="setDisplayMode"
    @update:mode="handleModeChange"
    @select-replace="handleReplaceSelection"
    @close-replace="cancelReplace"
    @select="handleSelectWrapper"
  />
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue';

// Types
import type { DatabaseEngineDto } from '@genesis-labs/shared-types';
import type { DisplayMode } from '@genesis-labs/web-core/core/components/layouts/display/GenesisItem.types';

// Composables
import { useDatabase } from '@genesis-labs/web-core/features/database/composables/useDatabase';

// Composants
import DatabaseLayout, { type DatabaseLayoutProps } from '@genesis-labs/web-core/features/database/components/DatabaseLayout.vue';

// ============================================================================
// 1. PROPS & EMITS
// ============================================================================
withDefaults(defineProps<{
  showBackButton?: boolean;
}>(), {
  showBackButton: true
});

const emit = defineEmits<{
  'back': [];
  'select': [result: { action: string; engine: DatabaseEngineDto; event?: MouseEvent }];
}>();

// ============================================================================
// 2. COMPOSABLE MÉTIER
// ============================================================================
const {
  engines,
  selectedId,
  databaseSlots,
  displayMode,
  compareMode,
  hasEngines,
  fetchAvailableEngines,
  handleSelect,
  handleModeChange,
  handleReplace,
  compare,
  showReplacePopup,
  pendingEngine,
  mouseX,
  mouseY,
  cancelReplace,
  setDisplayMode
} = useDatabase();

// ============================================================================
// 3. COMPUTEDS (Données dérivées pour l'UI)
// ============================================================================

/**
 * Transforme les slots de comparaison actifs en une liste d'options 
 * exploitables par le popup de remplacement du composant DatabaseLayout.
 */
const replaceOptions = computed(() => {
  if (!compare?.slots?.value) return [];
  
  return Object.entries(compare.slots.value)
    .filter(([, engine]) => engine !== null)
    .map(([slot, engine]) => ({
      id: slot,
      label: `Slot ${slot}`,
      description: (engine as DatabaseEngineDto).name
    }));
});

/**
 * Regroupe toutes les propriétés réactives nécessaires au composant 
 * DatabaseLayout pour éviter une imbrication excessive dans le template.
 */
const layoutProps = computed<DatabaseLayoutProps>(() => ({
  engines: engines.value,
  selectedId: selectedId.value,
  databaseSlots: databaseSlots.value,
  displayMode: displayMode.value,
  compareMode: compareMode.value,
  replaceOptions: replaceOptions.value,
  showReplacePopup: showReplacePopup.value,
  mouseX: mouseX.value,
  mouseY: mouseY.value,
  isLoading: false // Note : À connecter à un état isLoading si useDatabase l'expose à l'avenir
}));

// ============================================================================
// 4. ACTIONS & HANDLERS
// ============================================================================

/**
 * Wrapper autour de handleSelect pour intercepter le résultat, 
 * gérer les erreurs potentielles et émettre l'événement vers le parent.
 */
async function handleSelectWrapper(engine: DatabaseEngineDto, event?: MouseEvent) {
  try {
    const result = await handleSelect(engine, event);
    emit('select', result);
  } catch (error) {
    console.error("[DatabaseView] Erreur lors de la sélection du moteur :", error);
  }
}

/**
 * Gère le remplacement d'un moteur dans un slot de comparaison.
 * Vérifie d'abord qu'un moteur est en attente de remplacement avant d'agir.
 */
function handleReplaceSelection(slotId: string | number) {
  if (pendingEngine.value) {
    handleReplace(slotId, pendingEngine.value);
  }
  cancelReplace();
}

// ============================================================================
// 5. LIFECYCLE & EXPOSE
// ============================================================================

// Permet aux composants parents de déclencher manuellement le chargement si nécessaire
defineExpose({
  fetchAvailableEngines
});

onMounted(() => {
  if (!hasEngines.value) {
    fetchAvailableEngines();
  }
});
</script>
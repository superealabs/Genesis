<template>
  <FrontendLayout
    v-bind="layoutProps"
    @back="$emit('back')"
    @update:searchValue="setSearch"
    @update:displayMode="setDisplayMode"
    @update:mode="handleModeChange"
    @select-replace="handleReplaceSelection"
    @close-replace="cancelReplace"
    @select="handleSelectWrapper"
    @info="handleInfo"
  />
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue';

// Types
import type { FrontendFramework } from '@genesis-labs/shared-types';

// Composables
import { useFrontend } from '@genesis-labs/web-core/features/frontend/composables/useFrontend';

// Composants
import FrontendLayout, { type FrontendLayoutProps } from '@genesis-labs/web-core/features/frontend/components/FrontendLayout.vue';

// ============================================================================
// 1. PROPS & EMITS
// ============================================================================
const props = withDefaults(defineProps<{
  showBackButton?: boolean;
}>(), {
  showBackButton: true
});

const emit = defineEmits<{
  'back': [];
  'select': [result: { action: string; framework: FrontendFramework; event?: MouseEvent }];
}>();

// ============================================================================
// 2. COMPOSABLE MÉTIER
// ============================================================================
const {
  availableFrontendFrameworks,
  selectedId,
  frontendFrameworkSlots,
  displayMode,
  searchQuery,
  compareMode,
  compare,
  initialize,
  setSearch,
  setDisplayMode,
  handleSelect,
  handleReplace,
  handleModeChange,
  showReplacePopup,
  pendingFramework,
  mouseX,
  mouseY,
  cancelReplace,
  handleInfo,
  isLoading
} = useFrontend();

// ============================================================================
// 3. COMPUTEDS (Données dérivées pour l'UI)
// ============================================================================

/**
 * Transforme les slots de comparaison actifs en une liste d'options 
 * exploitables par le popup de remplacement du composant FrontendLayout.
 */
const replaceOptions = computed(() => {
  if (!compare?.slots?.value) return [];
  
  return Object.entries(compare.slots.value)
    .filter(([, fw]) => fw !== null)
    .map(([slot, fw]) => ({
      id: slot,
      label: `Slot ${slot}`,
      description: (fw as FrontendFramework).name
    }));
});

/**
 * Regroupe toutes les propriétés réactives nécessaires au composant 
 * FrontendLayout pour éviter une imbrication excessive dans le template.
 */
const layoutProps = computed<FrontendLayoutProps>(() => ({
  frontends: availableFrontendFrameworks.value,
  selectedId: selectedId.value,
  frontendFrameworkSlots: frontendFrameworkSlots.value,
  displayMode: displayMode.value,
  searchQuery: searchQuery.value,
  compareMode: compareMode.value,
  replaceOptions: replaceOptions.value,
  showReplacePopup: showReplacePopup.value,
  mouseX: mouseX.value,
  mouseY: mouseY.value,
  isLoading: isLoading.value, // Utilisation correcte de l'état réactif du composable
  showBackButton: props.showBackButton
}));

// ============================================================================
// 4. ACTIONS & HANDLERS
// ============================================================================

/**
 * Wrapper autour de handleSelect pour intercepter le résultat, 
 * gérer les erreurs potentielles et émettre l'événement vers le parent.
 */
async function handleSelectWrapper(framework: FrontendFramework, event?: MouseEvent) {
  try {
    const result = await handleSelect(framework, event);
    emit('select', result);
  } catch (error) {
    console.error("[FrontendsView] Erreur lors de la sélection du framework :", error);
  }
}

/**
 * Gère le remplacement d'un framework dans un slot de comparaison.
 * Vérifie d'abord qu'un framework est en attente de remplacement avant d'agir.
 */
function handleReplaceSelection(slotId: string | number) {
  if (pendingFramework.value) {
    handleReplace(slotId, pendingFramework.value);
  }
  cancelReplace();
}

// ============================================================================
// 5. LIFECYCLE & EXPOSE
// ============================================================================

// Permet aux composants parents de déclencher manuellement le chargement si nécessaire
defineExpose({
  initialize
});

onMounted(() => {
  initialize();
});
</script>
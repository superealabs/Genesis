<template>
  <FrameworkLayout
    v-bind="layoutProps"
    @back="$emit('back')"
    @update:mode="handleModeChange"
    @update:searchValue="setSearch"
    @update:displayMode="setDisplayMode"
    @update:filters="setFilters"
    @openFilter="isFilterOpen = true"
    @closeFilter="isFilterOpen = false"
    @closeDetail="detailFramework = null"
    @select-replace="handleReplaceSelection"
    @close-replace="cancelReplace"
    @select="handleSelectWrapper"
    @info="handleInfo"
  />
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import type { Framework } from '@genesis-labs/shared-types';

import { useFrameworks } from '@genesis-labs/web-core/features/frameworks/composables/useFrameworks';
import FrameworkLayout, { type FrameworkLayoutProps } from '@genesis-labs/web-core/features/frameworks/components/FrameworkLayout.vue';

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
  'select': [result: { action: string; framework: Framework; event?: MouseEvent }];
}>();

// ============================================================================
// 2. INJECTION DU COMPOSABLE METIER
// ============================================================================
const {
  frameworks,
  selectedId,
  displayMode,
  compareMode,
  frameworkSlots,
  filters,
  searchQuery,
  isLoading,
  showReplacePopup,
  pendingFramework,
  mouseX,
  mouseY,
  compare,
  initialize,
  setSearch,
  setFilters,
  setDisplayMode,
  handleModeChange,
  handleSelect,
  handleReplace,
  cancelReplace,
  triggerReplace
} = useFrameworks();

// ============================================================================
// 3. ÉTAT LOCAL (UI Spécifique à cette vue)
// ============================================================================
const detailFramework = ref<Framework | null>(null);
const isFilterOpen = ref(false);

// ============================================================================
// 4. COMPUTEDS (Données dérivées)
// ============================================================================

/**
 * Transforme les slots de comparaison actifs en une liste d'options 
 * exploitables par le popup de remplacement.
 */
const replaceOptions = computed(() => {
  if (!compare?.slots?.value) return [];
  
  return Object.entries(compare.slots.value)
    .filter(([, fw]) => fw !== null)
    .map(([slot, fw]) => ({
      id: slot,
      label: `Slot ${slot}`,
      description: (fw as Framework).name
    }));
});

/**
 * Regroupe toutes les propriétés réactives nécessaires au composant 
 * FrameworkLayout pour éviter une imbrication excessive dans le template.
 */
const layoutProps = computed<FrameworkLayoutProps>(() => ({
  frameworks: frameworks.value,
  selectedId: selectedId.value,
  displayMode: displayMode.value,
  compareMode: compareMode.value,
  frameworkSlots: frameworkSlots.value,
  filters: filters.value,
  searchQuery: searchQuery.value,
  showReplacePopup: showReplacePopup.value,
  replaceOptions: replaceOptions.value,
  mouseX: mouseX.value,
  mouseY: mouseY.value,
  pendingFramework: pendingFramework.value,
  detailFramework: detailFramework.value,
  isFilterOpen: isFilterOpen.value,
  isLoading: isLoading.value,
  showBackButton: props.showBackButton // Transmission explicite de la prop
}));

// ============================================================================
// 5. ACTIONS & HANDLERS
// ============================================================================

function handleInfo(framework: Framework) { 
  detailFramework.value = framework; 
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

/**
 * Wrapper autour de handleSelect pour intercepter le résultat, 
 * gérer les erreurs potentielles et émettre l'événement vers le parent.
 */
async function handleSelectWrapper(framework: Framework, event?: MouseEvent) {
  try {
    await handleSelect(framework, event);
    emit('select', { action: 'select', framework: framework, event }); 
  } catch (error) {
    console.error("[FrameworksView] Erreur lors de la sélection du framework :", error);
  }
}

// ============================================================================
// 6. LIFECYCLE & EXPOSE
// ============================================================================

// Permet aux composants parents de déclencher manuellement ces actions si nécessaire
defineExpose({ 
  initialize,
  triggerReplace 
});

onMounted(() => { 
  initialize(); 
});
</script>
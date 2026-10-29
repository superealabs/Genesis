<template>
  <RepositoryLayout
    v-bind="layoutProps"
    :group-by="groupBy"
    @back="$emit('back')"
    @update:mode="handleModeChange"
    @update:searchValue="setSearch"
    @update:displayMode="setDisplayMode"
    @closeDetail="detailProvider = null"
    @select-replace="handleReplaceSelection"
    @close-replace="cancelReplace"
    @select="handleSelectWrapper"
    @info="handleInfo"
    @compare="comparator.open"
    @close-compare="comparator.close"
    @update:group-by="handleUpdateGroupBy"
  />
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import type { RepositoryProviderMetadata } from '@genesis-labs/shared-types';

import { useRepository } from '@genesis-labs/web-core/features/repository/composables/useRepository';
import RepositoryLayout, { type RepositoryLayoutProps } from '@genesis-labs/web-core/features/repository/components/RepositoryLayout.vue';

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
  'select': [result: { action: string; provider: RepositoryProviderMetadata; event?: MouseEvent }];
}>();

// ============================================================================
// 2. INJECTION DU COMPOSABLE MÉTIER
// ============================================================================
const {
  providers,
  selectedId,
  displayMode,
  compareMode,
  providerSlots,
  searchQuery,
  isLoading,
  showReplacePopup,
  pendingProvider,
  mouseX,
  mouseY,
  compare,
  comparator,
  initialize,
  setSearch,
  setDisplayMode,
  handleModeChange,
  handleSelect,
  handleReplace,
  cancelReplace,
  triggerReplace
} = useRepository();

// ============================================================================
// 3. ÉTAT LOCAL (UI Spécifique à cette vue)
// ============================================================================
const detailProvider = ref<RepositoryProviderMetadata | null>(null);

// ÉTAT DE REGROUPEMENT LOCAL (Rend le composant autonome)
const groupBy = ref<keyof RepositoryProviderMetadata | null>(null);

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
    .filter(([, provider]) => provider !== null)
    .map(([slot, provider]) => ({
      id: slot,
      label: `Slot ${slot}`,
      description: (provider as RepositoryProviderMetadata).name
    }));
});

/**
 * Regroupe toutes les propriétés réactives nécessaires au composant 
 * RepositoryLayout pour éviter une imbrication excessive dans le template.
 */
const layoutProps = computed<RepositoryLayoutProps>(() => {
  const propsObj = {
    providers: providers.value,
    selectedId: selectedId.value,
    displayMode: displayMode.value,
    compareMode: compareMode.value,
    providerSlots: providerSlots.value,
    searchQuery: searchQuery.value,
    showReplacePopup: showReplacePopup.value,
    replaceOptions: replaceOptions.value,
    mouseX: mouseX.value,
    mouseY: mouseY.value,
    pendingProvider: pendingProvider.value,
    detailProvider: detailProvider.value,
    isLoading: isLoading.value,
    showBackButton: props.showBackButton,
    groupBy: groupBy.value,

    // ── Comparaison (trigger + vue) ──
    canCompare: comparator.canOpen.value,
    compareCount: compare.compareCount.value,
    comparing: comparator.isOpen.value,
    comparatorEditors: comparator.editors.value,
    comparatorEditorIds: comparator.initialEditorIds.value
  };

  return propsObj;
});

// ============================================================================
// 5. ACTIONS & HANDLERS
// ============================================================================

function handleUpdateGroupBy(value: string | null) {
  groupBy.value = value as keyof RepositoryProviderMetadata | null;
}

function handleInfo(provider: RepositoryProviderMetadata) { 
  detailProvider.value = provider; 
}

/**
 * Gère le remplacement d'un provider dans un slot de comparaison.
 * Vérifie d'abord qu'un provider est en attente de remplacement avant d'agir.
 */
function handleReplaceSelection(slotId: string | number) {
  if (pendingProvider.value) {
    handleReplace(slotId, pendingProvider.value);
  }
  cancelReplace();
}

/**
 * Wrapper autour de handleSelect pour intercepter le résultat, 
 * gérer les erreurs potentielles et émettre l'événement vers le parent.
 */
async function handleSelectWrapper(provider: RepositoryProviderMetadata, event?: MouseEvent) {
  try {
    await handleSelect(provider, event);
    emit('select', { action: 'select', provider: provider, event }); 
  } catch (error) {
    console.error("[RepositoryView] Erreur lors de la sélection du provider :", error);
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
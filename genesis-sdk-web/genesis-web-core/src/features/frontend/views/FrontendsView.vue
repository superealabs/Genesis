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
import { useFrontend } from '@genesis-labs/web-core/features/frontend/composables/useFrontend';
import FrontendLayout, { type FrontendLayoutProps } from '@genesis-labs/web-core/features/frontend/components/FrontendLayout.vue';
import type { FrontendFramework } from '@genesis-labs/shared-types';

withDefaults(defineProps<{ showBackButton?: boolean }>(), { showBackButton: true });

const emit = defineEmits<{
  'back': [];
  'select': [result: { action: string; framework: FrontendFramework; event?: MouseEvent }];
}>();

const {
  availableFrontendFrameworks, selectedId, frontendFrameworkSlots, displayMode,
  searchQuery, compareMode, compare, initialize, setSearch, setDisplayMode,
  handleSelect, handleReplace, handleModeChange, showReplacePopup,
  pendingFramework, mouseX, mouseY, cancelReplace, handleInfo
} = useFrontend();

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
  isLoading: false // À adapter si useFrontend expose un isLoading
}));

async function handleSelectWrapper(framework: FrontendFramework, event?: MouseEvent) {
  try {
    const result = await handleSelect(framework, event);
    emit('select', result);
  } catch (error) {
    console.error("❌ [FrontEndSelectionView] Erreur sur le handleSelect :", error);
  }
}

function handleReplaceSelection(slotId: string | number) {
  if (pendingFramework.value) handleReplace(slotId, pendingFramework.value);
  cancelReplace();
}

defineExpose({ initialize });

onMounted(() => {
  initialize();
});
</script>
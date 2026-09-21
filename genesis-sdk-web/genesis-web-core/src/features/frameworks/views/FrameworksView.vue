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
import { useFrameworks } from '@genesis-labs/web-core/features/frameworks/composables/useFrameworks';
import FrameworkLayout, { type FrameworkLayoutProps } from '@genesis-labs/web-core/features/frameworks/components/FrameworkLayout.vue';
import type { Framework } from '@genesis-labs/shared-types';

withDefaults(defineProps<{ showBackButton?: boolean }>(), { showBackButton: true });

const emit = defineEmits<{
  'back': [];
  'select': [result: { action: string; framework: Framework; event?: MouseEvent }];
}>();

const {
  frameworks, selectedId, displayMode, compareMode, frameworkSlots,
  filters, searchQuery, isLoading, handleModeChange, handleSelect,
  handleReplace, compare, initialize, setSearch, setFilters, setDisplayMode,
  showReplacePopup, pendingFramework, mouseX, mouseY, cancelReplace,
  triggerReplace
} = useFrameworks();

const detailFramework = ref<Framework | null>(null);
const isFilterOpen = ref(false);

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
  isLoading: isLoading.value
}));

function handleInfo(framework: Framework) { detailFramework.value = framework; }

function handleReplaceSelection(slotId: string | number) {
  if (pendingFramework.value) handleReplace(slotId, pendingFramework.value);
  cancelReplace();
}

async function handleSelectWrapper(framework: Framework, event?: MouseEvent) {
  try {
    await handleSelect(framework, event);
    emit('select', { action: 'select', framework: framework, event }); 
  } catch (error) {
    console.error("❌ [FrameworksView] Alerte sur le handleSelect :", error);
  }
}

defineExpose({ 
  initialize,
  triggerReplace 
});

onMounted(() => { initialize(); });
</script>
<template>
  <GenesisCollectionLayout
    title="Frameworks"
    v-model:searchValue="searchQuery"
    v-model:displayMode="displayMode"
    :mode="compareMode"
    searchPlaceholder="Rechercher par nom, core, type..."
    :showBackButton="showBackButton"
    @back="$emit('back')"
    @openFilter="isFilterOpen = true"
    @update:mode="handleModeChange"
  >
    <FrameworkList
      :frameworks="frameworks"
      :selectedId="selectedId"
      :display="displayMode"
      :frameworkSlots="frameworkSlots"
      @select="handleSelectWrapper"
      @info="handleInfo"
    />
  </GenesisCollectionLayout>

  <!-- ═══ POPUP DE FILTRE ═══ -->
  <BaseFormPopup
    v-if="isFilterOpen"
    title="Filtres des Frameworks"
    :size="'lg'"
    @close="isFilterOpen = false"
  >
    <FrameworkFilter 
      v-model:filters="filters" 
      @close="isFilterOpen = false" 
    />
  </BaseFormPopup>

  <SimpleSelectionPopup
    :show="showReplacePopup"
    :mouseX="mouseX"
    :mouseY="mouseY"
    :options="replaceOptions"
    position="bottom-right"
    @select="handleReplaceSelection"
    @close="cancelReplace"
  />

  <FrameworkDetail
    v-if="detailFramework"
    :framework="detailFramework"
    @close="detailFramework = null"
  />
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useFrameworks } from '../composables/useFrameworks';
import FrameworkList from '../components/FrameworkList.vue';
import FrameworkFilter from '../components/FrameworkFilter.vue';
import FrameworkDetail from '../components/FrameworkDetail.vue';
import GenesisCollectionLayout from '@/core/components/layouts/GenesisCollectionLayout.vue';
import BaseFormPopup from '@/core/components/layouts/Popup/BaseFormPopup.vue'; // ✅ Ajouté
import SimpleSelectionPopup from '@/core/components/layouts/Popup/SimpleSelectionPopup.vue';
import type { SelectionOption } from '@/core/components/layouts/Popup/SimpleSelectionPopup.vue';
import type { Framework } from '../types/framework.types';

withDefaults(defineProps<{ showBackButton?: boolean }>(), { showBackButton: true });

const emit = defineEmits<{
  'back': [];
  'select': [result: { action: string; framework: Framework; event?: MouseEvent }];
}>();

const {
  frameworks, selectedId, displayMode, compareMode, frameworkSlots, filters, searchQuery,
  /*setSearch, setFilters, toggleDisplayMode,*/ handleModeChange,
  handleSelect, handleReplace, compare, initialize
} = useFrameworks();

const detailFramework = ref<Framework | null>(null);
const showReplacePopup = ref(false);
const pendingFramework = ref<Framework | null>(null);
const mouseX = ref<number | null>(null);
const mouseY = ref<number | null>(null);
const isFilterOpen = ref(false); // ✅ État du popup de filtre

const replaceOptions = computed<SelectionOption[]>(() => {
  if (!compare?.slots?.value) return [];
  return Object.entries(compare.slots.value)
    .filter(([, framework]) => framework !== null)
    .map(([slot, framework]) => ({
      id: slot,
      label: `Slot ${slot}`,
      description: (framework as Framework).name
    }));
});

function handleInfo(framework: Framework) { detailFramework.value = framework; }
function handleReplaceSelection(slotId: string | number) {
  if (pendingFramework.value) handleReplace(slotId, pendingFramework.value);
  cancelReplace();
}
function cancelReplace() {
  showReplacePopup.value = false;
  pendingFramework.value = null;
  mouseX.value = null;
  mouseY.value = null;
}
async function handleSelectWrapper(framework: Framework, event?: MouseEvent) {
  const result = await handleSelect(framework, event);
  emit('select', result);
}

defineExpose({
  triggerReplace: (framework: Framework, event?: MouseEvent) => {
    pendingFramework.value = framework;
    mouseX.value = event ? event.clientX : window.innerWidth / 2;
    mouseY.value = event ? event.clientY : window.innerHeight / 2;
    showReplacePopup.value = true;
  }
});

onMounted(() => { initialize(); });
</script>
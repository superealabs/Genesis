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

    :replace-options="replaceOptions"
    :show-replace-popup="showReplacePopup"
    :mouse-x="mouseX"
    :mouse-y="mouseY"
    @select-replace="handleReplaceSelection"
    @close-replace="cancelReplace"
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

  <FrameworkDetail
    v-if="detailFramework"
    :framework="detailFramework"
    @close="detailFramework = null"
  />
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useFrameworks } from '@genesis-labs/web-core/features/frameworks/composables/useFrameworks';
import { 
  FrameworkList, 
  FrameworkFilter, 
  FrameworkDetail 
} from '@genesis-labs/web-core/features/frameworks/components';
import GenesisCollectionLayout from '@genesis-labs/web-core/core/components/layouts/GenesisCollectionLayout.vue';
import BaseFormPopup from '@genesis-labs/web-core/core/components/layouts/Popup/BaseFormPopup.vue';
import type { SelectionOption } from '@genesis-labs/web-core/core/components/layouts/Popup/SimpleSelectionPopup.vue';
import type { Framework } from '@genesis-labs/shared-types';

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
const isFilterOpen = ref(false); //  État du popup de filtre

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
  
  if (result.action === 'replace-needed') {
    triggerReplace(framework, event);
  } else {
    emit('select', result);
  }
}

function triggerReplace(framework: Framework, event?: MouseEvent) {
  pendingFramework.value = framework;
  mouseX.value = event ? event.clientX : window.innerWidth / 2;
  mouseY.value = event ? event.clientY : window.innerHeight / 2;
  showReplacePopup.value = true;
  console.warn("déclenchement du remplacement")
}


defineExpose({
  triggerReplace
});

onMounted(() => { initialize(); });
</script>
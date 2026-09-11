<template>
  <GenesisCollectionLayout
    title="Frameworks"
    :model-value:searchValue="searchQuery"
    :model-value:displayMode="displayMode"
    :mode="compareMode"
    searchPlaceholder="Rechercher par nom, core, type..."
    :showBackButton="showBackButton"
    :displayMode="displayMode"
    @back="$emit('back')"
    @openFilter="$emit('openFilter')"
    @update:mode="handleModeChange"
    @update:searchValue="setSearch"
    @update:displayMode="toggleDisplayMode"
    @update:filters="setFilters"
  >
    <template #header-actions>
      <slot name="header-actions"></slot>
    </template>

    <template #filter>
      <FrameworkFilter :model-value:filters="filters" @update:filters="setFilters" />
    </template>

    <FrameworkList
      :frameworks="frameworks"
      :selectedId="selectedId"
      :display="displayMode"
      :frameworkSlots="frameworkSlots"
      @select="handleSelectWrapper"
      @info="handleInfo"
    />
  </GenesisCollectionLayout>

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

// ✅ 1. Le composant gère son propre état via son composable
import { useFrameworks } from '../composables/useFrameworks';
import FrameworkList from '../components/FrameworkList.vue';
import FrameworkFilter from '../components/FrameworkFilter.vue';
import FrameworkDetail from '../components/FrameworkDetail.vue';
import GenesisCollectionLayout from '@/core/components/layouts/GenesisCollectionLayout.vue';
import SimpleSelectionPopup from '@/core/components/layouts/Popup/SimpleSelectionPopup.vue';
import type { SelectionOption } from '@/core/components/layouts/Popup/SimpleSelectionPopup.vue';
import type { Framework } from '../types/framework.types';

// ✅ 2. On ne demande que showBackButton en prop
const props = withDefaults(defineProps<{
  showBackButton?: boolean;
}>(), {
  showBackButton: true
});

// ✅ 3. On émet 'select' pour que le GeneratorStepper puisse avancer à l'étape suivante
const emit = defineEmits<{
  'back': [];
  'openFilter': [];
  'select': [result: { action: string; framework: Framework; event?: MouseEvent }];
}>();

// ✅ 4. Récupération de l'état et des actions du composable de la feature
const {
  frameworks, selectedId, displayMode, compareMode, frameworkSlots, filters, searchQuery,
  setSearch, setFilters, toggleDisplayMode, handleModeChange,
  handleSelect, handleReplace, compare, initialize
} = useFrameworks();

// ═══ ÉTAT LOCAL UI ═══
const detailFramework = ref<Framework | null>(null);
const showReplacePopup = ref(false);
const pendingFramework = ref<Framework | null>(null);
const mouseX = ref<number | null>(null);
const mouseY = ref<number | null>(null);

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

// ═══ HANDLERS UI ═══
function handleInfo(framework: Framework) {
  detailFramework.value = framework;
}

function handleReplaceSelection(slotId: string | number) {
  if (pendingFramework.value) {
    handleReplace(slotId, pendingFramework.value);
  }
  cancelReplace();
}

function cancelReplace() {
  showReplacePopup.value = false;
  pendingFramework.value = null;
  mouseX.value = null;
  mouseY.value = null;
}

// 5. Wrapper pour émettre l'événement vers le Generator après la logique interne
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

// ═══ LIFECYCLE ═══
onMounted(() => {
  initialize();
});
</script>
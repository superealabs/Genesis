<template>
  <GenesisCollectionLayout
    title="Frameworks"
    :model-value:searchValue="searchQuery"
    :model-value:displayMode="displayMode"
    :mode="compareMode"
    searchPlaceholder="Rechercher par nom, core, type..."
    :showBackButton="showBackButton"
    @back="$emit('back')"
    @openFilter="$emit('openFilter')"
    @update:mode="$emit('update:mode', $event)"
    @update:searchValue="$emit('update:searchValue', $event)"
    @update:displayMode="$emit('update:displayMode', $event)"
  >
    <!-- ✅ SLOT : Permet au VSC/Web d'ajouter des actions dans le header (ex: bouton Refresh) -->
    <template #header-actions>
      <slot name="header-actions"></slot>
    </template>

    <template #filter>
      <FrameworkFilter :model-value:filters="filters" @update:filters="$emit('update:filters', $event)" />
    </template>

    <FrameworkList
      :frameworks="frameworks"
      :selectedId="selectedId"
      :display="displayMode"
      :frameworkSlots="frameworkSlots"
      @select="$emit('select', $event)"
      @info="handleInfo"
    />
  </GenesisCollectionLayout>

  <!-- Popup de remplacement (Logique UI de la feature, reste dans le Core) -->
  <SimpleSelectionPopup
    :show="showReplacePopup"
    :mouseX="mouseX"
    :mouseY="mouseY"
    :options="replaceOptions"
    position="bottom-right"
    @select="handleReplaceSelection"
    @close="cancelReplace"
  />

  <!-- Panneau de détails -->
  <FrameworkDetail
    v-if="detailFramework"
    :framework="detailFramework"
    @close="detailFramework = null"
  />
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import FrameworkList from '../components/FrameworkList.vue';
import FrameworkFilter from '../components/FrameworkFilter.vue';
import FrameworkDetail from '../components/FrameworkDetail.vue';
import GenesisCollectionLayout from '@/core/components/layouts/GenesisCollectionLayout.vue';
import SimpleSelectionPopup from '@/core/components/layouts/Popup/SimpleSelectionPopup.vue';
import type { SelectionOption } from '@/core/components/layouts/Popup/SimpleSelectionPopup.vue';
import type { Framework, FrameworkFilters } from '../types/framework.types';

// ═══ PROPS (Données venant du composable) ═══
const props = withDefaults(defineProps<{
  frameworks: Framework[];
  selectedId: number | undefined;
  displayMode: 'grid' | 'list';
  compareMode: 'selection' | 'compare';
  frameworkSlots: Map<number, string>;
  filters: FrameworkFilters;
  searchQuery: string;
  showBackButton?: boolean;
  compare: any; // Type à affiner si besoin, contient .slots.value
}>(), {
  showBackButton: true
});

// ═══ EMITS (Actions vers le composable ou le parent) ═══
const emit = defineEmits<{
  'back': [];
  'select': [framework: Framework, event?: MouseEvent];
  'replace': [slotId: string | number, framework: Framework];
  'update:searchValue': [query: string];
  'update:displayMode': [mode: 'grid' | 'list'];
  'update:mode': [mode: 'selection' | 'compare'];
  'update:filters': [filters: FrameworkFilters];
  'openFilter': [];
}>();

// ═══ ÉTAT LOCAL UI (Spécifique à l'affichage de cette feature, pas au métier) ═══
const detailFramework = ref<Framework | null>(null);
const showReplacePopup = ref(false);
const pendingFramework = ref<Framework | null>(null);
const mouseX = ref<number | null>(null);
const mouseY = ref<number | null>(null);

const replaceOptions = computed<SelectionOption[]>(() => {
  if (!props.compare?.slots?.value) return [];
  return Object.entries(props.compare.slots.value)
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
    emit('replace', slotId, pendingFramework.value);
  }
  cancelReplace();
}

function cancelReplace() {
  showReplacePopup.value = false;
  pendingFramework.value = null;
  mouseX.value = null;
  mouseY.value = null;
}

// ═══ EXPOSITION POUR LE WRAPPER VSC ═══
// Le wrapper VSC a besoin de déclencher l'ouverture du popup de remplacement
// Nous exposons une fonction que le VSC peut appeler via une ref ou un événement
defineExpose({
  triggerReplace: (framework: Framework, event?: MouseEvent) => {
    pendingFramework.value = framework;
    mouseX.value = event ? event.clientX : window.innerWidth / 2;
    mouseY.value = event ? event.clientY : window.innerHeight / 2;
    showReplacePopup.value = true;
  }
});
</script>
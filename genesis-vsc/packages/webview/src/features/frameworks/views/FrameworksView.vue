<template>
  <!-- ✅ On utilise la Vue Core en lui passant toutes les données et actions -->
  <CoreFrameworksView
    ref="coreViewRef"
    :frameworks="frameworks"
    :selectedId="selectedId"
    :displayMode="displayMode"
    :compareMode="compareMode"
    :frameworkSlots="frameworkSlots"
    :filters="filters"
    :searchQuery="searchQuery"
    :showBackButton="showBackButton"
    :compare="compare"
    @back="$emit('back')"
    @select="handleSelectWrapper"
    @replace="handleReplace"
    @update:searchValue="setSearch"
    @update:displayMode="toggleDisplayMode"
    @update:mode="handleModeChange"
    @update:filters="setFilters"
  >
    <!-- ✅ SLOT : Exemple d'ajout d'élément spécifique à VSC (optionnel) -->
    <template #header-actions>
      <!-- <GenesisButtonIcon @click="refreshVscSpecific" icon="refresh">Actualiser</GenesisButtonIcon> -->
    </template>
  </CoreFrameworksView>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
// ✅ 1. Import de la Vue Core
import CoreFrameworksView from '@genesis-labs/core/features/frameworks/views/FrameworksView.vue';

// ✅ 2. Import du Composable VSC (qui contient déjà l'injection du service)
import { useFrameworksVscode } from '../composables/useFrameworksVscode';
import type { Framework } from '@genesis-labs/core/features/frameworks/types/framework.types';

const props = withDefaults(defineProps<{
  showBackButton?: boolean;
}>(), {
  showBackButton: true
});

const emit = defineEmits<{
  'back': [];
  'select': [framework: Framework];
}>();

// ✅ 3. Récupération de l'état et des actions via le composable VSC
const {
  frameworks, selectedId, displayMode, compareMode, frameworkSlots, filters, searchQuery,
  setSearch, setFilters, toggleDisplayMode, handleModeChange,
  handleSelect, handleReplace, compare, initialize
} = useFrameworksVscode();

// Ref pour accéder aux méthodes exposées par la Vue Core (defineExpose)
const coreViewRef = ref<InstanceType<typeof CoreFrameworksView> | null>(null);

// ═══ HANDLERS VSC ═══
function handleSelectWrapper(framework: Framework, event?: MouseEvent) {
  const result = handleSelect(framework, event);
  
  if (result.action === 'replace-needed') {
    // ✅ On délègue l'ouverture du popup à la Vue Core via defineExpose
    coreViewRef.value?.triggerReplace(result.framework, result.event);
  } else {
    emit('select', framework);
  }
}

// ═══ LIFECYCLE ═══
onMounted(() => {
  initialize();
});
</script>
<template>
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
    <template #header-actions>
      <!-- Slot spécifique VSC -->
    </template>
  </CoreFrameworksView>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';

// 🚨 LOG 1 : Prouve que le fichier est bien chargé et exécuté
console.log('🚨 [VUE VSC] 1. FrameworksView.vue : Le script setup commence...');

import { 
  FrameworksView as CoreFrameworksView, 
  type Framework 
} from '@genesis-labs/core/features/frameworks/manifest';

import { useFrameworksVscode } from '../composables/useFrameworksVscode';

const props = withDefaults(defineProps<{
  showBackButton?: boolean;
}>(), {
  showBackButton: true
});

const emit = defineEmits<{
  'back': [];
  'select': [framework: Framework];
}>();

// Récupération de l'état et des actions
const composableResult = useFrameworksVscode();

// 🚨 LOG 2 : Vérifie que le composable retourne bien quelque chose, et surtout que 'initialize' est une fonction
console.log('🚨 [VUE VSC] 2. Résultat du composable reçu. initialize est une fonction ?', typeof composableResult.initialize === 'function');

const {
  frameworks, selectedId, displayMode, compareMode, frameworkSlots, filters, searchQuery,
  setSearch, setFilters, toggleDisplayMode, handleModeChange,
  handleSelect, handleReplace, compare, initialize
} = composableResult;

const coreViewRef = ref<InstanceType<typeof CoreFrameworksView> | null>(null);

async function handleSelectWrapper(framework: Framework, event?: MouseEvent) {
  const result = await handleSelect(framework, event);
  
  if (result.action === 'replace-needed') {
    coreViewRef.value?.triggerReplace(result.framework, result.event);
  } else {
    emit('select', framework);
  }
}

// 🚨 LOG 3 : Prouve que le cycle de vie onMounted se déclenche
onMounted(() => {
  console.log('🚨 [VUE VSC] 3. onMounted déclenché !');
  
  if (typeof initialize === 'function') {
    console.log('🚨 [VUE VSC] 4. Appel de initialize() en cours...');
    initialize();
  } else {
    console.error('❌ [VUE VSC] ERREUR CRITIQUE : initialize n\'est pas une fonction !', initialize);
  }
});
</script>
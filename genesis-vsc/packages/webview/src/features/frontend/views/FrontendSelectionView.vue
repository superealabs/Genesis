<template>
  <!-- ✅ On utilise la Vue Core en lui passant toutes les données et en écoutant ses événements -->
  <CoreFrontendSelectionView
    :frontends="filteredFrontends"
    :selectedId="selectedId"
    v-model:searchValue="searchQuery"
    v-model:displayMode="displayMode"
    :showBackButton="showBackButton"
    @back="$emit('back')"
    @select="handleSelectWrapper"
    @openFilter="openFilter"
  />
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';

// ✅ 1. Import de la Vue Core
import CoreFrontendSelectionView from '@genesis-labs/core/features/frontend/views/FrontEndSelectionView.vue';

// ✅ 2. Import du Composable VSC (qui contient déjà l'injection du service)
import { useFrontendVscode } from '../composables/useFrontendVscode';
import type { FrontendFramework } from '@genesis-labs/core/features/frontend/types/frontend.types';

const props = withDefaults(defineProps<{
  showBackButton?: boolean;
}>(), {
  showBackButton: true
});

const emit = defineEmits<{
  'back': [];
  'select': [framework: FrontendFramework];
}>();

// ═══ Injection du composable VSC ═══
const { 
  availableFrameworks, 
  selectedFramework,
  initialize, 
  selectFramework 
} = useFrontendVscode();

// ═══ État local UI (Spécifique à la gestion de cette vue dans VSC) ═══
const searchQuery = ref('');
const displayMode = ref<'grid' | 'list'>('grid');

// ═══ Computed ═══
const selectedId = computed(() => selectedFramework.value?.id);

const filteredFrontends = computed(() => {
  if (!searchQuery.value.trim()) {
    return availableFrameworks.value;
  }
  const q = searchQuery.value.toLowerCase();
  return availableFrameworks.value.filter(fw =>
    fw.name.toLowerCase().includes(q) ||
    fw.coreFramework.toLowerCase().includes(q)
  );
});

// ═══ Handlers ═══
function handleSelectWrapper(framework: FrontendFramework, event?: MouseEvent) {
  // 1. Met à jour le store via le composable
  selectFramework(framework);
  
  if (event) {
    console.log("hi"); // Gardé de ton code original, à nettoyer si inutile
  }
  
  // 2. Émet vers le parent (GeneratorStepper) pour sauvegarde dans le flux global
  emit('select', framework);
}

function openFilter() {
  console.log('Ouverture des filtres frontend (à implémenter)');
}

// ═══ Lifecycle ═══
onMounted(() => {
  initialize();
});
</script>
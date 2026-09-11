<template>
  <GenesisCollectionLayout
    title="Framework Frontend"
    :searchValue="searchQuery"
    :displayMode="displayMode"
    searchPlaceholder="Rechercher par nom (ex: React, Vue)..."
    :showBackButton="showBackButton"
    @back="$emit('back')"
    @openFilter="$emit('openFilter')"
    @update:searchValue="setSearch"
    @update:displayMode="toggleDisplayMode"
  >
    <!-- ✅ SLOT : Permet au VSC/Web d'injecter ses propres filtres si besoin -->
    <template #filter>
      <slot name="filter">
        <div class="p-4 text-sm text-text-muted">
          Filtres avancés frontend (à venir)
        </div>
      </slot>
    </template>

    <FrontendList
      :frontends="availableFrameworks"
      :selectedId="selectedFramework?.id"
      :display="displayMode"
      @select="handleSelectWrapper"
      @info="(fw) => $emit('info', fw)"
    />
  </GenesisCollectionLayout>

  <!-- Panneau de détails (réservé pour plus tard) -->
  <!-- 
  <FrontendDetail
    v-if="detailFramework"
    :framework="detailFramework"
    @close="detailFramework = null"
  /> 
  -->
</template>

<script setup lang="ts">
import { onMounted } from 'vue';

// ✅ 1. Le composant gère son propre état via le composable du Core
import { useFrontend } from '../composables/useFrontend';
import FrontendList from '../components/FrontendList.vue';
import GenesisCollectionLayout from '@/core/components/layouts/GenesisCollectionLayout.vue';
import type { FrontendFramework } from '../types/frontend.types';

// ✅ 2. On ne demande que showBackButton en prop
const props = withDefaults(defineProps<{
  showBackButton?: boolean;
}>(), {
  showBackButton: true
});

// ✅ 3. On émet 'select' pour que le GeneratorStepper puisse avancer à l'étape suivante
const emit = defineEmits<{
  'back': [];
  'select': [framework: FrontendFramework, event?: MouseEvent];
  'info': [framework: FrontendFramework];
  'openFilter': [];
}>();

// ✅ 4. Récupération de l'état et des actions du composable
const {
  availableFrameworks,
  selectedFramework,
  displayMode,
  searchQuery,
  setSearch,
  toggleDisplayMode,
  selectFramework,
  initialize
} = useFrontend();

// ═══ HANDLERS UI ═══
function handleSelectWrapper(framework: FrontendFramework, event?: MouseEvent) {
  // 1. Met à jour l'état interne du core (store)
  selectFramework(framework);
  
  // 2. Notifie le parent (GeneratorStepper) pour qu'il passe à l'étape suivante
  emit('select', framework, event);
}

// ═══ LIFECYCLE ═══
onMounted(() => {
  initialize();
});
</script>
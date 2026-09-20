<template>
  <GenesisCollectionLayout
    title="Framework Frontend"
    v-model:searchValue="searchQuery"
    v-model:displayMode="displayMode"
    :mode="compareMode"
    searchPlaceholder="Rechercher par nom (ex: React, Vue)..."
    :showBackButton="showBackButton"
    :showFilter="false"
    :showSort="false"
    :showCarousel="true"
    @back="$emit('back')"
    @update:mode="handleModeChange"
    
    :replace-options="replaceOptions"
    :show-replace-popup="showReplacePopup"
    :mouse-x="mouseX"
    :mouse-y="mouseY"
    @select-replace="handleReplaceSelection"
    @close-replace="cancelReplace"
  >
    <!--  SLOT : Permet au VSC/Web d'injecter ses propres filtres si besoin -->
    <template #filter>
      <slot name="filter">
        <div class="p-4 text-sm text-text-muted">
          Filtres avancés frontend (à venir)
        </div>
      </slot>
    </template>

    <FrontendList
      :frontends="availableFrontendFrameworks"
      :selectedId="selectedId"
      :display="displayMode"
      :frameworkSlots="frontendFrameworkSlots"
      @select="handleSelectWrapper"
      @info="handleInfo" 
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
import { computed, onMounted } from 'vue';

//  1. Le composant gère son propre état via le composable du Core
import { useFrontend } from '@genesis-labs/web-core/features/frontend/composables/useFrontend';
import FrontendList from '@genesis-labs/web-core/features/frontend/components/FrontendList.vue';
import GenesisCollectionLayout from '@genesis-labs/web-core/core/components/layouts/GenesisCollectionLayout.vue';
import type { FrontendFramework } from '@genesis-labs/shared-types';
import { SelectionOption } from '@genesis-labs/web-core/core/components/layouts/Popup/SimpleSelectionPopup.vue';

//  2. On ne demande que showBackButton en prop
withDefaults(defineProps<{
  showBackButton?: boolean;
}>(), {
  showBackButton: true
});

//  3. On émet 'select' pour que le GeneratorStepper puisse avancer à l'étape suivante
//  3. On émet 'select' avec le résultat complet (action, framework, event)
const emit = defineEmits<{
  'back': [];
  'select': [result: { action: string; framework: FrontendFramework; event?: MouseEvent }];
  // 'info': [framework: FrontendFramework];
  // 'openFilter': [];
}>();

//  4. Récupération de l'état et des actions du composable
const {
  availableFrontendFrameworks,
  selectedId,
  frontendFrameworkSlots,
  displayMode,
  searchQuery,
  compareMode,
  compare,
  initialize,
  // setSearch,
  // toggleDisplayMode,
  handleSelect,
  handleReplace,
  handleModeChange,
  showReplacePopup,
  pendingFramework,
  mouseX,
  mouseY,
  cancelReplace,
  // triggerReplace
} = useFrontend();

const replaceOptions = computed<SelectionOption[]>(() => {
  if (!compare?.slots?.value) return [];
  return Object.entries(compare.slots.value)
    .filter(([, fw]) => fw !== null)
    .map(([slot, fw]) => ({
      id: slot,
      label: `Slot ${slot}`,
      description: (fw as FrontendFramework).name
    }));
});

function handleInfo(framework: FrontendFramework) {
  // Pour l'instant on ne fait rien, ou on prépare le terrain pour le panneau de détail
  console.log("Détails demandés pour :", framework.name);
  // detailFramework.value = framework; (à décommenter quand le panneau sera actif)
}

// 3. HANDLERS SIMPLIFIÉS
async function handleSelectWrapper(framework: FrontendFramework, event?: MouseEvent) {
  const result = await handleSelect(framework, event);
  emit('select', result);
}

function handleReplaceSelection(slotId: string | number) {
  if (pendingFramework.value) handleReplace(slotId, pendingFramework.value);
  cancelReplace();
}

onMounted(() => {
  initialize();
});
</script>
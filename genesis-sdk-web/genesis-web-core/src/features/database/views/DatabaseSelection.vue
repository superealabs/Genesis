<template>
  <GenesisCollectionLayout
    title="Base de Données"
    v-model:displayMode="displayMode"
    :mode="compareMode" 
    searchPlaceholder="Rechercher un moteur..." 
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
    <!-- DÉLÉGATION AVEC LES NOUVELLES PROPS DE SÉLECTION/COMPARAISON -->
    <DatabaseList
      :engines="engines"
      :selectedId="selectedId"
      :display="displayMode"
      :databaseSlots="databaseSlots"
      @select="handleSelectWrapper"
    />
  </GenesisCollectionLayout>
</template>

<script setup lang="ts">
import { onMounted, computed } from 'vue';
import { useDatabase } from '@genesis-labs/web-core/features/database/composables/useDatabase';
import DatabaseList from '@genesis-labs/web-core/features/database/components/DatabaseList.vue';
import GenesisCollectionLayout from '@genesis-labs/web-core/core/components/layouts/GenesisCollectionLayout.vue';
import type { DatabaseEngineDto } from '@genesis-labs/shared-types';
import { SelectionOption } from '@genesis-labs/web-core/core/components/layouts/Popup/SimpleSelectionPopup.vue';

// ═══ PROPS & EMITS ═══
withDefaults(defineProps<{
    showBackButton?: boolean;
}>(), {
    showBackButton: true
});

const emit = defineEmits<{
    'back': [];
    'select': [result: { action: string; engine: DatabaseEngineDto; event?: MouseEvent }];
}>();

// ═══ COMPOSABLE ═══
const {
    engines,
    selectedId,
    databaseSlots,
    displayMode,
    compareMode, // RÉACTIVÉ pour le binding du layout
    hasEngines,
    fetchAvailableEngines,
    handleSelect,
    handleModeChange,
    handleReplace,
    compare, // Nécessaire pour le computed des options
    // États du popup
    showReplacePopup,
    pendingEngine,
    mouseX,
    mouseY,
    cancelReplace,
    // triggerReplace
} = useDatabase();

const replaceOptions = computed<SelectionOption[]>(() => {
    if (!compare?.slots?.value) return [];
    return Object.entries(compare.slots.value)
        .filter(([, engine]) => engine !== null)
        .map(([slot, engine]) => ({
            id: slot,
            label: `Slot ${slot}`,
            description: (engine as DatabaseEngineDto).name
        }));
});


// 3. SIMPLIFIER LE HANDLER (Le composable gère déjà l'affichage du popup)
async function handleSelectWrapper(engine: DatabaseEngineDto, event?: MouseEvent) {
    const result = await handleSelect(engine, event);
    emit('select', result);
}

// 4. GÉRER LA SÉLECTION DANS LE POPUP
function handleReplaceSelection(slotId: string | number) {
    if (pendingEngine.value) handleReplace(slotId, pendingEngine.value);
    cancelReplace();
}

// ═══ LIFECYCLE ═══
onMounted(() => {
    if (!hasEngines.value) {
        fetchAvailableEngines();
    }
});
</script>
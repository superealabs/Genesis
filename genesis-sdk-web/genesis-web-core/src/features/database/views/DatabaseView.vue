<template>
  <DatabaseLayout
    v-bind="layoutProps"
    @back="$emit('back')"
    @update:displayMode="setDisplayMode"
    @update:mode="handleModeChange"
    @select-replace="handleReplaceSelection"
    @close-replace="cancelReplace"
    @select="handleSelectWrapper"
  />
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue';
import { useDatabase } from '@genesis-labs/web-core/features/database/composables/useDatabase';
import DatabaseLayout, { type DatabaseLayoutProps } from '@genesis-labs/web-core/features/database/components/DatabaseLayout.vue';
import type { DatabaseEngineDto } from '@genesis-labs/shared-types';
import { DisplayMode } from '@genesis-labs/web-core/core/components/layouts/display/GenesisItem.types';

withDefaults(defineProps<{
    showBackButton?: boolean;
}>(), {
    showBackButton: true
});

const emit = defineEmits<{
    'back': [];
    'select': [result: { action: string; engine: DatabaseEngineDto; event?: MouseEvent }];
}>();

const {
    engines,
    selectedId,
    databaseSlots,
    displayMode,
    compareMode,
    hasEngines,
    fetchAvailableEngines,
    handleSelect,
    handleModeChange,
    handleReplace,
    compare,
    showReplacePopup,
    pendingEngine,
    mouseX,
    mouseY,
    cancelReplace,
    setDisplayMode
} = useDatabase();

const replaceOptions = computed(() => {
    if (!compare?.slots?.value) return [];
    return Object.entries(compare.slots.value)
        .filter(([, engine]) => engine !== null)
        .map(([slot, engine]) => ({
            id: slot,
            label: `Slot ${slot}`,
            description: (engine as DatabaseEngineDto).name
        }));
});

const layoutProps = computed<DatabaseLayoutProps>(() => ({
    engines: engines.value,
    selectedId: selectedId.value,
    databaseSlots: databaseSlots.value,
    displayMode: displayMode.value,
    compareMode: compareMode.value,
    replaceOptions: replaceOptions.value,
    showReplacePopup: showReplacePopup.value,
    mouseX: mouseX.value,
    mouseY: mouseY.value,
    isLoading: false // À adapter si useDatabase expose un isLoading
}));

async function handleSelectWrapper(engine: DatabaseEngineDto, event?: MouseEvent) {
    try {
        const result = await handleSelect(engine, event);
        emit('select', result);
    } catch (error) {
        console.error("❌ [DatabaseSelection] Erreur sur le handleSelect :", error);
    }
}

function handleReplaceSelection(slotId: string | number) {
    if (pendingEngine.value) handleReplace(slotId, pendingEngine.value);
    cancelReplace();
}

// Expose la fonction de chargement pour un usage externe si nécessaire
defineExpose({
    fetchAvailableEngines
});

onMounted(() => {
    if (!hasEngines.value) {
        fetchAvailableEngines();
    }
});
</script>
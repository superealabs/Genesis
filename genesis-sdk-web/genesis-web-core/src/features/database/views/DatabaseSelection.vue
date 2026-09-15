<template>
  <GenesisCollectionLayout
    title="Base de Données"
    v-model:displayMode="displayMode"
    searchPlaceholder="Rechercher un moteur..." 
    :showBackButton="showBackButton"
    :showFilter="false"
    :showSort="false"
    :showCarousel="true"
    @back="$emit('back')"
    @update:mode="handleModeChange"
  >
    <!-- ✅ DÉLÉGATION AVEC LES NOUVELLES PROPS DE SÉLECTION/COMPARAISON -->
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
import { onMounted } from 'vue';
import { useDatabase } from '@genesis-labs/web-core/features/database/composables/useDatabase';
import DatabaseList from '@genesis-labs/web-core/features/database/components/DatabaseList.vue';
import GenesisCollectionLayout from '@genesis-labs/web-core/core/components/layouts/GenesisCollectionLayout.vue';
import type { DatabaseEngineDto } from '@genesis-labs/shared-types';

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
    // compareMode,
    // isLoading,
    hasEngines,
    fetchAvailableEngines,
    handleSelect,
    handleModeChange
} = useDatabase();

// ═══ HANDLERS ═══
async function handleSelectWrapper(engine: DatabaseEngineDto, event?: MouseEvent) {
    const result = await handleSelect(engine, event);
    // On émet le résultat complet (avec l'action 'select' ou 'replace-needed') vers le parent
    emit('select', result);
}

// ═══ LIFECYCLE ═══
onMounted(() => {
    if (!hasEngines.value) {
        fetchAvailableEngines();
    }
});
</script>
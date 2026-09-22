<template>
  <GenesisCollectionLayout
    :title="title"
    :displayMode="displayMode"
    @update:displayMode="$emit('update:displayMode', $event)"
    :mode="compareMode"
    @update:mode="$emit('update:mode', $event)"
    :searchPlaceholder="searchPlaceholder"
    :showBackButton="showBackButton"
    :showFilter="false"
    :showSort="false"
    :showCarousel="true"
    @back="$emit('back')"
    :replace-options="replaceOptions"
    :show-replace-popup="showReplacePopup"
    :mouse-x="mouseX"
    :mouse-y="mouseY"
    @select-replace="$emit('select-replace', $event)"
    @close-replace="$emit('close-replace')"
    :is-loading="isLoading"
  >
    <DatabaseList
      :engines="engines"
      :selectedId="selectedId"
      :display="displayMode"
      :databaseSlots="databaseSlots"
      @select="(engine, ev) => $emit('select', engine, ev)"
    />
  </GenesisCollectionLayout>
</template>

<script setup lang="ts">
import DatabaseList from './DatabaseList.vue'; 
import GenesisCollectionLayout from '@genesis-labs/web-core/core/components/layouts/GenesisCollectionLayout.vue';
import type { SelectionOption } from '@genesis-labs/web-core/core/components/layouts/Popup/SimpleSelectionPopup.vue';
import type { DatabaseEngineDto } from '@genesis-labs/shared-types';
import type { DisplayMode } from '@genesis-labs/web-core/core/components/layouts/display/GenesisItem.types';

// ✅ Export du type pour réutilisation par les composants parents
export interface DatabaseLayoutProps {
  title?: string;
  displayMode: DisplayMode;
  compareMode: 'selection' | 'compare';
  searchPlaceholder?: string;
  showBackButton?: boolean;
  engines: DatabaseEngineDto[];
  selectedId?: number;
  databaseSlots: Map<number, string>;
  replaceOptions: SelectionOption[];
  showReplacePopup: boolean;
  mouseX: number | null;
  mouseY: number | null;
  isLoading: boolean;
}

withDefaults(defineProps<DatabaseLayoutProps>(), {
  title: 'Base de Données',
  searchPlaceholder: 'Rechercher un moteur...',
  showBackButton: true,
  isLoading: false
});

defineEmits<{
  'back': [];
  'update:displayMode': [mode: DisplayMode];
  'update:mode': [mode: 'selection' | 'compare'];
  'select-replace': [slotId: string | number];
  'close-replace': [];
  'select': [engine: DatabaseEngineDto, event?: MouseEvent];
}>();
</script>
<template>
  <GenesisCollectionLayout
    :title="title"
    :searchValue="searchQuery"
    :displayMode="displayMode"
    @update:searchValue="$emit('update:searchValue', $event)"
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
    <FrontendList
      :frontends="frontends"
      :selectedId="selectedId"
      :display="displayMode"
      :framework-slots="frontendFrameworkSlots"
      @select="(fw, ev) => $emit('select', fw, ev)"
      @info="(fw) => $emit('info', fw)"
    />
  </GenesisCollectionLayout>
</template>

<script setup lang="ts">
import FrontendList from './FrontendList.vue'; 
import GenesisCollectionLayout from '@genesis-labs/web-core/core/components/layouts/GenesisCollectionLayout.vue';
import type { SelectionOption } from '@genesis-labs/web-core/core/components/layouts/Popup/SimpleSelectionPopup.vue';
import type { FrontendFramework } from '@genesis-labs/shared-types';
import type { DisplayMode } from '@genesis-labs/web-core/core/components/layouts/display/GenesisItem.types';

// ✅ Export du type pour réutilisation par les composants parents
export interface FrontendLayoutProps {
  title?: string;
  searchQuery: string;
  displayMode: DisplayMode;
  compareMode: 'selection' | 'compare';
  searchPlaceholder?: string;
  showBackButton?: boolean;
  frontends: FrontendFramework[];
  selectedId?: number;
  frontendFrameworkSlots: Map<number, string>;
  replaceOptions: SelectionOption[];
  showReplacePopup: boolean;
  mouseX: number | null;
  mouseY: number | null;
  isLoading: boolean;
}

withDefaults(defineProps<FrontendLayoutProps>(), {
  title: 'Framework Frontend',
  searchPlaceholder: 'Rechercher par nom (ex: React, Vue)...',
  showBackButton: true,
  isLoading: false
});

defineEmits<{
  'back': [];
  'update:searchValue': [value: string];
  'update:displayMode': [mode: DisplayMode];
  'update:mode': [mode: 'selection' | 'compare'];
  'select-replace': [slotId: string | number];
  'close-replace': [];
  'select': [framework: FrontendFramework, event?: MouseEvent];
  'info': [framework: FrontendFramework];
}>();
</script>
<template>
  <GenesisCollectionLayout
    :title="title"
    :searchValue="searchQuery"
    :displayMode="displayMode"
    @update:searchValue="$emit('update:searchValue', $event)"
    @update:displayMode="$emit('update:displayMode', $event)"
    @update:mode="$emit('update:mode', $event)"
    :mode="compareMode"
    :searchPlaceholder="searchPlaceholder"
    :showBackButton="showBackButton"
    @back="$emit('back')"
    @openFilter="$emit('openFilter')"
    :replace-options="replaceOptions"
    :show-replace-popup="showReplacePopup"
    :mouse-x="mouseX"
    :mouse-y="mouseY"
    @select-replace="$emit('select-replace', $event)"
    @close-replace="$emit('close-replace')"
    :is-loading="isLoading"
  >
    <FrameworkList
      :frameworks="frameworks"
      :selectedId="selectedId"
      :display="displayMode"
      :frameworkSlots="frameworkSlots"
      @select="(fw, ev) => $emit('select', fw, ev)"
      @info="(fw) => $emit('info', fw)"
    />
  </GenesisCollectionLayout>

  <BaseFormPopup
    v-if="isFilterOpen"
    title="Filtres des Frameworks"
    :size="'lg'"
    @close="$emit('closeFilter')"
  >
    <FrameworkFilter 
      :filters="filters"
      @update:filters="$emit('update:filters', $event)"
      @close="$emit('closeFilter')" 
    />
  </BaseFormPopup>

  <FrameworkDetail
    v-if="detailFramework"
    :framework="detailFramework"
    @close="$emit('closeDetail')"
  />
</template>

<script setup lang="ts">
import FrameworkList from './FrameworkList.vue'; 
import FrameworkFilter from './FrameworkFilter.vue'; 
import FrameworkDetail from './FrameworkDetail.vue'; 
import GenesisCollectionLayout from '@genesis-labs/web-core/core/components/layouts/GenesisCollectionLayout.vue';
import BaseFormPopup from '@genesis-labs/web-core/core/components/layouts/Popup/BaseFormPopup.vue';
import type { SelectionOption } from '@genesis-labs/web-core/core/components/layouts/Popup/SimpleSelectionPopup.vue';
import type { Framework } from '@genesis-labs/shared-types';
// ✅ CORRECTION : Suppression de l'extension .ts dans l'import
import type { DisplayMode } from '@genesis-labs/web-core/core/components/layouts/display/GenesisItem.types';

// ✅ NOUVEAU : Export du type pour réutilisation par les composants parents
export interface FrameworkLayoutProps {
  title?: string;
  searchQuery: string;
  displayMode: DisplayMode;
  compareMode: 'selection' | 'compare';
  searchPlaceholder?: string;
  showBackButton?: boolean;
  frameworks: Framework[];
  selectedId?: number;
  frameworkSlots: Map<number, string>;
  replaceOptions: SelectionOption[];
  showReplacePopup: boolean;
  mouseX: number | null;
  mouseY: number | null;
  filters?: any; 
  detailFramework: Framework | null;
  isFilterOpen: boolean;
  pendingFramework: Framework | null;
  isLoading: boolean;
}

withDefaults(defineProps<FrameworkLayoutProps>(), {
  title: 'Frameworks',
  searchPlaceholder: 'Rechercher par nom, core, type...',
  showBackButton: true,
  isLoading: false
});

defineEmits<{
  'back': [];
  'openFilter': [];
  'closeFilter': [];
  'closeDetail': [];
  'select-replace': [slotId: string | number];
  'close-replace': [];
  'select': [framework: Framework, event?: MouseEvent];
  'info': [framework: Framework];
  'update:searchValue': [value: string];
  'update:displayMode': [mode: DisplayMode];
  'update:mode': [mode: 'selection' | 'compare'];
  'update:filters': [filters: any];
}>();
</script>
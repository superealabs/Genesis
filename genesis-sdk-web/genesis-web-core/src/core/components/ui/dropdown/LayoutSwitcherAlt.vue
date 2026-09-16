<template>
  <GenesisDropdown
    :align="align"
    :hide-chevron="hideChevron"
    dropdown-size="md"
    :trigger-size="'lg'"
    :close-on-select="true"
    :open-at-hover="true"
  >
    <template #triggerIcon>
      <component :is="currentIcon" :size="24" aria-hidden="true" />
    </template>

    <div class="p-1">
      <!-- MODE GRID -->
      <MenuItem v-slot="{ active }" as="template">
        <GenesisButton
          :class="[active ? 'active' : '']"
          variant="tertiary"
          :fill-width="true"
          size="lg"
          @click="selectView('grid')"
        >
          <template #leftIcon>
            <IconGrid aria-hidden="true" />
          </template>
          Grid
        </GenesisButton>
      </MenuItem>

      <!-- ✅ MODE TABLE (anciennement List) -->
      <MenuItem v-slot="{ active }" as="template">
        <GenesisButton
          :class="[active ? 'active' : '']"
          variant="tertiary"
          :fill-width="true"
          size="lg"
          @click="selectView('table')"
        >
          <template #leftIcon>
            <!-- Utilise l'icône de tableau de ton projet (ex: IconTable, IconTableAlt, ou IconListUl) -->
            <IconTable aria-hidden="true" /> 
          </template>
          Table
        </GenesisButton>
      </MenuItem>

      <!-- ✅ MODE LIST (anciennement Line) -->
      <MenuItem v-slot="{ active }" as="template">
        <GenesisButton
          :class="[active ? 'active' : '']"
          variant="tertiary"
          :fill-width="true"
          size="lg"
          @click="selectView('list')"
        >
          <template #leftIcon>
            <IconListUl aria-hidden="true" />
          </template>
          List
        </GenesisButton>
      </MenuItem>
    </div>
  </GenesisDropdown>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { MenuItem } from '@headlessui/vue'; 

import GenesisDropdown from '@genesis-labs/web-core/core/components/ui/dropdown/GenesisDropdown.vue';
import GenesisButton from '@genesis-labs/web-core/core/components/ui/actions/GenesisButton.vue';
import IconGrid from '@genesis-labs/web-core/core/components/ui/icons/IconGrid.vue';
import IconTable from '@genesis-labs/web-core/core/components/ui/icons/IconTable.vue'; // ✅ À adapter si le nom est différent
import IconListUl from '@genesis-labs/web-core/core/components/ui/icons/IconListUl.vue';

const props = withDefaults(defineProps<{
    modelValue: 'grid' | 'table' | 'list'; // ✅ Mis à jour
    align?: 'left' | 'right';
    hideChevron?: boolean;
}>(), {
    align: 'right',
    hideChevron: false
});

const emit = defineEmits<{
    'update:modelValue': [value: 'grid' | 'table' | 'list']; // ✅ Mis à jour
}>();

const currentIcon = computed(() => {
    if (props.modelValue === 'grid') return IconGrid;
    if (props.modelValue === 'table') return IconTable;
    return IconListUl; 
});

const selectView = (view: 'grid' | 'table' | 'list') => { // ✅ Mis à jour
    emit('update:modelValue', view);
};
</script>
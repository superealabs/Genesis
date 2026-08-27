<template>
  <!-- 1. Plus besoin de div wrapper, de Menu, de transition manuelle -->
  <GenesisDropdown
    :align="align"
    :hide-chevron="hideChevron"
    dropdown-size="md"
    :trigger-size="'lg'"
    :close-on-select="true"
    :open-at-hover="true"
  >
    <!-- 2. Le trigger est géré proprement via un slot nommé -->
    <template #triggerIcon>
      <component :is="currentIcon" :size="24" aria-hidden="true" />
    </template>

    <div class="p-1">
      <!-- 3. Le contenu du menu est injecté directement. On garde MenuItem pour l'état 'active' et l'accessibilité clavier -->
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


    <div class="p-1">
      <!-- 3. Le contenu du menu est injecté directement. On garde MenuItem pour l'état 'active' et l'accessibilité clavier -->
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
// 4. On n'importe PLUS que MenuItem de Headless UI (pour le slot { active })
import { MenuItem } from '@headlessui/vue'; 

// 5. On importe notre super-composant wrapper
import GenesisDropdown from '@/core/components/ui/dropdown/GenesisDropdown.vue';
import GenesisButton from '@/core/components/ui/actions/GenesisButton.vue';
import IconGrid from '@/core/components/ui/icons/IconGrid.vue';
import IconListUl from '@/core/components/ui/icons/IconListUl.vue';

const props = withDefaults(defineProps<{
    modelValue: 'grid' | 'list';
    align?: 'left' | 'right';
    hideChevron?: boolean;
}>(), {
    align: 'right',
    hideChevron: false
});

const emit = defineEmits<{
    'update:modelValue': [value: 'grid' | 'list'];
}>();

const currentIcon = computed(() => {
    return props.modelValue === 'grid' ? IconGrid : IconListUl;
});

const selectView = (view: 'grid' | 'list') => {
    emit('update:modelValue', view);
};
</script>
<template>
  <TabGroup
    :selectedIndex="selectedIndex"
    @change="handleChange"
    as="div"
    class="inline-flex flex-col gap-4"
  >
    <!-- Conteneur du Segmented Control -->
    <TabList class="inline-flex rounded-md p-1 gap-1 bg-bg-light border border-bg-light" role="group">
      <Tab
        v-for="option in options"
        :key="String(option.value)"
        :disabled="disabled || option.disabled"
        v-slot="{ selected }"
        as="template"
      >
        <GenesisButton
          :variant="selected ? 'primary' : 'tertiary'"
          :size="size"
          :disabled="disabled || option.disabled"
        >
          <template v-if="option.icon" #leftIcon>
            <component :is="option.icon" />
          </template>
          
          <span v-if="option.label">{{ option.label }}</span>
        </GenesisButton>
      </Tab>
    </TabList>

    <!-- Panneaux de contenu (optionnel) -->
    <TabPanels v-if="$slots.panels" class="w-full">
      <TabPanel
        v-for="option in options"
        :key="String(option.value)"
        v-slot="{ selected }"
        as="template"
      >
        <div v-show="selected">
          <slot name="panels" :option="option" :selected="selected" />
        </div>
      </TabPanel>
    </TabPanels>
  </TabGroup>
</template>

<script setup lang="ts">
import { computed, type Component } from 'vue';
import { TabGroup, TabList, Tab, TabPanels, TabPanel } from '@headlessui/vue';
// 👇 Import du composant source de vérité
import GenesisButton from '@/core/components/ui/actions/GenesisButton.vue';

interface SegmentedOption {
  label?: string;
  value: string | number;
  icon?: Component;
  disabled?: boolean;
}

interface Props {
  modelValue: string | number;
  options: SegmentedOption[];
  // 👇 On reprend exactement les mêmes tailles que GenesisButton
  size?: 'xs' | 'sm' | 'md' | 'lg' | 'xl' | '2xl';
  disabled?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  size: 'sm',
  disabled: false
});

const emit = defineEmits<{
  'update:modelValue': [value: string | number];
}>();

// ═══ Logique Headless UI : Conversion valeur ↔ index ═══
const selectedIndex = computed(() => {
  const index = props.options.findIndex(opt => opt.value === props.modelValue);
  return index >= 0 ? index : 0;
});

function handleChange(index: number) {
  if (props.disabled) return;
  const option = props.options[index];
  if (option && !option.disabled) {
    emit('update:modelValue', option.value);
  }
}
</script>
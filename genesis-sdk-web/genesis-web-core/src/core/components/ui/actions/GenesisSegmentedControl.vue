<template>
  <TabGroup
    :selectedIndex="selectedIndex"
    @change="handleChange"
    as="div"
    class="inline-flex flex-col gap-4"
  >
    <!-- Conteneur du Segmented Control :
         sa hauteur EXTÉRIEURE est celle de l'échelle unique (CONTROL_SIZES), comme un bouton ou un input
         de la même taille. Le padding est compris dedans (box-border). -->
    <TabList
      class="inline-flex rounded-md gap-1 bg-bg-light"
      :class="[tokens.box, paddingClasses]"
      role="group"
    >
      <Tab
        v-for="option in options"
        :key="String(option.value)"
        :disabled="disabled || option.disabled"
        v-slot="{ selected }"
        as="template"
      >
        <!-- Même `size` que le conteneur : texte, icônes et padding horizontal viennent de la même table.
             `!h-auto` remplace la hauteur fixe du bouton : il remplit l'espace restant (hauteur - padding). -->
        <GenesisButton
          class="!h-auto"
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
import GenesisButton from '@genesis-labs/web-core/core/components/ui/actions/GenesisButton.vue';
import { CONTROL_SIZES, type UI_Size_Unit } from '@genesis-labs/web-core/core/config/ui.config';


interface SegmentedOption {
  label?: string;
  value: string | number;
  icon?: Component;
  disabled?: boolean;
}

interface Props {
  modelValue: string | number;
  options: SegmentedOption[];
  /** Hauteur extérieure du composant : même échelle que GenesisButton / GenesisInput */
  size?: UI_Size_Unit;
  disabled?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  size: 'md',
  disabled: false
});

const emit = defineEmits<{
  'update:modelValue': [value: string | number];
}>();

// ═══ Tokens de la taille courante (échelle unique, voir ui.config.ts) ═══
const tokens = computed(() => CONTROL_SIZES[props.size]);

/**
 * Marge entre le conteneur et les boutons internes.
 * Hauteur interne obtenue : xs 20 · sm 24 · md 24 · lg 32 · xl 40 · 2xl 48 px.
 */
const SEGMENT_PADDING: Record<UI_Size_Unit, string> = {
  xs: 'p-0.5',
  sm: 'p-0.5',
  md: 'p-1',
  lg: 'p-1',
  xl: 'p-1',
  '2xl': 'p-1',
};
const paddingClasses = computed(() => SEGMENT_PADDING[props.size]);

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
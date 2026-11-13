<template>
  <TabGroup
    :selectedIndex="selectedIndex"
    @change="handleChange"
    as="div"
    class="flex flex-col gap-4"
    :class="fillWidth ? 'w-full' : 'inline-flex'"
  >
    <!-- Conteneur : hauteur extérieure = échelle unique (CONTROL_SIZES), padding compris dedans -->
    <TabList
      ref="listRef"
      class="relative rounded-md border-b-4 gap-1"
      :class="[variantClasses, tokens.box, paddingClasses, fillWidth ? 'flex w-full' : 'inline-flex']"
      role="group"
    >
      <!-- Indicateur glissant : mesuré sur le bouton actif -->
      <span
        aria-hidden="true"
        class="absolute top-0 left-0 rounded-sm bg-primary pointer-events-none"
        :class="{ 'transition-all duration-300 ease-out': ready }"
        :style="indicatorStyle"
      />

      <Tab
        v-for="(option, index) in options"
        :key="String(option.value)"
        :disabled="disabled || option.disabled"
        v-slot="{ selected }"
        as="template"
      >
        <button
          :ref="(el: any) => setTabRef(el, index)"
          type="button"
          class="relative z-10 inline-flex items-center justify-center whitespace-nowrap rounded-lg h-full
                 transition-colors duration-200
                 focus:outline-none focus-visible:ring-2 focus-visible:ring-primary"
          :class="[
            tokens.text,
            tokens.px,
            tokens.gap,
            selected ? 'text-primary-text' : 'text-text-muted hover:text-text',
            { 'flex-1': fillWidth },
            (disabled || option.disabled) ? 'opacity-50 cursor-not-allowed' : 'cursor-pointer'
          ]"
        >
          <component v-if="option.icon" :is="option.icon" :class="tokens.icon" class="flex-shrink-0" />
          <span v-if="option.label" class="font-medium">{{ option.label }}</span>
        </button>
      </Tab>
    </TabList>

    <!-- Panneaux de contenu (optionnel) -->
    <TabPanels v-if="$slots.panels" :class="fillWidth ? 'w-full' : ''">
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
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch, type Component } from 'vue';
import { TabGroup, TabList, Tab, TabPanels, TabPanel } from '@headlessui/vue';
import { CONTROL_SIZES, type UI_Size_Unit } from '@genesis-labs/web-core/core/config/ui.config';

type SegmentedVariant = 'primary' | 'secondary' | 'tertiary';

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
  fillWidth?: boolean;

  variant?: SegmentedVariant;
}

const props = withDefaults(defineProps<Props>(), {
  size: 'md',
  disabled: false,
  fillWidth: false,
  variant: 'secondary',
});

const emit = defineEmits<{
  'update:modelValue': [value: string | number];
}>();

// ═══ Tokens de la taille courante ═══
const tokens = computed(() => CONTROL_SIZES[props.size]);

const SEGMENT_PADDING: Record<UI_Size_Unit, string> = {
  xs: 'p-0.5',
  sm: 'p-0.5',
  md: 'p-1',
  lg: 'p-1',
  xl: 'p-1',
  '2xl': 'p-1',
};
const paddingClasses = computed(() => SEGMENT_PADDING[props.size]);

const VARIANT_CLASSES: Record<SegmentedVariant, string> = {
  primary: 'bg-bg-light border-neutral-shadow/50',
  secondary: 'bg-bg-secondary border-b-secondary-shadow',
  tertiary: 'border bg-bg-dark border-bg-neutral-genesis',
};

const variantClasses = computed(() => VARIANT_CLASSES[props.variant]);

// ═══ Logique Headless UI : valeur ↔ index ═══
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

// ═══ Indicateur glissant (mesuré sur le bouton actif) ═══
const listRef = ref<{ el: HTMLElement } | HTMLElement | null>(null);
const tabEls = ref<(HTMLElement | null)[]>([]);
const ready = ref(false); // évite l'animation au tout premier affichage
const rect = ref({ x: 0, y: 0, w: 0, h: 0 });

function setTabRef(el: any, index: number) {
  tabEls.value[index] = (el?.$el ?? el) as HTMLElement | null;
}

function measure() {
  const el = tabEls.value[selectedIndex.value];
  if (!el) return;
  rect.value = { x: el.offsetLeft, y: el.offsetTop, w: el.offsetWidth, h: el.offsetHeight };
}

const indicatorStyle = computed(() => ({
  width: `${rect.value.w}px`,
  height: `${rect.value.h}px`,
  transform: `translate(${rect.value.x}px, ${rect.value.y}px)`,
  opacity: rect.value.w > 0 ? 1 : 0,
}));

let observer: ResizeObserver | null = null;

onMounted(async () => {
  await nextTick();
  measure();
  requestAnimationFrame(() => { ready.value = true; });

  const list = (listRef.value as any)?.el ?? (listRef.value as any)?.$el ?? listRef.value;
  if (list && 'ResizeObserver' in window) {
    observer = new ResizeObserver(() => measure());
    observer.observe(list as HTMLElement);
  }
});

onBeforeUnmount(() => observer?.disconnect());

watch(() => [props.modelValue, props.options, props.size], async () => {
  await nextTick();
  measure();
}, { deep: true });
</script>
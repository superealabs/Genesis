<template>
  <Menu
    v-slot="{ open, close }"
    as="div"
  >
    <div
      class="inline-flex flex-col gap-1"
      @mouseenter="handleMouseEnter(open)"
      @mouseleave="handleMouseLeave(open, close)"
    >
      <!-- ═══ Label ═══ -->
      <label
        v-if="label"
        class="text-sm font-medium text-muted"
      >
        {{ label }}<span v-if="isMandatory" class="text-accent ml-0.5">*</span>
      </label>

      <!-- ═══ Wrapper trigger + dropdown (le relative est ici) ═══ -->
      <div class="relative inline-block">
        <MenuButton as="template">

          <GenesisButton
            v-if="$slots.trigger"
            ref="triggerRef"
            :variant="triggerVariant"
            :size="triggerSize"
            :disabled="triggerDisabled"
            :use-default-text="false"
            :class="{ '!border !border-secondary': open }"
          >
            <template v-if="$slots.triggerIcon" #leftIcon>
              <slot name="triggerIcon" />
            </template>
            <slot name="trigger" />
            <template #rightIcon>
              <IconChevronDown
                v-if="!hideChevron"
                class="transition-transform duration-200"
                :class="{ 'rotate-180': open }"
              />
            </template>
          </GenesisButton>

          <GenesisButton
            v-else-if="!hideChevron"
            ref="triggerRef"
            :variant="triggerVariant"
            :size="triggerSize"
            :disabled="triggerDisabled"
            :use-default-text="false"
            :class="{ '!border !border-secondary': open }"
          >
            <template v-if="$slots.triggerIcon" #leftIcon>
              <slot name="triggerIcon" />
            </template>
            <template #rightIcon>
              <IconChevronDown
                class="transition-transform duration-200"
                :class="{ 'rotate-180': open }"
              />
            </template>
          </GenesisButton>

          <GenesisButtonIcon
            v-else
            ref="triggerRef"
            :variant="triggerVariant"
            :size="triggerSize"
            :disabled="triggerDisabled"
            :class="{ '!border !border-secondary': open }"
          >
            <slot name="triggerIcon" />
          </GenesisButtonIcon>

        </MenuButton>

        <transition
          enter-active-class="transition duration-100 ease-out"
          enter-from-class="transform scale-95 opacity-0"
          enter-to-class="transform scale-100 opacity-100"
          leave-active-class="transition duration-75 ease-in"
          leave-from-class="transform scale-100 opacity-100"
          leave-to-class="transform scale-95 opacity-0"
        >
          <MenuItems
            :class="menuItemsClasses"
            :style="dropdownStyle"
            class="focus:outline-none"
          >
            <slot />
          </MenuItems>
        </transition>
      </div>
    </div>
  </Menu>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, nextTick, type ComponentPublicInstance } from 'vue';
import { Menu, MenuButton, MenuItems } from '@headlessui/vue';
import GenesisButton from '@/core/components/ui/actions/GenesisButton.vue';
import GenesisButtonIcon from '@/core/components/ui/actions/GenesisButtonIcon.vue';
import IconChevronDown from '@/core/components/ui/icons/IconChevronDown.vue';
import { MENU_SIZES, type MenuSize } from '@/core/config/ui.config';

const props = withDefaults(defineProps<{
    position?: 'top-left' | 'top-right' | 'bottom-left' | 'bottom-right';
    align?: 'left' | 'right';
    dropdownSize?: MenuSize | '3xl';
    hideChevron?: boolean;
    matchTriggerWidth?: boolean;
    triggerVariant?: 'primary' | 'secondary' | 'tertiary';
    triggerSize?: 'xs' | 'sm' | 'md' | 'lg' | 'xl' | '2xl';
    triggerDisabled?: boolean;
    openAtHover?: boolean;
    label?: string;
    isMandatory?: boolean;
}>(), {
    align: 'right',
    dropdownSize: 'md',
    hideChevron: false,
    matchTriggerWidth: false,
    triggerVariant: 'secondary',
    triggerSize: 'md',
    triggerDisabled: false,
    openAtHover: false,
    label: '',
    isMandatory: false,
});

const emit = defineEmits<{ close: [] }>();

const triggerRef = ref<ComponentPublicInstance | HTMLElement | null>(null);
const triggerWidth = ref<number | null>(null);
let hoverTimeout: number | null = null;

const resolvedPosition = computed(() => {
    if (props.position) return props.position;
    const el = (triggerRef.value as ComponentPublicInstance)?.$el || triggerRef.value;
    if (!el) return `bottom-${props.align}` as const;
    const rect = (el as HTMLElement).getBoundingClientRect();
    const vertical = (window.innerHeight - rect.bottom) >= 150 ? 'bottom' : 'top';
    return `${vertical}-${props.align}` as const;
});

const menuItemsClasses = computed(() => {
    const base = 'absolute z-20 bg-bg-light border border-bg-light rounded-lg shadow-lg p-1 max-h-[40vh] overflow-y-auto divide-y divide-slate-400';
    const size = (MENU_SIZES as Record<string, string>)[props.dropdownSize] || 'w-56';
    const pos: Record<string, string> = {
        'bottom-right': 'right-0 mt-2',
        'bottom-left':  'left-0 mt-2',
        'top-right':    'right-0 mb-2 bottom-full',
        'top-left':     'left-0 mb-2 bottom-full',
    };
    return `${base} ${size} ${pos[resolvedPosition.value]}`;
});

const dropdownStyle = computed(() => {
    if (!props.matchTriggerWidth || triggerWidth.value === null) return {};
    const w = `${triggerWidth.value}px`;
    return { width: w, minWidth: w, maxWidth: w };
});

function getMenuButtonEl(): HTMLElement | null {
    const el = (triggerRef.value as ComponentPublicInstance)?.$el || triggerRef.value;
    return (el as HTMLElement) ?? null;
}

function measureTriggerWidth() {
    const el = getMenuButtonEl();
    if (el) triggerWidth.value = el.offsetWidth;
}

function handleMouseEnter(open: boolean) {
    if (!props.openAtHover) return;
    clearHoverTimeout();
    if (!open) {
        if (props.matchTriggerWidth) measureTriggerWidth();
        getMenuButtonEl()?.click();
    }
}

function handleMouseLeave(open: boolean, close: () => void) {
    if (!props.openAtHover) return;
    if (open) {
        hoverTimeout = window.setTimeout(() => {
            close();
            emit('close');
        }, 150);
    }
}

function clearHoverTimeout() {
    if (hoverTimeout !== null) { clearTimeout(hoverTimeout); hoverTimeout = null; }
}

onMounted(() => {
    if (props.matchTriggerWidth) nextTick(measureTriggerWidth);
});

onUnmounted(clearHoverTimeout);
</script>
<template>
  <Menu 
    v-slot="{ open, close }" 
    as="div" 
    :model-value="internalOpen" 
    @update:model-value="internalOpen = $event"
  >
    <div
      class="inline-flex flex-col gap-1"
      @mouseenter="handleMouseEnter(open)"
      @mouseleave="handleMouseLeave(open, close)"
    >
      <!-- Label -->
      <label
        v-if="label"
        class="text-sm font-medium text-muted"
      >
        {{ label }}<span v-if="isMandatory" class="text-accent ml-0.5">*</span>
      </label>

      <!-- Wrapper trigger + dropdown -->
      <div class="relative inline-block">
        <MenuButton as="template">
          
          <!-- Cas 1 : Slot trigger personnalisé -->
          <GenesisButton
            v-if="$slots.trigger"
            ref="triggerRef"
            :variant="triggerVariant"
            :size="triggerSize"
            :disabled="triggerDisabled"
            :use-default-text="false"
            @mousedown="(e) => handleTriggerMouseDown(e, open)"
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

          <!-- Cas 2 : Trigger par défaut avec texte et chevron -->
          <GenesisButton
            v-else-if="!hideChevron"
            ref="triggerRef"
            :variant="triggerVariant"
            :size="triggerSize"
            :disabled="triggerDisabled"
            :use-default-text="false"
            @mousedown="(e) => handleTriggerMouseDown(e, open)"
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

          <!-- Cas 3 : Trigger icône uniquement -->
          <GenesisButtonIcon
            v-else
            ref="triggerRef"
            :variant="triggerVariant"
            :size="triggerSize"
            :disabled="triggerDisabled"
            @mousedown="(e) => handleTriggerMouseDown(e, open)"
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
            <slot :close="close" />
          </MenuItems>
        </transition>
      </div>
    </div>
  </Menu>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted, nextTick, type ComponentPublicInstance, onUnmounted } from 'vue';
import { Menu, MenuButton, MenuItems } from '@headlessui/vue';

import GenesisButton from '@genesis-labs/web-core/core/components/ui/actions/GenesisButton.vue';
import GenesisButtonIcon from '@genesis-labs/web-core/core/components/ui/actions/GenesisButtonIcon.vue';
import IconChevronDown from '@genesis-labs/web-core/core/components/ui/icons/IconChevronDown.vue';
import { MENU_SIZES, type MenuSize, type UI_Size_Unit, type UI_Variant } from '@genesis-labs/web-core/core/config/ui.config';

// ============================================================================
// 1. PROPS
// ============================================================================
const props = withDefaults(defineProps<{
  position?: 'top-left' | 'top-right' | 'bottom-left' | 'bottom-right';
  align?: 'left' | 'right';
  dropdownSize?: MenuSize | '3xl';
  hideChevron?: boolean;
  matchTriggerWidth?: boolean;
  triggerVariant?: UI_Variant;
  triggerSize?: UI_Size_Unit;
  triggerDisabled?: boolean;
  openAtHover?: boolean;
  label?: string;
  isMandatory?: boolean;
  forceDown?: boolean;
}>(), {
  align: 'right',
  dropdownSize: 'lg',
  hideChevron: false,
  matchTriggerWidth: false,
  triggerVariant: 'neutral',
  triggerSize: 'lg',
  triggerDisabled: false,
  openAtHover: false,
  label: '',
  isMandatory: false,
  forceDown: false
});

const emit = defineEmits<{ close: [] }>();

// ============================================================================
// 2. ÉTAT LOCAL
// ============================================================================
const internalOpen = ref(false);
const isPersistent = ref(false); // Verrouille le menu contre la fermeture au survol après un clic
const triggerRef = ref<ComponentPublicInstance | HTMLElement | null>(null);
const triggerWidth = ref<number | null>(null);
let hoverTimeout: number | null = null; 

// Surveille la fermeture du menu pour réinitialiser l'état de persistance
watch(internalOpen, (newValue) => {
  if (!newValue) {
    isPersistent.value = false;
    clearHoverTimeout();
  }
});

// ============================================================================
// 3. COMPUTEDS & LOGIQUE DE POSITIONNEMENT
// ============================================================================

/**
 * Détermine les classes de couleur (fond et bordure) du menu déroulant 
 * en fonction de la variante du trigger pour une cohérence visuelle totale.
 */
const menuItemsVariantClasses = computed(() => {
  const variants = {
    accent: 'bg-accent text-accent-900 border border-accent',
    primary: 'bg-primary text-text border border-primary',
    secondary: 'bg-bg-secondary text-text border border-secondary',
    tertiary: 'bg-bg-secondary text-text border border-secondary',
    neutral: 'bg-bg-neutral-genesis text-text'
  };
  return variants[props.triggerVariant] || variants.neutral;
});

const menuItemsClasses = computed(() => {
  const base = `absolute z-50 rounded-lg shadow-lg p-1 max-h-[40vh] overflow-y-auto`;
  const size = (MENU_SIZES as Record<string, string>)[props.dropdownSize] || 'w-56';
  
  // ✅ Ajout des classes de variante au style de base
  return `${base} ${size} ${menuItemsVariantClasses.value}`;
});

const dropdownStyle = computed(() => {
  const style: Record<string, string> = {};

  const el = getMenuButtonEl();
  const rect = el?.getBoundingClientRect();
  const goesUp = !props.forceDown && rect ? (window.innerHeight - rect.bottom) < 150 : false;

  if (goesUp) {
    style.bottom = '100%';
    style.marginBottom = '8px';
  } else {
    style.top = '100%';
    style.marginTop = '8px';
  }

  if (props.align === 'right') {
    style.right = '0';
  } else {
    style.left = '0';
  }

  // Gestion de la largeur identique au trigger
  if (props.matchTriggerWidth && triggerWidth.value !== null) {
    style.width = `${triggerWidth.value}px`;
    style.minWidth = `${triggerWidth.value}px`;
  }

  return style;
});

// ============================================================================
// 4. ACTIONS
// ============================================================================

function getMenuButtonEl(): HTMLElement | null {
  const el = (triggerRef.value as ComponentPublicInstance)?.$el || triggerRef.value;
  return (el as HTMLElement) ?? null;
}

function measureTriggerWidth() {
  const el = getMenuButtonEl();
  if (el) triggerWidth.value = el.offsetWidth;
}

/**
 * Intercepte le mousedown pour empêcher Headless UI de fermer le menu 
 * si l'utilisateur clique sur le trigger alors qu'il est déjà ouvert via hover.
 */
function handleTriggerMouseDown(e: MouseEvent, isOpen: boolean) {
  if (props.openAtHover && isOpen) {
    e.preventDefault();
    e.stopImmediatePropagation(); // Empêche Headless UI de recevoir l'événement et de fermer le menu
    isPersistent.value = true;    // Active la persistance
    clearHoverTimeout();          // Annule tout timer de fermeture en attente
  }
}

function handleMouseEnter(isOpen: boolean) {
  if (!props.openAtHover || props.triggerDisabled) return;
  clearHoverTimeout();
  if (!isOpen) {
    if (props.matchTriggerWidth) measureTriggerWidth();
    getMenuButtonEl()?.click(); // Simule un clic pour ouvrir via Headless UI
  }
}

function handleMouseLeave(isOpen: boolean, close: () => void) {
  if (!props.openAtHover) return;
  
  // Ne ferme le menu au survol que s'il n'est PAS en mode persistant (déclenché par un clic)
  if (isOpen && !isPersistent.value) {
    hoverTimeout = window.setTimeout(() => {
      close();
      emit('close');
    }, 150);
  }
}

function clearHoverTimeout() {
  if (hoverTimeout !== null) { 
    clearTimeout(hoverTimeout); 
    hoverTimeout = null; 
  }
}

// ============================================================================
// 5. LIFECYCLE
// ============================================================================
onMounted(() => {
  if (props.matchTriggerWidth) {
    nextTick(measureTriggerWidth);
  }
});

onUnmounted(() => {
  clearHoverTimeout();
});
</script>
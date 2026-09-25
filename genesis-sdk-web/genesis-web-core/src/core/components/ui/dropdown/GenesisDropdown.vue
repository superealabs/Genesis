<template>
  <Menu v-slot="{ open, close }" as="div">
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

      <!-- Wrapper trigger + dropdown (le relative est essentiel pour le mode absolute) -->
      <div class="relative inline-block">
        <MenuButton as="template" @click="onTriggerClick">
          
          <!-- Cas 1 : Slot trigger personnalisé -->
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

          <!-- Cas 2 : Trigger par défaut avec texte et chevron -->
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

          <!-- Cas 3 : Trigger icône uniquement -->
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
            <slot :close="close" />
          </MenuItems>
        </transition>
      </div>
    </div>
  </Menu>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, nextTick, type ComponentPublicInstance } from 'vue';
import { Menu, MenuButton, MenuItems } from '@headlessui/vue';

import GenesisButton from '@genesis-labs/web-core/core/components/ui/actions/GenesisButton.vue';
import GenesisButtonIcon from '@genesis-labs/web-core/core/components/ui/actions/GenesisButtonIcon.vue';
import IconChevronDown from '@genesis-labs/web-core/core/components/ui/icons/IconChevronDown.vue';
import { MENU_SIZES, type MenuSize } from '@genesis-labs/web-core/core/config/ui.config';

// ============================================================================
// 1. PROPS
// ============================================================================
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
  /**
   * Détermine le contexte de positionnement du dropdown.
   * - 'absolute' (défaut) : Respecte les limites du conteneur parent (idéal pour les listes dans des frames scrollables).
   * - 'fixed' : Positionné par rapport au viewport (idéal pour les overlays globaux qui doivent sortir des conteneurs).
   */
  positioning?: 'absolute' | 'fixed';
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
  positioning: 'absolute' // Changé de 'fixed' à 'absolute' pour corriger les problèmes de débordement
});

const emit = defineEmits<{ close: [] }>();

// ============================================================================
// 2. ÉTAT LOCAL
// ============================================================================
const triggerRef = ref<ComponentPublicInstance | HTMLElement | null>(null);
const triggerWidth = ref<number | null>(null);
let hoverTimeout: number | null = null; 

// ============================================================================
// 3. COMPUTEDS & LOGIQUE DE POSITIONNEMENT
// ============================================================================

/**
 * Détermine les classes CSS de base du menu, en adaptant le positionnement 
 * et en garantissant un scroll interne (max-h + overflow-y-auto) pour éviter 
 * que le dropdown ne dépasse de l'écran, quelle que soit sa taille.
 */
const menuItemsClasses = computed(() => {
  const positionClass = props.positioning === 'fixed' ? 'fixed z-[9999]' : 'absolute z-50';
  const base = `${positionClass} bg-bg-light border border-secondary rounded-lg shadow-lg p-1 max-h-[40vh] overflow-y-auto`;
  const size = (MENU_SIZES as Record<string, string>)[props.dropdownSize] || 'w-56';
  return `${base} ${size}`;
});

/**
 * Calcule le style inline pour le positionnement.
 * En mode 'absolute', on utilise des pourcentages relatifs au parent (plus robuste dans les conteneurs scrollables).
 * En mode 'fixed', on utilise les coordonnées du viewport (getBoundingClientRect).
 */
const dropdownStyle = computed(() => {
  const style: Record<string, string> = {};

  if (props.positioning === 'absolute') {
    // Logique robuste pour les conteneurs : utilisation de top/bottom 100%
    const el = getMenuButtonEl();
    const rect = el?.getBoundingClientRect();
    // Estimation simple : si le bas du bouton est à moins de 150px du bas de l'écran, on affiche au-dessus
    const goesUp = rect ? (window.innerHeight - rect.bottom) < 150 : false;

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
  } else {
    // Logique legacy pour le mode 'fixed' (par rapport au viewport)
    const el = getMenuButtonEl();
    if (!el) return {};
    const rect = el.getBoundingClientRect();
    
    style.top = `${rect.bottom + 8}px`;
    
    if (props.align === 'right') {
      style.right = `${window.innerWidth - rect.right}px`;
    } else {
      style.left = `${rect.left}px`;
    }
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

async function onTriggerClick() {
  if (props.positioning === 'fixed') {
    updateFixedPosition();
    await nextTick();
    updateFixedPosition();
  }
}

function updateFixedPosition() {
  // Cette fonction n'est plus utilisée pour le mode 'absolute', 
  // mais conservée pour la compatibilité du mode 'fixed' si nécessaire.
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

onUnmounted(clearHoverTimeout);
</script>
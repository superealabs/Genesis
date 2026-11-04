<template>
  <Transition
    enter-active-class="transition-opacity duration-200 ease-out"
    enter-from-class="opacity-0"
    enter-to-class="opacity-100"
    leave-active-class="transition-opacity duration-150 ease-in"
    leave-from-class="opacity-100"
    leave-to-class="opacity-0"
  >
    <div
      v-if="visible"
      class="absolute inset-0 z-[300] flex items-center justify-center"
      :class="overlayClass"
      role="status"
      aria-live="polite"
      aria-busy="true"
    >
      <!-- Spinner + textes, sans carte ni fond propre -->
      <div class="flex flex-col items-center justify-center gap-4 p-8">
        <svg
          :width="spinnerSize"
          :height="spinnerSize"
          viewBox="0 0 24 24"
          fill="none"
          xmlns="http://www.w3.org/2000/svg"
          class="animate-spin"
        >
          <circle
            cx="12" cy="12" r="9"
            stroke="currentColor" stroke-width="2.5"
            class="text-secondary opacity-30"
          />
          <path
            d="M 12 3 A 9 9 0 0 1 21 12"
            stroke="currentColor" stroke-width="2.5"
            stroke-linecap="round"
            class="text-primary"
          />
        </svg>

        <div v-if="title || message" class="flex flex-col items-center gap-1 text-center">
          <span v-if="title" class="text-base font-medium text-text">{{ title }}</span>
          <span v-if="message" class="text-sm text-text-muted">{{ message }}</span>
        </div>
      </div>
    </div>
  </Transition>
</template>

<script setup lang="ts">
import { computed } from 'vue';

type Size = 'sm' | 'md' | 'lg';

interface Props {
  /** Affiche ou masque l'overlay (transition automatique) */
  visible?: boolean;
  /** Titre principal (ex: "Validation en cours") */
  title?: string;
  /** Message secondaire (ex: "Vérification des données...") */
  message?: string;
  /** Taille du spinner */
  size?: Size;
  /** Intensité du voile de l'overlay */
  variant?: 'light' | 'dark';
}

const props = withDefaults(defineProps<Props>(), {
  visible: true,
  size: 'md',
  variant: 'dark',
});

const SIZES: Record<Size, number> = { sm: 32, md: 48, lg: 64 };
const spinnerSize = computed(() => SIZES[props.size]);

const overlayClass = computed(() =>
  props.variant === 'dark'
    ? 'bg-bg-dark/90 backdrop-blur-sm'
    : 'bg-white/70 backdrop-blur-sm'
);
</script>
<!-- genesis-sdk-web/genesis-web-core/src/core/components/layouts/GenesisContainer.vue -->
<template>
  <div 
    class="rounded-md overflow-hidden flex flex-col border border-bg-neutral-genesis relative" 
    :class="props.width"
    :style="[{ height: props.height + 'px' }, resizeStyle]"
  >
    <!-- État de chargement -->
    <div v-if="isLoading" class="p-6 text-center text-text-muted text-sm flex items-center justify-center gap-2">
      <svg class="animate-spin h-4 w-4 text-text-muted" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
        <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
        <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
      </svg>
      <span>{{ loadingText }}</span>
    </div>

    <!-- Contenu ou État vide -->
    <template v-else>
      <div class="flex-1 overflow-y-auto custom-scrollbar">
        <slot></slot>

        <div v-if="isEmpty && !$slots.empty" class="p-6 text-center text-text-muted text-sm flex-shrink-0">
          {{ emptyText }}
        </div>
        <slot v-else-if="isEmpty" name="empty"></slot>
      </div>
    </template>

    <GenesisResizeHandle @resize-start="startResizeBottom" />
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useResizable } from '@genesis-labs/web-core/core/composables/ux/useResizable';
import GenesisResizeHandle from '@genesis-labs/web-core/core/components/ui/actions/GenesisResizeHandle.vue';

interface Props {
  minHeight?: number;
  maxHeight?: number | (() => number);
  height?: number;
  width?: string;
  isLoading?: boolean;
  loadingText?: string;
  isEmpty?: boolean;
  emptyText?: string;
}

const props = withDefaults(defineProps<Props>(), {
  minHeight: 300,
  maxHeight: () => window.innerHeight * 0.8,
  height: 400,
  width: 'w-full',
  isLoading: false,
  loadingText: 'Chargement en cours...',
  isEmpty: false,
  emptyText: 'Aucun élément trouvé.'
});

const { resizeStyle, startResizeBottom } = useResizable({
  minHeight: props.minHeight,
  maxHeight: props.maxHeight,
  resizableX: ref(false), 
  resizableY: ref(true)
});
</script>

<style scoped>
.custom-scrollbar::-webkit-scrollbar { width: 6px; }
.custom-scrollbar::-webkit-scrollbar-track { background: transparent; }
.custom-scrollbar::-webkit-scrollbar-thumb { background-color: var(--color-secondary, #cbd5e1); border-radius: 3px; }
.custom-scrollbar::-webkit-scrollbar-thumb:hover { background-color: var(--color-secondary-hover, #94a3b8); }
</style>
<template>
  <div
    class="absolute inset-0 flex items-center justify-center bg-black/20 z-50 transition-opacity"
    :class="{ 'opacity-0 pointer-events-none': !visible, 'opacity-100': visible }"
  >
    <!-- Custom overlay content -->
    <slot name="overlay">
      <!-- Default: Spinner + Loading text -->
      <div class="flex flex-col items-center gap-2">
        <span class="loading loading-spinner loading-lg text-primary"></span>
        <div class="text-white text font-medium">{{ text }}</div>
      </div>
    </slot>
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'

export default defineComponent({
  name: 'GenesisOverlay',
  props: {
    visible: {
      type: Boolean,
      default: false,
    },
    text: {
      type: String,
      default: 'Loading...',
    },
  },
})
</script>

<style scoped>
.overlay {
  background-color: rgba(255, 255, 255, 0.7);
  z-index: 100;
}
</style>

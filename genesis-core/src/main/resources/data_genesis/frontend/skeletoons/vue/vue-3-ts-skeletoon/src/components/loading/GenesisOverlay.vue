<template>
  <div
    class="absolute inset-0 flex items-center justify-center bg-base-200/80 z-50 transition-opacity duration-300"
    :class="{ 'opacity-0 pointer-events-none': !visible, 'opacity-100': visible }"
  >
    <!-- Custom overlay content -->
    <slot name="overlay">
      <!-- Default: Spinner + Loading text -->
      <div class="flex flex-col items-center gap-2">
        <span class="loading loading-spinner loading-lg text-primary"></span>
        <div class="text-white font-medium">{{ text }}</div>
      </div>
    </slot>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  visible?: boolean
  text?: string
}>()

// Defaults are already handled in defineProps
</script>

<style scoped>
/* The previous .overlay style is unused, so can be removed */
</style>

<template>
  <transition name="fade">
    <div
      v-if="visible"
      class="fixed inset-0 bg-black/40 z-50 flex items-center justify-center"
      @click.self="close"
    >
      <div
        class="bg-base-100 rounded-lg shadow-lg w-full max-w-lg mx-4 overflow-hidden flex flex-col"
      >
        <!-- Header -->
        <div class="flex items-center justify-between p-1">
          <h5 class="text-lg font-medium">{{ title }}</h5>
          <button type="button" class="btn btn-ghost btn-square" @click="close">
            <XIcon />
          </button>
        </div>

        <!-- Body -->
        <div class="flex-1 overflow-auto">
          <slot />
        </div>

        <!-- Footer -->
        <div class="flex justify-end p-4 gap-2">
          <slot name="footer" />
        </div>
      </div>
    </div>
  </transition>
</template>

<script setup lang="ts">
import XIcon from '../icons/XIcon.vue'

// Props
const props = defineProps<{
  title?: string
  visible: boolean
}>()

// Emits
const emit = defineEmits<{
  (e: 'close', value: boolean): void
}>()

// Methods
const close = () => emit('close', false)
</script>

<style>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>

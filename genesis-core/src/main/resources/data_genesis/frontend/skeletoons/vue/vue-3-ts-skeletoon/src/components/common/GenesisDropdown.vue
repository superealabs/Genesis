@ -0,0 +1,37 @@
<template>
  <div class="relative w-full">
    <div @click="toggleDropdown">
      <slot
        name="trigger"
        :is-open="showDropdown"
        :toggle="toggleDropdown"
        :trigger-ref="triggerRef"
      >
        <button ref="triggerRef" class="btn btn-block">Toggle Dropdown</button>
      </slot>
    </div>

    <div
      v-show="showDropdown"
      ref="contentRef"
      class="absolute mt-1 w-full z-10 card border border-base-300 bg-base-200 shadow-lg"
      tabindex="-1"
    >
      <slot name="content" :hide="hideDropdown">
        <div class="p-4 text-center">Dropdown Content</div>
      </slot>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useDropdown } from '@/composables/useDropdown' // Adjust path as needed

// Refs for the composable
const triggerRef = ref<HTMLElement | null>(null)
const contentRef = ref<HTMLElement | null>(null)

// Use the composable to manage visibility and click-outside logic
const { showDropdown, hideDropdown, toggleDropdown } = useDropdown(triggerRef, contentRef)
</script>

<template>
  <div
    class="fixed inset-0 flex items-center justify-center bg-base-200/85 z-50"
    :class="{ invisible: !freezeStore.freezeState }"
  >
    <!-- Custom overlay content -->
    <slot name="overlay">
      <!-- Default: Spinner + Loading text -->
      <div class="text-center">
        <!-- Spinner Tailwind -->
        <div
          class="h-12 w-12 animate-spin rounded-full border-4 border-gray-300 border-t-primary mx-auto"
        ></div>
        <div class="mt-3 text-gray-800 font-medium">
          {{ freezeStore.freezeMessage }}
        </div>
      </div>
    </slot>
  </div>
</template>

<script setup lang="ts">
import { useFreezeScreenStore } from '@/stores/useFreezeScreenStore.ts'

const freezeStore = useFreezeScreenStore()
</script>

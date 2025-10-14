<template>
  <GenesisPopup :visible="visible" @close="close">
    <div class="d-flex flex-column text-center p-3">
      <!-- Icon -->
      <div>
        <i class="bi bi-exclamation-triangle text-danger" style="font-size: 4rem"></i>
      </div>

      <!-- Message -->
      <div>
        <h5 class="text-danger">{{ title }}</h5>
        <p class="mb-1">{{ message }}</p>
        <small v-if="subMessage" class="text-muted">{{ subMessage }}</small>
      </div>

      <!-- Button -->
      <div class="d-flex justify-content-center mt-4">
        <GenesisButton @click="close" class="btn-danger" label="Close" />
      </div>
    </div>
  </GenesisPopup>
</template>

<script setup lang="ts">
import GenesisPopup from '@/components/popup/GenesisPopup.vue'
import GenesisButton from '@/components/button/GenesisButton.vue'

// Props
const props = defineProps<{
  visible: boolean
  title?: string
  message?: string
  subMessage?: string
}>()

// Emit
const emit = defineEmits<{
  (e: 'close', value: boolean): void
}>()

// Methods
const close = () => emit('close', false)
</script>

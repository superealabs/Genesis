<template>
  <GenesisPopup :visible="visible" @close="cancel">
    <div class="flex flex-col items-center text-center gap-4 p-4">
      <!-- Icon -->
      <div>
        <WarningIcon class="text-6xl text-warning" />
      </div>

      <!-- Messages -->
      <div class="space-y-1">
        <p class="text-base font-medium">{{ message }}</p>
        <p class="text text-gray-500">{{ subMessage }}</p>
      </div>

      <!-- Buttons -->
      <div class="flex items-center justify-center gap-2">
        <GenesisButton @click="confirm" class="btn btn-error text-white">
          <span>Confirm delete</span>
        </GenesisButton>
        <GenesisButton @click="cancel" class="btn btn-outline">
          <span>Cancel</span>
        </GenesisButton>
      </div>
    </div>
  </GenesisPopup>
</template>

<script setup lang="ts">
import GenesisPopup from '@/components/popup/GenesisPopup.vue'
import GenesisButton from '@/components/button/GenesisButton.vue'
import WarningIcon from '../icons/WarningIcon.vue'

// Props
const props = defineProps<{
  visible: boolean
  message: string
  subMessage?: string
}>()

// Emits
const emit = defineEmits<{
  (e: 'confirm'): void
  (e: 'cancel'): void
}>()

// Methods
const confirm = () => emit('confirm')
const cancel = () => emit('cancel')
</script>

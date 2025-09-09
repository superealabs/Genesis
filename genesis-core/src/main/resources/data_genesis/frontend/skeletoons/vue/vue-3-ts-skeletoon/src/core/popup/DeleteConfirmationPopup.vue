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

<script lang="ts">
import { defineComponent } from 'vue'
import GenesisPopup from '@/core/popup/GenesisPopup.vue'
import GenesisButton from '@/core/button/GenesisButton.vue'
import WarningIcon from '../icons/WarningIcon.vue'

export default defineComponent({
  name: 'DeleteConfirmationPopup',
  components: { GenesisPopup, GenesisButton, WarningIcon },
  props: {
    visible: { type: Boolean, required: true },
    message: { type: String, required: true },
    subMessage: { type: String, default: 'This action is irreversible.' },
  },
  emits: ['confirm', 'cancel'],
  setup(props, { emit }) {
    return {
      confirm: () => emit('confirm'),
      cancel: () => emit('cancel'),
    }
  },
})
</script>

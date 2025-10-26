<template>
  <GenesisInput
    :mandatory="true"
    :type="inputType"
    v-bind="$attrs"
    required
    :placeholder="$t('authentication.passwordPlaceholder')"
  >
    <template #label>
      <slot name="label"></slot>
    </template>

    <template #suffix>
      <button
        type="button"
        @click="togglePasswordVisibility"
        class="absolute top-0 bottom-0 right-0 p-3 flex items-center text-base-content/70 hover:text-base-content"
        :aria-label="inputType === 'password' ? 'Show password' : 'Hide password'"
      >
        <ViewIcon v-if="inputType === 'password'" />
        <EyeSlashIcon v-else />
      </button>
    </template>
  </GenesisInput>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import GenesisInput from '@/components/form/GenesisInput.vue'
import ViewIcon from '@/components/icons/ViewIcon.vue'
import EyeSlashIcon from '@/components/icons/EyeSlashIcon.vue'

defineOptions({
  inheritAttrs: true,
})
const isVisible = ref(false)

const inputType = computed(() => (isVisible.value ? 'text' : 'password'))

// Method to toggle the visibility state
const togglePasswordVisibility = () => {
  isVisible.value = !isVisible.value
}
</script>

<style scoped>
/* NOTE: For the icon to be positioned correctly, you might need to ensure:
  1. The main input container inside GenesisInput has `position: relative`.
  2. The input field itself has padding on the right to prevent the icon from overlapping the text.

  Since we are slotting into GenesisInput, you might need to adjust the structure of GenesisInput
  to support this append/suffix placement accurately.
*/
</style>

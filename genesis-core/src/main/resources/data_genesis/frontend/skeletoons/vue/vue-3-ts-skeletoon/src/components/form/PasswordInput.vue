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
        class="absolute top-0 bottom-0 right-0 p-3 flex items-center text-base-content/70 hover:text-base-content z-2"
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

const togglePasswordVisibility = () => {
  isVisible.value = !isVisible.value
}
</script>

<style scoped></style>

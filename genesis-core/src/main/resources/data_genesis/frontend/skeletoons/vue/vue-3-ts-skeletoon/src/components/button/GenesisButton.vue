<template>
  <button v-bind="$attrs" class="btn" :disabled="isLoading">
    <span v-if="isLoading" class="loading loading-spinner"></span>
    <slot v-else>
      <i v-if="icon" :class="icon" />
      <span v-if="label">{{ label }}</span>
    </slot>
  </button>
</template>

<script setup lang="ts">
defineProps<{
  icon?: string
  label?: string
  isLoading?: boolean
}>()
</script>
qa

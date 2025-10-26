<template>
  <button v-bind="$attrs" class="btn" @click="onClick" :disabled="isLoading">
    <span v-if="isLoading" class="loading loading-spinner"></span>
    <span v-else>
      <i v-if="icon" :class="icon" />
      <slot>
        <span v-if="label">{{ label }}</span>
      </slot>
    </span>
  </button>
</template>

<script setup lang="ts">
defineProps<{
  icon?: string
  label?: string
  isLoading?: boolean
}>()

const emit = defineEmits<{
  (e: 'click', event: MouseEvent): void
}>()

const onClick = (event: MouseEvent) => {
  emit('click', event)
}
</script>

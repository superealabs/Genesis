<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps({
  username: String,
  size: {
    type: Number,
    default: 10,
  },
})

/* Computed initials */
const initials = computed(() => {
  if (!props.username) return ''
  return props.username
    .split(' ')
    .map((n) => n[0])
    .join('')
    .toUpperCase()
})
</script>

<template>
  <div
    class="flex items-center justify-center w-10 h-10 rounded-full bg-primary text-primary-content font-bold"
  >
    {{ initials }}
  </div>
</template>

<style scoped></style>

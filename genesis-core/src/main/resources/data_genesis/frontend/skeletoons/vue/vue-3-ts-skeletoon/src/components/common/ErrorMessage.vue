<script setup lang="ts">
defineProps<{
  message: string | null | undefined
}>()
</script>

<template>
  <p v-if="message" class="text-error-content">
    {{ message }}
  </p>
</template>

<style scoped></style>

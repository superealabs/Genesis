<script setup lang="ts">
defineProps<{
  message: string | null
}>()
</script>

<template>
  <p class="text-error-content">
    {{ message }}
  </p>
</template>

<style scoped></style>

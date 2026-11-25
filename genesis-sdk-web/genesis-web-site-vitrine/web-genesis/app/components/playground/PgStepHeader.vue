<script setup lang="ts">
// Titre d'étape avec illustration (assets genesis-vsc)
defineProps<{ title: string; description?: string; illustration?: string }>()
</script>

<template>
  <div class="mb-5 flex items-center gap-4 rounded-lg bg-bg-light p-4">
    <img v-if="illustration" :src="illustration" alt="" class="hidden h-16 w-16 object-contain sm:block">
    <div class="min-w-0">
      <h2 class="font-display text-xl text-text">{{ title }}</h2>
      <p v-if="description" class="mt-1 text-sm text-text-muted opacity-80">{{ description }}</p>
    </div>
    <div class="ml-auto"><slot /></div>
  </div>
</template>

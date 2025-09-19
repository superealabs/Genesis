<template>
  <div class="flex items-center justify-between gap-2">
    <!-- Avatar + Nom -->
    <div class="flex items-center gap-2">
      <div
        class="flex items-center justify-center w-10 h-10 rounded-full bg-base-100 text-base-content font-bold"
      >
        {{ initials }}
      </div>
      <p class="m-0 font-medium">{{ name }}</p>
    </div>

    <!-- Bouton Déconnexion -->
    <div>
      <LogoutButton />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import LogoutButton from '@/core/button/LogoutButton.vue'

// Props
const props = defineProps<{
  name: string
}>()

// Computed: initials
const initials = computed(() => {
  if (!props.name) return ''
  return props.name
    .split(' ')
    .map((n) => n[0])
    .join('')
    .toUpperCase()
})
</script>

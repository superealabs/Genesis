<script setup lang="ts">
// Carte de sélection (framework, base, frontend) — style des cartes genesis-vsc
import IconCheck from '@genesis-labs/web-core/core/components/ui/icons/IconCheck.vue'
defineProps<{ title: string; subtitle?: string; logo?: string; selected?: boolean; disabled?: boolean }>()
defineEmits<{ select: [] }>()
</script>

<template>
  <button type="button" :disabled="disabled"
    class="relative flex items-center gap-3 rounded-lg p-3 text-left transition-colors border cursor-pointer disabled:opacity-40 disabled:cursor-not-allowed"
    :class="selected ? 'bg-primary/15 border-primary' : 'bg-bg-light border-transparent hover:border-primary/50'"
    @click="$emit('select')">
    <span class="grid h-11 w-11 shrink-0 place-items-center rounded-md bg-bg-dark">
      <img v-if="logo" :src="logo" alt="" class="h-7 w-7 object-contain">
    </span>
    <span class="min-w-0 flex-1">
      <span class="block truncate text-sm font-semibold text-text">{{ title }}</span>
      <span v-if="subtitle" class="block truncate text-xs text-text-muted opacity-70">{{ subtitle }}</span>
    </span>
    <span v-if="selected" class="grid h-5 w-5 place-items-center rounded-full bg-primary">
      <IconCheck color="var(--color-primary-text)" />
    </span>
  </button>
</template>

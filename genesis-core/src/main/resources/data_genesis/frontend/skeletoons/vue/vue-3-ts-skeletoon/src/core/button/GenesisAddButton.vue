<template>
  <GenesisButton icon="fa fa-plus">
    <span v-if="label">{{ label }}</span>
    <span v-else><slot /></span>
  </GenesisButton>
</template>

<script setup lang="ts">
import GenesisButton from './GenesisButton.vue'
defineProps<{
  label: string
}>()
</script>

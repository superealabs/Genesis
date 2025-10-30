<template>
  <GenesisButton v-bind="$attrs" class="btn-primary">
    <LeftArrowIcon />
    <span v-if="label">{{ label }}</span>
    <span v-else><slot /></span>
  </GenesisButton>
</template>

<script setup lang="ts">
import GenesisButton from './GenesisButton.vue'
import LeftArrowIcon from '@/components/icons/LeftArrowIcon.vue'
defineProps<{
  label: string
}>()
</script>

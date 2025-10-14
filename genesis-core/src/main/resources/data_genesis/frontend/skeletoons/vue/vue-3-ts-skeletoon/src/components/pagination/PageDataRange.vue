<script setup lang="ts">
defineProps<{
  totalElements?: number
  startElement?: number
  endElement?: number
}>()
</script>

<template>
  <div class="text-sm text-base-content">
    <span class="font-medium">{{ startElement }}</span>
    <span v-if="Number(endElement) > Number(startElement)">
      – <span class="font-medium">{{ endElement }}</span>
    </span>
    <span class="font-medium"> {{ $t('pagination.totalRows', { 'total':totalElements }) }}</span>
  </div>
</template>

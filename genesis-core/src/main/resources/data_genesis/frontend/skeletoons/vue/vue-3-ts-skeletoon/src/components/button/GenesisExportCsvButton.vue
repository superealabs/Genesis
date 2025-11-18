<template>
  <GenesisButton v-bind="$attrs">
    <ExcelFileIcon />
    <span>
      {{ $t('button.export') }}
    </span>
  </GenesisButton>
</template>

<script setup lang="ts">
import ExcelFileIcon from '../icons/ExcelFileIcon.vue'
import GenesisButton from './GenesisButton.vue'
</script>

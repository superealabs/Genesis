<template>
  <div class="flex flex-col gap-3 w-64 shrink-0 justify-center">
    <GenesisButton :size="'lg'" variant="accent" :disabled="!canAddAll" @click="$emit('addAll')">
      <template #leftIcon><IconChevronsRight /></template>
      Add All
    </GenesisButton>
    <GenesisButton :size="'lg'" variant="secondary" :disabled="!canAdd" @click="$emit('add')">
      <template #leftIcon><IconChevronRight /></template>
      Add
    </GenesisButton>
    <GenesisButton :size="'lg'" :disabled="!canRemove" @click="$emit('remove')">
      <template #leftIcon><IconChevronLeft /></template>
      Remove
    </GenesisButton>
    <GenesisButton :size="'lg'" variant="neutral" :disabled="!canRemoveAll" @click="$emit('removeAll')">
      <template #leftIcon><IconChevronsLeft /></template>
      Remove All
    </GenesisButton>
  </div>
</template>

<script setup lang="ts">
import GenesisButton from '@genesis-labs/web-core/core/components/ui/actions/GenesisButton.vue';
import IconChevronLeft from '@genesis-labs/web-core/core/components/ui/icons/IconChevronLeft.vue';
import IconChevronRight from '@genesis-labs/web-core/core/components/ui/icons/IconChevronRight.vue';
import IconChevronsLeft from '@genesis-labs/web-core/core/components/ui/icons/IconChevronsLeft.vue';
import IconChevronsRight from '@genesis-labs/web-core/core/components/ui/icons/IconChevronsRight.vue';

defineProps<{
  canAdd: boolean;
  canAddAll: boolean;
  canRemove: boolean;
  canRemoveAll: boolean;
}>();

defineEmits<{
  add: [];
  addAll: [];
  remove: [];
  removeAll: [];
}>();
</script>
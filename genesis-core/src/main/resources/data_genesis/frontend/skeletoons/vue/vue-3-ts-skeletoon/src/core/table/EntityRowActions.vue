<template>
  <GenesisButton class="btn-outline btn-square border-0" title="Voir Details" @click="viewRow">
    <ViewIcon />
  </GenesisButton>

  <GenesisButton
    icon="bi bi-pen"
    class="btn-outline btn-square border-0"
    title="Modifier"
    @click="editRow"
  >
    <EditIcon />
  </GenesisButton>

  <GenesisButton
    icon="bi bi-trash"
    class="btn-outline btn-square border-0 text-error"
    title="Supprimer"
    @click="deleteRow"
  >
    <TrashIcon />
  </GenesisButton>
</template>

<script setup lang="ts">
import GenesisButton from '@/core/button/GenesisButton.vue'
import ViewIcon from '../icons/ViewIcon.vue'
import EditIcon from '../icons/EditIcon.vue'
import TrashIcon from '../icons/TrashIcon.vue'

// ✅ Declare emits
const emit = defineEmits<{
  (e: 'delete-row'): void
  (e: 'view-row'): void
  (e: 'edit-row'): void
}>()

// ✅ Handlers
const deleteRow = () => emit('delete-row')
const viewRow = () => emit('view-row')
const editRow = () => emit('edit-row')
</script>

<style scoped></style>

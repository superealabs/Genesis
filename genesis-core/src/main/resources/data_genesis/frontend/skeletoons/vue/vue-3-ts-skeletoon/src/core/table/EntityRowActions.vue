<template>
  <GenesisButton class="btn-outline btn-square border-0" title="Voir Details" @click="viewRow"
    ><ViewIcon
  /></GenesisButton>
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

<script lang="ts">
import GenesisButton from '@/core/button/GenesisButton.vue'
import { defineComponent } from 'vue'
import ViewIcon from '../icons/ViewIcon.vue'
import EditIcon from '../icons/EditIcon.vue'
import TrashIcon from '../icons/TrashIcon.vue'

export default defineComponent({
  name: 'EntityRowActions',
  components: { GenesisButton, ViewIcon, EditIcon, TrashIcon },
  emits: ['delete-row', 'view-row', 'edit-row'],
  setup(props, { emit }) {
    const deleteRow = () => {
      emit('delete-row')
    }

    const viewRow = () => {
      emit('view-row')
    }

    const editRow = () => {
      emit('edit-row')
    }
    return {
      deleteRow,
      viewRow,
      editRow,
    }
  },
})
</script>

<style scoped></style>

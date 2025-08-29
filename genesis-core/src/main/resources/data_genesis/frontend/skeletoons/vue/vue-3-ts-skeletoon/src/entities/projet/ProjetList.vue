<template>
  <div>
    <table class="table align-middle table-rounded text-nowrap">
      <thead class="table-light">
        <tr>
          <th scope="col">Id</th>
          <th scope="col">Nom projet</th>
          <th scope="col">Budget</th>
          <th scope="col">Date debut</th>
          <th scope="col">Date fin prevue</th>

          <th scope="col" class="text-end">Actions</th>
        </tr>
      </thead>
      <tbody class="position-relative">
        <tr v-if="message && !loading">
          <td colspan="10" class="text-muted text-center py-4">
            {{ message }}
          </td>
        </tr>
        <ProjetRow
          v-else
          v-for="projet in data"
          :key="projet.id"
          :projet="projet"
          @request-delete="openDeletePopup"
          @request-view="viewProjet"
          @request-update="goToUpdateFormView"
        />
        <GenesisOverlay :visible="loading" />
      </tbody>
    </table>

    <DeleteConfirmationPopup
      :visible="deletePopup"
      :message="`Are you sure you want to delete Projet ?`"
      @confirm="confirmDelete"
      @cancel="closePopup"
    />
  </div>
</template>

<script lang="ts">
import { defineComponent, ref } from 'vue'
import type { PropType } from 'vue'
import ProjetRow from './ProjetRow.vue'
import { Projet } from '@/models/ProjetModel'
import GenesisOverlay from '@/core/loading/GenesisOverlay.vue'
import DeleteConfirmationPopup from '@/core/popup/DeleteConfirmationPopup.vue'
import { usePopup } from '@/composables/usePopup'
import { useProjets } from '@/composables/useProjets'

export default defineComponent({
  name: 'ProjetList',
  components: { ProjetRow, GenesisOverlay, DeleteConfirmationPopup },
  props: {
    data: {
      required: true,
      type: Array as PropType<Projet[]>,
    },
    message: {
      required: false,
      type: String,
    },
    loading: {
      type: Boolean,
      required: false,
      default: false,
    },
  },
  emits: ['request:refresh'],
  setup(props, { emit }) {
    const selectedEntity = ref<Partial<Projet>>({})
    const { closePopup, visible: deletePopup } = usePopup(false)
    const { deleteProjet, viewProjet, goToUpdateFormView } = useProjets()

    function openDeletePopup(entity: Projet) {
      if (!entity) return
      selectedEntity.value = entity
      deletePopup.value = true
    }

    async function confirmDelete() {
      const { success, error } = await deleteProjet(selectedEntity.value)
      deletePopup.value = false
      emit('request:refresh')
    }

    function jsonView(data: unknown): string {
      if (!data) return ''
      try {
        return JSON.stringify(data, null, 2)
      } catch {
        return 'Invalid data'
      }
    }

    return {
      deletePopup,
      selectedEntity,
      openDeletePopup,
      confirmDelete,
      closePopup,
      jsonView,
      viewProjet,
      goToUpdateFormView,
    }
  },
})
</script>

<style scoped></style>

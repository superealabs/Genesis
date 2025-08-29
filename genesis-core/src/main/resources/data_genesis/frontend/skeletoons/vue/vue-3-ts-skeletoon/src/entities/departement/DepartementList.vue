<template>
  <div>
    <table class="table align-middle table-rounded text-nowrap">
      <thead class="table-light">
        <tr>
          <th scope="col">Id</th>
          <th scope="col">Nom departement</th>
          <th scope="col">Code departement</th>

          <th scope="col" class="text-end">Actions</th>
        </tr>
      </thead>
      <tbody class="position-relative">
        <tr v-if="message && !loading">
          <td colspan="10" class="text-muted text-center py-4">
            {{ message }}
          </td>
        </tr>
        <DepartementRow
          v-else
          v-for="departement in data"
          :key="departement.id"
          :departement="departement"
          @request-delete="openDeletePopup"
          @request-view="viewDepartement"
          @request-update="goToUpdateFormView"
        />
        <GenesisOverlay :visible="loading" />
      </tbody>
    </table>

    <DeleteConfirmationPopup
      :visible="deletePopup"
      :message="`Are you sure you want to delete Departement ?`"
      @confirm="confirmDelete"
      @cancel="closePopup"
    />
  </div>
</template>

<script lang="ts">
import { defineComponent, ref } from 'vue'
import type { PropType } from 'vue'
import DepartementRow from './DepartementRow.vue'
import { Departement } from '@/models/DepartementModel'
import GenesisOverlay from '@/core/loading/GenesisOverlay.vue'
import DeleteConfirmationPopup from '@/core/popup/DeleteConfirmationPopup.vue'
import { usePopup } from '@/composables/usePopup'
import { useDepartements } from '@/composables/useDepartements'

export default defineComponent({
  name: 'DepartementList',
  components: { DepartementRow, GenesisOverlay, DeleteConfirmationPopup },
  props: {
    data: {
      required: true,
      type: Array as PropType<Departement[]>,
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
    const selectedEntity = ref<Partial<Departement>>({})
    const { closePopup, visible: deletePopup } = usePopup(false)
    const { deleteDepartement, viewDepartement, goToUpdateFormView } = useDepartements()

    function openDeletePopup(entity: Departement) {
      if (!entity) return
      selectedEntity.value = entity
      deletePopup.value = true
    }

    async function confirmDelete() {
      const { success, error } = await deleteDepartement(selectedEntity.value)
      deletePopup.value = false
      if (success) {
        emit('request:refresh')
      } else {
      }
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
      viewDepartement,
      goToUpdateFormView,
    }
  },
})
</script>

<style scoped></style>

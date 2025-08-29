<template>
  <div>
    <table class="table align-middle table-rounded text-nowrap">
      <thead class="table-light">
        <tr>
          <th scope="col">Id</th>
          <th scope="col">Prenom</th>
          <th scope="col">Nom</th>
          <th scope="col">Email</th>
          <th scope="col">Date embauche</th>
          <th scope="col">Salaire</th>
          <th scope="col">Departementid departements</th>

          <th scope="col" class="text-end">Actions</th>
        </tr>
      </thead>
      <tbody class="position-relative">
        <tr v-if="message && !loading">
          <td colspan="10" class="text-muted text-center py-4">
            {{ message }}
          </td>
        </tr>
        <EmployeRow
          v-else
          v-for="employe in data"
          :key="employe.id"
          :employe="employe"
          @request-delete="openDeletePopup"
          @request-view="viewEmploye"
          @request-update="goToUpdateFormView"
        />
        <GenesisOverlay :visible="loading" />
      </tbody>
    </table>

    <DeleteConfirmationPopup
      :visible="deletePopup"
      :message="`Are you sure you want to delete Employe ?`"
      @confirm="confirmDelete"
      @cancel="closePopup"
    />
  </div>
</template>

<script lang="ts">
import { defineComponent, ref } from 'vue'
import type { PropType } from 'vue'
import EmployeRow from './EmployeRow.vue'
import { Employe } from '@/models/EmployeModel'
import GenesisOverlay from '@/core/loading/GenesisOverlay.vue'
import DeleteConfirmationPopup from '@/core/popup/DeleteConfirmationPopup.vue'
import { usePopup } from '@/composables/usePopup'
import { useEmployes } from '@/composables/useEmployes'

export default defineComponent({
  name: 'EmployeList',
  components: { EmployeRow, GenesisOverlay, DeleteConfirmationPopup },
  props: {
    data: {
      required: true,
      type: Array as PropType<Employe[]>,
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
    const selectedEntity = ref<Partial<Employe>>({})
    const { closePopup, visible: deletePopup } = usePopup(false)
    const { deleteEmploye, viewEmploye, goToUpdateFormView } = useEmployes()

    function openDeletePopup(entity: Employe) {
      if (!entity) return
      selectedEntity.value = entity
      deletePopup.value = true
    }

    async function confirmDelete() {
      const { success, error } = await deleteEmploye(selectedEntity.value)
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
      viewEmploye,
      goToUpdateFormView,
    }
  },
})
</script>

<style scoped></style>

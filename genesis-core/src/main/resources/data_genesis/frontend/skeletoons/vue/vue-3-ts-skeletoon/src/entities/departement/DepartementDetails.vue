<template>
  <div class="card border-0 shadow">
    <div class="card-body">
      <div v-if="departement">
        <div class="row g-3">
          <div class="form-group col-md-6">
            <label for="departementId" class="form-label"> Id </label>
            <input
              id="departementId"
              type="text"
              class="form-control bg-white"
              readonly
              disabled
              :value="departement.id ?? ''"
            />
          </div>
          <div class="form-group col-md-6">
            <label for="departementNomDepartement" class="form-label"> Nom departement </label>
            <input
              id="departementNomDepartement"
              type="text"
              class="form-control bg-white"
              readonly
              disabled
              :value="departement.nomDepartement ?? ''"
            />
          </div>
          <div class="form-group col-md-6">
            <label for="departementCodeDepartement" class="form-label"> Code departement </label>
            <input
              id="departementCodeDepartement"
              type="text"
              class="form-control bg-white"
              readonly
              disabled
              :value="departement.codeDepartement ?? ''"
            />
          </div>
        </div>
        <!-- Action buttons -->
        <div class="row g-3 mt-4 justify-content-end">
          <div class="col-auto">
            <GenesisButton
              icon="bi bi-trash me-2"
              @click="openDeletePopup(departement)"
              class="btn-outline-danger"
              label="Delete"
            />
          </div>
          <div class="col-auto">
            <GenesisButton
              icon="bi bi-pen me-2"
              @click="goToUpdateFormView(departement)"
              class="btn-outline-dark"
              label="Update"
            />
          </div>
        </div>
      </div>
      <p v-else>Chargement...</p>
    </div>
  </div>

  <!-- Delete confirmation popup -->
  <DeleteConfirmationPopup
    :visible="deletePopup"
    :message="`Êtes-vous sûr de vouloir supprimer Departement \departement.id} ?`"
    subMessage="Cette action est irréversible."
    @confirm="confirmDelete"
    @cancel="closePopup"
  />
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import type { Departement } from '@/models/DepartementModel'
import GenesisButton from '@/core/button/GenesisButton.vue'
import { usePopup } from '@/composables/usePopup'
import { useDepartements } from '@/composables/useDepartements'
import DeleteConfirmationPopup from '@/core/popup/DeleteConfirmationPopup.vue'

export default defineComponent({
  name: 'DepartementDetails',
  components: { GenesisButton, DeleteConfirmationPopup },
  props: {
    departement: {
      required: true,
      type: Object as PropType<Departement>,
    },
  },
  setup(props) {
    const { closePopup, openPopup, visible: deletePopup } = usePopup(false)
    const { deleteDepartement, goToListView, goToUpdateFormView } = useDepartements()

    function openDeletePopup(entity: Departement) {
      if (!entity) return
      openPopup()
    }

    async function confirmDelete() {
      const { success, error } = await deleteDepartement(props.departement)
      if (!success) {
        console.error(error)
      }
      closePopup()
      goToListView()
    }

    return {
      deletePopup,
      openDeletePopup,
      confirmDelete,
      closePopup,
      goToUpdateFormView,
    }
  },
})
</script>

<style scoped></style>

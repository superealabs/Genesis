<template>
  <div class="card border-0 shadow">
    <div class="card-body">
      <div v-if="employe">
        <div class="row g-3">
          <div class="form-group col-md-6">
            <label for="employeId" class="form-label"> Id </label>
            <input
              id="employeId"
              type="text"
              class="form-control bg-white"
              readonly
              disabled
              :value="employe.id ?? ''"
            />
          </div>
          <div class="form-group col-md-6">
            <label for="employePrenom" class="form-label"> Prenom </label>
            <input
              id="employePrenom"
              type="text"
              class="form-control bg-white"
              readonly
              disabled
              :value="employe.prenom ?? ''"
            />
          </div>
          <div class="form-group col-md-6">
            <label for="employeNom" class="form-label"> Nom </label>
            <input
              id="employeNom"
              type="text"
              class="form-control bg-white"
              readonly
              disabled
              :value="employe.nom ?? ''"
            />
          </div>
          <div class="form-group col-md-6">
            <label for="employeEmail" class="form-label"> Email </label>
            <input
              id="employeEmail"
              type="text"
              class="form-control bg-white"
              readonly
              disabled
              :value="employe.email ?? ''"
            />
          </div>
          <div class="form-group col-md-6">
            <label for="employeDateEmbauche" class="form-label"> Date embauche </label>
            <input
              id="employeDateEmbauche"
              type="text"
              class="form-control bg-white"
              readonly
              disabled
              :value="employe.dateEmbauche ?? ''"
            />
          </div>
          <div class="form-group col-md-6">
            <label for="employeSalaire" class="form-label"> Salaire </label>
            <input
              id="employeSalaire"
              type="text"
              class="form-control bg-white"
              readonly
              disabled
              :value="employe.salaire ?? ''"
            />
          </div>
          <div class="form-group col-md-6">
            <label for="employeDepartementidDepartements" class="form-label">
              Departementid departements
            </label>
            <input
              id="employeDepartementidDepartements"
              type="text"
              class="form-control bg-white"
              readonly
              disabled
              :value="employe.departementidDepartements ?? ''"
            />
          </div>
        </div>
        <!-- Action buttons -->
        <div class="row g-3 mt-4 justify-content-end">
          <div class="col-auto">
            <GenesisButton
              icon="bi bi-trash me-2"
              @click="openDeletePopup(employe)"
              class="btn-outline-danger"
              label="Delete"
            />
          </div>
          <div class="col-auto">
            <GenesisButton
              icon="bi bi-pen me-2"
              @click="goToUpdateFormView(employe)"
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
    :message="`Êtes-vous sûr de vouloir supprimer Employe \employe.id} ?`"
    subMessage="Cette action est irréversible."
    @confirm="confirmDelete"
    @cancel="closePopup"
  />
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { Employe } from '@/models/EmployeModel'
import GenesisButton from '@/core/button/GenesisButton.vue'
import { usePopup } from '@/composables/usePopup'
import { useEmployes } from '@/composables/useEmployes'
import DeleteConfirmationPopup from '@/core/popup/DeleteConfirmationPopup.vue'

export default defineComponent({
  name: 'EmployeDetails',
  components: { GenesisButton, DeleteConfirmationPopup },
  props: {
    employe: {
      required: true,
      type: Object as PropType<Employe>,
    },
  },
  setup(props) {
    const { closePopup, openPopup, visible: deletePopup } = usePopup(false)
    const { deleteEmploye, goToListView, goToUpdateFormView } = useEmployes()

    function openDeletePopup(entity: Employe) {
      if (!entity) return
      openPopup()
    }

    async function confirmDelete() {
      const { success, error } = await deleteEmploye(props.employe)
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

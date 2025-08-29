<template>
  <div class="card border-0 shadow">
    <div class="card-body">
      <div v-if="projet">
        <div class="row g-3">
          <div class="form-group col-md-6">
            <label for="projetId" class="form-label"> Id </label>
            <input
              id="projetId"
              type="text"
              class="form-control bg-white"
              readonly
              disabled
              :value="projet.id ?? ''"
            />
          </div>
          <div class="form-group col-md-6">
            <label for="projetNomProjet" class="form-label"> Nom projet </label>
            <input
              id="projetNomProjet"
              type="text"
              class="form-control bg-white"
              readonly
              disabled
              :value="projet.nomProjet ?? ''"
            />
          </div>
          <div class="form-group col-md-6">
            <label for="projetBudget" class="form-label"> Budget </label>
            <input
              id="projetBudget"
              type="text"
              class="form-control bg-white"
              readonly
              disabled
              :value="projet.budget ?? ''"
            />
          </div>
          <div class="form-group col-md-6">
            <label for="projetDateDebut" class="form-label"> Date debut </label>
            <input
              id="projetDateDebut"
              type="text"
              class="form-control bg-white"
              readonly
              disabled
              :value="projet.dateDebut ?? ''"
            />
          </div>
          <div class="form-group col-md-6">
            <label for="projetDateFinPrevue" class="form-label"> Date fin prevue </label>
            <input
              id="projetDateFinPrevue"
              type="text"
              class="form-control bg-white"
              readonly
              disabled
              :value="projet.dateFinPrevue ?? ''"
            />
          </div>
        </div>
        <!-- Action buttons -->
        <div class="row g-3 mt-4 justify-content-end">
          <div class="col-auto">
            <GenesisButton
              icon="bi bi-trash me-2"
              @click="openDeletePopup(projet)"
              class="btn-outline-danger"
              label="Delete"
            />
          </div>
          <div class="col-auto">
            <GenesisButton
              icon="bi bi-pen me-2"
              @click="goToUpdateFormView(projet)"
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
    :message="`Êtes-vous sûr de vouloir supprimer Projet \projet.id} ?`"
    subMessage="Cette action est irréversible."
    @confirm="confirmDelete"
    @cancel="closePopup"
  />
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { Projet } from '@/models/ProjetModel'
import GenesisButton from '@/core/button/GenesisButton.vue'
import { usePopup } from '@/composables/usePopup'
import { useProjets } from '@/composables/useProjets'
import DeleteConfirmationPopup from '@/core/popup/DeleteConfirmationPopup.vue'

export default defineComponent({
  name: 'ProjetDetails',
  components: { GenesisButton, DeleteConfirmationPopup },
  props: {
    projet: {
      required: true,
      type: Object as PropType<Projet>,
    },
  },
  setup(props) {
    const { closePopup, openPopup, visible: deletePopup } = usePopup(false)
    const { deleteProjet, goToListView, goToUpdateFormView } = useProjets()

    function openDeletePopup(entity: Projet) {
      if (!entity) return
      openPopup()
    }

    async function confirmDelete() {
      const { success, error } = await deleteProjet(props.projet)
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

<template>
  <div class="card border-0 shadow-sm">
    <div class="card-body">
      <div v-if="tache">
        <div class="row g-3">
          <!-- ID -->
          <div class="form-group col-md-6">
            <label for="tacheId" class="form-label">ID</label>
            <input
              id="tacheId"
              type="text"
              class="form-control bg-white"
              readonly
              disabled
              :value="tache.id ?? ''"
            />
          </div>

          <!-- Titre -->
          <div class="form-group col-md-6">
            <label for="tacheTitre" class="form-label">Titre</label>
            <input
              id="tacheTitre"
              type="text"
              class="form-control bg-white"
              readonly
              disabled
              :value="tache.titre ?? ''"
            />
          </div>

          <!-- Description -->
          <div class="form-group col-md-6">
            <label for="tacheDescription" class="form-label">Description</label>
            <textarea
              id="tacheDescription"
              class="form-control bg-white"
              rows="2"
              readonly
              disabled
              :value="tache.description ?? ''"
            ></textarea>
          </div>

          <!-- Priorité -->
          <div class="form-group col-md-6">
            <label for="tachePriorite" class="form-label">Priorité</label>
            <input
              id="tachePriorite"
              type="text"
              class="form-control bg-white"
              readonly
              disabled
              :value="tache.priorite ?? ''"
            />
          </div>

          <!-- Projet associé -->
          <div class="form-group col-md-6">
            <label for="tacheProjet" class="form-label">Projet (ID)</label>
            <input
              id="tacheProjet"
              type="text"
              class="form-control bg-white"
              readonly
              disabled
              :value="tache.projetidProjets?.id ?? ''"
            />
          </div>

          <!-- Employé assigné -->
          <div class="form-group col-md-6">
            <label for="tacheEmploye" class="form-label">
              Employé assigné (ID)
            </label>
            <input
              id="tacheEmploye"
              type="text"
              class="form-control bg-white"
              readonly
              disabled
              :value="tache.assigneaidEmployes?.id ?? ''"
            />
          </div>
        </div>
        <!-- Bouton supprimer -->
        <div class="row g-3 mt-4 justify-content-end">
          <div class="col-auto">
            <GenesisButton
              icon="bi bi-trash me-2"
              @click="openDeletePopup(tache)"
              class="btn-outline-danger"
              label="Delete"
            />
          </div>
          <div class="col-auto">
            <GenesisButton
              icon="bi bi-pen me-2"
              @click="goToUpdateFormView(tache)"
              class="btn-outline-dark"
              label="Update"
            />
          </div>
        </div>
      </div>
      <p v-else>Chargement...</p>
    </div>
  </div>

  <!-- Popup confirmation -->
  <DeleteConfirmationPopup
    :visible="deletePopup"
    :message="`Êtes-vous sûr de vouloir supprimer Tâche ${tache.id} ?`"
    subMessage="Cette action est irréversible."
    @confirm="confirmDelete"
    @cancel="closePopup"
  />
</template>

<script lang="ts">
import { defineComponent, PropType } from "vue";
import type { Tache } from "@/models/TacheModel";
import GenesisButton from "@/core/button/GenesisButton.vue";
import { usePopup } from "@/composables/usePopup";
import { useTaches } from "@/composables/useTaches";
import DeleteConfirmationPopup from "@/core/popup/DeleteConfirmationPopup.vue";

export default defineComponent({
  name: "TacheDetails",
  components: { GenesisButton, DeleteConfirmationPopup },
  props: {
    tache: {
      required: true,
      type: Object as PropType<Tache>,
    },
  },
  setup(props) {
    const { closePopup, openPopup, visible: deletePopup } = usePopup(false);
    const { deleteTache, goToListView, goToUpdateFormView } = useTaches();

    function openDeletePopup(entity: Tache) {
      if (!entity) return;
      openPopup();
    }

    async function confirmDelete() {
      const { success, error } = await deleteTache(props.tache);
      if (!success) {
        console.error(error);
      }
      closePopup();
      goToListView();
    }

    return {
      deletePopup,
      openDeletePopup,
      confirmDelete,
      closePopup,
      goToUpdateFormView,
    };
  },
});
</script>

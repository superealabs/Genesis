<template>
  <div class="card bg-base-100">
    <div class="card-body">
      <div v-if="tache">
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <!-- Id -->
          <div>
            <label for="tacheId" class="block text-sm font-medium mb-1">Id</label>
            <input
              id="tacheId"
              type="text"
              class="input w-full bg-base-100"
              readonly
              :value="tache.id ?? ''"
            />
          </div>

          <!-- Titre -->
          <div>
            <label for="tacheTitre" class="block text-sm font-medium mb-1">Titre</label>
            <input
              id="tacheTitre"
              type="text"
              class="input w-full bg-base-100"
              readonly
              :value="tache.titre ?? ''"
            />
          </div>

          <!-- Description -->
          <div>
            <label for="tacheDescription" class="block text-sm font-medium mb-1">Description</label>
            <input
              id="tacheDescription"
              type="text"
              class="input w-full bg-base-100"
              readonly
              :value="tache.description ?? ''"
            />
          </div>

          <!-- Priorité -->
          <div>
            <label for="tachePriorite" class="block text-sm font-medium mb-1">Priorité</label>
            <input
              id="tachePriorite"
              type="text"
              class="input w-full bg-base-100"
              readonly
              :value="tache.priorite ?? ''"
            />
          </div>

          <!-- Projet -->
          <div>
            <label for="tacheProjetidProjets" class="block text-sm font-medium mb-1">Projet</label>
            <input
              id="tacheProjetidProjets"
              type="text"
              class="input w-full bg-base-100"
              readonly
              :value="tache.projetidProjets?.getReferenceValue() ?? ''"
            />
          </div>

          <!-- Employé assigné -->
          <div>
            <label for="tacheAssigneaidEmployes" class="block text-sm font-medium mb-1"
              >Assigné à</label
            >
            <input
              id="tacheAssigneaidEmployes"
              type="text"
              class="input w-full bg-base-100"
              readonly
              :value="tache.assigneaidEmployes?.getReferenceValue() ?? ''"
            />
          </div>
        </div>

        <!-- Action buttons -->
        <div class="flex justify-end gap-3 mt-6">
          <GenesisButton
            @click="openDeletePopup(tache)"
            class="btn-outline btn-error hover:text-white"
          >
            <TrashIcon class="mr-2" />
            <span>Delete</span>
          </GenesisButton>
          <GenesisButton @click="goToUpdateFormView(tache)" class="btn btn-outline btn-neutral">
            <EditIcon class="mr-2" />
            <span>Update</span>
          </GenesisButton>
        </div>
      </div>
      <p v-else class="text-center text-gray-500">Chargement...</p>
    </div>
  </div>

  <!-- Delete confirmation popup -->
  <DeleteConfirmationPopup
    :visible="deletePopup"
    :message="`Êtes-vous sûr de vouloir supprimer la Tâche ${tache?.id} ?`"
    subMessage="Cette action est irréversible."
    @confirm="confirmDelete"
    @cancel="closePopup"
  />
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import type { PropType } from 'vue'
import { Tache } from '@/models/TacheModel'
import GenesisButton from '@/core/button/GenesisButton.vue'
import { usePopup } from '@/composables/usePopup'
import { useTaches } from '@/composables/useTaches'
import DeleteConfirmationPopup from '@/core/popup/DeleteConfirmationPopup.vue'
import { useObjectUtils } from '@/composables/useObjectUtils'
import TrashIcon from '@/core/icons/TrashIcon.vue'
import EditIcon from '@/core/icons/EditIcon.vue'

export default defineComponent({
  name: 'TacheDetails',
  components: { GenesisButton, DeleteConfirmationPopup, TrashIcon, EditIcon },
  props: {
    tache: {
      required: true,
      type: Object as PropType<Tache>,
    },
  },
  setup(props) {
    const { closePopup, openPopup, visible: deletePopup } = usePopup(false)
    const { deleteTache, goToListView, goToUpdateFormView } = useTaches()
    const { getSecondValue } = useObjectUtils()

    function openDeletePopup(entity: Tache) {
      if (!entity) return
      openPopup()
    }

    async function confirmDelete() {
      const { success, error } = await deleteTache(props.tache)
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
      getSecondValue,
    }
  },
})
</script>

<style scoped></style>

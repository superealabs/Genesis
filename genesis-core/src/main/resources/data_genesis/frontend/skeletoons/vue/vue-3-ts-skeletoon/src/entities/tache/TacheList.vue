<template>
  <div class="">
    <table class="table align-middle table-rounded text-nowrap">
      <thead class="table-light">
        <tr>
          <th scope="col">Id</th>
          <th scope="col">Titre</th>
          <th scope="col">Description</th>
          <th scope="col">Priorite</th>
          <th scope="col">Projetid projets</th>
          <th scope="col">Assigneaid employes</th>
          <th scope="col" class="text-end">Actions</th>
        </tr>
      </thead>
      <tbody class="position-relative">
        <tr v-if="message && !loading">
          <td colspan="10" class="text-muted text-center py-4">
            {{ message }}
          </td>
        </tr>
        <TacheRow
          v-else
          v-for="tache in data"
          :key="tache.id"
          :tache="tache"
          @request-delete="openDeletePopup"
          @request-view="viewTache"
          @request-update="goToUpdateFormView"
        />
        <GenesisOverlay :visible="loading"></GenesisOverlay>
      </tbody>
    </table>
  </div>
  <DeleteConfirmationPopup
    :visible="deletePopup"
    :message="`Are you sure you want to delete Tâche ${selectedEntity?.id}?`"
    @confirm="confirmDelete"
    @cancel="closePopup"
  />
</template>

<script lang="ts">
import { defineComponent, PropType } from "vue";
import TacheRow from "./TacheRow.vue";
import { Tache } from "@/models/TacheModel";
import GenesisOverlay from "@/core/loading/GenesisOverlay.vue";
import { ref } from "vue";
import { usePopup } from "@/composables/usePopup";
import { useTaches } from "@/composables/useTaches";
import DeleteConfirmationPopup from "@/core/popup/DeleteConfirmationPopup.vue";

export default defineComponent({
  name: "TacheList",
  components: { TacheRow, GenesisOverlay, DeleteConfirmationPopup },
  props: {
    data: {
      required: true,
      type: Array as PropType<Tache[]>,
    },
    message: {
      required: false,
      type: String,
    },
    loading: {
      type: Boolean,
      required: false,
      defauld: false,
    },
  },
  emits: ["request:refresh"],
  setup(props, { emit }) {
    function jsonView(data: unknown): string {
      if (!data) return "";
      try {
        return JSON.stringify(data, null, 2); // pretty print with 2 spaces indent
      } catch {
        return "Invalid data";
      }
    }

    const selectedEntity = ref<Partial<Tache>>({});

    const { closePopup, visible: deletePopup } = usePopup(false);
    const { deleteTache, viewTache, goToUpdateFormView } = useTaches();

    function openDeletePopup(entity: Tache) {
      if (!entity) {
        return;
      }
      selectedEntity.value = entity;
      deletePopup.value = true;
    }

    async function confirmDelete() {
      // call your API or emit event to delete selectedEntity.value
      const { success, error } = await deleteTache(selectedEntity.value);
      deletePopup.value = false;
      emit("request:refresh");
    }
    return {
      deletePopup,
      selectedEntity,
      openDeletePopup,
      confirmDelete,
      closePopup,
      jsonView,
      viewTache,
      goToUpdateFormView,
    };
  },
});
</script>

<style scoped>
.table-container {
  position: relative;
}

.loading-overlay {
  background-color: rgba(255, 255, 255, 0.7);
  z-index: 10;
}

.loading-text {
  font-size: 1.2rem;
  font-weight: bold;
  color: #333;
}
</style>

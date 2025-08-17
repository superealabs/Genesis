<template>
  <div class="container-fluid">
    <div class="d-flex mb-4 justify-content-between align-items-center">
      <div>
        <h3>Tache / <span class="text-muted">Update</span></h3>
      </div>
    </div>

    <div v-if="entity">
      <tache-form
        :tache="entity"
        submit-label="Update Tache"
        @submit="updateHandler"
        @cancel="viewTache"
      ></tache-form>
    </div>
    <AlertPopup
      :message="message ?? undefined"
      :title="Error 500"
      :visible="alertPopup"
      @close="closePopup"
    ></AlertPopup>

    <div velse></div>
  </div>
</template>

<script lang="ts">
import TacheForm from "@/entities/tache/TacheForm.vue";
import { defineComponent, ref, onMounted } from "vue";
import { useRoute } from "vue-router";
import { useTaches } from "@/composables/useTaches";
import { TacheFormDTO } from "@/models/TacheModel";
import { useLoading } from "@/composables/useLoading";
import AlertPopup from "@/core/popup/AlertPopup.vue";
import { usePopup } from "@/composables/usePopup";

export default defineComponent({
  components: { TacheForm, AlertPopup },
  setup() {
    // Global usage
    const route = useRoute();
    const pathId = Number(route.params.id);
    const { loading, startLoading, stopLoading } = useLoading();
    const { closePopup, openPopup, visible: alertPopup } = usePopup();

    // Logics
    const { getTacheById, goToListView, updateTache, viewTache, message } =
      useTaches();
    const entity = ref<TacheFormDTO | null>(null);
    const updateHandler = async (tacheFormDTO: Partial<TacheFormDTO>) => {
      startLoading();
      const data = await updateTache(pathId, tacheFormDTO);
      if (data && !message.value) {
        viewTache(data);
      } else {
        openPopup();
      }
      stopLoading();
    };

    onMounted(async () => {
      const result = await getTacheById(pathId);
      entity.value = TacheFormDTO.parseTache(result.data) || null;
    });

    return {
      entity,
      route,
      goToListView,
      updateTache,
      viewTache,
      updateHandler,
      loading,
      message,
      closePopup,
      alertPopup,
    };
  },
});
</script>

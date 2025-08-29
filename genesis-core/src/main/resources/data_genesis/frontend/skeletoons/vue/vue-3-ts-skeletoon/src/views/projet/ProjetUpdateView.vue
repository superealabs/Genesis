<template>
  <div class="container-fluid">
    <div class="d-flex mb-4 justify-content-between align-items-center">
      <div>
        <h3 class="text-dark">
          Projet / <span class="text-muted">Update</span>
        </h3>
      </div>
    </div>

    <div v-if="entity">
      <projet-form
        :projet="entity"
        submit-label="Update Projet"
        @submit="updateHandler"
        @cancel="viewProjet"
      ></projet-form>
    </div>

    <AlertPopup
      :message="message ?? undefined"
      title="Error 500"
      :visible="alertPopup"
      @close="closePopup"
    ></AlertPopup>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref, onMounted } from "vue";
import { useRoute } from "vue-router";
import ProjetForm from "@/entities/projet/ProjetForm.vue";
import AlertPopup from "@/core/popup/AlertPopup.vue";
import { useProjets } from "@/composables/useProjets";
import { ProjetFormDTO } from "@/models/ProjetModel";
import { useLoading } from "@/composables/useLoading";
import { usePopup } from "@/composables/usePopup";

export default defineComponent({
  name: "ProjetUpdateView",
  components: { ProjetForm, AlertPopup },
  setup() {
    const route = useRoute();
    const pathId = Number(route.params.id);
    const { loading, startLoading, stopLoading } = useLoading();
    const { closePopup, openPopup, visible: alertPopup } = usePopup();

    const { getProjetById, goToListView, updateProjet, viewProjet, message } =
      useProjets();
    const entity = ref<ProjetFormDTO | null>(null);

    const updateHandler = async (formDTO: Partial<ProjetFormDTO>) => {
      startLoading();
      const data = await updateProjet(pathId, formDTO);
      if (data && !message.value) {
        viewProjet(data);
      } else {
        openPopup();
      }
      stopLoading();
    };

    onMounted(async () => {
      const result = await getProjetById(pathId);
      entity.value = ProjetFormDTO.parse(result.data) || null;
    });

    return {
      entity,
      route,
      goToListView,
      updateProjet,
      viewProjet,
      updateHandler,
      loading,
      message,
      closePopup,
      alertPopup,
    };
  },
});
</script>

<style scoped></style>

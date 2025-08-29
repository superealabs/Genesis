<template>
  <div class="container-fluid">
    <div class="d-flex mb-4 justify-content-between align-items-center">
      <div>
        <h3 class="text-dark">
          Departement / <span class="text-muted">Update</span>
        </h3>
      </div>
    </div>

    <div v-if="entity">
      <departement-form
        :departement="entity"
        submit-label="Update Departement"
        @submit="updateHandler"
        @cancel="viewDepartement"
      ></departement-form>
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
import DepartementForm from "@/entities/departement/DepartementForm.vue";
import AlertPopup from "@/core/popup/AlertPopup.vue";
import { useDepartements } from "@/composables/useDepartements";
import { DepartementFormDTO } from "@/models/DepartementModel";
import { useLoading } from "@/composables/useLoading";
import { usePopup } from "@/composables/usePopup";

export default defineComponent({
  name: "DepartementUpdateView",
  components: { DepartementForm, AlertPopup },
  setup() {
    const route = useRoute();
    const pathId = Number(route.params.id);
    const { loading, startLoading, stopLoading } = useLoading();
    const { closePopup, openPopup, visible: alertPopup } = usePopup();

    const {
      getDepartementById,
      goToListView,
      updateDepartement,
      viewDepartement,
      message,
    } = useDepartements();
    const entity = ref<DepartementFormDTO | null>(null);

    const updateHandler = async (formDTO: Partial<DepartementFormDTO>) => {
      startLoading();
      const data = await updateDepartement(pathId, formDTO);
      if (data && !message.value) {
        viewDepartement(data);
      } else {
        openPopup();
      }
      stopLoading();
    };

    onMounted(async () => {
      const result = await getDepartementById(pathId);
      entity.value = DepartementFormDTO.parse(result.data) || null;
    });

    return {
      entity,
      route,
      goToListView,
      updateDepartement,
      viewDepartement,
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

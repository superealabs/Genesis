<template>
  <div class="container-fluid">
    <div class="d-flex mb-4 justify-content-between align-items-center">
      <div>
        <h3 class="text-dark">
          Employe / <span class="text-muted">Update</span>
        </h3>
      </div>
    </div>

    <div v-if="entity">
      <employe-form
        :employe="entity"
        submit-label="Update Employe"
        @submit="updateHandler"
        @cancel="viewEmploye"
      ></employe-form>
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
import EmployeForm from "@/entities/employe/EmployeForm.vue";
import AlertPopup from "@/core/popup/AlertPopup.vue";
import { useEmployes } from "@/composables/useEmployes";
import { EmployeFormDTO } from "@/models/EmployeModel";
import { useLoading } from "@/composables/useLoading";
import { usePopup } from "@/composables/usePopup";

export default defineComponent({
  name: "EmployeUpdateView",
  components: { EmployeForm, AlertPopup },
  setup() {
    const route = useRoute();
    const pathId = Number(route.params.id);
    const { loading, startLoading, stopLoading } = useLoading();
    const { closePopup, openPopup, visible: alertPopup } = usePopup();

    const {
      getEmployeById,
      goToListView,
      updateEmploye,
      viewEmploye,
      message,
    } = useEmployes();
    const entity = ref<EmployeFormDTO | null>(null);

    const updateHandler = async (formDTO: Partial<EmployeFormDTO>) => {
      startLoading();
      const data = await updateEmploye(pathId, formDTO);
      if (data && !message.value) {
        viewEmploye(data);
      } else {
        openPopup();
      }
      stopLoading();
    };

    onMounted(async () => {
      const result = await getEmployeById(pathId);
      entity.value = EmployeFormDTO.parse(result.data) || null;
    });

    return {
      entity,
      route,
      goToListView,
      updateEmploye,
      viewEmploye,
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

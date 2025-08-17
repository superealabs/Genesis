<template>
  <div class="container-fluid">
    <div class="d-flex mb-4 justify-content-between align-items-center">
      <div>
        <h3>Tache / <span class="text-muted">New</span></h3>
      </div>
    </div>

    <tache-form
      submit-label="Save Tache"
      :tache="tacheFormDto"
      @submit="createHandler"
      @cancel="goToListView"
    />

    <AlertPopup
      :message="message ?? undefined"
      title="Error 500"
      :visible="alertPopup"
      @close="closePopup"
    />
  </div>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import { useTaches } from "@/composables/useTaches";
import TacheForm from "@/entities/tache/TacheForm.vue";
import { TacheFormDTO } from "@/models/TacheModel";
import AlertPopup from "@/core/popup/AlertPopup.vue";
import { usePopup } from "@/composables/usePopup";
import { useFreezeScreen } from "@/store/useFreezeScreen";

export default defineComponent({
  name: "TacheCreateView",
  components: { TacheForm, AlertPopup },
  setup() {
    const tacheFormDto = new TacheFormDTO();
    const { createTache, goToListView, message } = useTaches();
    const { openPopup, closePopup, visible: alertPopup } = usePopup();
    const { freeze, unfreeze } = useFreezeScreen();

    const createHandler = async (tacheFormDTO: Partial<TacheFormDTO>) => {
      freeze("Create a new Tache...");
      useFreezeScreen().debug("TacheCreateView");
      // const data = await createTache(tacheFormDTO);
      try {
        // if (data && !message.value) {
        // goToListView();
        // } else {
        // throw new Error();
        // }
      } catch (error) {
        openPopup();
      } finally {
        // unfreeze();
      }
    };

    return {
      tacheFormDto,
      createHandler,
      goToListView,
      message,
      alertPopup,
      closePopup,
    };
  },
});
</script>

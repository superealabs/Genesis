<template>
  <div class="container-fluid">
    <div class="mb-4 d-flex justify-content-between align-items-center">
      <div>
        <h3 class="text-dark">
          Departement / <span class="text-muted">Details</span>
        </h3>
      </div>
      <div>
        <GenesisButton
          icon="bi bi-arrow-left me-2"
          title="Go back to list view"
          @click="goToListView"
        >
          <span>Back to list</span>
        </GenesisButton>
      </div>
    </div>
    <DepartementDetails v-if="entity" :departement="entity" />
    <p v-else>Chargement...</p>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref, onMounted } from "vue";
import { useRoute } from "vue-router";
import DepartementDetails from "@/entities/departement/DepartementDetails.vue";
import { useDepartements } from "@/composables/useDepartements";
import type { Departement } from "@/models/DepartementModel";
import GenesisButton from "@/core/button/GenesisButton.vue";

export default defineComponent({
  name: "DepartementDetailsView",
  components: {
    DepartementDetails,
    GenesisButton,
  },
  setup() {
    const route = useRoute();
    const { getDepartementById, goToListView } = useDepartements();
    const entity = ref<Departement | null>(null);

    onMounted(async () => {
      const result = await getDepartementById(Number(route.params.id));
      entity.value = result.data || null;
    });

    return { entity, route, goToListView };
  },
});
</script>

<style scoped></style>

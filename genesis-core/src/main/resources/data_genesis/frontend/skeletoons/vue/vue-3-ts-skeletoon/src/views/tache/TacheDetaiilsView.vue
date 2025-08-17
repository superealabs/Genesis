<template>
  <div class="container-fluid">
    <div class="mb-4 d-flex justify-content-between align-items-center">
      <div>
        <h3>Tache / <span class="text-muted">Details</span></h3>
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
    <TacheDetails v-if="entity" :tache="entity" />
    <p v-else>Chargement...</p>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref, onMounted } from "vue";
import { useRoute } from "vue-router";
import TacheDetails from "@/entities/tache/TacheDetails.vue";
import { useTaches } from "@/composables/useTaches";
import type { Tache } from "@/models/TacheModel";
import GenesisButton from "@/core/button/GenesisButton.vue";

export default defineComponent({
  components: { TacheDetails, GenesisButton },
  setup() {
    const route = useRoute();
    const { getTacheById, goToListView } = useTaches();
    const entity = ref<Tache | null>(null);

    onMounted(async () => {
      const result = await getTacheById(Number(route.params.id));
      entity.value = result.data || null;
    });

    return { entity, route, goToListView };
  },
});
</script>

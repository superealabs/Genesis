<template>
  <div class="">
    <div class="d-flex justify-content-between align-items-center">
      <div>
        <h3>Projets / <span class="text-muted">List</span></h3>
      </div>
      <div>
        <GenesisAddButton>
          <span>Add Projets</span>
        </GenesisAddButton>
      </div>
    </div>
    <div class="mt-3"></div>
    <ProjetsList :projetss="filteredProjetss" />

    <PaginationLayout :start="1" :end="100" />
  </div>
</template>

<script lang="ts">
import { computed, defineComponent, ref } from "vue";
import { Projets } from "@/models/ProjetsModel";
import ProjetsList from "@/entities/projets/ProjetsList.vue";
import GenesisAddButton from "@/core/button/GenesisAddButton.vue";
import PaginationLayout from "@/core/pagination/PaginationLayout.vue";

export default defineComponent({
  name: "ProjetsListView",
  components: {
    ProjetsList,
    GenesisAddButton,
    PaginationLayout,
  },
  setup() {
    const projetss = ref<Projets[]>([]);
    const model = new Projets();
    const filters = ref();

    const filteredProjetss = computed(() => {
      if (!filters.value) {
        console.log("Initial Values");
        return projetss.value;
      }
      console.log("Filfil");

      return [...projetss.value];
    });

    const handleSearch = (filterValues: Record<string, any>) => {
      filters.value = filterValues;
    };

    return { filteredProjetss, handleSearch, model };
  },
});
</script>

<style scoped></style>

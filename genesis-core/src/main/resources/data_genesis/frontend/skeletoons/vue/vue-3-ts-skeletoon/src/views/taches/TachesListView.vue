<template>
  <div class="">
    <div class="d-flex justify-content-between align-items-center">
      <div>
        <h3>Taches / <span class="text-muted">List</span></h3>
      </div>
      <div>
        <GenesisAddButton>
          <span>Add Taches</span>
        </GenesisAddButton>
      </div>
    </div>
    <div class="mt-3"></div>
    <TachesList :tachess="filteredTachess" />

    <PaginationLayout :start="1" :end="100" />
  </div>
</template>

<script lang="ts">
import { computed, defineComponent, ref } from "vue";
import { Taches } from "@/models/TachesModel";
import TachesList from "@/entities/taches/TachesList.vue";
import GenesisAddButton from "@/core/button/GenesisAddButton.vue";
import PaginationLayout from "@/core/pagination/PaginationLayout.vue";

export default defineComponent({
  name: "TachesListView",
  components: {
    TachesList,
    GenesisAddButton,
    PaginationLayout,
  },
  setup() {
    const tachess = ref<Taches[]>([]);
    const model = new Taches();
    const filters = ref();

    const filteredTachess = computed(() => {
      if (!filters.value) {
        console.log("Initial Values");
        return tachess.value;
      }
      console.log("Filfil");

      return [...tachess.value];
    });

    const handleSearch = (filterValues: Record<string, any>) => {
      filters.value = filterValues;
    };

    return { filteredTachess, handleSearch, model };
  },
});
</script>

<style scoped></style>

<template>
  <div class="">
    <div class="d-flex justify-content-between align-items-center">
      <div>
        <h3>Employes / <span class="text-muted">List</span></h3>
      </div>
      <div>
        <GenesisAddButton>
          <span>Add Employes</span>
        </GenesisAddButton>
      </div>
    </div>
    <div class="mt-3"></div>
    <EmployesList :employess="filteredEmployess" />

    <PaginationLayout :start="1" :end="100" />
  </div>
</template>

<script lang="ts">
import { computed, defineComponent, ref } from "vue";
import { Employes } from "@/models/EmployesModel";
import EmployesList from "@/entities/employes/EmployesList.vue";
import GenesisAddButton from "@/core/button/GenesisAddButton.vue";
import PaginationLayout from "@/core/pagination/PaginationLayout.vue";

export default defineComponent({
  name: "EmployesListView",
  components: {
    EmployesList,
    GenesisAddButton,
    PaginationLayout,
  },
  setup() {
    const employess = ref<Employes[]>([]);
    const model = new Employes();
    const filters = ref();

    const filteredEmployess = computed(() => {
      if (!filters.value) {
        console.log("Initial Values");
        return employess.value;
      }
      console.log("Filfil");

      return [...employess.value];
    });

    const handleSearch = (filterValues: Record<string, any>) => {
      filters.value = filterValues;
    };

    return { filteredEmployess, handleSearch, model };
  },
});
</script>

<style scoped></style>

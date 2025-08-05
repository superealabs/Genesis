<template>
  <div>
    <div class="d-flex justify-content-between align-items-center">
      <div>
        <h3>Departements / <span class="text-muted">List</span></h3>
      </div>
      <div>
        <GenesisAddButton>
          <span>Add Departements</span>
        </GenesisAddButton>
      </div>
    </div>

    <div class="mt-3">
      <DepartementSpecification @search="handleSearch" />
    </div>

    <DepartementsList class="mt-3" />

    <PaginationLayout :start="1" :end="100" />
  </div>
</template>

<script lang="ts">
import { defineComponent, ref, computed } from "vue";
import { Departements } from "@/models/DepartementsModel";
import DepartementsList from "@/entities/departements/DepartementsList.vue";
import GenesisAddButton from "@/core/button/GenesisAddButton.vue";
import PaginationLayout from "@/core/pagination/PaginationLayout.vue";
import DepartementSpecification from "@/entities/departements/DepartementSpecification.vue";

export default defineComponent({
  name: "DepartementsListView",
  components: {
    DepartementsList,
    DepartementSpecification,
    GenesisAddButton,
    PaginationLayout,
  },
  setup() {
    const departementss = ref<Departements[]>([]);
    const filters = ref<Record<string, any> | null>(null);

    const filteredDepartementss = computed(() => {
      if (!filters.value) return departementss.value;

      return departementss.value.filter((dep) =>
        Object.entries(filters.value!).every(([key, val]) =>
          dep[key as keyof Departements]
            ?.toString()
            .toLowerCase()
            .includes(val.toString().toLowerCase())
        )
      );
    });

    const handleSearch = (filterValues: Record<string, any>) => {
      filters.value = filterValues;
    };

    const model = new Departements();

    return { filteredDepartementss, handleSearch, model };
  },
});
</script>

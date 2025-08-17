<template>
  <div>
    <div class="d-flex justify-content-between align-items-center">
      <div>
        <h3>Departement / <span class="text-muted">List</span></h3>
      </div>
      <div>
        <GenesisButton icon="bi bi-plus me-2" title="Create new departement">
          <span>Add new departement</span>
        </GenesisButton>
      </div>
    </div>
    <EntityTable
      :entity-model="new Departement()"
      :entity-search-fields="searchFields"
      :searchFn="searchDepartements"
      :getPaginationData="getPaginationData"
      :listComponent="DepartementList"
      :entities="departements"
      :loading="loading"
      :message="message"
    />
  </div>
</template>

<script lang="ts">
import { defineComponent, ref } from "vue";
import EntityTable from "@/core/table/EntityTable.vue";
import DepartementList from "@/entities/departement/DepartementList.vue";
import { Departement } from "@/models/DepartementModel";
import { useDepartements } from "@/composables/useDepartements";
import { EntitySearchField } from "@/models/EntityModel";
import GenesisButton from "@/core/button/GenesisButton.vue";

export default defineComponent({
  name: "DepartementListView",
  components: {
    EntityTable,
    GenesisButton,
  },
  setup() {
    const {
      departements,
      loading,
      searchDepartements,
      paginationData,
      message,
      getPaginationData,
    } = useDepartements();
    const searchFields = ref<EntitySearchField[]>(
      Departement.getFieldListMetadata()
    );
    return {
      departements,
      loading,
      searchDepartements,
      paginationData,
      DepartementList,
      Departement,
      searchFields,
      getPaginationData,
      message,
    };
  },
});
</script>

<style scoped></style>

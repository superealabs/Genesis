<template>
  <div>
    <div class="d-flex justify-content-between align-items-center">
      <div>
        <h3>Employe / <span class="text-muted">List</span></h3>
      </div>
      <div>
        <GenesisButton icon="bi bi-plus me-2" title="Create new employe">
          <span>Add new employe</span>
        </GenesisButton>
      </div>
    </div>
    <EntityTable
      :entity-model="new Employe()"
      :entity-search-fields="searchFields"
      :searchFn="searchEmployes"
      :getPaginationData="getPaginationData"
      :listComponent="EmployeList"
      :entities="employes"
      :loading="loading"
      :message="message"
    />
  </div>
</template>

<script lang="ts">
import { defineComponent, ref } from "vue";
import EntityTable from "@/core/table/EntityTable.vue";
import EmployeList from "@/entities/employe/EmployeList.vue";
import { Employe } from "@/models/EmployeModel";
import { useEmployes } from "@/composables/useEmployes";
import { EntitySearchField } from "@/models/EntityModel";
import GenesisButton from "@/core/button/GenesisButton.vue";

export default defineComponent({
  name: "EmployeListView",
  components: {
    EntityTable,
    GenesisButton,
  },
  setup() {
    const {
      employes,
      loading,
      searchEmployes,
      paginationData,
      message,
      getPaginationData,
    } = useEmployes();
    const searchFields = ref<EntitySearchField[]>(
      Employe.getFieldListMetadata()
    );
    return {
      employes,
      loading,
      searchEmployes,
      paginationData,
      EmployeList,
      Employe,
      searchFields,
      getPaginationData,
      message,
    };
  },
});
</script>

<style scoped></style>

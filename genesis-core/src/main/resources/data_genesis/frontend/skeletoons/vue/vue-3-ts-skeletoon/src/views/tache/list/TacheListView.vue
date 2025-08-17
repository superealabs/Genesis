<template>
  <div class="container-fluid">
    <div class="d-flex justify-content-between align-items-center">
      <div>
        <h3>Tache / <span class="text-muted">List</span></h3>
      </div>
      <div>
        <GenesisButton icon="bi bi-plus me-2" title="Create new tache">
          <span>Add new tache</span>
        </GenesisButton>
      </div>
    </div>
    <EntityTable
      :entity-model="new Tache()"
      :entity-search-fields="searchFields"
      :searchFn="searchTaches"
      :getPaginationData="getPaginationData"
      :listComponent="TacheList"
      :entities="taches"
      :loading="loading"
      :message="message"
    />
  </div>
</template>

<script lang="ts">
import { defineComponent, ref } from "vue";
import EntityTable from "@/core/table/EntityTable.vue";
import TacheList from "@/entities/tache/TacheList.vue";
import { Tache } from "@/models/TacheModel";
import { useTaches } from "@/composables/useTaches";
import { EntitySearchField } from "@/models/EntityModel";
import GenesisButton from "@/core/button/GenesisButton.vue";

export default defineComponent({
  name: "TacheListView",
  components: {
    EntityTable,
    GenesisButton,
  },
  setup() {
    const {
      taches,
      loading,
      searchTaches,
      paginationData,
      message,
      getPaginationData,
    } = useTaches();
    const searchFields = ref<EntitySearchField[]>(Tache.getSearchDaoMetadata());
    return {
      taches,
      loading,
      searchTaches,
      paginationData,
      TacheList,
      Tache,
      searchFields,
      getPaginationData,
      message,
    };
  },
});
</script>

<style scoped></style>

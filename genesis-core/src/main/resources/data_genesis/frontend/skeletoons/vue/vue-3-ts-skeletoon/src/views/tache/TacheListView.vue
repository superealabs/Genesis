<template>
  <div class="w-full">
    <!-- Header -->
    <div class="flex items-center justify-between mb-6">
      <h3 class="text-xl font-semibold">
        Tache /
        <span class="text-gray-500 font-normal">List</span>
      </h3>
      <GenesisButton title="Create new departement" @click="goToCreateFormView" class="btn-primary">
        <PlusIcon />
        Add new tache
      </GenesisButton>
    </div>

    <!-- Table -->
    <EntityTable
      :entity-model="new Tache()"
      :entity-search-fields="searchFields"
      :searchFn="searchTaches"
      :getPaginationData="getPaginationData"
      :listComponent="TacheList"
      :entities="taches"
      :message="message"
    />
  </div>
</template>

<script lang="ts">
import { defineComponent, ref } from 'vue'
import EntityTable from '@/core/table/EntityTable.vue'
import TacheList from '@/entities/tache/TacheList.vue'
import { Tache } from '@/models/TacheModel'
import { useTaches } from '@/composables/useTaches'
import type { EntitySearchField } from '@/models/EntityModel'
import GenesisButton from '@/core/button/GenesisButton.vue'
import PlusIcon from '@/core/icons/PlusIcon.vue'

export default defineComponent({
  name: 'TacheListView',
  components: {
    EntityTable,
    GenesisButton,
    PlusIcon,
  },
  setup() {
    const { taches, searchTaches, paginationData, message, getPaginationData, goToCreateFormView } =
      useTaches()
    const searchFields = ref<EntitySearchField[]>(Tache.getAllSearchFieldsMetadata())
    return {
      taches,
      searchTaches,
      paginationData,
      TacheList,
      Tache,
      searchFields,
      getPaginationData,
      message,
      goToCreateFormView,
    }
  },
})
</script>

<style scoped></style>

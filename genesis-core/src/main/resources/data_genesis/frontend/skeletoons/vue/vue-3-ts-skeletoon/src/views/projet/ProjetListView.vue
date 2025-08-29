<template>
  <div class="container-fluid">
    <div class="d-flex justify-content-between align-items-center">
      <div>
        <h3 class="text-dark">Projet / <span class="text-muted">List</span></h3>
      </div>
      <div>
        <GenesisButton
          icon="bi bi-plus me-2"
          title="Create new departement"
          @click="goToCreateFormView"
        >
          <span>Add new projet</span>
        </GenesisButton>
      </div>
    </div>
    <EntityTable
      :entity-model="new Projet()"
      :entity-search-fields="searchFields"
      :searchFn="searchProjets"
      :getPaginationData="getPaginationData"
      :listComponent="ProjetList"
      :entities="projets"
      :loading="loading"
      :message="message"
    />
  </div>
</template>

<script lang="ts">
import { defineComponent, ref } from 'vue'
import EntityTable from '@/core/table/EntityTable.vue'
import ProjetList from '@/entities/projet/ProjetList.vue'
import { Projet } from '@/models/ProjetModel'
import { useProjets } from '@/composables/useProjets'
import { EntitySearchField } from '@/models/EntityModel'
import GenesisButton from '@/core/button/GenesisButton.vue'

export default defineComponent({
  name: 'ProjetListView',
  components: {
    EntityTable,
    GenesisButton,
  },
  setup() {
    const {
      projets,
      loading,
      searchProjets,
      paginationData,
      message,
      getPaginationData,
      goToCreateFormView,
    } = useProjets()
    const searchFields = ref<EntitySearchField[]>(Projet.getAllSearchFieldsMetadata())
    return {
      projets,
      loading,
      searchProjets,
      paginationData,
      ProjetList,
      Projet,
      searchFields,
      getPaginationData,
      message,
      goToCreateFormView,
    }
  },
})
</script>

<style scoped></style>

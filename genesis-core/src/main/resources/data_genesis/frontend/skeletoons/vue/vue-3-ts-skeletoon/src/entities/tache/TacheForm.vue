<template>
  <div class="card bg-base-100">
    <div class="card-body">
      <form @submit.prevent="handleSubmit" class="grid grid-cols-1 w-6/12 gap-4 mx-auto">
        <!-- Titre -->
        <GenesisInput
          label="Titre"
          placeholder="Entrer Titre"
          type="text"
          v-model="formModel.titre"
        />

        <!-- Description -->
        <GenesisInput
          label="Description"
          placeholder="Entrer Description"
          type="text"
          v-model="formModel.description"
        />

        <!-- Priorité -->
        <GenesisInput
          label="Priorité"
          placeholder="Entrer Priorité"
          type="number"
          v-model="formModel.priorite"
        />

        <!-- Projet -->
        <GenesisSelectSearch
          label="Projet"
          v-if="projetSearchField?.selectSearch"
          placeholder="-- Select an projet --"
          :default-value="defaultProjetValue"
          :search-function="projetSearchField?.selectSearch"
          @option-selected="(selectedValue) => (formModel.projetidProjets = selectedValue)"
        />

        <!-- Employee -->
        <GenesisSelectSearch
          label="Employee"
          v-if="employeSearchField?.selectSearch"
          placeholder="-- Select an employee --"
          :default-value="defaultEmployeeValue"
          :search-function="employeSearchField?.selectSearch"
          @option-selected="(selectedValue) => (formModel.assigneaidEmployes = selectedValue)"
        />

        <!-- Boutons -->
        <div class="flex justify-end gap-3 mt-6">
          <GenesisButton type="submit" class="btn btn-primary text-white" :label="submitLabel" />
          <GenesisButton @click="cancelForm" class="btn btn-secondary" label="Cancel" />
        </div>
      </form>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref } from 'vue'
import type { PropType } from 'vue'
import { Tache, TacheFormDTO } from '@/models/TacheModel'
import GenesisButton from '@/core/button/GenesisButton.vue'
import GenesisInput from '@/core/form/GenesisInput.vue'
import GenesisSelectSearch from '@/core/form/GenesisSelectSearch.vue'
import { useObjectUtils } from '@/composables/useObjectUtils'

export default defineComponent({
  name: 'TacheForm',
  components: { GenesisButton, GenesisInput, GenesisSelectSearch },
  props: {
    tache: { type: Object as PropType<Tache>, required: false },
    submitLabel: { type: String, default: 'Submit' },
  },
  emits: ['submit', 'cancel'],
  setup(props, { emit }) {
    const formModel = ref<Partial<TacheFormDTO>>({ ...TacheFormDTO.parse(props.tache) })
    const objectUtils = useObjectUtils()

    // Projet FK
    const defaultProjetValue = objectUtils.getSecondValue(props.tache?.projetidProjets ?? {})
    const projetSearchField = Tache.getSearchFieldByKey('projetidProjets')

    // Employee FK
    const defaultEmployeeValue = objectUtils.getSecondValue(props.tache?.assigneaidEmployes ?? {})
    const employeSearchField = Tache.getSearchFieldByKey('assigneaidEmployes')

    function handleSubmit() {
      emit('submit', formModel.value)
    }

    function cancelForm() {
      emit('cancel', formModel.value)
    }

    return {
      formModel,
      handleSubmit,
      cancelForm,
      // Projet FK Data
      projetSearchField,
      defaultProjetValue,

      // Employee FK Data
      employeSearchField,
      defaultEmployeeValue,
    }
  },
})
</script>

<style scoped></style>

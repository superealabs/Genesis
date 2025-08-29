<template>
  <div class="card border-0 shadow">
    <div class="card-body">
      <form @submit.prevent="handleSubmit" class="row w-50 mx-auto g-3">
        <div class="col-md-12">
          <GenesisInput
            label="Prenom"
            placeholder="Entrer Prenom"
            type="text"
            v-model="formModel.prenom"
          />
        </div>

        <div class="col-md-12">
          <GenesisInput label="Nom" placeholder="Entrer Nom" type="text" v-model="formModel.nom" />
        </div>

        <div class="col-md-12">
          <GenesisInput
            label="Email"
            placeholder="Entrer Email"
            type="text"
            v-model="formModel.email"
          />
        </div>

        <div class="col-md-12">
          <GenesisInput
            label="Date embauche"
            placeholder="Entrer Date embauche"
            type="date"
            v-model="formModel.dateEmbauche"
          />
        </div>

        <div class="col-md-12">
          <GenesisInput
            label="Salaire"
            placeholder="Entrer Salaire"
            type="number"
            v-model="formModel.salaire"
          />
        </div>

        <div class="col-md-12">
          <GenesisSelect
            label="Departementid departements"
            :options="departementData"
            v-model="formModel.departementidDepartements"
          />
        </div>

        <!-- Buttons -->
        <div class="row mt-4 justify-content-end g-3">
          <div class="col-auto">
            <GenesisButton
              type="submit"
              class="btn-primary text-white"
              icon="bi bi-save me-2"
              :label="submitLabel"
            />
          </div>
          <div class="col-auto">
            <GenesisButton @click="cancelForm" class="btn-secondary" label="Cancel" />
          </div>
        </div>
      </form>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, onMounted, ref } from 'vue'

import { EmployeFormDTO } from '@/models/EmployeModel'
import GenesisButton from '@/core/button/GenesisButton.vue'
import GenesisInput from '@/core/form/GenesisInput.vue'

import GenesisSelect from '@/core/form/GenesisSelect.vue'
import { extractSelectOptionsFromOjectsData, type SelectOption } from '@/models/SelectOption'
import { useEmployes } from '@/composables/useEmployes'
import type { PropType } from 'vue'

export default defineComponent({
  name: 'EmployeForm',
  components: { GenesisButton, GenesisInput, GenesisSelect },
  props: {
    employe: { type: Object as PropType<EmployeFormDTO>, required: false },
    submitLabel: { type: String, default: 'Submit' },
  },
  emits: ['submit', 'cancel'],
  setup(props, { emit }) {
    const formModel = ref<Partial<EmployeFormDTO>>({ ...props.employe })
    const departementData = ref<SelectOption[]>([])

    const { loadFkMapData } = useEmployes()
    async function loadFkData() {
      const { departements } = await loadFkMapData()
      departementData.value = extractSelectOptionsFromOjectsData(departements)
    }

    onMounted(loadFkData)

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
      departementData,
    }
  },
})
</script>

<style scoped></style>

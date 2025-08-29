<template>
  <div class="card border-0 shadow">
    <div class="card-body">
      <form @submit.prevent="handleSubmit" class="row w-50 mx-auto g-3">
        <div class="col-md-12">
          <GenesisInput
            label="Nom projet"
            placeholder="Entrer Nom projet"
            type="text"
            v-model="formModel.nomProjet"
          />
        </div>

        <div class="col-md-12">
          <GenesisInput
            label="Budget"
            placeholder="Entrer Budget"
            type="number"
            v-model="formModel.budget"
          />
        </div>

        <div class="col-md-12">
          <GenesisInput
            label="Date debut"
            placeholder="Entrer Date debut"
            type="date"
            v-model="formModel.dateDebut"
          />
        </div>

        <div class="col-md-12">
          <GenesisInput
            label="Date fin prevue"
            placeholder="Entrer Date fin prevue"
            type="date"
            v-model="formModel.dateFinPrevue"
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
import { defineComponent, ref } from 'vue'
import type { PropType } from 'vue'
import { ProjetFormDTO } from '@/models/ProjetModel'
import GenesisButton from '@/core/button/GenesisButton.vue'
import GenesisInput from '@/core/form/GenesisInput.vue'

export default defineComponent({
  name: 'ProjetForm',
  components: { GenesisButton, GenesisInput },
  props: {
    projet: { type: Object as PropType<ProjetFormDTO>, required: false },
    submitLabel: { type: String, default: 'Submit' },
  },
  emits: ['submit', 'cancel'],
  setup(props, { emit }) {
    const formModel = ref<Partial<ProjetFormDTO>>({ ...props.projet })

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
    }
  },
})
</script>

<style scoped></style>

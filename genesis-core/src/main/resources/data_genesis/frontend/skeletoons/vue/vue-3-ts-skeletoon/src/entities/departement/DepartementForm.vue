<template>
  <div class="card border-0">
    <div class="card-body">
      <form @submit.prevent="handleSubmit" class="row w-50 mx-auto g-3">
        <div class="col-md-12">
          <GenesisInput
            label="Nom departement"
            placeholder="Entrer Nom departement"
            type="text"
            v-model="formModel.nomDepartement"
          />
        </div>

        <div class="col-md-12">
          <GenesisInput
            label="Code departement"
            placeholder="Entrer Code departement"
            type="text"
            v-model="formModel.codeDepartement"
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
import { DepartementFormDTO } from '@/models/DepartementModel'
import GenesisButton from '@/core/button/GenesisButton.vue'
import GenesisInput from '@/core/form/GenesisInput.vue'

export default defineComponent({
  name: 'DepartementForm',
  components: { GenesisButton, GenesisInput },
  props: {
    departement: {
      type: Object as PropType<DepartementFormDTO>,
      required: false,
    },
    submitLabel: { type: String, default: 'Submit' },
  },
  emits: ['submit', 'cancel'],
  setup(props, { emit }) {
    const formModel = ref<Partial<DepartementFormDTO>>({
      ...props.departement,
    })

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

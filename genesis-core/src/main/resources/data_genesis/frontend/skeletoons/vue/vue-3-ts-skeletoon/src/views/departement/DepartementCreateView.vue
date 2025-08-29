<template>
  <div class="container-fluid">
    <div class="d-flex mb-4 justify-content-between align-items-center">
      <div>
        <h3 class="text-dark">Departement / <span class="text-muted">New</span></h3>
      </div>
    </div>

    <departement-form
      submit-label="Save Departement"
      :departement="departementFormDto"
      @submit="createHandler"
      @cancel="goToListView"
    />

    <AlertPopup
      :message="message ?? undefined"
      title="Error 500"
      :visible="alertPopup"
      @close="closePopup"
    />
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { useDepartements } from '@/composables/useDepartements'
import DepartementForm from '@/entities/departement/DepartementForm.vue'
import { DepartementFormDTO } from '@/models/DepartementModel'
import AlertPopup from '@/core/popup/AlertPopup.vue'
import { usePopup } from '@/composables/usePopup'
import { useFreezeScreen } from '@/stores/useFreezeScreen'

export default defineComponent({
  name: 'DepartementCreateView',
  components: { DepartementForm, AlertPopup },
  setup() {
    const departementFormDto = new DepartementFormDTO()
    const { createDepartement, goToListView, message } = useDepartements()
    const { openPopup, closePopup, visible: alertPopup } = usePopup()
    const { freeze, unfreeze } = useFreezeScreen()

    const createHandler = async (formDTO: Partial<DepartementFormDTO>) => {
      freeze('Create a new Departement...')
      try {
        const data = await createDepartement(formDTO)
        if (data && !message.value) goToListView()
        else throw new Error()
      } catch (error) {
        openPopup()
      } finally {
        unfreeze()
      }
    }

    return {
      departementFormDto,
      createHandler,
      goToListView,
      message,
      alertPopup,
      closePopup,
    }
  },
})
</script>

<style scoped></style>

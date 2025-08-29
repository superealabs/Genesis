<template>
  <div class="container-fluid">
    <div class="d-flex mb-4 justify-content-between align-items-center">
      <div>
        <h3 class="text-dark">Employe / <span class="text-muted">New</span></h3>
      </div>
    </div>

    <employe-form
      submit-label="Save Employe"
      :employe="employeFormDto"
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
import { useEmployes } from '@/composables/useEmployes'
import EmployeForm from '@/entities/employe/EmployeForm.vue'
import { EmployeFormDTO } from '@/models/EmployeModel'
import AlertPopup from '@/core/popup/AlertPopup.vue'
import { usePopup } from '@/composables/usePopup'
import { useFreezeScreen } from '@/stores/useFreezeScreen'

export default defineComponent({
  name: 'EmployeCreateView',
  components: { EmployeForm, AlertPopup },
  setup() {
    const employeFormDto = new EmployeFormDTO()
    const { createEmploye, goToListView, message } = useEmployes()
    const { openPopup, closePopup, visible: alertPopup } = usePopup()
    const { freeze, unfreeze } = useFreezeScreen()

    const createHandler = async (formDTO: Partial<EmployeFormDTO>) => {
      freeze('Create a new Employe...')
      try {
        const data = await createEmploye(formDTO)
        if (data && !message.value) goToListView()
        else throw new Error()
      } catch (error) {
        openPopup()
      } finally {
        unfreeze()
      }
    }

    return {
      employeFormDto,
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

<template>
  <div class="container-fluid">
    <div class="d-flex mb-4 justify-content-between align-items-center">
      <div>
        <h3 class="text-dark">Projet / <span class="text-muted">New</span></h3>
      </div>
    </div>

    <projet-form
      submit-label="Save Projet"
      :projet="projetFormDto"
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
import { useProjets } from '@/composables/useProjets'
import ProjetForm from '@/entities/projet/ProjetForm.vue'
import { ProjetFormDTO } from '@/models/ProjetModel'
import AlertPopup from '@/core/popup/AlertPopup.vue'
import { usePopup } from '@/composables/usePopup'
import { useFreezeScreen } from '@/stores/useFreezeScreen'

export default defineComponent({
  name: 'ProjetCreateView',
  components: { ProjetForm, AlertPopup },
  setup() {
    const projetFormDto = new ProjetFormDTO()
    const { createProjet, goToListView, message } = useProjets()
    const { openPopup, closePopup, visible: alertPopup } = usePopup()
    const { freeze, unfreeze } = useFreezeScreen()

    const createHandler = async (formDTO: Partial<ProjetFormDTO>) => {
      freeze('Create a new Projet...')
      try {
        const data = await createProjet(formDTO)
        if (data && !message.value) goToListView()
        else throw new Error()
      } catch (error) {
        openPopup()
      } finally {
        unfreeze()
      }
    }

    return {
      projetFormDto,
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

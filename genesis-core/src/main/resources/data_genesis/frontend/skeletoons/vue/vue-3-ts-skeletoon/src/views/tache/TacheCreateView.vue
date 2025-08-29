<template>
  <div class="w-full">
    <!-- Header -->
    <div class="flex items-center justify-between mb-6">
      <h3 class="text-xl font-semibold">
        Tache /
        <span class="text-gray-500 font-normal">New</span>
      </h3>
      <GenesisButton title="Create new departement" @click="goToListView" class="btn-primary">
        <LeftArrowIcon />
        Back to list
      </GenesisButton>
    </div>
    <tache-form
      submit-label="Save Tache"
      :tache="tacheFormDto"
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
import { useTaches } from '@/composables/useTaches'
import TacheForm from '@/entities/tache/TacheForm.vue'
import { Tache, TacheFormDTO } from '@/models/TacheModel'
import AlertPopup from '@/core/popup/AlertPopup.vue'
import { usePopup } from '@/composables/usePopup'
import { useFreezeScreen } from '@/stores/useFreezeScreen'
import GenesisButton from '@/core/button/GenesisButton.vue'
import LeftArrowIcon from '@/core/icons/LeftArrowIcon.vue'

export default defineComponent({
  name: 'TacheCreateView',
  components: { TacheForm, AlertPopup, GenesisButton, LeftArrowIcon },
  setup() {
    const tacheFormDto = new Tache()
    const { createTache, goToListView, message } = useTaches()
    const { openPopup, closePopup, visible: alertPopup } = usePopup()
    const freezeStore = useFreezeScreen()

    const createHandler = async (formDTO: Partial<TacheFormDTO>) => {
      freezeStore.freeze('Creating a new Tache ...')
      try {
        const data = await createTache(formDTO)
        if (data && !message.value) goToListView()
        else throw new Error(String(message.value))
      } catch (error: unknown) {
        console.error(error)
        openPopup()
      } finally {
        freezeStore.unfreeze()
      }
    }

    return {
      tacheFormDto,
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

<template>
  <div class="w-full">
    <!-- Header -->
    <div class="flex items-center justify-between mb-6">
      <h3 class="text-xl font-semibold">
        Tache /
        <span class="text-gray-500 font-normal">Update</span>
      </h3>
      <GenesisButton title="Create new departement" @click="goToListView" class="btn-primary">
        <LeftArrowIcon />
        Back to list
      </GenesisButton>
    </div>
    <!-- Form -->
    <div v-if="entity">
      <tache-form
        :tache="entity"
        submit-label="Update Tache"
        @submit="updateHandler"
        @cancel="viewTache"
      ></tache-form>
    </div>

    <!-- Alert -->
    <AlertPopup
      :message="message ?? undefined"
      title="Error 500"
      :visible="alertPopup"
      @close="closePopup"
    ></AlertPopup>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import TacheForm from '@/entities/tache/TacheForm.vue'
import AlertPopup from '@/core/popup/AlertPopup.vue'
import { useTaches } from '@/composables/useTaches'
import { Tache, TacheFormDTO } from '@/models/TacheModel'
import { useLoading } from '@/composables/useLoading'
import { usePopup } from '@/composables/usePopup'
import GenesisButton from '@/core/button/GenesisButton.vue'
import LeftArrowIcon from '@/core/icons/LeftArrowIcon.vue'

export default defineComponent({
  name: 'TacheUpdateView',
  components: { TacheForm, AlertPopup, GenesisButton, LeftArrowIcon },
  setup() {
    const route = useRoute()
    const pathId = Number(route.params.id)
    const { loading, startLoading, stopLoading } = useLoading()
    const { closePopup, openPopup, visible: alertPopup } = usePopup()

    const { getTacheById, goToListView, updateTache, viewTache, message } = useTaches()
    const entity = ref<Tache | null>(null)

    const updateHandler = async (formDTO: Partial<TacheFormDTO>) => {
      startLoading()
      const data = await updateTache(pathId, formDTO)
      if (data && !message.value) {
        viewTache(data)
      } else {
        openPopup()
      }
      stopLoading()
    }

    onMounted(async () => {
      const result = await getTacheById(pathId)
      entity.value = result.data || null
    })

    return {
      entity,
      route,
      goToListView,
      updateTache,
      viewTache,
      updateHandler,
      loading,
      message,
      closePopup,
      alertPopup,
    }
  },
})
</script>

<style scoped></style>

<template>
  <div class="w-full">
    <!-- Header -->
    <div class="flex items-center justify-between mb-6">
      <h3 class="text-xl font-semibold text-gray-800">
        Tache /
        <span class="text-gray-500 font-normal">Details</span>
      </h3>
      <GenesisButton title="Create new departement" @click="goToListView" class="btn-primary">
        <LeftArrowIcon />
        Back to list
      </GenesisButton>
    </div>
    <TacheDetails v-if="entity" :tache="entity" />
    <p v-else>Chargement...</p>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import TacheDetails from '@/entities/tache/TacheDetails.vue'
import { useTaches } from '@/composables/useTaches'
import { Tache } from '@/models/TacheModel'
import GenesisButton from '@/core/button/GenesisButton.vue'
import LeftArrowIcon from '@/core/icons/LeftArrowIcon.vue'

export default defineComponent({
  name: 'TacheDetailsView',
  components: {
    TacheDetails,
    GenesisButton,
    LeftArrowIcon,
  },
  setup() {
    const route = useRoute()
    const { getTacheById, goToListView } = useTaches()
    const entity = ref<Tache | null>(null)

    onMounted(async () => {
      const result = await getTacheById(Number(route.params.id))
      entity.value = result.data || null
    })

    return { entity, route, goToListView }
  },
})
</script>

<style scoped></style>

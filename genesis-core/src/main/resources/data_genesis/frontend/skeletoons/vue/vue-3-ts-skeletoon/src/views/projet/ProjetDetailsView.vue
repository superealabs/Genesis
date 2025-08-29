<template>
  <div class="container-fluid">
    <div class="mb-4 d-flex justify-content-between align-items-center">
      <div>
        <h3 class="text-dark">Projet / <span class="text-muted">Details</span></h3>
      </div>
      <div>
        <GenesisButton
          icon="bi bi-arrow-left me-2"
          title="Go back to list view"
          @click="goToListView"
        >
          <span>Back to list</span>
        </GenesisButton>
      </div>
    </div>
    <ProjetDetails v-if="entity" :projet="entity" />
    <p v-else>Chargement...</p>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import ProjetDetails from '@/entities/projet/ProjetDetails.vue'
import { useProjets } from '@/composables/useProjets'
import { Projet } from '@/models/ProjetModel'
import GenesisButton from '@/core/button/GenesisButton.vue'

export default defineComponent({
  name: 'ProjetDetailsView',
  components: {
    ProjetDetails,
    GenesisButton,
  },
  setup() {
    const route = useRoute()
    const { getProjetById, goToListView } = useProjets()
    const entity = ref<Projet | null>(null)

    onMounted(async () => {
      const result = await getProjetById(Number(route.params.id))
      entity.value = result.data || null
    })

    return { entity, route, goToListView }
  },
})
</script>

<style scoped></style>
